package com.seristic.morphlib.logging;

import java.time.Duration;
import java.time.Instant;

/**
 * Helper to time operations. Use try-with-resources or manual start/stop.
 */
public final class TimedOperation implements AutoCloseable {
    private final String name;
    private final Instant start;
    private final LoggerManager logger;
    private final LogContext context;

    private boolean closed = false;

    public TimedOperation(String name, LoggerManager logger, LogContext context) {
        this.name = name;
        this.logger = logger;
        this.context = context == null ? LogContext.empty() : context;
        this.start = Instant.now();
        logger.debug("timed.start", "Starting timed operation: " + name, this.context);
    }

    public static TimedOperation start(String name, LoggerManager logger) {
        return new TimedOperation(name, logger, LogContext.empty());
    }

    public static TimedOperation start(String name, LoggerManager logger, LogContext context) {
        return new TimedOperation(name, logger, context);
    }

    public Duration stop() {
        if (closed)
            return Duration.ZERO;
        closed = true;
        Duration dur = Duration.between(start, Instant.now());
        logger.info("timed.stop", String.format("Operation %s finished in %d ms", name, dur.toMillis()), context);
        return dur;
    }

    @Override
    public void close() {
        stop();
    }
}
