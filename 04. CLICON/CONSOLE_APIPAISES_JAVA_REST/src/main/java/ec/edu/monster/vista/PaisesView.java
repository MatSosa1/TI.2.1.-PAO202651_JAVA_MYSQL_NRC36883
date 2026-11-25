package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.dto.ResultadoBusqueda;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.utils.Formato;
import ec.edu.monster.vista.utils.MessagesUtils;
import ec.edu.monster.vista.utils.Tablas;
import java.util.List;

/**
 * ECUD4 Gestionar Países: tabla de países con Nuevo, Actualizar y Eliminar, y el filtro por
 * región (ECUD6), que queda activo mientras el usuario está en esta pantalla.
 */
public class PaisesView {

  private static final PaisController CONTROLLER = new PaisController();

  public static void show() {
    String region = "";
    while (ConfigHTTP.haySesion()) {
      MessagesUtils.printTitle("PAISES");
      List<Pais> paises = cargar(region);
      if (paises == null)
        return;

      if (!region.isEmpty())
        System.out.println("Filtro de region: " + region);
      if (paises.isEmpty()) {
        MessagesUtils.printMessage(region.isEmpty()
          ? "No hay países registrados. Use “Consultar país en la API” para guardarlos"
          : "No se encontraron países en esa región");
      } else {
        Tablas.paises(paises, false);
        System.out.println(Formato.total(paises.size()));
      }

      System.out.println();
      System.out.println("1. Nuevo");
      System.out.println("2. Actualizar");
      System.out.println("3. Eliminar");
      System.out.println("4. Filtrar por region");
      System.out.println("5. Regresar");

      String opcion = MessagesUtils.readText("\nSeleccione una opcion");
      switch (opcion) {
        case "1" -> nuevo();
        case "2" -> actualizar(paises);
        case "3" -> eliminar(paises);
        case "4" -> {
          String elegida = Tablas.elegirRegion();
          if (elegida != null)
            region = elegida;
        }
        case "5" -> {
          return;
        }
        default -> MessagesUtils.printError("Opcion invalida");
      }
      System.out.println();
    }
  }

  /** Listar (ECUD4) o, con una región activa, Filtrar (ECUD6). Null si hubo un error. */
  private static List<Pais> cargar(String region) {
    MessagesUtils.printMessage("Procesando...");
    if (region.isEmpty()) {
      Respuesta<List<Pais>> respuesta = CONTROLLER.listar();
      return datos(respuesta, respuesta.getDatos());
    }
    Respuesta<ResultadoBusqueda> respuesta = CONTROLLER.buscar(null, null, region);
    return datos(respuesta, respuesta.getDatos() == null ? null : respuesta.getDatos().paises());
  }

  private static List<Pais> datos(Respuesta<?> respuesta, List<Pais> paises) {
    if (MenuView.revisarSesion(respuesta))
      return null;
    if (!respuesta.esOk()) {
      MessagesUtils.printError(respuesta.getMensaje());
      return null;
    }
    return paises == null ? List.of() : paises;
  }

  /** ECUD4 Registrar: consulta en la API, elige un país de los resultados y lo registra. */
  private static void nuevo() {
    MessagesUtils.printTitle("NUEVO PAIS");
    String consulta = MessagesUtils.readText("Nombre del pais a consultar en la API");
    String error = ValidadorDatos.errorNombre(consulta);
    if (error != null) {
      MessagesUtils.printError(error);
      return;
    }

    MessagesUtils.printMessage("Procesando...");
    Respuesta<List<Pais>> resultados = CONTROLLER.consultarApi(consulta);
    if (MenuView.revisarSesion(resultados))
      return;
    if (!resultados.esOk()) {
      MessagesUtils.printError(resultados.getMensaje());
      return;
    }
    List<Pais> paises = resultados.getDatos();
    Tablas.paises(paises, true);

    int numero = MessagesUtils.readOption("\nNumero del pais a registrar (Enter para cancelar)", paises.size());
    if (numero == 0) {
      MessagesUtils.printMessage("Operacion cancelada");
      return;
    }
    if (numero < 0) {
      MessagesUtils.printError("Opcion invalida");
      return;
    }

    MessagesUtils.printMessage("Procesando...");
    MenuView.mostrarResultado(CONTROLLER.registrar(consulta, paises.get(numero - 1).nombre()));
  }

  /** ECUD4 Actualizar: el servidor vuelve a consultar la API y actualiza el país elegido. */
  private static void actualizar(List<Pais> paises) {
    Pais pais = elegir(paises, "Numero del pais a actualizar");
    if (pais == null)
      return;
    MessagesUtils.printMessage("Procesando...");
    MenuView.mostrarResultado(CONTROLLER.actualizar(pais.nombre()));
  }

  /** ECUD4 Eliminar: pide confirmación antes de enviar (alterno 6). */
  private static void eliminar(List<Pais> paises) {
    Pais pais = elegir(paises, "Numero del pais a eliminar");
    if (pais == null)
      return;
    if (!MessagesUtils.confirm("¿Eliminar el país " + pais.nombre() + "?")) {
      MessagesUtils.printMessage("Operacion cancelada");
      return;
    }
    MessagesUtils.printMessage("Procesando...");
    MenuView.mostrarResultado(CONTROLLER.eliminar(pais.nombre()));
  }

  private static Pais elegir(List<Pais> paises, String etiqueta) {
    if (paises.isEmpty()) {
      MessagesUtils.printError("No hay países en la tabla");
      return null;
    }
    int numero = MessagesUtils.readOption(etiqueta + " (Enter para cancelar)", paises.size());
    if (numero == 0) {
      MessagesUtils.printMessage("Operacion cancelada");
      return null;
    }
    if (numero < 0) {
      MessagesUtils.printError("Opcion invalida");
      return null;
    }
    return paises.get(numero - 1);
  }
}
