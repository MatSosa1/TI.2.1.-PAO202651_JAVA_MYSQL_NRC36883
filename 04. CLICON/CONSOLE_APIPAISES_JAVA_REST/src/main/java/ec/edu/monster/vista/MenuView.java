package ec.edu.monster.vista;

import ec.edu.monster.controlador.ErroresCliente;
import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.http.ConfigHTTP;
import ec.edu.monster.vista.utils.MessagesUtils;

/** ECUD1 paso 7: menú principal con Países, Consultar país en la API, Buscar países, Informes y Cerrar sesión. */
public class MenuView {

  public static void show() {
    while (ConfigHTTP.haySesion()) {
      MessagesUtils.printTitle("MENU PRINCIPAL");
      System.out.println(ConfigHTTP.getNombre() + "\n");

      System.out.println("1. Paises");
      System.out.println("2. Consultar pais en la API");
      System.out.println("3. Buscar paises");
      System.out.println("4. Informes");
      System.out.println("5. Cerrar sesion");

      String opcion = MessagesUtils.readText("\nSeleccione una opcion");
      switch (opcion) {
        case "1" -> PaisesView.show();
        case "2" -> ConsultaApiView.show();
        case "3" -> BusquedaView.show();
        case "4" -> InformesView.show();
        case "5" -> cerrarSesion();
        default -> MessagesUtils.printError("Opcion invalida");
      }
      System.out.println();
    }
  }

  /** ECUD1 alterno 4: se borra la sesión y se vuelve al inicio aunque el servidor no responda. */
  private static void cerrarSesion() {
    Respuesta<Object> respuesta = new LoginController().logout();
    MessagesUtils.printMessage(respuesta.esOk() ? respuesta.getMensaje() : "Sesión cerrada");
  }

  /**
   * ECUD8 alterno 2: con el token rechazado se informa, se borra la sesión y se vuelve al
   * inicio de sesión. Devuelve true si la sesión dejó de ser válida.
   */
  public static boolean revisarSesion(Respuesta<?> respuesta) {
    if (!respuesta.sesionNoValida())
      return false;
    MessagesUtils.printError(ErroresCliente.SESION_NO_VALIDA);
    ConfigHTTP.limpiar();
    return true;
  }

  /** Muestra el mensaje de una operación: normal si salió bien, como error si no. */
  public static boolean mostrarResultado(Respuesta<?> respuesta) {
    if (revisarSesion(respuesta))
      return false;
    if (respuesta.esOk()) {
      MessagesUtils.printMessage(respuesta.getMensaje());
      return true;
    }
    MessagesUtils.printError(respuesta.getMensaje());
    return false;
  }
}
