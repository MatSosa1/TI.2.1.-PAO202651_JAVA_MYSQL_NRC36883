package ec.edu.monster.controlador;

/** Mensajes del cliente cuando no hay una respuesta válida del servidor (textos del ECUD). */
public final class ErroresCliente {

  /** ECUD1 alterno 3 (y todos los casos de uso): sin conexión o sin respuesta a tiempo. */
  public static final String SIN_SERVIDOR = "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";
  /** ECUD10 alterno 4: la respuesta no se pudo interpretar; la aplicación sigue en ejecución. */
  public static final String RESPUESTA_INCOMPLETA = "El servidor envió una respuesta vacía o incompleta.";
  /** ECUD8 alterno 2: el token ya no es válido. */
  public static final String SESION_NO_VALIDA = "Su sesión no es válida. Inicie sesión nuevamente.";

  private ErroresCliente() {
  }
}
