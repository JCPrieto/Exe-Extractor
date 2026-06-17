package es.jklabs.utilidades;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constantes {
    private static final Properties APP_PROPERTIES = cargarPropiedades();

    public static final String NOMBRE_APP = "Exe Extractor";
    public static final String VERSION = cargarVersion();
    public static final String GITHUB_REPO = cargarPropiedad("app.github.repo", "");
    public static final String GITHUB_ASSET_PATTERN = cargarPropiedad("app.github.asset.pattern", "ExeExtractor-{version}.zip");
    public static final String OUTPUT_ZIP_NAME = cargarPropiedad("app.output.zip.name", "Exe.zip");
    public static final String UI_MENU_HELP = cargarPropiedad("app.ui.menu.help", "Ayuda");
    public static final String UI_MENU_ABOUT = cargarPropiedad("app.ui.menu.about", "Acerca de");
    public static final String UI_MENU_UPDATE_AVAILABLE = cargarPropiedad("app.ui.menu.update.available", "Nueva version disponible");
    public static final String UI_MENU_UPDATE_TOOLTIP = cargarPropiedad("app.ui.menu.update.tooltip", "Descargar la ultima version disponible");
    public static final String UI_BUTTON_SOURCE = cargarPropiedad("app.ui.button.source", "Origen");
    public static final String UI_BUTTON_DESTINATION = cargarPropiedad("app.ui.button.destination", "Destino");
    public static final String UI_BUTTON_EXECUTE = cargarPropiedad("app.ui.button.execute", "Ejecutar");
    public static final String UI_BUTTON_CONTINUE = cargarPropiedad("app.ui.button.continue", "Continuar");
    public static final String UI_LABEL_SELECT_FILE = cargarPropiedad("app.ui.label.select.file", "Selecciona archivo");
    public static final String UI_LABEL_SELECT_DIRECTORY = cargarPropiedad("app.ui.label.select.directory", "Selecciona directorio");
    public static final String UI_TEXT_INSTRUCTIONS = cargarPropiedad("app.ui.text.instructions",
            "-Selecciona el archivo de origen.\n\n-Selecciona la carpeta de destino.\n\n-Ejecutar y listo, ya tienes extraido tu instalador.exe\n\n");
    public static final String UI_DIALOG_INFO_TITLE = cargarPropiedad("app.ui.dialog.info.title", "Info");
    public static final String UI_DIALOG_CONTINUE_MESSAGE = cargarPropiedad("app.ui.dialog.continue.message",
            "Si se han extraido los archivos pulse continuar, en caso contrario espere");
    public static final String UI_DIALOG_ERROR_TITLE = cargarPropiedad("app.ui.dialog.error.title", "Error");
    public static final String UI_INFO_AUTHOR = cargarPropiedad("app.ui.info.author", "Creado por: <b>Juan Carlos Prieto Silos</b>");
    public static final String UI_INFO_WEBSITE = cargarPropiedad("app.ui.info.website", "Web Site: JCPrieto.es");
    public static final String UI_INFO_CREATED_BY = cargarPropiedad("app.ui.info.created.by", "Creado por");
    public static final String UI_INFO_AUTHOR_NAME = cargarPropiedad("app.ui.info.author.name", "Juan Carlos Prieto Silos");
    public static final String UI_INFO_WEBSITE_LABEL = cargarPropiedad("app.ui.info.website.label", "JCPrieto.es");
    public static final String UI_INFO_WEBSITE_URL = cargarPropiedad("app.ui.info.website.url", "https://www.jcprieto.es");
    public static final String UI_INFO_EMAIL = cargarPropiedad("app.ui.info.email", "JuanC.Prieto.Silos@gmail.com");
    public static final String UI_INFO_POWERED_BY = cargarPropiedad("app.ui.info.powered.by", "Powered by");
    public static final String UI_INFO_LICENSE = cargarPropiedad("app.ui.info.license", "Licencia GPLv3");
    public static final String UI_INFO_LICENSE_URL = cargarPropiedad("app.ui.info.license.url", "https://www.gnu.org/licenses/gpl-3.0.html");
    public static final String UI_BUTTON_OK = cargarPropiedad("app.ui.button.ok", "Aceptar");

    private Constantes() {

    }

    private static String cargarVersion() {
        return cargarPropiedad("app.version", "dev");
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        try (InputStream input = Constantes.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input != null) {
                propiedades.load(input);
            }
        } catch (IOException ignored) {
            // Si falla la carga se utilizaran los valores por defecto.
        }
        return propiedades;
    }

    private static String cargarPropiedad(String clave, String valorPorDefecto) {
        return APP_PROPERTIES.getProperty(clave, valorPorDefecto);
    }
}
