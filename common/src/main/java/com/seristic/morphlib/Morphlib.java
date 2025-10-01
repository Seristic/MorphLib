package com.seristic.morphlib;

import java.time.ZoneId;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import com.seristic.logging.ModLogger;
import com.seristic.logging.LoggingConfig;

public final class Morphlib {
    public static final String MOD_ID = "morphlib";

    public static void init() {
        // Write common init code here.

        initLogger();

        ModLogger.info("MorphLib", "Common Module Initialized");
    }

    private static void initLogger() {
        LoggingConfig config = new LoggingConfig(
                Path.of("logs"),
                "morphlib",
                5 * 1024 * 1024L, // 5 MB
                5,
                false,
                true,
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                ZoneId.systemDefault());

        ModLogger.init("MorphLib", config);
        ModLogger.info("MorphLib", "Logger Initialized Successfully");
    }
}
