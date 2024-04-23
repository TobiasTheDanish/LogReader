package dk.skancode.event;

public abstract class Event {
    private EventType type;

    Event(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}
