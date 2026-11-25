package ec.edu.monster.modelos.repositorios;

import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelos.data.Datos.Busqueda;
import ec.edu.monster.modelos.data.Datos.Importacion;
import ec.edu.monster.modelos.data.Datos.Informe;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.modelos.data.Datos.Resumen;
import ec.edu.monster.modelos.data.Datos.Sesion;
import ec.edu.monster.modelos.data.Resultado;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Único lugar de la app que conoce las rutas del servidor (contrato del plan, §3):
 * /Login, /Paises y /Reportes.
 */
public class ApiPaisesRepositorio {

    private static final Type LISTA_PAISES = new TypeToken<List<Pais>>() { }.getType();

    private final ClienteRest cliente;

    public ApiPaisesRepositorio(ClienteRest cliente) {
        this.cliente = cliente;
    }

    // ------------------------------------------------------------------ ECUD1

    public Resultado<Sesion> login(String usuario, String clave) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("usuario", usuario);
        cuerpo.put("clave", clave);
        Resultado<Sesion> resultado = cliente.enviar("POST", "/Login/login", null, cuerpo, Sesion.class,
                ClienteRest.ESPERA_MS);
        if (resultado.isExito() && (resultado.getDatos() == null || resultado.getDatos().getToken() == null)) {
            return Resultado.error(ClienteRest.RESPUESTA_INCOMPLETA);
        }
        return resultado;
    }

    public Resultado<Object> logout(String token) {
        return cliente.enviar("POST", "/Login/logout", token, null, Object.class, ClienteRest.ESPERA_MS);
    }

    // ------------------------------------------------------------------ ECUD4 / ECUD5 / ECUD6

    public Resultado<List<Pais>> listar(String token) {
        return cliente.enviar("GET", "/Paises", token, null, LISTA_PAISES, ClienteRest.ESPERA_MS);
    }

    /** Los criterios vacíos se omiten. */
    public Resultado<Busqueda> buscar(String token, String nombre, String capital, String region) {
        StringBuilder consulta = new StringBuilder();
        agregar(consulta, "nombre", nombre);
        agregar(consulta, "capital", capital);
        agregar(consulta, "region", region);
        return cliente.enviar("GET", "/Paises/buscar" + consulta, token, null, Busqueda.class, ClienteRest.ESPERA_MS);
    }

    public Resultado<Pais> registrar(String token, String consulta, String nombre) {
        return cliente.enviar("POST", "/Paises", token, solicitud(consulta, nombre), Pais.class, ClienteRest.ESPERA_MS);
    }

    public Resultado<Pais> actualizar(String token, String nombre) {
        return cliente.enviar("PUT", "/Paises/" + ClienteRest.codificar(nombre), token, null, Pais.class,
                ClienteRest.ESPERA_MS);
    }

    public Resultado<Object> eliminar(String token, String nombre) {
        return cliente.enviar("DELETE", "/Paises/" + ClienteRest.codificar(nombre), token, null, Object.class,
                ClienteRest.ESPERA_MS);
    }

    // ------------------------------------------------------------------ ECUD3 / ECUD2

    public Resultado<List<Pais>> consultarApi(String token, String nombre) {
        return cliente.enviar("GET", "/Paises/externo/" + ClienteRest.codificar(nombre), token, null, LISTA_PAISES,
                ClienteRest.ESPERA_MS);
    }

    public Resultado<Importacion> importar(String token, String consulta, String nombre) {
        return cliente.enviar("POST", "/Paises/importar", token, solicitud(consulta, nombre), Importacion.class,
                ClienteRest.ESPERA_MS);
    }

    public Resultado<Resumen> importarTodos(String token) {
        return cliente.enviar("POST", "/Paises/importar/todos", token, null, Resumen.class,
                ClienteRest.ESPERA_IMPORTACION_MS);
    }

    // ------------------------------------------------------------------ ECUD7

    public Resultado<Informe> informes(String token) {
        return cliente.enviar("GET", "/Reportes", token, null, Informe.class, ClienteRest.ESPERA_MS);
    }

    private static Map<String, String> solicitud(String consulta, String nombre) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("consulta", consulta == null ? "" : consulta.trim());
        cuerpo.put("nombre", nombre.trim());
        return cuerpo;
    }

    private static void agregar(StringBuilder consulta, String clave, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return;
        }
        consulta.append(consulta.length() == 0 ? '?' : '&').append(clave).append('=')
                .append(ClienteRest.codificar(valor));
    }
}
