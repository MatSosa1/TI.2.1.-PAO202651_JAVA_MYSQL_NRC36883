package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.dto.LoginDatos;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.vista.utils.MessagesUtils;

/** ECUD1: formulario de inicio de sesión con usuario y clave (la clave se lee oculta). */
public class LoginView {

  private static final LoginController CONTROLLER = new LoginController();

  /** Devuelve true al iniciar sesión, o false si el usuario elige salir. */
  public static boolean userHasLogin() {
    while (true) {
      MessagesUtils.printTitle("INICIAR SESION");
      System.out.println("APIPaises - Servicio REST / Java\n");
      System.out.println("1. Ingresar");
      System.out.println("2. Salir");

      String opcion = MessagesUtils.readText("\nSeleccione una opcion");
      switch (opcion) {
        case "1" -> {
          if (ingresar())
            return true;
        }
        case "2" -> {
          return false;
        }
        default -> MessagesUtils.printError("Opcion invalida");
      }
      System.out.println();
    }
  }

  /** ECUD1 pasos 3 a 7 y alternos 1 a 3. */
  private static boolean ingresar() {
    String usuario = MessagesUtils.readText("Usuario");
    if (usuario.isEmpty()) {
      MessagesUtils.printError(ValidadorDatos.INGRESE_USUARIO);
      return false;
    }
    String clave = MessagesUtils.readPassword("Clave");
    if (clave.isEmpty()) {
      MessagesUtils.printError(ValidadorDatos.INGRESE_CLAVE);
      return false;
    }

    MessagesUtils.printMessage("Procesando...");
    Respuesta<LoginDatos> respuesta = CONTROLLER.login(usuario, clave);
    if (!respuesta.esOk()) {
      // Credenciales incorrectas o sin servidor: se mantiene el formulario para otro intento
      MessagesUtils.printError(respuesta.getMensaje());
      return false;
    }
    MessagesUtils.printMessage(respuesta.getMensaje());
    MessagesUtils.esperar(1);
    return true;
  }
}
