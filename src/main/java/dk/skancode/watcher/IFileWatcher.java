package dk.skancode.watcher;

import java.io.IOException;

public interface IFileWatcher {
    void register(String dirPath) throws IOException;
    void deregister(String dirPath);
    void watch() throws InterruptedException, IOException;
}
