package es.jklabs;

import es.jklabs.utilidades.Constantes;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
}
