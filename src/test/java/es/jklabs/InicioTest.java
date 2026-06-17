package es.jklabs;

import com.fasterxml.jackson.databind.JsonNode;
import es.jklabs.utilidades.Constantes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.*;

class InicioTest {

    private static Inicio newInstanceWithoutConstructor() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            return (Inicio) unsafe.allocateInstance(Inicio.class);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear Inicio sin constructor", e);
        }
    }

    private static Object invoke(Inicio target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = Inicio.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo invocar " + methodName, e);
        }
    }

    private static void setField(Inicio target, String fieldName, Object value) {
        try {
            Field field = Inicio.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo asignar " + fieldName, e);
        }
    }

    @Test
    void normalizeVersionHandlesNullAndPrefix() {
        Inicio inicio = newInstanceWithoutConstructor();
        assertEquals("", invoke(inicio, "normalizeVersion", new Class<?>[]{String.class}, (Object) null));
        assertEquals("1.2.3", invoke(inicio, "normalizeVersion", new Class<?>[]{String.class}, "v1.2.3"));
        assertEquals("2", invoke(inicio, "normalizeVersion", new Class<?>[]{String.class}, "V2"));
        assertEquals("1.0", invoke(inicio, "normalizeVersion", new Class<?>[]{String.class}, " 1.0 "));
    }

    @Test
    void parseVersionPartExtractsLeadingDigits() {
        Inicio inicio = newInstanceWithoutConstructor();
        assertEquals(1, invoke(inicio, "parseVersionPart", new Class<?>[]{String.class}, "1"));
        assertEquals(1, invoke(inicio, "parseVersionPart", new Class<?>[]{String.class}, "01alpha"));
        assertEquals(0, invoke(inicio, "parseVersionPart", new Class<?>[]{String.class}, "beta"));
    }

    @Test
    void compareVersionPartsUsesNumericThenLexicalFallback() {
        Inicio inicio = newInstanceWithoutConstructor();
        int numericCompare = (int) invoke(inicio, "compareVersionParts",
                new Class<?>[]{String.class, String.class}, "1.2.3", "1.10.0");
        assertTrue(numericCompare < 0);

        int lexicalFallback = (int) invoke(inicio, "compareVersionParts",
                new Class<?>[]{String.class, String.class}, "1.2.0", "1.2");
        assertTrue(lexicalFallback > 0);

        int latestHasAdditionalParts = (int) invoke(inicio, "compareVersionParts",
                new Class<?>[]{String.class, String.class}, "1", "1.0.1");
        assertTrue(latestHasAdditionalParts < 0);

        int currentHasAdditionalParts = (int) invoke(inicio, "compareVersionParts",
                new Class<?>[]{String.class, String.class}, "1.0.1", "1");
        assertTrue(currentHasAdditionalParts > 0);
    }

    @Test
    void isNewerVersionDetectsUpdates() {
        Inicio inicio = newInstanceWithoutConstructor();
        assertTrue((boolean) invoke(inicio, "isNewerVersion",
                new Class<?>[]{String.class}, "9999.0.0"));
        assertFalse((boolean) invoke(inicio, "isNewerVersion",
                new Class<?>[]{String.class}, "0.0.0"));
    }

    @Test
    void extractJsonValueFindsTagName() {
        Inicio inicio = newInstanceWithoutConstructor();
        String json = "{\"tag_name\":\"v1.2.3\",\"name\":\"release\"}";
        assertEquals("v1.2.3", invoke(inicio, "extractJsonValue",
                new Class<?>[]{String.class}, json));
        assertNull(invoke(inicio, "extractJsonValue",
                new Class<?>[]{String.class}, "{\"name\":\"release\"}"));
        assertNull(invoke(inicio, "extractJsonValue",
                new Class<?>[]{String.class}, "{\"tag_name\":null}"));
        assertNull(invoke(inicio, "extractJsonValue",
                new Class<?>[]{String.class}, ""));
        assertNull(invoke(inicio, "extractJsonValue",
                new Class<?>[]{String.class}, "{"));
    }

    @Test
    void buildAssetNameReplacesVersion() {
        Inicio inicio = newInstanceWithoutConstructor();
        String expected = null;
        if (Constantes.GITHUB_ASSET_PATTERN != null && !Constantes.GITHUB_ASSET_PATTERN.isEmpty()) {
            expected = Constantes.GITHUB_ASSET_PATTERN.replace("{version}", "1.2");
        }
        assertEquals(expected, invoke(inicio, "buildAssetName",
                new Class<?>[]{String.class}, "v1.2"));
    }

    @Test
    void extractAssetUrlPrefersNamedAssetThenZipFallback() {
        Inicio inicio = newInstanceWithoutConstructor();
        String desiredAssetName = Constantes.GITHUB_ASSET_PATTERN;
        if (desiredAssetName != null && !desiredAssetName.isEmpty()) {
            desiredAssetName = desiredAssetName.replace("{version}", "1.2.3");
        }
        String namedJson = "{\"assets\":[{\"name\":\"" + desiredAssetName + "\"," +
                "\"browser_download_url\":\"https://example.com/named.zip\"}]}";
        if (desiredAssetName != null && !desiredAssetName.isEmpty()) {
            assertEquals("https://example.com/named.zip", invoke(inicio, "extractAssetUrl",
                    new Class<?>[]{String.class, String.class}, namedJson, "v1.2.3"));
        }

        String zipFallbackJson = "{\"assets\":[{\"name\":\"other\"," +
                "\"browser_download_url\":\"https://example.com/first.zip\"}]}";
        assertEquals("https://example.com/first.zip", invoke(inicio, "extractAssetUrl",
                new Class<?>[]{String.class, String.class}, zipFallbackJson, "1.0"));
    }

    @Test
    void extractAssetUrlHandlesMissingAssetsAndInvalidDownloads() {
        Inicio inicio = newInstanceWithoutConstructor();
        assertNull(invoke(inicio, "extractAssetUrl",
                new Class<?>[]{String.class, String.class}, (Object) null, "1.0"));
        assertNull(invoke(inicio, "extractAssetUrl",
                new Class<?>[]{String.class, String.class}, "{\"name\":\"release\"}", "1.0"));
        assertNull(invoke(inicio, "extractAssetUrl",
                new Class<?>[]{String.class, String.class}, "{\"assets\":{}}", "1.0"));

        String desiredAssetName = Constantes.GITHUB_ASSET_PATTERN.replace("{version}", "1.2.3");
        String json = "{\"assets\":[" +
                "{\"name\":\"" + desiredAssetName + "\"}," +
                "{\"name\":\"" + desiredAssetName + "\",\"browser_download_url\":null}," +
                "{\"name\":\"" + desiredAssetName + "\",\"browser_download_url\":\"https://example.com/named.zip\"}" +
                "]}";
        assertEquals("https://example.com/named.zip", invoke(inicio, "extractAssetUrl",
                new Class<?>[]{String.class, String.class}, json, "v1.2.3"));

        String noZipJson = "{\"assets\":[" +
                "{\"name\":\"other\",\"browser_download_url\":\"https://example.com/app.exe\"}," +
                "{\"name\":\"other\",\"browser_download_url\":null}," +
                "{\"name\":\"other\"}" +
                "]}";
        assertNull(invoke(inicio, "extractAssetUrl",
                new Class<?>[]{String.class, String.class}, noZipJson, "1.0"));
    }

    @Test
    void namedAssetHelpersHandleBlankDesiredNameAndMissingAssetName() {
        Inicio inicio = newInstanceWithoutConstructor();
        JsonNode release = (JsonNode) invoke(inicio, "readJson",
                new Class<?>[]{String.class}, "{\"assets\":[{\"browser_download_url\":\"https://example.com/app.zip\"}]}");
        JsonNode assets = (JsonNode) invoke(inicio, "getReleaseAssets",
                new Class<?>[]{JsonNode.class}, release);

        assertNull(invoke(inicio, "getNamedAssetUrl",
                new Class<?>[]{JsonNode.class, String.class}, assets, ""));
        assertFalse((boolean) invoke(inicio, "isNamedAsset",
                new Class<?>[]{JsonNode.class, String.class}, assets.get(0), "app.zip"));
    }

    @Test
    void validatorsAcceptExistingPathsAndRejectMissingValues(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path file = Files.createFile(tempDir.resolve("installer.exe"));
        Path directory = Files.createDirectory(tempDir.resolve("output"));

        assertTrue((boolean) invoke(inicio, "validarArchivo",
                new Class<?>[]{String.class, boolean.class}, file.toString(), false));
        assertFalse((boolean) invoke(inicio, "validarArchivo",
                new Class<?>[]{String.class, boolean.class}, null, false));
        assertFalse((boolean) invoke(inicio, "validarArchivo",
                new Class<?>[]{String.class, boolean.class}, "  ", false));
        assertFalse((boolean) invoke(inicio, "validarArchivo",
                new Class<?>[]{String.class, boolean.class}, directory.toString(), false));

        assertTrue((boolean) invoke(inicio, "validarDirectorio",
                new Class<?>[]{String.class, boolean.class}, directory.toString(), false));
        assertFalse((boolean) invoke(inicio, "validarDirectorio",
                new Class<?>[]{String.class, boolean.class}, null, false));
        assertFalse((boolean) invoke(inicio, "validarDirectorio",
                new Class<?>[]{String.class, boolean.class}, "  ", false));
        assertFalse((boolean) invoke(inicio, "validarDirectorio",
                new Class<?>[]{String.class, boolean.class}, file.toString(), false));
    }

    @Test
    void routeValidatorsCombineFileAndDirectoryChecks(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path file = Files.createFile(tempDir.resolve("installer.exe"));
        Path directory = Files.createDirectory(tempDir.resolve("output"));
        setField(inicio, "rutaArchivo", file.toString());
        setField(inicio, "rutaSave", directory.toString());

        assertTrue((boolean) invoke(inicio, "validarRutasSilencioso", new Class<?>[]{}));
        assertTrue((boolean) invoke(inicio, "validarRutas", new Class<?>[]{}));

        setField(inicio, "rutaSave", tempDir.resolve("missing").toString());
        assertFalse((boolean) invoke(inicio, "validarRutasSilencioso", new Class<?>[]{}));
    }

    @Test
    void listFilesReturnsOnlyFilesOrEmptyArray(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path file = Files.createFile(tempDir.resolve("source.exe"));
        Files.createDirectory(tempDir.resolve("nested"));

        File[] files = (File[]) invoke(inicio, "listFiles", new Class<?>[]{File.class}, tempDir.toFile());
        assertEquals(1, files.length);
        assertEquals("source.exe", files[0].getName());

        File[] empty = (File[]) invoke(inicio, "listFiles", new Class<?>[]{File.class}, file.toFile());
        assertEquals(0, empty.length);
    }

    @Test
    void normalizeZipNameUsesConfiguredValue() {
        Inicio inicio = newInstanceWithoutConstructor();
        assertEquals("Exe.zip", invoke(inicio, "normalizeZipName", new Class<?>[]{}));
    }

    @Test
    void resolveOutputFileAddsNumericSuffixWhenTargetExists(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Files.createFile(tempDir.resolve("Exe.zip"));
        Files.createFile(tempDir.resolve("Exe-1.zip"));

        File resolved = (File) invoke(inicio, "resolveOutputFile",
                new Class<?>[]{File.class, String.class}, tempDir.toFile(), "Exe.zip");

        assertEquals("Exe-2.zip", resolved.getName());
    }

    @Test
    void resolveOutputFileUsesRequestedNameWhenAvailable(@TempDir Path tempDir) {
        Inicio inicio = newInstanceWithoutConstructor();

        File resolved = (File) invoke(inicio, "resolveOutputFile",
                new Class<?>[]{File.class, String.class}, tempDir.toFile(), "Custom.zip");

        assertEquals("Custom.zip", resolved.getName());
    }

    @Test
    void detectarArchivoGeneradoPrefersPlaceholder(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        File[] beforeFiles = new File[0];
        Files.createFile(tempDir.resolve("%EXENAME%"));
        Files.createFile(tempDir.resolve("otro.dat"));

        File detected = (File) invoke(inicio, "detectarArchivoGenerado",
                new Class<?>[]{File.class, File[].class}, tempDir.toFile(), beforeFiles);

        assertEquals("%EXENAME%", detected.getName());
    }

    @Test
    void detectarArchivoGeneradoFindsNewestNewFile(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path existing = Files.createFile(tempDir.resolve("existing.dat"));
        Path oldNewFile = Files.createFile(tempDir.resolve("old.dat"));
        Path newestNewFile = Files.createFile(tempDir.resolve("newest.dat"));
        Files.setLastModifiedTime(oldNewFile, FileTime.fromMillis(1_000));
        Files.setLastModifiedTime(newestNewFile, FileTime.fromMillis(2_000));

        File detected = (File) invoke(inicio, "detectarArchivoGenerado",
                new Class<?>[]{File.class, File[].class}, tempDir.toFile(), new File[]{existing.toFile()});

        assertEquals("newest.dat", detected.getName());
    }

    @Test
    void detectarArchivoGeneradoFallsBackToNewestExistingFile(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path older = Files.createFile(tempDir.resolve("older.dat"));
        Path newer = Files.createFile(tempDir.resolve("newer.dat"));
        Files.setLastModifiedTime(older, FileTime.fromMillis(1_000));
        Files.setLastModifiedTime(newer, FileTime.fromMillis(2_000));

        File detected = (File) invoke(inicio, "detectarArchivoGenerado",
                new Class<?>[]{File.class, File[].class}, tempDir.toFile(), new File[]{
                        older.toFile(), newer.toFile()
                });

        assertEquals("newer.dat", detected.getName());
    }

    @Test
    void detectarArchivoGeneradoReturnsNullWhenDirectoryHasNoFiles(@TempDir Path tempDir) {
        Inicio inicio = newInstanceWithoutConstructor();

        assertNull(invoke(inicio, "detectarArchivoGenerado",
                new Class<?>[]{File.class, File[].class}, tempDir.toFile(), new File[0]));
    }
}
