package ec.edu.monster.controladores;

import ec.edu.monster.dao.PaisDAO;
import ec.edu.monster.dao.RegionDAO;
import ec.edu.monster.modelos.Mensajes;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.modelos.dto.ResultadoBusqueda;
import ec.edu.monster.modelos.dto.ResultadoImportacion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.dto.SolicitudPais;
import ec.edu.monster.modelos.entidades.Pais;
import ec.edu.monster.servicios.ConsumoAPIPaises;
import ec.edu.monster.utilidades.Textos;
import ec.edu.monster.vista.JsonResponseView;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * Países: listar, registrar, actualizar y eliminar (ECUD4), buscar y filtrar (ECUD5, ECUD6),
 * consultar en la API (ECUD3) e importar (ECUD2).
 */
@Path("Paises")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class PaisesController extends ControladorBase {

    private final ConsumoAPIPaises consumoAPIPaises = new ConsumoAPIPaises();
    private final PaisDAO paisDAO = new PaisDAO();
    private final RegionDAO regionDAO = new RegionDAO();

    /** GET /api/Paises: todos los países guardados, ordenados por nombre (ECUD4, Listar). */
    @GET
    public Response listar() {
        return responder("listar", () -> JsonResponseView.ok(Mensajes.PAISES_LISTADOS, paisDAO.listar()));
    }

    /**
     * GET /api/Paises/buscar?nombre=&amp;capital=&amp;region=. Buscar (ECUD5) cuando hay nombre o
     * capital; Filtrar (ECUD6) cuando solo llega la región.
     */
    @GET
    @Path("buscar")
    public Response buscar(@QueryParam("nombre") String nombre, @QueryParam("capital") String capital,
            @QueryParam("region") String region) {
        return responder("buscar", () -> {
            if (!Textos.vacio(region) && !regionDAO.existe(region)) {
                return JsonResponseView.error(400, Mensajes.REGION_NO_VALIDA);
            }
            List<Pais> paises = paisDAO.buscar(nombre, capital, region);
            boolean soloFiltro = Textos.vacio(nombre) && Textos.vacio(capital) && !Textos.vacio(region);
            return JsonResponseView.ok(soloFiltro ? Mensajes.FILTRO_APLICADO : Mensajes.BUSQUEDA_REALIZADA,
                    new ResultadoBusqueda(paises));
        });
    }

    /**
     * GET /api/Paises/externo/{nombre}: consulta en la API REST Countries y marca los países
     * que ya están guardados (ECUD3).
     */
    @GET
    @Path("externo/{nombre}")
    public Response consultarExterno(@PathParam("nombre") String nombre) {
        String error = errorNombre(nombre);
        if (error != null) {
            return JsonResponseView.error(400, error);
        }
        return responder("consultarExterno", () -> {
            List<PaisDTO> paises = consumoAPIPaises.obtenerPorNombre(nombre);
            Set<String> guardados = paisDAO.clavesGuardadas();
            for (PaisDTO pais : paises) {
                pais.setGuardado(guardados.contains(Textos.clave(pais.getNombre())));
            }
            return JsonResponseView.ok(Mensajes.CONSULTA_API_EXITOSA, paises);
        });
    }

    /**
     * POST /api/Paises/importar {consulta, nombre}: guarda el país elegido; lo registra si es
     * nuevo o actualiza sus datos si ya existe (ECUD2).
     */
    @POST
    @Path("importar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importar(SolicitudPais solicitud) {
        String error = errorSolicitud(solicitud);
        if (error != null) {
            return JsonResponseView.error(400, error);
        }
        return responder("importar", () -> {
            Pais pais = Pais.desde(consumoAPIPaises.obtenerPais(solicitud.getConsulta(), solicitud.getNombre()));
            String accion = paisDAO.importar(pais);
            return JsonResponseView.ok(Mensajes.PAIS_IMPORTADO, new ResultadoImportacion(pais, accion));
        });
    }

    /**
     * POST /api/Paises/importar/todos: importa todos los países de la API en una sola
     * transacción, sin duplicar (ERS §7.2; endpoint agregado en el punto de control 1).
     */
    @POST
    @Path("importar/todos")
    public Response importarTodos() {
        return responder("importarTodos", () -> {
            List<Pais> paises = consumoAPIPaises.obtenerTodos().stream().map(Pais::desde).toList();
            ResumenImportacion resumen = paisDAO.importarTodos(paises);
            return JsonResponseView.ok(Mensajes.IMPORTACION_FINALIZADA, resumen);
        });
    }

    /**
     * POST /api/Paises {consulta, nombre}: registra un país nuevo con los datos de la API.
     * 409 si ya existe (ECUD4, Registrar).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrar(SolicitudPais solicitud) {
        String error = errorSolicitud(solicitud);
        if (error != null) {
            return JsonResponseView.error(400, error);
        }
        return responder("registrar", () -> {
            Pais pais = Pais.desde(consumoAPIPaises.obtenerPais(solicitud.getConsulta(), solicitud.getNombre()));
            if (paisDAO.existe(pais.getNombre())) {
                return JsonResponseView.error(409, Mensajes.PAIS_DUPLICADO);
            }
            try {
                paisDAO.registrar(pais);
            } catch (SQLIntegrityConstraintViolationException e) {
                // Otro usuario lo registró entre la verificación y la inserción
                return JsonResponseView.error(409, Mensajes.PAIS_DUPLICADO);
            }
            return JsonResponseView.creado(Mensajes.PAIS_REGISTRADO, pais);
        });
    }

    /**
     * PUT /api/Paises/{nombre}: vuelve a consultar el país en la API y actualiza su capital,
     * región, población y moneda (ECUD4, Actualizar).
     */
    @PUT
    @Path("{nombre}")
    public Response actualizar(@PathParam("nombre") String nombre) {
        String error = errorNombre(nombre);
        if (error != null) {
            return JsonResponseView.error(400, error);
        }
        return responder("actualizar", () -> {
            if (!paisDAO.existe(nombre)) {
                return JsonResponseView.error(404, Mensajes.PAIS_NO_EXISTE);
            }
            Pais pais = Pais.desde(consumoAPIPaises.obtenerPais(null, nombre));
            pais.setNombre(nombre.trim());
            if (paisDAO.actualizar(pais) == 0) {
                return JsonResponseView.error(404, Mensajes.PAIS_NO_EXISTE);
            }
            return JsonResponseView.ok(Mensajes.PAIS_ACTUALIZADO, pais);
        });
    }

    /** DELETE /api/Paises/{nombre}: elimina el país de la base local (ECUD4, Eliminar). */
    @DELETE
    @Path("{nombre}")
    public Response eliminar(@PathParam("nombre") String nombre) {
        return responder("eliminar", () -> paisDAO.eliminar(nombre) == 0
                ? JsonResponseView.error(404, Mensajes.PAIS_NO_EXISTE)
                : JsonResponseView.ok(Mensajes.PAIS_ELIMINADO, null));
    }

    /** Valida el cuerpo {consulta, nombre}; la consulta puede faltar (se usa el nombre). */
    private static String errorSolicitud(SolicitudPais solicitud) {
        if (solicitud == null) {
            return Mensajes.INGRESE_NOMBRE_PAIS;
        }
        String error = errorNombre(solicitud.getNombre());
        if (error == null && !Textos.vacio(solicitud.getConsulta())) {
            error = errorNombre(solicitud.getConsulta());
        }
        return error;
    }
}
