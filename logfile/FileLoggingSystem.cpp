#include "web-ds-logger/logfile/LoggingSystem.h"

#include "Constants.h"
#include <fstream>
#include <cstdio>
#include <mutex>

ModeInfo last_mode = {LOG_RESET, "[INIT]"};
std::ofstream logFile;
std::mutex log_mutex;

inline constexpr const char *FILE_PATH = "/home/pi/robot.log";

void SetupLogging()
{
    std::remove(FILE_PATH);
    logFile.open(FILE_PATH, std::ios::out);
    std::cout.rdbuf(logFile.rdbuf());
    std::cerr.rdbuf(logFile.rdbuf());
}