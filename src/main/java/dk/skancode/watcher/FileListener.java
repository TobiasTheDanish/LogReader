package dk.skancode.watcher;

import dk.skancode.event.EventType;
import dk.skancode.event.FileEvent;
import dk.skancode.event.IEventListener;
import dk.skancode.reader.Log;
import dk.skancode.tree.FileTree;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class FileListener implements IEventListener<FileEvent> {
    private final Map<String, List<Log>> logMap;
    private final FileTree tree;
    private FileEvent lastEvent = null;
    protected FileListener(String path) throws IOException {
        tree = new FileTree(Paths.get(path));
        logMap = tree.getLogMap();
    }

    public Map<String, List<Log>> getLogMap() {
        return logMap;
    }

    public List<Log> getLogsForFile(String name) {
        return logMap.get(name);
    }
    @Override
    public void onNotify(FileEvent event) {
        if (lastEvent != null && lastEvent.getType() == event.getType() && lastEvent.getPath().toString().equalsIgnoreCase(event.getPath().toString())) {
            System.out.println("Event '" + event.getType() + "' skipped for '" + event.getPath().toString() + "'.");
            return;
        }
        lastEvent = event;

        System.out.println(event.getPath() + " was " + event.getType());
        if (event.getType() == EventType.CREATE) {
            if(tree.addNode(event.getPath())) {
                logMap.put(event.getPath().toString(), new ArrayList<>());
            } else {
                System.err.println("Could not add node to tree!");
                System.exit(1);
            }
        } else if (event.getType() == EventType.DELETE) {
            if (tree.removeNode(event.getPath())) {
                logMap.remove(event.getPath().toString());
            } else {
                System.err.println("Could not remove node to tree!");
                System.exit(1);
            }
        } else if (event.getType() == EventType.MODIFY) {
            Optional<FileTree.Node> optNode = tree.getNode(event.getPath());

            if (optNode.isEmpty()) {
                return;
            }

            try {
                FileTree.Node node = optNode.get();

                node.readNewLogs();
            } catch (Exception e) {
                System.err.println("Could not read new logs for path: " + event.getPath() + " | Error message: " + e.getMessage());
            }
        }
    }
}
