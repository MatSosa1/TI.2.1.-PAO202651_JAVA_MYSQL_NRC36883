package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.dto.ResultadoImportacion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.utils.MessagesUtils;
import ec.edu.monster.vista.utils.Tablas;
import java.util.ArrayList;
import java.util.List;

/**
 * ECUD3 Consultar país en la API y ECUD2 Importar: tabla de resultados con la columna
 * «Guardado» y opción para guardar el país elegido. También «Importar todos».
 */
public class ConsultaApiView {

  private static final PaisController CONTROLLER = new PaisController();

  public static void show() {
    while (ConfigHTTP.haySesion()) {
      MessagesUtils.printTitle("CONSULTAR PAIS EN LA API");
      System.out.println("1. Consultar pais por nombre");
      System.out.println("2. Importar todos los paises");
      System.out.println("3. Regresar");

      String opcion = MessagesUtils.readText("\nSeleccione una opcion");
      switch (opcion) {
        case "1" -> consultar();
        case "2" -> importarTodos();
        case "3" -> {
          return;
        }
        default -> MessagesUtils.printError("Opcion invalida");
      }
      System.out.println();
    }
  }

  /** ECUD3 pasos 2 a 7; luego ECUD2 para cada país que el usuario decida guardar. */
  private static void consultar() {
    String consulta = MessagesUtils.readText("Nombre del pais");
    String error = ValidadorDatos.errorNombre(consulta);
    if (error != null) {
      MessagesUtils.printError(error);
      return;
    }

    MessagesUtils.printMessage("Procesando...");
    Respuesta<List<Pais>> respuesta = CONTROLLER.consultarApi(consulta);
    if (MenuView.revisarSesion(respuesta))
      return;
    if (!respuesta.esOk()) {
      MessagesUtils.printError(respuesta.getMensaje());
      return;
    }
    MessagesUtils.printMessage(respuesta.getMensaje());

    List<Pais> paises = new ArrayList<>(respuesta.getDatos());
    while (ConfigHTTP.haySesion()) {
      Tablas.paises(paises, true);
      int numero = MessagesUtils.readOption("\nNumero del pais a guardar (Enter para regresar)", paises.size());
      if (numero == 0)
        return;
      if (numero < 0) {
        MessagesUtils.printError("Opcion invalida");
        continue;
      }
      Pais elegido = paises.get(numero - 1);
      if (guardar(consulta, elegido))
        paises.set(numero - 1, new Pais(elegido.nombre(), elegido.capital(), elegido.region(),
          elegido.poblacion(), elegido.moneda(), true));
    }
  }

  /** ECUD2 pasos 2 a 9. Devuelve true si el país quedó guardado. */
  private static boolean guardar(String consulta, Pais pais) {
    if (!MessagesUtils.confirm("¿Guardar el país " + pais.nombre() + " en la base de datos local?")) {
      MessagesUtils.printMessage("Operacion cancelada");
      return false;
    }
    MessagesUtils.printMessage("Procesando...");
    Respuesta<ResultadoImportacion> respuesta = CONTROLLER.importar(consulta, pais.nombre());
    if (MenuView.revisarSesion(respuesta))
      return false;
    if (!respuesta.esOk()) {
      MessagesUtils.printError(respuesta.getMensaje());
      return false;
    }
    String accion = respuesta.getDatos() == null ? "" : " (" + respuesta.getDatos().accion() + ")";
    MessagesUtils.printMessage(respuesta.getMensaje() + accion);
    return true;
  }

  /** Importa todos los países de la API: los nuevos se registran y los existentes se actualizan. */
  private static void importarTodos() {
    if (!MessagesUtils.confirm("¿Importar todos los países desde la API REST Countries?")) {
      MessagesUtils.printMessage("Operacion cancelada");
      return;
    }
    MessagesUtils.printMessage("Procesando... (puede tardar unos segundos)");
    Respuesta<ResumenImportacion> respuesta = CONTROLLER.importarTodos();
    if (!MenuView.mostrarResultado(respuesta) || respuesta.getDatos() == null)
      return;
    ResumenImportacion resumen = respuesta.getDatos();
    MessagesUtils.printMessage("Recibidos: " + resumen.recibidos()
      + " | Registrados: " + resumen.registrados()
      + " | Actualizados: " + resumen.actualizados());
  }
}
