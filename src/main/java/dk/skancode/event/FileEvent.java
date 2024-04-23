package dk.skancode.event;

import java.nio.file.Path;

public class FileEvent extends Event {
    private final Path path;
    public FileEvent(EventType type, Path path) {
        super(type);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Path getAbsolutePath() {
        return path.toAbsolutePath().normalize();
    }
}
