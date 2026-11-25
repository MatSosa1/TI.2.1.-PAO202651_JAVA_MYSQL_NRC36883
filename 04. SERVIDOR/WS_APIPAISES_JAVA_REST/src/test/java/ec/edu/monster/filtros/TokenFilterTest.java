package ec.edu.monster.filtros;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TokenFilterTest {

    @Test
    void soloElInicioDeSesionEsPublico() {
        assertTrue(TokenFilter.esPublica("Login/login"));
        assertTrue(TokenFilter.esPublica("/Login/login/"));

        assertFalse(TokenFilter.esPublica("Login/logout"), "cerrar sesión exige token (ECUD1, alterno 4)");
        assertFalse(TokenFilter.esPublica("Paises"));
        assertFalse(TokenFilter.esPublica("Reportes"));
        assertFalse(TokenFilter.esPublica(""));
        assertFalse(TokenFilter.esPublica(null));
    }
}
