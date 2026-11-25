package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.enums.EstadosEnum;

/**
 * Respuesta estructurada del servidor: estado, mensaje y datos (ECUD10). {@code codigoHttp}
 * no viene en el JSON: lo agrega el cliente para reconocer el 401 de sesión no válida.
 */
public class Respuesta<T> {

  private EstadosEnum estado;
  private String mensaje;
  private T datos;
  private transient int codigoHttp;

  public Respuesta() {
  }

  public Respuesta(EstadosEnum estado, String mensaje, T datos) {
    this.estado = estado;
    this.mensaje = mensaje;
    this.datos = datos;
  }

  /** Error generado en el cliente (sin conexión, respuesta ilegible...). */
  public static <T> Respuesta<T> error(String mensaje) {
    return new Respuesta<>(EstadosEnum.ERROR, mensaje, null);
  }

  public boolean esOk() {
    return estado == EstadosEnum.OK;
  }

  /** ECUD8: el servidor rechazó el token (401); el cliente debe borrar la sesión. */
  public boolean sesionNoValida() {
    return codigoHttp == 401;
  }

  public EstadosEnum getEstado() {
    return estado;
  }

  public String getMensaje() {
    return mensaje == null ? "" : mensaje;
  }

  public T getDatos() {
    return datos;
  }

  public int getCodigoHttp() {
    return codigoHttp;
  }

  public void setCodigoHttp(int codigoHttp) {
    this.codigoHttp = codigoHttp;
  }
}
