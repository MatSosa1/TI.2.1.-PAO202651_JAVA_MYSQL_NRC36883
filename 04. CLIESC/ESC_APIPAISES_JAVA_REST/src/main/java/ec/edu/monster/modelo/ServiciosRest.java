package ec.edu.monster.modelo;

import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Datos.ResultadoBusqueda;
import ec.edu.monster.modelo.Datos.ResultadoImportacion;
import ec.edu.monster.modelo.Datos.ResumenImportacion;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/** Implementación de los servicios contra el servidor RESTful (contrato del plan, §3). */
public class ServiciosRest implements LoginServicio, PaisServicio, ReporteServicio {

    private static final Type LISTA_PAISES = new TypeToken<List<Pais>>() { }.getType();

    private final ClienteRest cliente;

    public ServiciosRest(ClienteRest cliente) {
        this.cliente = cliente;
    }

    // ------------------------------------------------------------------ ECUD1

    @Override
    public DatosSesion iniciarSesion(String usuario, String clave) throws ServicioException {
        Resultado<DatosSesion> resultado = cliente.enviar("POST", "/Login/login", null,
                Map.of("usuario", usuario, "clave", clave), DatosSesion.class, ClienteRest.ESPERA);
        if (resultado.datos() == null || resultado.datos().token() == null) {
            throw new ServicioException(ServicioException.RESPUESTA_INCOMPLETA);
        }
        return resultado.datos();
    }

    @Override
    public void cerrarSesion(String token) throws ServicioException {
        cliente.enviar("POST", "/Login/logout", token, null, Object.class, ClienteRest.ESPERA);
    }

    // ------------------------------------------------------------------ ECUD2 a ECUD6

    @Override
    public Resultado<List<Pais>> listar(String token) throws ServicioException {
        return cliente.enviar("GET", "/Paises", token, null, LISTA_PAISES, ClienteRest.ESPERA);
    }

    @Override
    public Resultado<ResultadoBusqueda> buscar(String token, String nombre, String capital, String region)
            throws ServicioException {
        StringBuilder consulta = new StringBuilder();
        agregar(consulta, "nombre", nombre);
        agregar(consulta, "capital", capital);
        agregar(consulta, "region", region);
        return cliente.enviar("GET", "/Paises/buscar" + consulta, token, null, ResultadoBusqueda.class,
                ClienteRest.ESPERA);
    }

    @Override
    public Resultado<List<Pais>> consultarApi(String token, String nombre) throws ServicioException {
        return cliente.enviar("GET", "/Paises/externo/" + ClienteRest.codificar(nombre), token, null,
                LISTA_PAISES, ClienteRest.ESPERA);
    }

    @Override
    public Resultado<ResultadoImportacion> importar(String token, String consulta, String nombre)
            throws ServicioException {
        return cliente.enviar("POST", "/Paises/importar", token, solicitud(consulta, nombre),
                ResultadoImportacion.class, ClienteRest.ESPERA);
    }

    @Override
    public Resultado<ResumenImportacion> importarTodos(String token) throws ServicioException {
        return cliente.enviar("POST", "/Paises/importar/todos", token, null, ResumenImportacion.class,
                ClienteRest.ESPERA_IMPORTACION);
    }

    @Override
    public Resultado<Pais> registrar(String token, String consulta, String nombre) throws ServicioException {
        return cliente.enviar("POST", "/Paises", token, solicitud(consulta, nombre), Pais.class, ClienteRest.ESPERA);
    }

    @Override
    public Resultado<Pais> actualizar(String token, String nombre) throws ServicioException {
        return cliente.enviar("PUT", "/Paises/" + ClienteRest.codificar(nombre), token, null, Pais.class,
                ClienteRest.ESPERA);
    }

    @Override
    public Resultado<Object> eliminar(String token, String nombre) throws ServicioException {
        return cliente.enviar("DELETE", "/Paises/" + ClienteRest.codificar(nombre), token, null, Object.class,
                ClienteRest.ESPERA);
    }

    // ------------------------------------------------------------------ ECUD7

    @Override
    public Resultado<Informe> generar(String token) throws ServicioException {
        return cliente.enviar("GET", "/Reportes", token, null, Informe.class, ClienteRest.ESPERA);
    }

    private static Map<String, String> solicitud(String consulta, String nombre) {
        return Map.of("consulta", consulta == null ? "" : consulta.trim(), "nombre", nombre.trim());
    }

    /** Agrega «?clave=valor» o «&amp;clave=valor» si el valor no está vacío. */
    private static void agregar(StringBuilder consulta, String clave, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        consulta.append(consulta.isEmpty() ? '?' : '&').append(clave).append('=').append(ClienteRest.codificar(valor));
    }
}
