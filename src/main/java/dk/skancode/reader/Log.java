package dk.skancode.reader;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonSerialize(using = LogSerializer.class)
public record Log(LogLevel level, LocalDateTime time, Optional<String> thread, String method, String message) {
}
