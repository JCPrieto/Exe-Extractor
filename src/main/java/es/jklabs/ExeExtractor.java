package es.jklabs;

import es.jklabs.utilidades.Constantes;

public class ExeExtractor {
    public static final String LINUX_WM_CLASS = "ExeExtractor";

    private ExeExtractor() {

    }

    public static void main() {
        configureDesktopIdentity();
        Inicio.launch();
    }

    static void configureDesktopIdentity() {
        System.setProperty("sun.awt.X11.XWMClass", LINUX_WM_CLASS);
        System.setProperty("java.awt.application.name", Constantes.NOMBRE_APP);
    }
}
