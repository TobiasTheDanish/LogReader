package dk.skancode.reader;

import java.util.Arrays;
import java.util.Optional;

public enum LogLevel {
    TRACE(0, "trace"),
    DEBUG(1, "debug"),
    INFO(2, "info"),
    WARN(3, "warn"),
    ERROR(4, "error"),
    FATAL(5, "fatal");

    private final int num;
    private final String name;
    LogLevel(int num, String name) {
        this.num = num;
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public static Optional<LogLevel> fromInt(int num) {
        if (num > 5) {
            return Optional.empty();
        } else {
            return Arrays.stream(LogLevel.values()).filter((val) -> val.getNum() == num).findFirst();
        }
    }

    public static Optional<LogLevel> fromString(String name) {
        return Arrays.stream(LogLevel.values()).filter((level) -> level.getName().equalsIgnoreCase(name)).findFirst();
    }
}
