package es.jklabs.utilidades;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Mensajes {
    public static String getError(String key) {
        return getResource("i18n/errores", key);
    }

    private static String getResource(String resource, String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(resource, Locale.getDefault());
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
