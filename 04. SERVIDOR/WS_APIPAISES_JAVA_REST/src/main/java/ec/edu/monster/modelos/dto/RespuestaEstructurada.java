package ec.edu.monster.modelos.dto;

import jakarta.json.bind.annotation.JsonbNillable;

/**
 * Respuesta uniforme de todas las operaciones del servidor (RQF-010, ECUD10):
 * estado OK o ERROR, un mensaje en español y los datos. Con ERROR los datos van vacíos.
 */
public class RespuestaEstructurada<T> {

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";

    private String estado;
    private String mensaje;
    /** Se serializa aunque sea null («datos»: null), para que todas las respuestas tengan los tres campos. */
    @JsonbNillable
    private T datos;

    public RespuestaEstructurada() {
    }

    public RespuestaEstructurada(String estado, String mensaje, T datos) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public static <T> RespuestaEstructurada<T> ok(String mensaje, T datos) {
        return new RespuestaEstructurada<>(OK, mensaje, datos);
    }

    public static <T> RespuestaEstructurada<T> error(String mensaje) {
        return new RespuestaEstructurada<>(ERROR, mensaje, null);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }
}
