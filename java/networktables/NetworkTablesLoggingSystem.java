package java.networktables;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;

import java.LogLevel;
import java.LoggingSystem;
import java.ModeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * NetworkTables-based logging system for FRC robots
 * Logs messages to NetworkTables for web dashboard integration
 */

    

    
    private NetworkTable logsTable;
    private NetworkTable dashboardTable;
    private int logEntryCounter = 0;
    private List<String> logHistory;
    private ModeInfo lastMode;

    
    public NetworkTablesLoggingSystem() {
        this.logHistory = new ArrayList<>();
        this.lastMode = new ModeInfo("<span style=\"color:#008000\">", "[INIT]");
     

    
    @Override
    public void setupLogging() {
        // Initialize NetworkTables logging
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        logsTable = inst.getTable("Logs");

        
        logEntryCounter = 0

        
     

    
    @Override
    public void updateLogging() {
        // Update battery voltage
        double batteryVoltage = RobotController.getInputVoltage();

        
        // Update robot mode
        String mode = getRobotMode();

        
        // Update last mode info based on current mode
     

    
    @Override
    public void initLogging() {
        // Initialize NetworkTables connections
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable logsTable = inst.getTable("Logs

        
        NetworkTable dashboardTable = inst.getTable("Dashboard");
        dashboardTable.getEntry("RobotMode").setString("init")

        
        // Initialize logs table if not already done
        if (this.logsTable == null) {
         

        
        // Send initialization message
        String initMsg = formatLo

        
     

    
    @Override
    public String getRobotMode() {

        
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
     

    
    @Override
    public void logInfo(String message) {
     

    
    @Override
    public void logWarn(String message) {
     

    
    @Override
    public void logError(String message) {
     

    
    @Override
    public void logAutonomous(String message) {
     

    
    @Override
    public void logTeleop(String message) {
     

    
    @Override
    public void logTest(String message) {
     

    
    @Override
    public void logDisabled(String message) {
        logMutex.lock();
        try {
            String timestamp = "[" + getCurrentTime() + "] ";
            String formattedMessage = timestamp + lastMode.toString() + lastMode.getColor() + message;
            addToLogHistory(formattedMessage);
            System.out.println(formattedMessage);
        } finally {
            logMutex.unlock();
     

    
    @Override
    public void logThread(String message) {
     

    
    /**
     * Generic method to log a message with a specific level
     */
    private void logMessage(LogLevel level, String message) {
        logMutex.lock();
        try {
            String formattedMessage = formatLo

            
            // Output to console as well
            if (level == LogLevel.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        } finally {
            logMutex.unlock();
     

    
    /**
     * Add a message to the log history and update NetworkTables
     */
    private void addToLogHistory(String message) {
        if (message == null || message.trim().isEmpty()) {
         

        
        // Add to log history

        
        // Keep history size manageable
        if (logHistory.size() > MAX_LOG_HISTORY) {
         

        
        if (logsTable != null) {
            // Send the latest log entry for real-time updat

            
            // Send complete log history as a single string for initial load
            StringBuilder historyBuilder = new StringBuilder();
            for (String logLine : logHistory) {
                historyBuilder.append(logLine).append("\n");
            }

            
            // Also store with unique entry number for debugging
            String entryKey = "entry_" + logEntryCounter++;

            
            // Keep a timestamp
            logsTable.getEntry("timestamp").setDouble(System.currentTimeMillis() / 1000.0);
     

    
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
     

    
    /**
     * Get the current log history
     */
    public List<String> getLogHistory() {
     

    
    /**
     * Clear the log history
     */
    public void clearLogHistory() {
        logMutex.lock();
        try {
            logHistory.clear();
            logEntryCounter = 0;
            if (logsTable != null) {
                logsTable.getEntry("history").setString("");
            }
        } finally {
            logMutex.unlock();
        }
    }
}