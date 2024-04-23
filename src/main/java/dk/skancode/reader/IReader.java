package dk.skancode.reader;

import java.util.List;

public interface IReader<T> {
    IReader<T> readLine() throws Exception;
    IReader<T> readAll() throws Exception;
    List<T> getAll();
    boolean canRead();
}
