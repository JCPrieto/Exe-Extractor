package es.jklabs.utilidades;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class ConstantesTest {

    @Test
    void constructorIsPrivate() {
        Constructor<?>[] constructors = Constantes.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void constantsAreLoadedFromConfiguration() {
        assertEquals("Exe Extactor", Constantes.NOMBRE_APP);

        assertNotNull(Constantes.VERSION);
        assertFalse(Constantes.VERSION.isBlank());

        assertEquals("JCPrieto/Exe-Extractor", Constantes.GITHUB_REPO);
        assertEquals("ExeExtractor-{version}.zip", Constantes.GITHUB_ASSET_PATTERN);
        assertEquals("Exe.zip", Constantes.OUTPUT_ZIP_NAME);
        assertEquals("Ayuda", Constantes.UI_MENU_HELP);
        assertEquals("Acerca de", Constantes.UI_MENU_ABOUT);
        assertEquals("Nueva version disponible", Constantes.UI_MENU_UPDATE_AVAILABLE);
        assertEquals("Descargar la ultima version disponible", Constantes.UI_MENU_UPDATE_TOOLTIP);
        assertEquals("Origen", Constantes.UI_BUTTON_SOURCE);
        assertEquals("Destino", Constantes.UI_BUTTON_DESTINATION);
        assertEquals("Ejecutar", Constantes.UI_BUTTON_EXECUTE);
        assertEquals("Continuar", Constantes.UI_BUTTON_CONTINUE);
        assertEquals("Selecciona archivo", Constantes.UI_LABEL_SELECT_FILE);
        assertEquals("Selecciona directorio", Constantes.UI_LABEL_SELECT_DIRECTORY);
        assertEquals("-Selecciona el archivo de origen.\n\n-Selecciona la carpeta de destino.\n\n-Ejecutar y listo, ya tienes extraido tu instalador.exe\n\n",
                Constantes.UI_TEXT_INSTRUCTIONS);
        assertEquals("Info", Constantes.UI_DIALOG_INFO_TITLE);
        assertEquals("Si se han extraido los archivos pulse continuar, en caso contrario espere",
                Constantes.UI_DIALOG_CONTINUE_MESSAGE);
        assertEquals("Error", Constantes.UI_DIALOG_ERROR_TITLE);
        assertEquals("Creado por: <b>Juan Carlos Prieto Silos</b>", Constantes.UI_INFO_AUTHOR);
        assertEquals("Web Site: JCPrieto.es", Constantes.UI_INFO_WEBSITE);
    }
}
