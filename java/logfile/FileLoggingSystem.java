package java.logfile;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;

import java.LogLevel;
import java.LoggingSystem;
import java.ModeInfo;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

/**
 * File-based logging system for FRC robots
 * Logs messages to a file on the robot's filesystem
 */
public class FileLoggingSystem implements LoggingSystem {

    private static final String FILE_PATH = "/home/pi/robot.log";

    private PrintWriter logWriter;
    private ModeInfo lastMode;
    private final ReentrantLock logMutex;

    public FileLoggingSystem() {
        this.lastMode = new ModeInfo("</span>", "[INIT]");
        this.logMutex = new ReentrantLock();
    }

    @Override
    public void setupLogging() {
        try {
            // Remove existing log file if it exists
            Path logPath = Paths.get(FILE_PATH);
            Files.deleteIfExists(logPath);

            // Create new log file and writer
            logWriter = new PrintWriter(new FileWriter(FILE_PATH, true));

            // Redirect System.out and System.err to the log file
            System.setOut(new PrintWriter(logWriter, true));
            System.setErr(new PrintWriter(logWriter, true));

            logInfo("Finished Setup Logging (File-based)");
        } catch (IOException e) {
            System.err.println("Failed to setup file logging: " + e.getMessage());
        }
    }

    @Override
    public void updateLogging() {
        // File-based logging doesn't need to update external systems
        // But we can still update the last mode for consistency
        String mode = getRobotMode();
        updateLastMode(mode);
    }

    @Override
    public void initLogging() {
        logInfo("Initializing Robot...");
    }

    @Override
    public String getRobotMode() {
        DriverStation ds = DriverStation.getInstance();

        if (ds.isDisabled()) {
            return "disabled";
        } else if (ds.isAutonomous()) {
            return "autonomous";
        } else if (ds.isTeleop()) {
            return "teleop";
        } else if (ds.isTest()) {
            return "test";
        } else {
            return "unknown";
        }
    }

    @Override
    public void logInfo(String message) {
        logMessage(LogLevel.INFO, message);
    }

    @Override
    public void logWarn(String message) {
        logMessage(LogLevel.WARN, message);
    }

    @Override
    public void logError(String message) {
        logMessage(LogLevel.ERROR, message);
    }

    @Override
    public void logAutonomous(String message) {
        logMessage(LogLevel.AUTONOMOUS, message);
    }

    @Override
    public void logTeleop(String message) {
        logMessage(LogLevel.TELEOP, message);
    }

    @Override
    public void logTest(String message) {
        logMessage(LogLevel.TEST, message);
    }

    @Override
    public void logDisabled(String message) {
        logMutex.lock();
        try {
            String timestamp = "[" + getCurrentTime() + "] ";
            String formattedMessage = timestamp + lastMode.toString() + lastMode.getColor() + message;
            writeToLog(formattedMessage);
        } finally {
            logMutex.unlock();
        }
    }

    @Override
    public void logThread(String message) {
        logMessage(LogLevel.THREAD, message);
    }

    /**
     * Generic method to log a message with a specific level
     */
    private void logMessage(LogLevel level, String message) {
        logMutex.lock();
        try {
            String formattedMessage = formatLogMessage(level, message);
            writeToLog(formattedMessage);
        } finally {
            logMutex.unlock();
        }
    }

    /**
     * Write a message to the log file
     */
    private void writeToLog(String message) {
        if (logWriter != null) {
            logWriter.println(message);
            logWriter.flush();
        } else {
            // Fallback to console if file writer is not available
            System.out.println(message);
        }
    }

    /**
     * Update the last mode information based on current robot mode
     */
    private void updateLastMode(String mode) {
        switch (mode.toLowerCase()) {
            case "autonomous":
                lastMode = new ModeInfo("<span style=\"color:#800080\">", "[AUTONOMOUS]");
                break;
            case "teleop":
                lastMode = new ModeInfo("<span style=\"color:#00FFFF\">", "[TELEOP]");
                break;
            case "test":
                lastMode = new ModeInfo("<span style=\"color:#FFFF00\">", "[TEST]");
                break;
            case "disabled":
                // Keep the previous mode color and name for disabled
                break;
            default:
                lastMode = new ModeInfo("<span style=\"color:#008000\">", "[UNKNOWN]");
                break;
        }
    }

    /**
     * Close the log file and clean up resources
     */
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }

    /**
     * Get the path to the log file
     */
    public String getLogFilePath() {
        return FILE_PATH;
    }
}