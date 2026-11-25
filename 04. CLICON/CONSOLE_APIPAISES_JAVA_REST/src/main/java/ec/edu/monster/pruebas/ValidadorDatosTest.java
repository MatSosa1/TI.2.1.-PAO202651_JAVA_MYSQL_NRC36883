package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ec.edu.monster.controlador.ValidadorDatos;
import org.junit.jupiter.api.Test;

class ValidadorDatosTest {

  @Test
  void nombreDePais() {
    assertEquals(ValidadorDatos.INGRESE_NOMBRE_PAIS, ValidadorDatos.errorNombre(null));
    assertEquals(ValidadorDatos.INGRESE_NOMBRE_PAIS, ValidadorDatos.errorNombre("   "));
    assertEquals(ValidadorDatos.NOMBRE_MUY_LARGO, ValidadorDatos.errorNombre("x".repeat(101)));
    assertNull(ValidadorDatos.errorNombre("x".repeat(100)));
    assertNull(ValidadorDatos.errorNombre(" ecuador "));
  }

  @Test
  void lasSeisRegionesDelEcud() {
    assertEquals(6, ValidadorDatos.REGIONES.size());
    assertEquals("América", ValidadorDatos.REGIONES.get(0));
    assertEquals("Antártida", ValidadorDatos.REGIONES.get(5));
  }
}
