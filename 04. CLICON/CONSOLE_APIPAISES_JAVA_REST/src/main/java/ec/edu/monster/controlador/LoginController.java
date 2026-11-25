package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.LoginDatos;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.http.ClienteRest;
import ec.edu.monster.modelos.http.ConfigHTTP;
import java.util.Map;

/** Iniciar y cerrar sesión (ECUD1). */
public class LoginController {

  /** ECUD1 pasos 4 a 7: envía las credenciales y, si son correctas, guarda el token de la sesión. */
  public Respuesta<LoginDatos> login(String usuario, String clave) {
    Respuesta<LoginDatos> respuesta = ClienteRest.post(Endpoints.LOGIN,
      Map.of("usuario", ValidadorDatos.limpiar(usuario), "clave", clave == null ? "" : clave), LoginDatos.class);

    if (respuesta.esOk() && respuesta.getDatos() != null) {
      ConfigHTTP.setToken(respuesta.getDatos().token());
      ConfigHTTP.setNombre(respuesta.getDatos().usuario());
    }
    return respuesta;
  }

  /** ECUD1 alterno 4: cierra la sesión; los datos locales se borran aunque el servidor no responda. */
  public Respuesta<Object> logout() {
    try {
      return ClienteRest.post(Endpoints.LOGOUT, null, Object.class);
    } finally {
      ConfigHTTP.limpiar();
    }
  }
}
