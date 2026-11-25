package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.http.RespuestaHttp;
import ec.edu.monster.modelos.http.Transporte;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Reemplaza la red: guarda la última petición y devuelve la respuesta preparada. */
class ServidorFalso implements Transporte {

  String metodo = "";
  String url = "";
  String cuerpo;
  Map<String, String> cabeceras = new HashMap<>();
  Duration espera;

  String respuesta;
  int codigo = 200;
  boolean sinRed;
  boolean tiempoAgotado;

  ServidorFalso(String respuesta) {
    this.respuesta = respuesta;
  }

  @Override
  public RespuestaHttp enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, Duration espera)
      throws IOException {
    this.metodo = metodo;
    this.url = url;
    this.cuerpo = cuerpo;
    this.cabeceras = cabeceras;
    this.espera = espera;
    if (sinRed)
      throw new ConnectException("sin red");
    if (tiempoAgotado)
      throw new HttpTimeoutException("tiempo agotado");
    return new RespuestaHttp(codigo, respuesta);
  }
}
