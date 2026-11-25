package ec.edu.monster.controlador;

import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.dto.ResultadoBusqueda;
import ec.edu.monster.modelos.dto.ResultadoImportacion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.http.ClienteRest;
import ec.edu.monster.modelos.http.ConfigHTTP;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/** Países: gestionar (ECUD4), buscar y filtrar (ECUD5, ECUD6), consultar en la API (ECUD3) e importar (ECUD2). */
public class PaisController {

  private static final Type LISTA_PAISES = new TypeToken<List<Pais>>() { }.getType();

  /** ECUD4 Listar: todos los países guardados, ordenados por nombre. */
  public Respuesta<List<Pais>> listar() {
    return ClienteRest.get(Endpoints.PAISES, LISTA_PAISES);
  }

  /** ECUD5 y ECUD6: criterios vacíos se omiten; sin criterios devuelve todos. */
  public Respuesta<ResultadoBusqueda> buscar(String nombre, String capital, String region) {
    String consulta = Endpoints.consulta(
      Endpoints.parametro("nombre", nombre),
      Endpoints.parametro("capital", capital),
      Endpoints.parametro("region", region));
    return ClienteRest.get(Endpoints.BUSCAR + consulta, ResultadoBusqueda.class);
  }

  /** ECUD3: países de la API que coinciden con el nombre, marcados si ya están guardados. */
  public Respuesta<List<Pais>> consultarApi(String nombre) {
    return ClienteRest.get(Endpoints.EXTERNO + Endpoints.segmento(nombre), LISTA_PAISES);
  }

  /** ECUD2: guarda el país elegido de la consulta (lo registra o lo actualiza). */
  public Respuesta<ResultadoImportacion> importar(String consulta, String nombre) {
    return ClienteRest.post(Endpoints.IMPORTAR, solicitud(consulta, nombre), ResultadoImportacion.class);
  }

  /** Importa todos los países de la API; puede tardar varios segundos. */
  public Respuesta<ResumenImportacion> importarTodos() {
    return ClienteRest.post(Endpoints.IMPORTAR_TODOS, null, ResumenImportacion.class,
      ConfigHTTP.TIMEOUT_IMPORTACION);
  }

  /** ECUD4 Registrar: registra un país nuevo elegido de la consulta (409 si ya existe). */
  public Respuesta<Pais> registrar(String consulta, String nombre) {
    return ClienteRest.post(Endpoints.PAISES, solicitud(consulta, nombre), Pais.class);
  }

  /** ECUD4 Actualizar: el servidor vuelve a consultar la API y actualiza el país. */
  public Respuesta<Pais> actualizar(String nombre) {
    return ClienteRest.put(Endpoints.PAISES + "/" + Endpoints.segmento(nombre), Pais.class);
  }

  /** ECUD4 Eliminar: borra el país de la base local. */
  public Respuesta<Object> eliminar(String nombre) {
    return ClienteRest.delete(Endpoints.PAISES + "/" + Endpoints.segmento(nombre), Object.class);
  }

  private static Map<String, String> solicitud(String consulta, String nombre) {
    return Map.of("consulta", ValidadorDatos.limpiar(consulta), "nombre", ValidadorDatos.limpiar(nombre));
  }
}
