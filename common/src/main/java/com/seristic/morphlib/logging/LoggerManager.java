package com.seristic.morphlib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Logger manager handling:
 * - Async file logging with rotation
 * - Colored console output
 * - Category-based log levels
 * - JSON/plain text formatting
 */
public final class LoggerManager implements Closeable {

    private static final Logger consoleLogger = LoggerFactory.getLogger("LoggerManager");
    private static volatile LoggerManager INSTANCE;

    public enum LogLevel {
        TRACE(0, "\u001B[37m"), DEBUG(1, "\u001B[36m"), INFO(2, "\u001B[32m"),
        WARN(3, "\u001B[33m"), ERROR(4, "\u001B[31m"), FATAL(5, "\u001B[35m");

        private final int priority;
        private final String color;

        LogLevel(int p, String c) {
            priority = p;
            color = c;
        }

        public int getPriority() {
            return priority;
        }

        public String getColor() {
            return color;
        }
    }

    private final String modId;
    private final LoggingConfig config;
    private final Map<String, LogLevel> categoryLevels = new ConcurrentHashMap<>();
    private LogLevel globalLevel = LogLevel.DEBUG;
    private boolean useColors = true;

    private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>(10_000);
    private final ExecutorService writerExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "logger-writer");
        t.setDaemon(true);
        return t;
    });

    private BufferedWriter currentWriter;
    private Path currentLogPath;
    private final AtomicLong currentFileBytes = new AtomicLong(0);
    private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // -------------------- Initialization --------------------
    public static void init(String modId, LoggingConfig cfg) {
        Objects.requireNonNull(modId);
        Objects.requireNonNull(cfg);
        if (INSTANCE != null)
            throw new IllegalStateException("Already initialized");
        INSTANCE = new LoggerManager(modId, cfg);
    }

    public static LoggerManager get() {
        if (INSTANCE == null)
            throw new IllegalStateException("Not initialized");
        return INSTANCE;
    }

    public static synchronized void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.closeQuietly();
            INSTANCE = null;
        }
    }

    private LoggerManager(String modId, LoggingConfig config) {
        this.modId = modId;
        this.config = config;
        try {
            if (!Files.exists(config.logDirectory))
                Files.createDirectories(config.logDirectory);
            openNewLogFile();
        } catch (Exception ex) {
            consoleLogger.error("Failed to init logger: {}", ex.getMessage(), ex);
        }
        writerExecutor.submit(this::writerLoop);
        consoleLogger.info("Logger initialized for '{}'", modId);
    }

    private void openNewLogFile() throws IOException {
        Path path = config.logDirectory.resolve(config.logFileBaseName + ".log");
        this.currentLogPath = path;
        currentWriter = new BufferedWriter(new FileWriter(path.toFile(), true));
        currentFileBytes.set(Files.exists(path) ? Files.size(path) : 0L);
    }

    // -------------------- Logging API --------------------
    public void trace(String tag, String msg, LogContext ctx) {
        log(LogLevel.TRACE, tag, msg, ctx, null);
    }

    public void debug(String tag, String msg, LogContext ctx) {
        log(LogLevel.DEBUG, tag, msg, ctx, null);
    }

    public void info(String tag, String msg, LogContext ctx) {
        log(LogLevel.INFO, tag, msg, ctx, null);
    }

    public void warn(String tag, String msg, LogContext ctx) {
        log(LogLevel.WARN, tag, msg, ctx, null);
    }

    public void error(String tag, String msg, LogContext ctx) {
        log(LogLevel.ERROR, tag, msg, ctx, null);
    }

    public void error(String tag, String msg, LogContext ctx, Throwable t) {
        log(LogLevel.ERROR, tag, msg, ctx, t);
    }

    public void fatal(String tag, String msg, LogContext ctx) {
        log(LogLevel.FATAL, tag, msg, ctx, null);
    }

    private boolean shouldLog(LogLevel level, String cat) {
        LogLevel l = categoryLevels.getOrDefault(cat, globalLevel);
        return level.getPriority() >= l.getPriority();
    }

    private void log(LogLevel level, String tag, String msg, LogContext ctx, Throwable t) {
        if (!shouldLog(level, tag))
            return;
        String line = formatLine(level, tag, msg, ctx, t);
        if (config.consoleEnabled) {
            System.out.println(useColors ? level.getColor() + line + "\u001B[0m" : line);
            if (t != null)
                t.printStackTrace(System.out);
        }
        enqueueWrite(line + (t != null ? " | EX:" + t : ""));
    }

    private String formatLine(LogLevel lvl, String tag, String msg, LogContext ctx, Throwable t) {
        String timestamp = TIMESTAMP.format(ZonedDateTime.now(config.zoneId));
        if (config.jsonFormat) {
            StringBuilder sb = new StringBuilder("{");
            appendJson(sb, "timestamp", timestamp);
            sb.append(',');
            appendJson(sb, "level", lvl.name());
            sb.append(',');
            appendJson(sb, "tag", tag);
            sb.append(',');
            appendJson(sb, "message", msg);
            if (ctx != null) {
                sb.append(',');
                appendJson(sb, "correlationId", ctx.correlationId());
            }
            if (t != null) {
                sb.append(',');
                appendJson(sb, "exception", t.toString());
            }
            sb.append('}');
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder(timestamp + " [" + lvl.name() + "] [" + tag + "]");
            if (ctx != null)
                sb.append(" [corr=").append(ctx.correlationId()).append("]");
            sb.append(' ').append(msg);
            if (t != null)
                sb.append(" | EX: ").append(t);
            return sb.toString();
        }
    }

    private static void appendJson(StringBuilder sb, String key, String val) {
        sb.append('"').append(key.replace("\"", "\\\"")).append('"').append(':').append('"')
                .append(val.replace("\"", "\\\"")).append('"');
    }

    // -------------------- Async file writing --------------------
    private void enqueueWrite(String line) {
        boolean offered = writeQueue.offer(line);
        if (!offered) {
            writeQueue.poll();
            writeQueue.offer(line);
        }
    }

    private void writerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String line = writeQueue.poll(1, TimeUnit.SECONDS);
                if (line != null)
                    writeLine(line);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                consoleLogger.error("Writer crashed: {}", t.getMessage(), t);
            }
        }
        try {
            String remaining;
            while ((remaining = writeQueue.poll()) != null)
                writeLine(remaining);
        } catch (Throwable t) {
            consoleLogger.error("Flush error: {}", t.getMessage(), t);
        } finally {
            closeQuietly();
        }
    }

    private synchronized void writeLine(String line) {
        try {
            currentWriter.write(line);
            currentWriter.newLine();
            currentWriter.flush();
            currentFileBytes.addAndGet(line.getBytes().length + System.lineSeparator().getBytes().length);
            rotateIfNeeded();
        } catch (IOException ex) {
            consoleLogger.error("Write failed: {}", ex.getMessage(), ex);
        }
    }

    private void rotateIfNeeded() throws IOException {
        if (currentFileBytes.get() < config.maxBytesPerFile)
            return;
        currentWriter.close();
        for (int i = config.maxBackupFiles - 1; i >= 0; i--) {
            Path from = config.logDirectory.resolve(config.logFileBaseName + (i == 0 ? ".log" : "." + i + ".log"));
            Path to = config.logDirectory.resolve(config.logFileBaseName + "." + (i + 1) + ".log");
            if (Files.exists(from))
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
        openNewLogFile();
    }

    // -------------------- Telemetry --------------------
    public void incrementCounter(String key) {
        counters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long getCounter(String key) {
        AtomicLong v = counters.get(key);
        return v == null ? 0 : v.get();
    }

    public void infoLazy(String tag, Supplier<String> msgSupplier, LogContext ctx) {
        info(tag, msgSupplier.get(), ctx);
    }

    // -------------------- Shutdown --------------------
    private void closeQuietly() {
        try {
            writerExecutor.shutdown();
            writerExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            if (currentWriter != null)
                currentWriter.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() {
        closeQuietly();
    }

    // -------------------- Configuration --------------------
    public void setUseColors(boolean val) {
        this.useColors = val;
    }

    public void setGlobalLevel(LogLevel lvl) {
        this.globalLevel = lvl;
    }

    public void setCategoryLevel(String cat, LogLevel lvl) {
        categoryLevels.put(cat, lvl);
    }
}
