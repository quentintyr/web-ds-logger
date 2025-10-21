package java;

/**
 * Utility class providing convenient static methods for logging
 * This class acts as a simple facade for the logging system
 */
public class Logger {

    private static LoggingSystem instance;

    /**
     * Initialize the logger with a specific logging system
     */
    public static void initialize(LoggingSystem loggingSystem) {
        instance = loggingSystem;
        instance.setupLogging();
        instance.initLogging();
    }

    /**
     * Initialize the logger with NetworkTables logging
     */
    public static void initializeNetworkTables() {
        initialize(LoggingSystemFactory.createNetworkTablesLoggingSystem());
    }

    /**
     * Initialize the logger with file-based logging
     */
    public static void initializeFile() {
        initialize(LoggingSystemFactory.createFileLoggingSystem());
    }

    /**
     * Update logging (should be called periodically)
     */
    public static void update() {
        if (instance != null) {
            instance.updateLogging();
        }
    }

    /**
     * Log an info message
     */
    public static void info(String message) {
        if (instance != null) {
            instance.logInfo(message);
        }
    }

    /**
     * Log a warning message
     */
    public static void warn(String message) {
        if (instance != null) {
            instance.logWarn(message);
        }
    }

    /**
     * Log an error message
     */
    public static void error(String message) {
        if (instance != null) {
            instance.logError(message);
        }
    }

    /**
     * Log an autonomous message
     */
    public static void autonomous(String message) {
        if (instance != null) {
            instance.logAutonomous(message);
        }
    }

    /**
     * Log a teleop message
     */
    public static void teleop(String message) {
        if (instance != null) {
            instance.logTeleop(message);
        }
    }

    /**
     * Log a test message
     */
    public static void test(String message) {
        if (instance != null) {
            instance.logTest(message);
        }
    }

    /**
     * Log a disabled message
     */
    public static void disabled(String message) {
        if (instance != null) {
            instance.logDisabled(message);
        }
    }

    /**
     * Log a thread message
     */
    public static void thread(String message) {
        if (instance != null) {
            instance.logThread(message);
        }
    }

    /**
     * Get the current robot mode
     */
    public static String getRobotMode() {
        if (instance != null) {
            return instance.getRobotMode();
        }
        return "unknown";
    }

    /**
     * Get the current logging system instance
     */
    public static LoggingSystem getInstance() {
        return instance;
    }
}