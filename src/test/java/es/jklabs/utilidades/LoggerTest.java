package es.jklabs.utilidades;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Handler;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    private final String originalUserDir = System.getProperty("user.dir");
    private boolean deleteCreatedLog;

    private static String currentLogName() throws Exception {
        Field archivoField = Logger.class.getDeclaredField("ARCHIVO");
        archivoField.setAccessible(true);
        return (String) archivoField.get(null);
    }

    private static Object getLogger() throws Exception {
        Field loggerField = Logger.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        return loggerField.get(null);
    }

    private static void resetLogger() throws Exception {
        java.util.logging.Logger log = java.util.logging.Logger.getLogger(Logger.class.getName());
        Arrays.stream(log.getHandlers()).forEach(LoggerTest::closeAndRemoveHandler);

        Field loggerField = Logger.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(null, null);
    }

    private static void closeAndRemoveHandler(Handler handler) {
        java.util.logging.Logger log = java.util.logging.Logger.getLogger(Logger.class.getName());
        handler.close();
        log.removeHandler(handler);
    }

    @AfterEach
    void restoreUserDir() throws Exception {
        System.setProperty("user.dir", originalUserDir);
        resetLogger();
        if (deleteCreatedLog) {
            Files.deleteIfExists(Path.of(originalUserDir, currentLogName()));
        }
    }

    @Test
    void eliminarLogsVaciosDeletesOnlyEmptyOldLogFiles(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        Path emptyLog = Files.createFile(tempDir.resolve("empty.log"));
        Path nonEmptyLog = Files.writeString(tempDir.resolve("non-empty.log"), "contenido");
        Path currentLog = Files.createFile(tempDir.resolve(currentLogName()));
        Path textFile = Files.createFile(tempDir.resolve("notes.txt"));
        Path logDirectory = Files.createDirectory(tempDir.resolve("directory.log"));

        Logger.eliminarLogsVacios();

        assertFalse(Files.exists(emptyLog));
        assertTrue(Files.exists(nonEmptyLog));
        assertTrue(Files.exists(currentLog));
        assertTrue(Files.exists(textFile));
        assertTrue(Files.exists(logDirectory));
    }

    @Test
    void eliminarLogsVaciosDoesNothingWhenUserDirDoesNotListFiles(@TempDir Path tempDir) throws Exception {
        System.setProperty("user.dir", Files.createFile(tempDir.resolve("not-a-directory")).toString());

        assertDoesNotThrow(Logger::eliminarLogsVacios);
    }

    @Test
    void initCreatesLoggerOnlyOnce() throws Exception {
        resetLogger();
        deleteCreatedLog = !Files.exists(Path.of(originalUserDir, currentLogName()));

        Logger.init();
        Object firstLogger = getLogger();

        Logger.init();

        assertNotNull(firstLogger);
        assertSame(firstLogger, getLogger());
    }
}
