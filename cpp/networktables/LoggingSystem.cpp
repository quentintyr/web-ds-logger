#include "web-ds-logger/cpp/networktables/LoggingSystem.h"

#include <networktables/NetworkTableInstance.h>
#include <frc/RobotController.h>
#include <frc/DriverStation.h>
#include <fstream>
#include <cstdio>
#include <mutex>
#include <sstream>
#include <vector>

ModeInfo last_mode = {LOG_GREEN, "[INIT]"};
std::mutex log_mutex;

// NetworkTables logging variables
static std::shared_ptr<nt::NetworkTable> logsTable;
static int logEntryCounter = 0;
static std::vector<std::string> logHistory;
static const int MAX_LOG_HISTORY = 500; // Keep last 500 log entries

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

                        // Also store with unique entry number for debugging
                        std::string entryKey = "entry_" + std::to_string(logEntryCounter++);
                        logsTable->PutString(entryKey, line);

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

    LOG_INFO("Finished Setup Logging (NetworkTables Only)");
}

void UpdateLogging()
{
    // check battery voltage
    auto table = nt::NetworkTableInstance::GetDefault().GetTable("Dashboard");
    double batteryVoltage = frc::RobotController::GetInputVoltage();
    table->PutNumber("Battery", batteryVoltage);
    // robot mode
    auto mode = GetRobotMode();
    table->PutString("RobotMode", mode);

    // get can status
    // bool GetCANStatus
}

void InitLogging()
{
    // Initialize NetworkTables connections
    auto inst = nt::NetworkTableInstance::GetDefault().GetTable("Logs");
    inst->PutString("Log", "test");
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

    LOG_INFO("Initializing Robot...");
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