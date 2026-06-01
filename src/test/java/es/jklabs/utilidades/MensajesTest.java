package es.jklabs.utilidades;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MensajesTest {

    @Test
    void constructorIsPrivate() {
        Constructor<?>[] constructors = Mensajes.class.getDeclaredConstructors();

        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void getErrorReturnsEmptyTextWhenKeyIsNull() {
        assertEquals("", Mensajes.getError(null));
    }

    @Test
    void getErrorReturnsEmptyTextWhenKeyIsBlank() {
        assertEquals("", Mensajes.getError("   "));
    }

    @Test
    void getErrorReturnsConfiguredMessageWhenKeyExists() {
        assertEquals("Error al leer el archivo de log.", Mensajes.getError("logs.read"));
    }

    @Test
    void getErrorReturnsKeyWhenMessageDoesNotExist() {
        assertEquals("unknown.key", Mensajes.getError("unknown.key"));
    }
}
