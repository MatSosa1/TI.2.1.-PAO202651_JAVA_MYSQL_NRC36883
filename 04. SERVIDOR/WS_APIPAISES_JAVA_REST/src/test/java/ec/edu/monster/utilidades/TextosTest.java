package ec.edu.monster.utilidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TextosTest {

    @Test
    void laClaveNoDistingueTildesNiMayusculas() {
        assertEquals(Textos.clave("peru"), Textos.clave(" Perú "));
        assertEquals(Textos.clave("AMERICA"), Textos.clave("América"));
        assertEquals("", Textos.clave(null));
    }

    @Test
    void recortaACienCaracteres() {
        assertEquals(100, Textos.recortar("x".repeat(150)).length());
        assertEquals("Quito", Textos.recortar("  Quito "));
        assertNull(Textos.recortar(null));
    }
}
