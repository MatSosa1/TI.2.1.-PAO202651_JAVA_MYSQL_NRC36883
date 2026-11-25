package ec.edu.monster.modelos.data;

/**
 * Resultado de una operación con el servidor: éxito con datos y el mensaje del servidor, o
 * error con el mensaje para el usuario. {@code sesionExpirada} indica que el servidor rechazó
 * el token (ECUD8) y hay que volver al inicio de sesión.
 */
public final class Resultado<T> {

    private final boolean exito;
    private final T datos;
    private final String mensaje;
    private final boolean sesionExpirada;

    private Resultado(boolean exito, T datos, String mensaje, boolean sesionExpirada) {
        this.exito = exito;
        this.datos = datos;
        this.mensaje = mensaje == null ? "" : mensaje;
        this.sesionExpirada = sesionExpirada;
    }

    public static <T> Resultado<T> ok(T datos, String mensaje) {
        return new Resultado<>(true, datos, mensaje, false);
    }

    public static <T> Resultado<T> error(String mensaje) {
        return new Resultado<>(false, null, mensaje, false);
    }

    public static <T> Resultado<T> sesionExpirada(String mensaje) {
        return new Resultado<>(false, null, mensaje, true);
    }

    public boolean isExito() {
        return exito;
    }

    public T getDatos() {
        return datos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isSesionExpirada() {
        return sesionExpirada;
    }
}
