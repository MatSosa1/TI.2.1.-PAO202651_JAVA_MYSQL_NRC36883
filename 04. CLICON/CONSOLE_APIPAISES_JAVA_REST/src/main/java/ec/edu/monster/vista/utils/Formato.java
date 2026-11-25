package ec.edu.monster.vista.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formatos de presentación: la población se muestra con separador de miles (ECUD, datos del país). */
public final class Formato {

  public static final String SIN_DATO = "—";

  private Formato() {
  }

  /** 18001000 → «18.001.000». */
  public static String poblacion(long valor) {
    DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
    simbolos.setGroupingSeparator('.');
    return new DecimalFormat("#,##0", simbolos).format(valor);
  }

  /** «1 país encontrado», «3 países encontrados» (ECUD5, paso 8). */
  public static String total(int cantidad) {
    return cantidad == 1 ? "1 país encontrado" : cantidad + " países encontrados";
  }

  public static String siNo(boolean valor) {
    return valor ? "Sí" : "No";
  }

  /** Texto o «—» si viene vacío. */
  public static String texto(String valor) {
    return valor == null || valor.isBlank() ? SIN_DATO : valor;
  }
}
