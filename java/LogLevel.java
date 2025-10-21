package java;

/**
 * Enumeration for different log levels
 */
public enum LogLevel {
    INFO("INFO", "<span style=\"color:#008000\">", "</span>"),
    WARN("WARN", "<span style=\"color:#FFFF00\">", "</span>"),
    ERROR("ERROR", "<span style=\"color:#FF0000\">", "</span>"),
    AUTONOMOUS("AUTONOMOUS", "<span style=\"color:#800080\">", "</span>"),
    TELEOP("TELEOP", "<span style=\"color:#00FFFF\">", "</span>"),
    TEST("TEST", "<span style=\"color:#FFFF00\">", "</span>"),
    DISABLED("DISABLED", "", "</span>"),
    THREAD("THREAD", "<span style=\"color:#FF0000\">", "</span>");

    private final String name;
    private final String htmlColorStart;
    private final String htmlColorEnd;

    LogLevel(String name, String htmlColorStart, String htmlColorEnd) {
        this.name = name;
        this.htmlColorStart = htmlColorStart;
        this.htmlColorEnd = htmlColorEnd;
    }

    public String getName() {
        return name;
    }

    public String getHtmlColorStart() {
        return htmlColorStart;
    }

    public String getHtmlColorEnd() {
        return htmlColorEnd;
    }
}