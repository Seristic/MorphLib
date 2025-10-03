package com.seristic.morphlib.logging;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class LoggingConfig {
    public final Path logDirectory;
    public final String logFileBaseName;
    public final long maxBytesPerFile;
    public final int maxBackupFiles;
    public final boolean jsonFormat;
    public final boolean consoleEnabled;
    public final DateTimeFormatter dateTimeFormatter;
    public final ZoneId zoneId;

    public LoggingConfig(Path logDirectory, String logFileBaseName, long maxBytesPerFile, int maxBackupFiles,
            boolean jsonFormat, boolean consoleEnabled, DateTimeFormatter dateTimeFormatter, ZoneId zoneId) {
        this.logDirectory = logDirectory;
        this.logFileBaseName = logFileBaseName;
        this.maxBytesPerFile = maxBytesPerFile;
        this.maxBackupFiles = maxBackupFiles;
        this.jsonFormat = jsonFormat;
        this.consoleEnabled = consoleEnabled;
        this.dateTimeFormatter = dateTimeFormatter;
        this.zoneId = zoneId;
    }

    public static LoggingConfig defaultConfig() {
        return new LoggingConfig(
                Path.of("logs"),
                "homestead",
                5 * 1024 * 1024L, // 5 MB
                5,
                false,
                true,
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                ZoneId.systemDefault());
    }
}
