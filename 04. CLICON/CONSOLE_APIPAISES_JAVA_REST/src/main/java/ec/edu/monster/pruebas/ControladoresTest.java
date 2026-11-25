package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.controlador.ErroresCliente;
import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.controlador.PaisController;
import ec.edu.monster.controlador.ReporteController;
import ec.edu.monster.modelos.dto.Informe;
import ec.edu.monster.modelos.dto.LoginDatos;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.dto.ResultadoBusqueda;
import ec.edu.monster.modelos.dto.ResultadoImportacion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.enums.EstadosEnum;
import ec.edu.monster.modelos.http.ConfigHTTP;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Controladores del cliente contra un servidor simulado: URL, método, encabezados, cuerpo y respuesta. */
class ControladoresTest {

  private static final String BASE = "http://servidor:8080/WS_APIPAISES_JAVA_REST/api";
  private static final String TOKEN = "a".repeat(64);
  private static final String ECUADOR =
    "{\"nombre\":\"Ecuador\",\"capital\":\"Quito\",\"region\":\"América\",\"poblacion\":18001000,"
      + "\"moneda\":\"United States dollar (USD)\"}";

  private static String ok(String mensaje, String datos) {
    return "{\"datos\":" + datos + ",\"estado\":\"OK\",\"mensaje\":\"" + mensaje + "\"}";
  }

  private static String error(String mensaje) {
    return "{\"datos\":null,\"estado\":\"ERROR\",\"mensaje\":\"" + mensaje + "\"}";
  }

  private static ServidorFalso preparar(String respuesta) {
    ServidorFalso falso = new ServidorFalso(respuesta);
    ConfigHTTP.setTransporte(falso);
    ConfigHTTP.setBaseUrl(BASE + "/");
    ConfigHTTP.limpiar();
    return falso;
  }

  private static void conSesion() {
    ConfigHTTP.setToken(TOKEN);
    ConfigHTTP.setNombre("Administrador");
  }

  private static JsonObject cuerpo(ServidorFalso s) {
    return JsonParser.parseString(s.cuerpo).getAsJsonObject();
  }

  @AfterEach
  void limpiar() {
    ConfigHTTP.limpiar();
  }

  // ------------------------------------------------------------------ ECUD1

  @Test
  void loginCorrectoGuardaLaSesion() {
    ServidorFalso s = preparar(ok("Ingreso de sesión exitoso", "{\"token\":\"" + TOKEN + "\",\"usuario\":\"Administrador\"}"));

    Respuesta<LoginDatos> r = new LoginController().login(" admin ", "admin123");

    assertEquals(EstadosEnum.OK, r.getEstado());
    assertEquals("Ingreso de sesión exitoso", r.getMensaje());
    assertEquals("POST", s.metodo);
    assertEquals(BASE + "/Login/login", s.url);
    assertEquals("admin", cuerpo(s).get("usuario").getAsString());
    assertEquals("admin123", cuerpo(s).get("clave").getAsString());
    assertFalse(s.cabeceras.containsKey("Authorization"), "el login no lleva token");
    assertEquals(TOKEN, ConfigHTTP.getToken());
    assertEquals("Administrador", ConfigHTTP.getNombre());
  }

  @Test
  void credencialesIncorrectasNoGuardanSesionNiSonSesionNoValida() {
    ServidorFalso s = preparar(error("Credenciales incorrectas"));
    s.codigo = 401;

    Respuesta<LoginDatos> r = new LoginController().login("admin", "mala");

    assertEquals(EstadosEnum.ERROR, r.getEstado());
    assertEquals("Credenciales incorrectas", r.getMensaje());
    assertFalse(ConfigHTTP.haySesion());
  }

  @Test
  void logoutBorraLaSesionAunqueNoHayaServidor() {
    ServidorFalso s = preparar("");
    conSesion();
    s.sinRed = true;

    Respuesta<Object> r = new LoginController().logout();

    assertEquals(ErroresCliente.SIN_SERVIDOR, r.getMensaje());
    assertEquals(BASE + "/Login/logout", s.url);
    assertFalse(ConfigHTTP.haySesion(), "ECUD1 alterno 4");
  }

  // ------------------------------------------------------------------ ECUD8 / ECUD10

  @Test
  void lasPeticionesLlevanElTokenSinPrefijo() {
    ServidorFalso s = preparar(ok("Países obtenidos correctamente", "[" + ECUADOR + "]"));
    conSesion();

    Respuesta<List<Pais>> r = new PaisController().listar();

    assertEquals("GET", s.metodo);
    assertEquals(BASE + "/Paises", s.url);
    assertEquals(TOKEN, s.cabeceras.get("Authorization"));
    assertNull(s.cuerpo);
    Pais ecuador = r.getDatos().get(0);
    assertEquals("Ecuador", ecuador.nombre());
    assertEquals(18001000L, ecuador.poblacion());
    assertFalse(ecuador.guardado(), "sin el campo, guardado es false");
  }

  @Test
  void token401EsSesionNoValida() {
    ServidorFalso s = preparar(error("Token inválido o sesión no activa"));
    s.codigo = 401;
    conSesion();

    Respuesta<List<Pais>> r = new PaisController().listar();

    assertTrue(r.sesionNoValida());
    assertEquals("Token inválido o sesión no activa", r.getMensaje());
  }

  @Test
  void sinRedOTiempoAgotadoEsSinServidor() {
    ServidorFalso s = preparar("");
    conSesion();
    s.sinRed = true;
    assertEquals(ErroresCliente.SIN_SERVIDOR, new PaisController().listar().getMensaje());

    s.sinRed = false;
    s.tiempoAgotado = true;
    Respuesta<List<Pais>> r = new PaisController().listar();
    assertEquals(ErroresCliente.SIN_SERVIDOR, r.getMensaje());
    assertFalse(r.sesionNoValida());
  }

  @Test
  void respuestaIlegibleNoDetieneElCliente() {
    for (String cuerpo : List.of("<html>404 Not Found</html>", "", "{}", "{\"estado\":")) {
      preparar(cuerpo);
      conSesion();
      Respuesta<List<Pais>> r = new PaisController().listar();
      assertEquals(EstadosEnum.ERROR, r.getEstado(), cuerpo);
      assertEquals(ErroresCliente.RESPUESTA_INCOMPLETA, r.getMensaje(), cuerpo);
    }
  }

  // ------------------------------------------------------------------ ECUD5 / ECUD6

  @Test
  void buscarCodificaLosCriteriosYOmiteLosVacios() {
    ServidorFalso s = preparar(ok("Búsqueda realizada correctamente", "{\"paises\":[" + ECUADOR + "],\"total\":1}"));
    conSesion();

    Respuesta<ResultadoBusqueda> r = new PaisController().buscar("san josé", "", "América");

    assertEquals(BASE + "/Paises/buscar?nombre=san%20jos%C3%A9&region=Am%C3%A9rica", s.url);
    assertEquals(1, r.getDatos().total());
    assertEquals("Ecuador", r.getDatos().paises().get(0).nombre());
  }

  @Test
  void buscarSinCriteriosNoAgregaParametros() {
    ServidorFalso s = preparar(ok("Búsqueda realizada correctamente", "{\"paises\":[],\"total\":0}"));
    conSesion();
    new PaisController().buscar(null, " ", "");
    assertEquals(BASE + "/Paises/buscar", s.url);
  }

  // ------------------------------------------------------------------ ECUD3 / ECUD2 / ECUD4

  @Test
  void consultarApiCodificaElNombreEnLaRuta() {
    ServidorFalso s = preparar(ok("Consulta en la API exitosa",
      "[{\"nombre\":\"Perú\",\"capital\":\"Lima\",\"region\":\"América\",\"poblacion\":34000000,"
        + "\"moneda\":\"Peruvian sol (PEN)\",\"guardado\":true}]"));
    conSesion();

    Respuesta<List<Pais>> r = new PaisController().consultarApi("Perú del Sur");

    assertEquals(BASE + "/Paises/externo/Per%C3%BA%20del%20Sur", s.url);
    assertTrue(r.getDatos().get(0).guardado());
  }

  @Test
  void importarEnviaConsultaYNombre() {
    ServidorFalso s = preparar(ok("País importado correctamente", "{\"pais\":" + ECUADOR + ",\"accion\":\"registrado\"}"));
    conSesion();

    Respuesta<ResultadoImportacion> r = new PaisController().importar("ecuador", "Ecuador");

    assertEquals("POST", s.metodo);
    assertEquals(BASE + "/Paises/importar", s.url);
    assertEquals("ecuador", cuerpo(s).get("consulta").getAsString());
    assertEquals("Ecuador", cuerpo(s).get("nombre").getAsString());
    assertEquals("application/json; charset=utf-8", s.cabeceras.get("Content-Type"));
    assertEquals("registrado", r.getDatos().accion());
  }

  @Test
  void importarTodosEsperaMasTiempo() {
    ServidorFalso s = preparar(ok("Importación finalizada", "{\"recibidos\":250,\"registrados\":240,\"actualizados\":10}"));
    conSesion();

    Respuesta<ResumenImportacion> r = new PaisController().importarTodos();

    assertEquals(BASE + "/Paises/importar/todos", s.url);
    assertEquals(ConfigHTTP.TIMEOUT_IMPORTACION, s.espera);
    assertEquals(240, r.getDatos().registrados());
  }

  @Test
  void registrarActualizarYEliminar() {
    ServidorFalso s = preparar(ok("País registrado correctamente", ECUADOR));
    s.codigo = 201;
    conSesion();
    PaisController controller = new PaisController();

    assertTrue(controller.registrar("ecuador", "Ecuador").esOk());
    assertEquals("POST", s.metodo);
    assertEquals(BASE + "/Paises", s.url);

    controller.actualizar("Países Bajos");
    assertEquals("PUT", s.metodo);
    assertEquals(BASE + "/Paises/Pa%C3%ADses%20Bajos", s.url);

    s.respuesta = error("El país no existe");
    s.codigo = 404;
    Respuesta<Object> r = controller.eliminar("Narnia");
    assertEquals("DELETE", s.metodo);
    assertEquals(BASE + "/Paises/Narnia", s.url);
    assertEquals("El país no existe", r.getMensaje());
    assertFalse(r.sesionNoValida());
  }

  // ------------------------------------------------------------------ ECUD7

  @Test
  void informesConLasTresSecciones() {
    preparar(ok("Informes generados correctamente",
      "{\"paisMasPoblado\":" + ECUADOR + ",\"paisesPorRegion\":[{\"cantidad\":1,\"region\":\"América\"}],"
        + "\"promedioPorRegion\":[{\"promedioPoblacion\":18001000,\"region\":\"América\"}]}"));
    conSesion();

    Informe informe = new ReporteController().generar().getDatos();

    assertEquals("Ecuador", informe.paisMasPoblado().nombre());
    assertEquals(1, informe.paisesPorRegion().get(0).cantidad());
    assertEquals(18001000L, informe.promedioPorRegion().get(0).promedioPoblacion());
  }

  @Test
  void informesSinPaisesEsErrorConHttp200() {
    preparar(error("No hay países registrados para generar informes"));
    conSesion();

    Respuesta<Informe> r = new ReporteController().generar();

    assertEquals(EstadosEnum.ERROR, r.getEstado());
    assertFalse(r.sesionNoValida());
    assertNull(r.getDatos());
  }
}
