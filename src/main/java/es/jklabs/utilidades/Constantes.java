package es.jklabs.utilidades;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constantes {
    public static final String NOMBRE_APP = "Exe Extactor";
    public static final String VERSION = cargarVersion();
    public static final String GITHUB_REPO = cargarPropiedad("app.github.repo", "");
    public static final String GITHUB_ASSET_PATTERN = cargarPropiedad("app.github.asset.pattern", "ExeExtractor-{version}.zip");
    public static final String OUTPUT_ZIP_NAME = cargarPropiedad("app.output.zip.name", "Exe.zip");

    private Constantes() {

    }

    private static String cargarVersion() {
        return cargarPropiedad("app.version", "dev");
    }

    private static String cargarPropiedad(String clave, String valorPorDefecto) {
        try (InputStream input = Constantes.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                return valorPorDefecto;
            }
            Properties propiedades = new Properties();
            propiedades.load(input);
            String valor = propiedades.getProperty(clave);
            return valor != null ? valor : valorPorDefecto;
        } catch (IOException e) {
            return valorPorDefecto;
        }
    }
}
