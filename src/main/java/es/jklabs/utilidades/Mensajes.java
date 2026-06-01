package es.jklabs.utilidades;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Mensajes {

    private Mensajes() {
    }

    public static String getError(String key) {
        return getResource(key);
    }

    private static String getResource(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n/errores", Locale.getDefault());
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
