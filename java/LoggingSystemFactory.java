package java;

import java.logfile.FileLoggingSystem;
import java.networktables.NetworkTablesLoggingSystem;

/**
 * Factory class for creating logging systems
 */
public class LoggingSystemFactory {

    public enum LoggingType {
        NETWORKTABLES,
        FILE
    }

    /**
     * Create a logging system based on the specified type
     */
    public static LoggingSystem createLoggingSystem(LoggingType type) {
        switch (type) {
            case NETWORKTABLES:
                return new NetworkTablesLoggingSystem();
            case FILE:
                return new FileLoggingSystem();
            default:
                throw new IllegalArgumentException("Unknown logging type: " + type);
        }
    }

    /**
     * Create a NetworkTables-based logging system
     */
    public static LoggingSystem createNetworkTablesLoggingSystem() {
        return new NetworkTablesLoggingSystem();
    }

    /**
     * Create a file-based logging system
     */
    public static LoggingSystem createFileLoggingSystem() {
        return new FileLoggingSystem();
    }
}