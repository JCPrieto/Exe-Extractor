package es.jklabs;

import com.fasterxml.jackson.databind.JsonNode;
import es.jklabs.utilidades.Constantes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sun.misc.Unsafe;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class InicioTest {

    @BeforeAll
    static void enableHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

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

    private static Object getField(Inicio target, String fieldName) {
        try {
            Field field = Inicio.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener " + fieldName, e);
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
                new Class<?>[]{String.class, String.class}, null, "1.0"));
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
        assertNull(invoke(inicio, "getNamedAssetUrl",
                new Class<?>[]{JsonNode.class, String.class}, assets, null));
        assertFalse((boolean) invoke(inicio, "isNamedAsset",
                new Class<?>[]{JsonNode.class, String.class}, assets.get(0), "app.zip"));
    }

    @Test
    void resourceIconHelperLoadsExistingResourcesAndIgnoresMissingOnes() {
        Inicio inicio = newInstanceWithoutConstructor();

        assertNull(invoke(inicio, "loadResourceIcon",
                new Class<?>[]{String.class}, "img/icons/missing.png"));
        assertInstanceOf(ImageIcon.class, invoke(inicio, "loadResourceIcon",
                new Class<?>[]{String.class}, "img/icons/app-icon.png"));
    }

    private static Inicio.UriBrowser uriBrowser(boolean desktopSupported, boolean browseSupported,
                                                AtomicReference<URI> openedUri) {
        return new Inicio.UriBrowser() {
            @Override
            public boolean isDesktopSupported() {
                return desktopSupported;
            }

            @Override
            public boolean isBrowseSupported() {
                return browseSupported;
            }

            @Override
            public void browse(URI uri) {
                if (openedUri != null) {
                    openedUri.set(uri);
                }
            }
        };
    }

    private static Inicio.ApplicationTaskbar taskbar(boolean supported, AtomicInteger applied) {
        return new Inicio.ApplicationTaskbar() {
            @Override
            public boolean isSupported() {
                return supported;
            }

            @Override
            public void setIconImage(Image image) {
                applied.incrementAndGet();
            }
        };
    }

    private static void invokeFinishExtraction(Inicio inicio, int exitCode, String processOutput, Path outputDir,
                                               File[] filesBefore, AtomicReference<String> error,
                                               AtomicReference<String> success) {
        invoke(inicio, "finishExtraction",
                new Class<?>[]{int.class, java.io.InputStream.class, File.class, File[].class,
                        Consumer.class, Consumer.class},
                exitCode, new ByteArrayInputStream(processOutput.getBytes()), outputDir.toFile(), filesBefore,
                (Consumer<String>) error::set, (Consumer<String>) success::set);
    }

    @Test
    void applyIconOnlyUsesAvailableIcons() {
        Inicio inicio = newInstanceWithoutConstructor();
        AtomicInteger applied = new AtomicInteger();
        Consumer<Image> iconSetter = image -> applied.incrementAndGet();

        invoke(inicio, "applyIcon", new Class<?>[]{ImageIcon.class, Consumer.class}, null, iconSetter);
        assertEquals(0, applied.get());

        ImageIcon icon = (ImageIcon) invoke(inicio, "loadResourceIcon",
                new Class<?>[]{String.class}, "img/icons/app-icon.png");
        invoke(inicio, "applyIcon", new Class<?>[]{ImageIcon.class, Consumer.class}, icon, iconSetter);
        assertEquals(1, applied.get());
    }

    @Test
    void openUriChecksDesktopCapabilitiesBeforeBrowsing() {
        Inicio inicio = newInstanceWithoutConstructor();
        Class<?>[] parameterTypes = {String.class, Inicio.UriBrowser.class};

        Inicio.UriBrowser unavailableDesktop = uriBrowser(false, false, null);
        assertThrows(RuntimeException.class, () -> invoke(inicio, "openUri", parameterTypes,
                "https://example.com", unavailableDesktop));

        Inicio.UriBrowser unavailableBrowse = uriBrowser(true, false, null);
        assertThrows(RuntimeException.class, () -> invoke(inicio, "openUri", parameterTypes,
                "https://example.com", unavailableBrowse));

        AtomicReference<URI> openedUri = new AtomicReference<>();
        Inicio.UriBrowser availableBrowser = uriBrowser(true, true, openedUri);
        invoke(inicio, "openUri", parameterTypes, "https://example.com/download", availableBrowser);

        assertEquals(URI.create("https://example.com/download"), openedUri.get());
    }

    @Test
    void scaledResourceIconHelperLoadsConfiguredResource() {
        Inicio inicio = newInstanceWithoutConstructor();

        assertInstanceOf(ImageIcon.class, invoke(inicio, "loadScaledResourceIcon", new Class<?>[]{}));
    }

    @Test
    void updateCheckStartsConfiguredTask() throws InterruptedException {
        Inicio inicio = newInstanceWithoutConstructor();
        CountDownLatch completed = new CountDownLatch(1);
        invoke(inicio, "startUpdateCheck", new Class<?>[]{Runnable.class}, (Runnable) completed::countDown);

        assertTrue(completed.await(1, TimeUnit.SECONDS));
    }

    @Test
    void updateResponseIsIgnoredUntilANewerDownloadIsAvailable() throws Exception {
        Inicio inicio = newInstanceWithoutConstructor();
        JMenuItem updateItem = new JMenuItem();
        updateItem.setVisible(false);
        setField(inicio, "menuUpdateItem", updateItem);
        Class<?>[] parameterTypes = {int.class, String.class};

        invoke(inicio, "handleUpdateResponse", parameterTypes, 500, "{}");
        invoke(inicio, "handleUpdateResponse", parameterTypes, 200, "{}");
        invoke(inicio, "handleUpdateResponse", parameterTypes, 200,
                "{\"tag_name\":\"" + Constantes.VERSION + "\",\"assets\":[]}");
        invoke(inicio, "handleUpdateResponse", parameterTypes, 200,
                "{\"tag_name\":\"v9999.0.0\",\"assets\":[]}");

        assertNull(getField(inicio, "updateDownloadUrl"));
        assertFalse(updateItem.isVisible());

        invoke(inicio, "handleUpdateResponse", parameterTypes, 200,
                "{\"tag_name\":\"v9999.0.0\",\"assets\":[{" +
                        "\"name\":\"update.zip\"," +
                        "\"browser_download_url\":\"https://example.com/update.zip\"}]}");
        SwingUtilities.invokeAndWait(() -> {
            // Espera a que se aplique la actualización pendiente en el EDT.
        });

        assertEquals("https://example.com/update.zip", getField(inicio, "updateDownloadUrl"));
        assertEquals(Constantes.UI_MENU_UPDATE_AVAILABLE + " (9999.0.0)", updateItem.getText());
        assertTrue(updateItem.isVisible());
    }

    @Test
    void openUpdateDownloadHandlesMissingAndConfiguredUrls() {
        Inicio inicio = newInstanceWithoutConstructor();

        invoke(inicio, "openUpdateDownload", new Class<?>[]{});
        setField(inicio, "updateDownloadUrl", "");
        invoke(inicio, "openUpdateDownload", new Class<?>[]{});
        setField(inicio, "updateDownloadUrl", "https://example.com/update.zip");
        assertDoesNotThrow(() -> invoke(inicio, "openUpdateDownload", new Class<?>[]{}));
    }

    @Test
    void configureTaskbarIconOnlyUsesSupportedTaskbar() {
        Inicio inicio = newInstanceWithoutConstructor();
        Image image = new ImageIcon(new byte[0]).getImage();
        AtomicInteger applied = new AtomicInteger();
        Class<?>[] parameterTypes = {Image.class, Inicio.ApplicationTaskbar.class};

        invoke(inicio, "configureTaskbarIcon", parameterTypes, image,
                taskbar(false, applied));
        assertEquals(0, applied.get());

        invoke(inicio, "configureTaskbarIcon", parameterTypes, image,
                taskbar(true, applied));
        assertEquals(1, applied.get());
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
    void runIfValidOnlyRunsActionForValidRoutes() {
        Inicio inicio = newInstanceWithoutConstructor();
        AtomicInteger executions = new AtomicInteger();
        Class<?>[] parameterTypes = {boolean.class, Runnable.class};

        invoke(inicio, "runIfValid", parameterTypes, false, (Runnable) executions::incrementAndGet);
        assertEquals(0, executions.get());

        invoke(inicio, "runIfValid", parameterTypes, true, (Runnable) executions::incrementAndGet);
        assertEquals(1, executions.get());
    }

    @Test
    void finishExtractionReportsProcessErrors(@TempDir Path tempDir) {
        Inicio inicio = newInstanceWithoutConstructor();
        AtomicReference<String> error = new AtomicReference<>();
        AtomicReference<String> success = new AtomicReference<>();

        invokeFinishExtraction(inicio, 7, "process failed", tempDir, new File[0], error, success);

        assertEquals("Error al extraer el archivo (codigo 7).\nprocess failed", error.get());
        assertNull(success.get());
    }

    @Test
    void finishExtractionReportsMissingGeneratedFile(@TempDir Path tempDir) {
        Inicio inicio = newInstanceWithoutConstructor();
        AtomicReference<String> error = new AtomicReference<>();
        AtomicReference<String> success = new AtomicReference<>();

        invokeFinishExtraction(inicio, 0, "", tempDir, new File[0], error, success);

        assertEquals("No se pudo detectar el archivo generado por el instalador.", error.get());
        assertNull(success.get());
    }

    @Test
    void finishExtractionMovesGeneratedFileAndReportsSuccess(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path generated = Files.createFile(tempDir.resolve("generated.dat"));
        AtomicReference<String> error = new AtomicReference<>();
        AtomicReference<String> success = new AtomicReference<>();

        invokeFinishExtraction(inicio, 0, "", tempDir, new File[0], error, success);

        assertNull(error.get());
        assertEquals("Exe.zip", success.get());
        assertFalse(Files.exists(generated));
        assertTrue(Files.isRegularFile(tempDir.resolve("Exe.zip")));
    }

    @Test
    void fileSelectionsUpdateRoutesOnlyWhenApproved(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path source = Files.createFile(tempDir.resolve("installer.exe"));
        Path destination = Files.createDirectory(tempDir.resolve("output"));
        JLabel sourceLabel = new JLabel();
        JLabel destinationLabel = new JLabel();
        JButton executeButton = new JButton();
        setField(inicio, "jLabel1", sourceLabel);
        setField(inicio, "jLabel2", destinationLabel);
        setField(inicio, "jButton3", executeButton);

        invoke(inicio, "applySourceSelection", new Class<?>[]{int.class, File.class},
                JFileChooser.APPROVE_OPTION, source.toFile());
        assertEquals(source.toString(), sourceLabel.getText());
        assertFalse(executeButton.isEnabled());

        invoke(inicio, "applySourceSelection", new Class<?>[]{int.class, File.class},
                JFileChooser.CANCEL_OPTION, tempDir.resolve("ignored.exe").toFile());
        assertEquals(source.toString(), getField(inicio, "rutaArchivo"));

        invoke(inicio, "applyDestinationSelection", new Class<?>[]{int.class, File.class},
                JFileChooser.APPROVE_OPTION, destination.toFile());
        assertEquals(destination.toString(), destinationLabel.getText());
        assertTrue(executeButton.isEnabled());

        invoke(inicio, "applyDestinationSelection", new Class<?>[]{int.class, File.class},
                JFileChooser.CANCEL_OPTION, tempDir.resolve("ignored").toFile());
        assertEquals(destination.toString(), getField(inicio, "rutaSave"));
    }

    @Test
    void validatorsShowErrorsWhenRequestedForInvalidPaths(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path directory = Files.createDirectory(tempDir.resolve("output"));
        Path file = Files.createFile(tempDir.resolve("installer.exe"));
        String directoryPath = directory.toString();
        String filePath = file.toString();
        Class<?>[] validatorParameterTypes = {String.class, boolean.class};

        assertThrows(RuntimeException.class, () -> invoke(inicio, "validarArchivo",
                validatorParameterTypes, null, true));
        assertThrows(RuntimeException.class, () -> invoke(inicio, "validarArchivo",
                validatorParameterTypes, directoryPath, true));
        assertThrows(RuntimeException.class, () -> invoke(inicio, "validarDirectorio",
                validatorParameterTypes, null, true));
        assertThrows(RuntimeException.class, () -> invoke(inicio, "validarDirectorio",
                validatorParameterTypes, filePath, true));
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
        assertThrows(RuntimeException.class, () -> invoke(inicio, "validarRutas", new Class<?>[]{}));
    }

    @Test
    void routeValidatorsShortCircuitWhenSourceFileIsInvalid(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path directory = Files.createDirectory(tempDir.resolve("output"));
        setField(inicio, "rutaArchivo", tempDir.resolve("missing.exe").toString());
        setField(inicio, "rutaSave", directory.toString());

        assertFalse((boolean) invoke(inicio, "validarRutasSilencioso", new Class<?>[]{}));
        assertThrows(RuntimeException.class, () -> invoke(inicio, "validarRutas", new Class<?>[]{}));
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
    void detectarArchivoGeneradoKeepsFirstNewFileWhenTimestampsAreEqual(@TempDir Path tempDir) throws IOException {
        Inicio inicio = newInstanceWithoutConstructor();
        Path first = Files.createFile(tempDir.resolve("first.dat"));
        Path second = Files.createFile(tempDir.resolve("second.dat"));
        FileTime sameTimestamp = FileTime.fromMillis(1_000);
        Files.setLastModifiedTime(first, sameTimestamp);
        Files.setLastModifiedTime(second, sameTimestamp);

        File detected = (File) invoke(inicio, "detectarArchivoGenerado",
                new Class<?>[]{File.class, File[].class}, tempDir.toFile(), new File[0]);

        assertTrue(Set.of("first.dat", "second.dat").contains(detected.getName()));
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
