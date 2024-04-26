package dk.skancode.tree;

import dk.skancode.reader.IReader;
import dk.skancode.reader.Log;
import dk.skancode.reader.LogReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileTree implements ITree<FileTree.Node, Path> {
    private final Node root;

    public FileTree(Path path) throws IOException {
        this.root = new Node(path.toString(), path);
        generateTree(this.root, path);
    }

    public Map<String, List<Log>> getLogMap() {
        return root.getLogMap();
    }

    private void generateTree(Node root, Path rootPath) throws IOException {
        if (!Files.isDirectory(rootPath)) throw new IOException("Expected directory but got file.");

        try (Stream<Path> stream = Files.list(rootPath)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            for (Path file : files) {
                System.out.println("Node '" + file + "' added to tree.");
                root.addChild(new Node(file.toString(), file));
            }
        }

        try (Stream<Path> stream = Files.list(rootPath)) {
            List<Path> dirs = stream.filter(Files::isDirectory).toList();

            for (Path dir : dirs) {
                Node child = new Node(dir.toString(), dir);
                root.addChild(child);
                System.out.println("Node '" + dir + "' added to tree.");
                generateTree(child, dir);
            }
        }
    }

    public Optional<Node> getNode(Path path) {
        return root.search(path.toString());
    }

    public boolean addNode(Path path) {
        Path parent = path.getParent();
        Optional<Node> optParentNode = root.search(parent.toString());
        if (optParentNode.isEmpty()) {
            System.err.println("Could not find a node with path: " + parent);
            System.out.println(this);
            return false;
        }

        Node parentNode = optParentNode.get();

        try {
            Node newChild = new Node(path.toString(), path);
            if (Files.isDirectory(path)) {
                generateTree(newChild, path);
            }
            parentNode.addChild(newChild);
            System.out.println("Node '" + path + "' added to tree.");

            return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean removeNode(Path path) {
        Optional<Node> optNode = root.search(path.toString());

        if (optNode.isEmpty()) {
            System.err.println("Could not find a node with path: " + path);
            System.out.println(this);
            return false;
        }

        Node nodeToRemove = optNode.get();

        Path parentPath = nodeToRemove.getRelativePath().getParent();
        Optional<Node> optParentNode = root.search(parentPath.toString());
        if (optParentNode.isEmpty()) {
            System.err.println("Could not find a node with path: " + parentPath);
            System.out.println(this);
            return false;
        }

        Node parentNode = optParentNode.get();
        parentNode.removeChild(path.toString());

        System.out.println("Node '" + path + "' removed from tree.");
        return true;
    }

    @Override
    public String toString() {
        return root.toString(2);
    }

    public static class Node {
        private final String name;
        private final Path relativePath;
        private final boolean exists;
        private final boolean isDir;
        private List<Node> children;
        private IReader<Log> reader;

        public Node(String name, Path relativePath) throws FileNotFoundException {
            this.name = name;
            this.relativePath = relativePath;
            this.exists = Files.exists(relativePath);
            this.isDir = Files.isDirectory(relativePath);
            children = new ArrayList<>();
            if (!this.isDir) {
                reader = new LogReader(relativePath.toString());
                if (reader.canRead()) {
                    try {
                        reader.readAll();
                    } catch (Exception e) {
                        throw new RuntimeException("Could not read from reader of path: '" + relativePath + "'. Error message: " + e);
                    }
                }
            }
        }

        public Optional<Node> search(String name) {
            if (this.name.equals(name)) return Optional.of(this);

            Optional<Node> node = children.stream().filter(child -> child.name.equals(name)).findFirst();

            if (!node.isEmpty()) {
                return node;
            }

            for (Node child : children) {
                node = child.search(name);
                if (!node.isEmpty()) {
                    return node;
                }
            }

            return Optional.empty();
        }

        public Map<String, List<Log>> getLogMap() {
            var map = new HashMap<String, List<Log>>();
            if (!this.isDir) {
                map.put(this.name, this.reader.getAll());
                return map;
            }

            for (Node child : children) {
                var childMap = child.getLogMap();

                map.putAll(childMap);
            }

            return map;
        }

        public List<Log> readNewLogs() throws Exception {
            int startListSize = reader.getAll().size();

            while (reader.canRead()) {
                reader.readLine();
            }

            return reader.getAll().stream().skip(startListSize).collect(Collectors.toList());
        }

        public void addChild(Node child) {
            children.add(child);
        }

        public void removeChild(String name) {
            children = children.stream().filter(child -> !child.name.equals(name)).collect(Collectors.toList());
        }

        public Node getChild(int i) {
            return children.get(i);
        }

        public String getName() {
            return name;
        }

        public Path getRelativePath() {
            return relativePath;
        }

        public boolean exists() {
            return exists;
        }

        public boolean isDir() {
            return isDir;
        }

        public String toString(int indentFactor) {
            return toString(indentFactor, 1);
        }

        private String toString(int indentFactor, int level) {
            StringBuilder sb = new StringBuilder();
            String baseIndent = " ".repeat(Math.max(0, indentFactor * (level-1)));
            String indent = " ".repeat(Math.max(0, indentFactor * level));
            sb.append(baseIndent).append("Node: {\n");
            sb.append(indent).append("Type: ").append(this.isDir ? "Directory" : "File").append(", \n");
            sb.append(indent).append("Name: ").append(this.name).append(", \n");
            sb.append(indent).append("Path: ").append(this.relativePath.toString()).append(", \n");
            if (this.isDir) {
                sb.append(indent).append("Children: {\n");
                for (Node child : children) {
                    sb.append(child.toString(indentFactor, level + 2));
                }
                sb.append(indent).append("},\n");
            }
            sb.append(baseIndent).append("},\n");

            return sb.toString();
        }
    }
}
