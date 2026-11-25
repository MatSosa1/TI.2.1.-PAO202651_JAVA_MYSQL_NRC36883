package ec.edu.monster.pruebas.repositorios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.modelos.data.Datos.Busqueda;
import ec.edu.monster.modelos.data.Datos.Informe;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.modelos.data.Datos.Resumen;
import ec.edu.monster.modelos.data.Datos.Sesion;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.repositorios.ApiPaisesRepositorio;
import ec.edu.monster.modelos.repositorios.ClienteRest;
import ec.edu.monster.pruebas.apoyo.ServidorFalso;
import java.util.List;
import org.junit.Test;

/** Repositorio contra el servidor simulado: rutas, métodos, encabezados, cuerpo y lectura del JSON. */
public class ApiPaisesRepositorioTest {

    private final ServidorFalso servidor = new ServidorFalso();
    private final ApiPaisesRepositorio repositorio = servidor.repositorio();

    @Test
    public void loginEnviaLasCredencialesSinToken() {
        servidor.ok("POST /Login/login", "Ingreso de sesión exitoso",
                "{\"token\":\"" + ServidorFalso.TOKEN + "\",\"usuario\":\"Administrador\"}");

        Resultado<Sesion> resultado = repositorio.login("admin", "admin123");

        assertTrue(resultado.isExito());
        assertEquals(ServidorFalso.TOKEN, resultado.getDatos().getToken());
        assertEquals("Administrador", resultado.getDatos().getNombre());
        assertTrue(servidor.ultima().cuerpo.contains("\"usuario\":\"admin\""));
        assertFalse(servidor.ultima().cabeceras.containsKey("Authorization"));
    }

    @Test
    public void credencialesIncorrectasNoSonSesionVencida() {
        servidor.error("POST /Login/login", 401, "Credenciales incorrectas");

        Resultado<Sesion> resultado = repositorio.login("admin", "mala");

        assertFalse(resultado.isExito());
        assertFalse(resultado.isSesionExpirada());
        assertEquals("Credenciales incorrectas", resultado.getMensaje());
    }

    @Test
    public void tokenRechazadoEsSesionVencida() {
        servidor.error("GET /Paises", 401, "Token inválido o sesión no activa");

        Resultado<List<Pais>> resultado = repositorio.listar(ServidorFalso.TOKEN);

        assertTrue(resultado.isSesionExpirada());
        assertEquals(ClienteRest.SESION_NO_VALIDA, resultado.getMensaje());
        assertEquals(ServidorFalso.TOKEN, servidor.ultima().cabeceras.get("Authorization"));
    }

    @Test
    public void listarLeeLosPaises() {
        servidor.ok("GET /Paises", "Países obtenidos correctamente", "[" + ServidorFalso.ECUADOR + "]");

        Pais ecuador = repositorio.listar(ServidorFalso.TOKEN).getDatos().get(0);

        assertEquals("Ecuador", ecuador.getNombre());
        assertEquals(18_001_000L, ecuador.getPoblacion());
        assertFalse(ecuador.isGuardado());
        assertNull(servidor.ultima().cuerpo);
    }

    @Test
    public void buscarCodificaYOmiteLosCriteriosVacios() {
        servidor.ok("GET /Paises/buscar?nombre=san%20jos%C3%A9&region=Am%C3%A9rica", "Búsqueda realizada correctamente",
                "{\"paises\":[" + ServidorFalso.ECUADOR + "],\"total\":1}");

        Resultado<Busqueda> resultado = repositorio.buscar(ServidorFalso.TOKEN, "san josé", " ", "América");

        assertTrue(resultado.isExito());
        assertEquals(1, resultado.getDatos().getTotal());
    }

    @Test
    public void rutasConNombreYMetodos() {
        servidor.ok("GET /Paises/externo/Per%C3%BA", "ok", "[]");
        repositorio.consultarApi(ServidorFalso.TOKEN, "Perú");
        assertEquals("GET", servidor.ultima().metodo);

        servidor.ok("PUT /Paises/Pa%C3%ADses%20Bajos", "ok", ServidorFalso.ECUADOR);
        assertTrue(repositorio.actualizar(ServidorFalso.TOKEN, "Países Bajos").isExito());

        servidor.ok("DELETE /Paises/Narnia", "ok", "null");
        assertTrue(repositorio.eliminar(ServidorFalso.TOKEN, "Narnia").isExito());

        servidor.responder("POST /Paises", 201, "OK", "País registrado correctamente", ServidorFalso.ECUADOR);
        assertTrue(repositorio.registrar(ServidorFalso.TOKEN, "germany", "Alemania").isExito());
        assertTrue(servidor.ultima().cuerpo.contains("\"consulta\":\"germany\""));
        assertEquals("application/json; charset=utf-8", servidor.ultima().cabeceras.get("Content-Type"));
    }

    @Test
    public void importarTodosEsperaMasTiempo() {
        servidor.ok("POST /Paises/importar/todos", "Importación finalizada",
                "{\"recibidos\":250,\"registrados\":240,\"actualizados\":10}");

        Resultado<Resumen> resultado = repositorio.importarTodos(ServidorFalso.TOKEN);

        assertEquals(240, resultado.getDatos().getRegistrados());
        assertEquals(ClienteRest.ESPERA_IMPORTACION_MS, servidor.ultima().esperaMs);
    }

    @Test
    public void informesConLasTresSecciones() {
        servidor.ok("GET /Reportes", "Informes generados correctamente", "{\"paisMasPoblado\":" + ServidorFalso.ECUADOR
                + ",\"paisesPorRegion\":[{\"cantidad\":1,\"region\":\"América\"}],"
                + "\"promedioPorRegion\":[{\"promedioPoblacion\":18001000,\"region\":\"América\"}]}");

        Informe informe = repositorio.informes(ServidorFalso.TOKEN).getDatos();

        assertEquals("Ecuador", informe.getPaisMasPoblado().getNombre());
        assertEquals(1, informe.getPaisesPorRegion().get(0).getCantidad());
        assertEquals(18_001_000L, informe.getPromedioPorRegion().get(0).getPromedioPoblacion());
    }

    @Test
    public void sinRedServidorApagadoORespuestaIlegible() {
        servidor.sinRed = true;
        assertEquals(ClienteRest.SIN_SERVIDOR, repositorio.listar(ServidorFalso.TOKEN).getMensaje());
        assertTrue("sin red no se intenta la petición", servidor.peticiones.isEmpty());

        servidor.sinRed = false;
        servidor.servidorApagado = true;
        assertEquals(ClienteRest.SIN_SERVIDOR, repositorio.listar(ServidorFalso.TOKEN).getMensaje());

        servidor.servidorApagado = false; // sin respuesta preparada: página 404 de Payara
        Resultado<List<Pais>> resultado = repositorio.listar(ServidorFalso.TOKEN);
        assertEquals(ClienteRest.RESPUESTA_INCOMPLETA, resultado.getMensaje());
        assertFalse(resultado.isSesionExpirada());
    }

    @Test
    public void estadoErrorTraeElMensajeDelServidor() {
        servidor.error("POST /Paises", 409, "Ya existe un país con ese nombre");
        assertEquals("Ya existe un país con ese nombre",
                repositorio.registrar(ServidorFalso.TOKEN, "ecuador", "Ecuador").getMensaje());
    }
}
