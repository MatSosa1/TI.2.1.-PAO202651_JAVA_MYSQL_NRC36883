package ec.edu.monster.pruebas.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.utils.Formato;
import org.junit.Test;

public class FormatoTest {

    @Test
    public void poblacionConSeparadorDeMiles() {
        assertEquals("18.001.000", Formato.poblacion(18_001_000));
        assertEquals("0", Formato.poblacion(0));
    }

    @Test
    public void totalEnSingularYPlural() {
        assertEquals("1 país encontrado", Formato.total(1));
        assertEquals("3 países encontrados", Formato.total(3));
    }

    @Test
    public void textoVacio() {
        assertEquals("—", Formato.texto(" "));
        assertEquals("Quito", Formato.texto("Quito"));
    }

    @Test
    public void validaciones() {
        assertEquals(ValidadorDatos.INGRESE_NOMBRE_PAIS, ValidadorDatos.errorNombre(null));
        assertNull(ValidadorDatos.errorNombre("x".repeat(100)));
        assertNull(ValidadorDatos.errorCriterio(""));
        assertEquals(6, ValidadorDatos.REGIONES.size());
    }
}
