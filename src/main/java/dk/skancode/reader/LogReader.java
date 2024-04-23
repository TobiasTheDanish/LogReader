package dk.skancode.reader;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogReader implements IReader<Log> {
    private BufferedReader reader;
    private final List<Log> logs;
    public LogReader(String filePath) throws FileNotFoundException {
        this.logs = new ArrayList<>();
        this.reader = new BufferedReader(new FileReader(filePath));
    }
    @Override
    public IReader<Log> readLine() throws IOException {
        String line = reader.readLine();

        line = line.trim();
        String separator = "[|]";
        String[] sections = line.split(separator);

        if (sections.length == 4) {
            parseErrorFormat(sections).ifPresent(logs::add);
        } else if (sections.length == 3) {
            parseLogFormat(sections).ifPresent(logs::add);
        } else {
            throw new IOException("Unknown logging format. Number of sections: " + sections.length);
        }

        return this;
    }

    @Override
    public IReader<Log> readAll() throws IOException {
        reader.lines().forEach((line) -> {
            line = line.trim();
            String separator = "[|]";
            String[] sections = line.split(separator);

            if (sections.length == 4) {
                parseErrorFormat(sections).ifPresent(logs::add);
            } else if (sections.length == 3) {
                parseLogFormat(sections).ifPresent(logs::add);
            } else {
                System.err.println("Unknown logging format. Number of sections: " + sections.length);
            }
        });

        return this;
    }

    @Override
    public List<Log> getAll() {
        return this.logs;
    }

    @Override
    public boolean canRead() {
        try {
            return reader.ready();
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    private Optional<Log> parseLogFormat(String[] sections) {
        String message = sections[2].trim();
        String method = sections[1].trim().replaceAll("[\\[\\]]", "");

        String dateAndLevel = sections[0].trim();

        var pair = parseDateAndLevel(dateAndLevel);

        if (pair.getFirst().isEmpty() || pair.getSecond().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Log(pair.getFirst().get(), pair.getSecond().get(), Optional.empty(), method, message));
    }

    @NotNull
    private Optional<Log> parseErrorFormat(String[] sections) {
        String message = sections[3].trim();
        String method = sections[2].trim();
        String thread = sections[1].trim().replaceAll("[\\[\\]]", "");
        String dateAndLevel = sections[0].trim();

        var pair = parseDateAndLevel(dateAndLevel);

        if (pair.getFirst().isEmpty() || pair.getSecond().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Log(pair.getFirst().get(), pair.getSecond().get(), Optional.of(thread), method, message));
    }

    private Pair<Optional<LogLevel>, Optional<LocalDateTime>> parseDateAndLevel(String dateAndLevel) {
        int levelStart = dateAndLevel.indexOf('[');
        String levelStr = dateAndLevel.substring(levelStart).replaceAll("[\\[\\]]", "").trim();
        Optional<LogLevel> level = LogLevel.fromString(levelStr);

        try {
            String dateStr = dateAndLevel.substring(0, levelStart).trim();
            LocalDateTime time = LocalDateTime.parse(dateStr);
            return  new Pair<>(level, Optional.of(time));
        } catch (DateTimeParseException e) {
            return new Pair<>(level, Optional.empty());
        }
    }
}
