package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.vista.utils.Formato;
import org.junit.jupiter.api.Test;

class FormatoTest {

  @Test
  void poblacionConSeparadorDeMiles() {
    assertEquals("18.001.000", Formato.poblacion(18_001_000));
    assertEquals("1.000", Formato.poblacion(1000));
    assertEquals("0", Formato.poblacion(0));
  }

  @Test
  void totalEnSingularYPlural() {
    assertEquals("1 país encontrado", Formato.total(1));
    assertEquals("3 países encontrados", Formato.total(3));
    assertEquals("0 países encontrados", Formato.total(0));
  }

  @Test
  void textoVacioSeMuestraComoGuion() {
    assertEquals("—", Formato.texto(" "));
    assertEquals("Quito", Formato.texto("Quito"));
    assertEquals("Sí", Formato.siNo(true));
  }
}
