package dk.skancode.watcher;

import com.sun.management.UnixOperatingSystemMXBean;
import dk.skancode.event.EventType;
import dk.skancode.event.FileEvent;
import dk.skancode.event.IEventListener;
import dk.skancode.event.IEventPublisher;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class FileWatcher implements IFileWatcher, IEventPublisher<FileEvent> {
    private final static long maxNumFiles = ((UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getMaxFileDescriptorCount();
    private final List<IEventListener<FileEvent>> listeners;
    private final Map<String, WatchService> watchServiceMap;
    public static FileWatcher instance = null;
    private final Queue<String> registerQueue = new ArrayDeque<>();
    private final Queue<String> deleteQueue = new ArrayDeque<>();
    private boolean isWatching = false;

    private FileWatcher() {
        this.listeners = new ArrayList<>();
        this.watchServiceMap = new HashMap<>();
        System.out.println("Max number of files: " + maxNumFiles);
    }

    public static FileWatcher getInstance() {
        if (instance == null) {
            instance = new FileWatcher();
        }

        return instance;
    }

    @Override
    public void register(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);
        watchServiceMap.put(dir + "/", FileSystems.getDefault().newWatchService());
        WatchService service = watchServiceMap.get(dir + "/");

        dir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        System.out.println("Path '" + dir + "' registered.");

        try (Stream<Path> stream = Files.list(Paths.get(dirPath))){
            List<Path> paths = stream.filter(Files::isDirectory).toList();
            for (Path path : paths) {
                this.register(path.toString());
            }
        };
    }

    private void queueRegistration(String dirPath) {
        registerQueue.add(dirPath);
    }

    @Override
    public void deregister(String dirPath) {
        watchServiceMap.remove(dirPath);
        System.out.println("Path '" + dirPath + "' deregistered.");
    }

    private void queueDeregistration(String dirPath) {
        deleteQueue.add(dirPath);
    }

    @Override
    public void watch() throws InterruptedException, IOException {
        isWatching = true;
        WatchKey key;
        for (Map.Entry<String, WatchService> entry : watchServiceMap.entrySet()) {
            while ((key = entry.getValue().poll()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path context = ((Path) event.context());
                    if (!context.toString().endsWith("~")) {
                        Path relativePath = Paths.get(entry.getKey() + context);
                        EventType eventType = EventType.fromEventKind(event.kind());

                        switch (eventType) {
                            case CREATE -> {
                                if (Files.isDirectory(relativePath)) {
                                    this.queueRegistration(relativePath.toString());
                                }
                            }
                            case DELETE -> {
                                this.queueDeregistration(relativePath + "/");
                            }
                        }

                        notifyListeners(new FileEvent(eventType, relativePath));
                    }
                }
                key.reset();
            }
        }
        for (String deletePath : deleteQueue) {
            this.deregister(deletePath);
        }
        for (String registerPath : registerQueue) {
            this.register(registerPath);
        }
        deleteQueue.clear();
        registerQueue.clear();
        isWatching = false;
    }

    @Override
    public void addListener(IEventListener<FileEvent> listener) {
        listeners.add(listener);
    }

    @Override
    public void notifyListeners(FileEvent event) {
        listeners.forEach(listener -> listener.notify(event));
    }
}
