package ec.edu.monster.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private final TokenService tokens = new TokenService();

    @Test
    void generaTokenSha256Activo() {
        String token = tokens.generarToken("admin");

        assertTrue(token.matches("[0-9a-f]{64}"), "64 caracteres hexadecimales (ECUD8, paso 2)");
        assertTrue(tokens.esValido(token));
        assertEquals("admin", tokens.usuarioDe(token));
    }

    @Test
    void cadaInicioDeSesionGeneraUnTokenDistinto() {
        assertNotEquals(tokens.generarToken("admin"), tokens.generarToken("admin"));
    }

    @Test
    void elRegistroSeCompartenEntreInstancias() {
        String token = tokens.generarToken("msosa");
        assertTrue(new TokenService().esValido(token), "el filtro y los controladores usan instancias distintas");
    }

    @Test
    void alCerrarSesionElTokenDejaDeSerValido() {
        String token = tokens.generarToken("admin");
        tokens.invalidar(token);

        assertFalse(tokens.esValido(token));
        assertNull(tokens.usuarioDe(token));
    }

    @Test
    void rechazaTokensMalFormadosONoRegistrados() {
        assertFalse(tokens.esValido(null));
        assertFalse(tokens.esValido(""));
        assertFalse(tokens.esValido("abc"));
        assertFalse(tokens.esValido("a".repeat(64)), "formato correcto pero no está entre los activos");
    }

    @Test
    void extraeElTokenDelEncabezado() {
        assertEquals("abc", TokenService.extraer("abc"));
        assertEquals("abc", TokenService.extraer("  abc "));
        assertEquals("abc", TokenService.extraer("Bearer abc"));
        assertEquals("abc", TokenService.extraer("bearer abc"));
        assertNull(TokenService.extraer(null));
        assertNull(TokenService.extraer("   "));
    }
}
