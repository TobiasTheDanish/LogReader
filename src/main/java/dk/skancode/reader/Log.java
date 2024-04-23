package dk.skancode.reader;

import java.time.LocalDateTime;
import java.util.Optional;

public record Log(LogLevel level, LocalDateTime time, Optional<String> thread, String method, String message) {
}
