package java;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interface for logging systems
 */
public interface LoggingSystem {

    /**
     * Setup the logging system
     */
    void setupLogging();

    /**
     * Update logging-related values (battery, mode, etc.)
     */
    void updateLogging();

    /**
     * Initialize logging
     */
    void initLogging();

    /**
     * Get the current robot mode
     */
    String getRobotMode();

    /**
     * Log an info message
     */
    void logInfo(String message);

    /**
     * Log a warning message
     */
    void logWarn(String message);

    /**
     * Log an error message
     */
    void logError(String message);

    /**
     * Log an autonomous message
     */
    void logAutonomous(String message);

    /**
     * Log a teleop message
     */
    void logTeleop(String message);

    /**
     * Log a test message
     */
    void logTest(String message);

    /**
     * Log a disabled message
     */
    void logDisabled(String message);

    /**
     * Log a thread message
     */
    void logThread(String message);

    /**
     * Get current timestamp formatted as string
     */
    default String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Format a log message with timestamp and level
     */
    default String formatLogMessage(LogLevel level, String message) {
        String timestamp = "[" + getCurrentTime() + "] ";
        String levelStr = level.getHtmlColorStart() + "[" + level.getName() + "] " + level.getHtmlColorEnd();
        return timestamp + levelStr + message;
    }
}