package dk.skancode.tree;

import java.util.Optional;

public interface ITree<T, K> {
    Optional<T> getNode(K key);
    boolean addNode(K key);
    boolean removeNode(K key);
}
