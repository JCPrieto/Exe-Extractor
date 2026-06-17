package es.jklabs;

import es.jklabs.utilidades.Constantes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExeExtractorTest {

    @Test
    void constructorIsPrivate() {
        Constructor<?>[] constructors = ExeExtractor.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void configureDesktopIdentitySetsLinuxWindowClassAndApplicationName() {
        ExeExtractor.configureDesktopIdentity();

        assertEquals(ExeExtractor.LINUX_WM_CLASS, System.getProperty("sun.awt.X11.XWMClass"));
        assertEquals(Constantes.NOMBRE_APP, System.getProperty("java.awt.application.name"));
    }
}
