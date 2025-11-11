#include "web-ds-logger/cpp/networktables/LoggingSystem.h"
#include "subsystems/sensor/IRRangeSubsystem.h"
#include "subsystems/sensor/UltrasonicSubsystem.h"
#include "subsystems/sensor/SensorManager.h"
#include "RobotContainer.h"

#include <networktables/NetworkTableInstance.h>
#include <frc/RobotController.h>
#include <frc/DriverStation.h>
#include <fstream>
#include <cstdio>
#include <mutex>
#include <sstream>
#include <vector>
#include <cmath>

ModeInfo last_mode = {LOG_GREEN, "[INIT]"};
std::mutex log_mutex;

// NetworkTables logging variables
static std::shared_ptr<nt::NetworkTable> logsTable;
static int logEntryCounter = 0;
static std::vector<std::string> logHistory;
static const int MAX_LOG_HISTORY = 500; // Keep last 500 log entries

inline double roundTo2Decimals(double value)
{
    return std::round(value * 100.0) / 100.0;
}

void SetupLogging()
{
    // Initialize NetworkTables logging
    logsTable = nt::NetworkTableInstance::GetDefault().GetTable("Logs");
    logEntryCounter = 0;
    logHistory.clear();

    // Create custom stream buffer that writes directly to NetworkTables (no file)
    class NetworkTablesBuffer : public std::streambuf
    {
    private:
        std::ostringstream lineBuffer;

    public:
        NetworkTablesBuffer() = default;

    protected:
        virtual int overflow(int c) override
        {
            if (c != EOF)
            {
                // Accumulate characters for NetworkTables
                lineBuffer << static_cast<char>(c);

                // If we hit a newline, send the complete line to NetworkTables
                if (c == '\n')
                {
                    std::string line = lineBuffer.str();
                    if (!line.empty() && line.back() == '\n')
                    {
                        line.pop_back(); // Remove trailing newline
                    }

                    // Send to NetworkTables
                    if (logsTable && !line.empty())
                    {
                        // Add to log history
                        logHistory.push_back(line);

                        // Keep history size manageable
                        if (logHistory.size() > MAX_LOG_HISTORY)
                        {
                            logHistory.erase(logHistory.begin());
                        }

                        // Send the latest log entry for real-time updates
                        logsTable->PutString("latest", line);

                        // Send complete log history as a single string for initial load
                        std::ostringstream historyStream;
                        for (const auto &logLine : logHistory)
                        {
                            historyStream << logLine << "\n";
                        }
                        logsTable->PutString("history", historyStream.str());

                        // std::string entryKey = "entry_" + std::to_string(logEntryCounter++);
                        // logsTable->PutString(entryKey, line);

                        // Keep a timestamp
                        logsTable->PutNumber("timestamp", std::time(nullptr));
                    }

                    lineBuffer.str(""); // Clear the buffer
                    lineBuffer.clear();
                }
            }
            return c;
        }

        virtual int sync() override
        {
            return 0; // No file sync needed
        }
    };

    // Create and install custom buffer for both cout and cerr
    static NetworkTablesBuffer *ntBuffer = new NetworkTablesBuffer();
    std::cout.rdbuf(ntBuffer);
    std::cerr.rdbuf(ntBuffer);

    LOG_INFO("Finished Setup Logging");
}

void UpdateLogging(SensorManager *sensorManager)
{
    // check battery voltage
    auto dashboard = nt::NetworkTableInstance::GetDefault().GetTable("Dashboard");
    double batteryVoltage = frc::RobotController::GetInputVoltage();
    dashboard->PutNumber("Battery", batteryVoltage);

    if (sensorManager && sensorManager->GetUltrasonicSubsystem())
    {
        auto ultraSonic = sensorManager->GetUltrasonicSubsystem();
        dashboard->PutNumber("USSensorLeft", roundTo2Decimals(ultraSonic->GetLeftDistance()));
        dashboard->PutNumber("USSensorRight", roundTo2Decimals(ultraSonic->GetRightDistance()));
    }

    if (sensorManager && sensorManager->GetIRRangeSubsystem())
    {
        auto irSensor = sensorManager->GetIRRangeSubsystem();
        dashboard->PutNumber("IRSensorLeft", roundTo2Decimals(irSensor->GetIRLeftDistance()));
        dashboard->PutNumber("IRSensorRight", roundTo2Decimals(irSensor->GetIRRightDistance()));
    }

    // LiDAR sensor data (if available)
    if (sensorManager)
    {
        auto lidarSensor = sensorManager->GetLidarSubsystem();
        if (lidarSensor)
        {
            dashboard->PutNumber("lidarDistance", roundTo2Decimals(lidarSensor->GetDistanceAtAngle(0)));
        }
    }

    // robot mode
    auto mode = GetRobotMode();
    dashboard->PutString("RobotMode", mode);
}

void InitLogging(SensorManager *sensorManager)
{
    // Initialize NetworkTables connections
    auto table = nt::NetworkTableInstance::GetDefault().GetTable("Dashboard");
    table->PutString("RobotMode", "init");
    table->PutBoolean("Connected", true);

    // Initialize logs table if not already done
    if (!logsTable)
    {
        logsTable = nt::NetworkTableInstance::GetDefault().GetTable("Logs");
    }

    // Send initialization message
    std::string initMsg = "[" + current_time() + "] " + LOG_GREEN + "[INFO]" + LOG_RESET + " Initializing Robot...";
    logHistory.push_back(initMsg);
    logsTable->PutString("latest", initMsg);
    logsTable->PutString("history", initMsg + "\n");
}

std::string GetRobotMode()
{
    auto &ds = frc::DriverStation::GetInstance();

    if (ds.IsDisabled())
    {
        return "disabled";
    }
    else if (ds.IsAutonomous())
    {
        return "autonomous";
    }
    else if (ds.IsOperatorControl())
    {
        return "teleop";
    }
    else if (ds.IsTest())
    {
        return "test";
    }
    else
    {
        return "unknown";
    }
}