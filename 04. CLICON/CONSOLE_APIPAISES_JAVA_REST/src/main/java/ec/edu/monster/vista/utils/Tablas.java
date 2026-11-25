package ec.edu.monster.vista.utils;

import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.Pais;
import java.util.ArrayList;
import java.util.List;

/** Tablas de texto de países y selección de región, comunes a las pantallas. */
public final class Tablas {

  private Tablas() {
  }

  /**
   * Tabla con N.º, Nombre, Capital, Región, Población y Moneda (ECUD4 paso 4). Con
   * {@code conGuardado} agrega la columna «Guardado» de la consulta a la API (ECUD3 paso 7).
   */
  public static void paises(List<Pais> paises, boolean conGuardado) {
    String[] encabezados = conGuardado
      ? new String[] { "N.º", "Nombre", "Capital", "Región", "Población", "Moneda", "Guardado" }
      : new String[] { "N.º", "Nombre", "Capital", "Región", "Población", "Moneda" };
    boolean[] derecha = conGuardado
      ? new boolean[] { true, false, false, false, true, false, false }
      : new boolean[] { true, false, false, false, true, false };

    List<String[]> filas = new ArrayList<>();
    for (int i = 0; i < paises.size(); i++) {
      Pais p = paises.get(i);
      String[] fila = {
        String.valueOf(i + 1),
        Formato.texto(p.nombre()),
        Formato.texto(p.capital()),
        Formato.texto(p.region()),
        Formato.poblacion(p.poblacion()),
        Formato.texto(p.moneda())
      };
      if (conGuardado) {
        String[] conColumna = new String[fila.length + 1];
        System.arraycopy(fila, 0, conColumna, 0, fila.length);
        conColumna[fila.length] = Formato.siNo(p.guardado());
        fila = conColumna;
      }
      filas.add(fila);
    }
    System.out.println();
    MessagesUtils.printTable(encabezados, filas, derecha);
  }

  /**
   * Lista numerada de regiones (ECUD5 paso 2, ECUD6 paso 1).
   *
   * @return la región elegida, "" para «Todas» o Enter, o null si la opción no es válida
   */
  public static String elegirRegion() {
    List<String> regiones = ValidadorDatos.REGIONES;
    System.out.println("0. Todas");
    for (int i = 0; i < regiones.size(); i++)
      System.out.println((i + 1) + ". " + regiones.get(i));

    String texto = MessagesUtils.readText("Region (Enter = Todas)");
    if (texto.isEmpty() || "0".equals(texto))
      return "";
    try {
      int opcion = Integer.parseInt(texto);
      if (opcion >= 1 && opcion <= regiones.size())
        return regiones.get(opcion - 1);
    } catch (NumberFormatException e) {
      // Se informa abajo
    }
    MessagesUtils.printError("Opcion invalida");
    return null;
  }
}
