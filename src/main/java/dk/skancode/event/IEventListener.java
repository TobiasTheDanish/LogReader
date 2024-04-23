package dk.skancode.event;

public interface IEventListener<T extends Event> {
    default void notify(T event) {
        onNotify(event);
    }
    void onNotify(T event);
}
