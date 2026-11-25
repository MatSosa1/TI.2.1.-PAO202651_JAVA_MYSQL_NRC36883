package ec.edu.monster.servicios;

import ec.edu.monster.modelos.Mensajes;

/**
 * Falla al consumir la API REST Countries (ECUD9, flujos alternos). El tipo decide el
 * mensaje y el código HTTP que devuelve el caso de uso invocador.
 */
public class ApiExternaException extends Exception {

    public enum Tipo {
        /** Sin conexión, tiempo agotado o código de error de la API (incluye 401/403/429): HTTP 502. */
        NO_DISPONIBLE(Mensajes.API_NO_DISPONIBLE, 502),
        /** La API no tiene países con ese nombre: HTTP 404. */
        SIN_COINCIDENCIAS(Mensajes.SIN_COINCIDENCIAS_API, 404),
        /** El cuerpo no tiene el formato esperado: HTTP 502. */
        RESPUESTA_INVALIDA(Mensajes.API_RESPUESTA_INVALIDA, 502);

        private final String mensaje;
        private final int codigoHttp;

        Tipo(String mensaje, int codigoHttp) {
            this.mensaje = mensaje;
            this.codigoHttp = codigoHttp;
        }

        public String getMensaje() {
            return mensaje;
        }

        public int getCodigoHttp() {
            return codigoHttp;
        }
    }

    private final Tipo tipo;

    public ApiExternaException(Tipo tipo, String detalle) {
        this(tipo, detalle, null);
    }

    public ApiExternaException(Tipo tipo, String detalle, Throwable causa) {
        super(detalle, causa);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    /** Mensaje para el usuario (el detalle técnico queda en getMessage(), solo para el log). */
    public String getMensajeUsuario() {
        return tipo.getMensaje();
    }

    public int getCodigoHttp() {
        return tipo.getCodigoHttp();
    }
}
