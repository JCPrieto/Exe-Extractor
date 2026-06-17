package es.jklabs;

import es.jklabs.utilidades.Constantes;

import java.awt.*;
import java.lang.reflect.Field;

public class ExeExtractor {
    public static final String LINUX_WM_CLASS = "ExeExtractor";

    private ExeExtractor() {

    }

    public static void main(String[] args) {
        configureDesktopIdentity();
        Inicio.launch();
    }

    static void configureDesktopIdentity() {
        System.setProperty("sun.awt.X11.XWMClass", LINUX_WM_CLASS);
        System.setProperty("java.awt.application.name", Constantes.NOMBRE_APP);
        configureXToolkitWindowClass();
    }

    private static void configureXToolkitWindowClass() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (!"sun.awt.X11.XToolkit".equals(toolkit.getClass().getName())) {
                return;
            }
            Class<?> xToolkitClass = Class.forName("sun.awt.X11.XToolkit");
            Field awtAppClassName = xToolkitClass.getDeclaredField("awtAppClassName");
            awtAppClassName.setAccessible(true);
            awtAppClassName.set(null, LINUX_WM_CLASS);
        } catch (Throwable ignored) {
            // En entornos headless o sin permisos de módulos, se conserva la configuración estándar de AWT.
        }
    }
}
