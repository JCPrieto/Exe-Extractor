package es.jklabs.file.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExeFilterTest {

    private final ExeFilter filter = new ExeFilter();

    @Test
    void acceptReturnsTrueForDirectories(@TempDir Path tempDir) {
        assertTrue(filter.accept(tempDir.toFile()));
    }

    @Test
    void acceptReturnsTrueForSupportedExtensions(@TempDir Path tempDir) throws IOException {
        File exeFile = Files.createFile(tempDir.resolve("installer.exe")).toFile();
        File upperCaseExeFile = Files.createFile(tempDir.resolve("INSTALLER.EXE")).toFile();
        File msiFile = Files.createFile(tempDir.resolve("package.msi")).toFile();

        assertTrue(filter.accept(exeFile));
        assertTrue(filter.accept(upperCaseExeFile));
        assertTrue(filter.accept(msiFile));
    }

    @Test
    void acceptReturnsFalseForUnsupportedOrMalformedNames(@TempDir Path tempDir) throws IOException {
        File txtFile = Files.createFile(tempDir.resolve("notes.txt")).toFile();
        File noExtensionFile = Files.createFile(tempDir.resolve("installer")).toFile();
        File hiddenLikeFile = Files.createFile(tempDir.resolve(".exe")).toFile();
        File trailingDotFile = Files.createFile(tempDir.resolve("installer.")).toFile();

        assertFalse(filter.accept(txtFile));
        assertFalse(filter.accept(noExtensionFile));
        assertFalse(filter.accept(hiddenLikeFile));
        assertFalse(filter.accept(trailingDotFile));
    }

    @Test
    void getDescriptionReturnsExpectedText() {
        assertEquals("*.exe, *.msi", filter.getDescription());
    }
}
