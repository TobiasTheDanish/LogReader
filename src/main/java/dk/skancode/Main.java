package dk.skancode;

import dk.skancode.event.EventType;
import dk.skancode.event.FileEvent;
import dk.skancode.event.IEventListener;
import dk.skancode.watcher.FileWatcher;
import dk.skancode.watcher.IFileWatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
//        var app = Javalin.create().get("/", (ctx) -> ctx.result("Hello world"));
//
//        app.start(7070);
        try {
            String testPath = args[0];
            FileWatcher watcher = FileWatcher.getInstance();
            watcher.addListener(new EventListener(testPath));

            watcher.register(testPath);
            while (true) {
                watcher.watch();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

//        try {
//            FileTree tree = new FileTree(Paths.get("./testDir"));
//            System.out.println(tree);
//            System.out.println(tree.getReaderMap());
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
    }

    private static class EventListener implements IEventListener<FileEvent> {
        private final FileTree tree;
        public EventListener(String path) throws IOException {
            tree = new FileTree(Paths.get(path));
        }
        @Override
        public void onNotify(FileEvent event) {
            if (event.getType() == EventType.CREATE) {
                if(!tree.addNode(event.getPath())) {
                    System.err.println("Could not add node to tree!");
                    System.exit(1);
                }
            } else if (event.getType() == EventType.DELETE) {
                if (!tree.removeNode(event.getPath())) {
                    System.err.println("Could not remove node to tree!");
                    System.exit(1);
                }
            }

//            System.out.println(tree);
        }
    }
}
