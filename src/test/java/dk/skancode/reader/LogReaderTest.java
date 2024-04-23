package dk.skancode.reader;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;

class LogReaderTest {
    final String logFile = "./log.txt";
    final String errorLogFile = "./errorlog.txt";
    IReader<Log> logReader;
    IReader<Log> errorLogReader;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        logReader = new LogReader(logFile);
        errorLogReader = new LogReader(errorLogFile);
    }

    @Test
    void readLine() throws Exception {
        // logReader test
        for (int i = 0; logReader.canRead() && i < 10; i++ ) {
            int prevSize = logReader.getAll().size();
            assertEquals(i, prevSize);
            logReader.readLine();
            assertNotEquals(prevSize, logReader.getAll().size());
        }

        // errorLogReader test
        for (int i = 0; errorLogReader.canRead() && i < 10; i++ ) {
            int prevSize = errorLogReader.getAll().size();
            assertEquals(i, prevSize);
            assertDoesNotThrow(() ->errorLogReader.readLine());
            assertNotEquals(prevSize, errorLogReader.getAll().size());
        }
    }

    @Test
    void readAll() {
        // logReader test
        int startSize = logReader.getAll().size();
        assertEquals(0, startSize);
        assertDoesNotThrow(() -> logReader.readAll());
        assertNotEquals(startSize, logReader.getAll().size());
        assertEquals(5, logReader.getAll().size());

        // errorLogReader test
        startSize = errorLogReader.getAll().size();
        assertEquals(0, startSize);
        assertDoesNotThrow(() -> errorLogReader.readAll());
        assertNotEquals(startSize, errorLogReader.getAll().size());
        assertEquals(5, errorLogReader.getAll().size());
    }
}