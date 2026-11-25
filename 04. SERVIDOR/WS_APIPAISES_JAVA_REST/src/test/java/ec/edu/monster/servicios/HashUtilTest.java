package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HashUtilTest {

    private final HashUtil hash = new HashUtil();

    @Test
    void sha256DeVectorConocido() {
        // Vector de prueba de FIPS 180-2
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash.sha256("abc"));
    }

    @Test
    void coincideConElHashDeInitSql() {
        // SHA2('admin123', 256) de MySQL, el que guarda init.sql para el usuario admin
        String deMysql = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        assertEquals(deMysql, hash.sha256("admin123"));
        assertTrue(hash.coincide("admin123", deMysql));
        assertTrue(hash.coincide("admin123", deMysql.toUpperCase()));
    }

    @Test
    void claveIncorrectaONulaNoCoincide() {
        String guardado = hash.sha256("admin123");
        assertFalse(hash.coincide("Admin123", guardado));
        assertFalse(hash.coincide(null, guardado));
        assertFalse(hash.coincide("admin123", null));
    }
}
