package dk.skancode.watcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class FileListenerFactory {
    private static FileListener listener = null;

    private FileListenerFactory() {}
    @Nullable
    public static FileListener getListener() {
        return listener;
    }

    @NotNull
    public static FileListener getListener(String listenerPath) {
        if (listener == null) {
            try {
                listener = new FileListener(listenerPath);
            } catch (IOException e) {
                System.err.println("Could not create a new file listener. Error message: " + e.getMessage());
            }
        }

        return listener;
    }
}
