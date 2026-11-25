package ec.edu.monster.modelos.servicios;

/**
 * La operación no se completó. El mensaje ya está listo para mostrarlo: es el del servidor
 * (estado ERROR) o uno del cliente (sin conexión, respuesta ilegible). {@code sesionNoValida}
 * indica que el servidor rechazó el token (ECUD8): hay que volver al inicio de sesión.
 */
public class ServicioException extends Exception {

    public static final String SIN_SERVIDOR = "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";
    public static final String RESPUESTA_INCOMPLETA = "El servidor envió una respuesta vacía o incompleta.";
    public static final String SESION_NO_VALIDA = "Su sesión no es válida. Inicie sesión nuevamente.";

    private final boolean sesionNoValida;

    public ServicioException(String mensaje) {
        this(mensaje, false);
    }

    public ServicioException(String mensaje, boolean sesionNoValida) {
        super(mensaje);
        this.sesionNoValida = sesionNoValida;
    }

    public boolean isSesionNoValida() {
        return sesionNoValida;
    }
}
