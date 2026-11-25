package ec.edu.monster.vista;

import ec.edu.monster.controlador.ReporteController;
import ec.edu.monster.modelos.dto.Informe;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.vista.utils.Formato;
import ec.edu.monster.vista.utils.MessagesUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * ECUD7 Generar Informes: países por región, país más poblado y promedio poblacional por región.
 * La consola muestra solo tablas de texto, sin gráfico (ECUD7, paso 8).
 */
public class InformesView {

  /** Mensaje del servidor cuando no hay países (ECUD7, alterno 2). */
  private static final String SIN_PAISES = "No hay países registrados para generar informes";

  private static final ReporteController CONTROLLER = new ReporteController();

  public static void show() {
    MessagesUtils.printTitle("INFORMES");
    MessagesUtils.printMessage("Procesando...");
    Respuesta<Informe> respuesta = CONTROLLER.generar();
    if (MenuView.revisarSesion(respuesta))
      return;
    if (!respuesta.esOk() || respuesta.getDatos() == null) {
      MessagesUtils.printError(respuesta.getMensaje());
      if (SIN_PAISES.equals(respuesta.getMensaje()))
        MessagesUtils.printMessage("Use “Consultar país en la API” para guardar países.");
      return;
    }
    MessagesUtils.printMessage(respuesta.getMensaje());
    Informe informe = respuesta.getDatos();

    System.out.println("\nPaises por region");
    List<String[]> conteos = new ArrayList<>();
    for (Informe.ConteoRegion conteo : informe.paisesPorRegion())
      conteos.add(new String[] { conteo.region(), String.valueOf(conteo.cantidad()) });
    MessagesUtils.printTable(new String[] { "Región", "Países" }, conteos, new boolean[] { false, true });

    Pais mayor = informe.paisMasPoblado();
    if (mayor != null) {
      System.out.println("\nPais mas poblado");
      System.out.println(mayor.nombre() + " (" + mayor.region() + "): "
        + Formato.poblacion(mayor.poblacion()) + " habitantes");
    }

    System.out.println("\nPromedio poblacional por region");
    List<String[]> promedios = new ArrayList<>();
    for (Informe.PromedioRegion promedio : informe.promedioPorRegion())
      promedios.add(new String[] { promedio.region(), Formato.poblacion(promedio.promedioPoblacion()) });
    MessagesUtils.printTable(new String[] { "Región", "Promedio de población" }, promedios,
      new boolean[] { false, true });
  }
}
