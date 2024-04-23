package dk.skancode.event;

public interface IEventPublisher<T extends Event> {
    void addListener(IEventListener<T> listener);
    void notifyListeners(T event);
}
