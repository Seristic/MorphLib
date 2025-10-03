package com.seristic.morphlib.logging;

/**
 * Facade logger for easy use across mods.
 * Supports categories, timed operations, and default "general" logging.
 */
public final class ModLogger {

    private static LoggerManager LOGGER;

    private ModLogger() {
    }

    /** Initialize the logger once on mod startup */
    public static void init(String modId, LoggingConfig config) {
        LoggerManager.init(modId, config);
        LOGGER = LoggerManager.get();
    }

    // -------------------- Simple logs --------------------
    public static void trace(String category, String message) {
        LOGGER.trace(category, message, LogContext.empty());
    }

    public static void debug(String category, String message) {
        LOGGER.debug(category, message, LogContext.empty());
    }

    public static void info(String category, String message) {
        LOGGER.info(category, message, LogContext.empty());
    }

    public static void warn(String category, String message) {
        LOGGER.warn(category, message, LogContext.empty());
    }

    public static void error(String category, String message) {
        LOGGER.error(category, message, LogContext.empty());
    }

    public static void fatal(String category, String message) {
        LOGGER.fatal(category, message, LogContext.empty());
    }

    public static void error(String category, String message, Throwable t) {
        LOGGER.error(category, message, LogContext.empty(), t);
    }

    // Convenience "general" category
    public static void trace(String message) {
        trace("general", message);
    }

    public static void debug(String message) {
        debug("general", message);
    }

    public static void info(String message) {
        info("general", message);
    }

    public static void warn(String message) {
        warn("general", message);
    }

    public static void error(String message) {
        error("general", message);
    }

    public static void fatal(String message) {
        fatal("general", message);
    }

    public static void error(String message, Throwable t) {
        error("general", message, t);
    }

    // -------------------- Timed operations --------------------
    public static TimedOperation startTimed(String name) {
        return TimedOperation.start(name, LOGGER);
    }

    public static TimedOperation startTimed(String name, LogContext ctx) {
        return TimedOperation.start(name, LOGGER, ctx);
    }

    // -------------------- Log level management --------------------
    public static void setGlobalLevel(LoggerManager.LogLevel level) {
        LOGGER.setGlobalLevel(level);
    }

    public static void setCategoryLevel(String category, LoggerManager.LogLevel level) {
        LOGGER.setCategoryLevel(category, level);
    }
}
