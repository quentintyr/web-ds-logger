package java;

/**
 * Information about the current robot mode
 */
public class ModeInfo {
    private String color;
    private String name;

    public ModeInfo(String color, String name) {
        this.color = color;
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return color + name;
    }
}