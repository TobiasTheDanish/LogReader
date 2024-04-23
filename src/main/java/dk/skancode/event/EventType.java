package dk.skancode.event;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public enum EventType {
    CREATE,
    MODIFY,
    DELETE;


    public static EventType fromEventKind(WatchEvent.Kind<?> kind) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            return EventType.CREATE;
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            return EventType.MODIFY;
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            return EventType.DELETE;
        }

        throw new IllegalArgumentException("WatchEvent Kind '" + kind + "', not supported");
    }
}
