package ec.edu.monster.controladores;

import static ec.edu.monster.controladores.NavegadorFalso.CTX;
import static ec.edu.monster.fakes.ServidorFalso.ALEMANIA;
import static ec.edu.monster.fakes.ServidorFalso.ECUADOR;
import static ec.edu.monster.fakes.ServidorFalso.TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.fakes.ServidorFalso;
import ec.edu.monster.modelos.dto.Informe;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.servicios.LoginService;
import ec.edu.monster.modelos.servicios.PaisService;
import ec.edu.monster.modelos.servicios.ReporteService;
import ec.edu.monster.modelos.servicios.ServicioException;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Controladores a través de doGet/doPost, con un navegador falso y el servidor REST simulado. */
class ControladoresTest {

    private final ServidorFalso servidor = new ServidorFalso();
    private final PaisService paisService = new PaisService(servidor.cliente());

    // ================================================================== ECUD1

    @Nested
    class Login {

        private final LoginController controller = new LoginController(new LoginService(servidor.cliente()));

        @Test
        void camposVaciosNoLlamanAlServidor() throws Exception {
            NavegadorFalso b = new NavegadorFalso().parametro("usuario", " ").parametro("clave", "x");
            controller.doPost(b.request, b.response);
            assertEquals("Ingrese el usuario", b.atributo("error"));

            b = new NavegadorFalso().parametro("usuario", "admin").parametro("clave", "");
            controller.doPost(b.request, b.response);
            assertEquals("Ingrese la clave", b.atributo("error"));
            assertEquals("admin", b.atributo("usuario"));
            assertEquals("login.jsp", b.vista);
            assertTrue(servidor.peticiones.isEmpty());
        }

        @Test
        void credencialesIncorrectasMuestranElMensajeSinSesion() throws Exception {
            servidor.error("POST /Login/login", 401, "Credenciales incorrectas");
            NavegadorFalso b = new NavegadorFalso().parametro("usuario", "admin").parametro("clave", "mala");

            controller.doPost(b.request, b.response);

            assertEquals("Credenciales incorrectas", b.atributo("error"));
            assertEquals("login.jsp", b.vista);
            assertNull(b.atributosSesion.get("token"));
            assertNull(b.redireccion);
        }

        @Test
        void loginCorrectoRenuevaLaSesionYVaAlMenu() throws Exception {
            servidor.ok("POST /Login/login", "Ingreso de sesión exitoso",
                    "{\"token\":\"" + TOKEN + "\",\"usuario\":\"Administrador\"}");
            NavegadorFalso b = new NavegadorFalso().parametro("usuario", " admin ").parametro("clave", "admin123");

            controller.doPost(b.request, b.response);

            assertTrue(b.sesionInvalidada, "id de sesión nuevo después del login");
            assertEquals(TOKEN, b.atributosSesion.get("token"));
            assertEquals("Administrador", b.atributosSesion.get("nombre"));
            assertEquals(CTX + "/menu", b.redireccion);
            assertTrue(servidor.ultima().cuerpo().contains("\"usuario\":\"admin\""));
        }

        @Test
        void avisoDeSesionVencida() throws Exception {
            NavegadorFalso b = new NavegadorFalso().parametro("sesion", "expirada");
            controller.doGet(b.request, b.response);
            assertEquals(ServicioException.SESION_NO_VALIDA, b.atributo("error"));
        }

        @Test
        void logoutCierraEnElServidorYLocalmente() throws Exception {
            servidor.ok("POST /Login/logout", "Sesión cerrada", "null");
            NavegadorFalso b = new NavegadorFalso().conSesion();

            new LogoutController(new LoginService(servidor.cliente())).doPost(b.request, b.response);

            assertEquals(TOKEN, servidor.ultima().cabeceras().get("Authorization"));
            assertTrue(b.sesionInvalidada);
            assertEquals(CTX + "/login", b.redireccion);
        }

        @Test
        void logoutSinServidorTambienCierraLaSesion() throws Exception {
            servidor.sinRed = true;
            NavegadorFalso b = new NavegadorFalso().conSesion();
            new LogoutController(new LoginService(servidor.cliente())).doPost(b.request, b.response);
            assertTrue(b.sesionInvalidada);
        }
    }

    // ================================================================== ECUD4 / ECUD6

    @Nested
    class Paises {

        private final PaisesController controller = new PaisesController(paisService);

        @Test
        @SuppressWarnings("unchecked")
        void listarYFiltrar() throws Exception {
            servidor.ok("GET /Paises", "Países obtenidos correctamente", "[" + ALEMANIA + "," + ECUADOR + "]");
            NavegadorFalso b = new NavegadorFalso().conSesion();
            controller.doGet(b.request, b.response);

            assertEquals("paises.jsp", b.vista);
            assertEquals(2, ((List<Pais>) b.atributo("paises")).size());
            assertEquals("2 países encontrados", b.atributo("total"));
            assertEquals("", b.atributo("region"));

            servidor.ok("GET /Paises/buscar?region=Europa", "Filtro aplicado correctamente",
                    "{\"paises\":[" + ALEMANIA + "],\"total\":1}");
            b = new NavegadorFalso().conSesion().parametro("region", "Europa");
            controller.doGet(b.request, b.response);
            assertEquals("1 país encontrado", b.atributo("total"));
            assertEquals("Europa", b.atributo("region"));

            b = new NavegadorFalso().conSesion().parametro("region", "Marte");
            controller.doGet(b.request, b.response);
            assertEquals("", b.atributo("region"), "una región que no es de la lista se ignora");
        }

        @Test
        void eliminarPideConfirmacionAntesDeEnviar() throws Exception {
            NavegadorFalso b = new NavegadorFalso().conSesion()
                    .parametro("accion", "eliminar").parametro("paso", "revisar").parametro("nombre", "Alemania");
            controller.doPost(b.request, b.response);

            assertEquals("Alemania", b.atributo("confirmarEliminar"));
            assertEquals("paises.jsp", b.vista);
            assertTrue(servidor.peticiones.isEmpty());
        }

        @Test
        void eliminarConfirmadoHacePostRedirectGetConElFiltro() throws Exception {
            servidor.ok("DELETE /Paises/Alemania", "País eliminado correctamente", "null");
            NavegadorFalso b = new NavegadorFalso().conSesion().parametro("accion", "eliminar")
                    .parametro("paso", "confirmar").parametro("nombre", "Alemania").parametro("region", "Europa");

            controller.doPost(b.request, b.response);

            assertEquals(CTX + "/paises?region=Europa", b.redireccion);
            assertEquals("País eliminado correctamente", b.atributosSesion.get("flashMensaje"));
        }

        @Test
        void actualizarConErrorDejaElMensajeDeError() throws Exception {
            servidor.error("PUT /Paises/Pa%C3%ADses%20Bajos", 502, "La API REST Countries no está disponible, intente más tarde");
            NavegadorFalso b = new NavegadorFalso().conSesion()
                    .parametro("accion", "actualizar").parametro("nombre", "Países Bajos");

            controller.doPost(b.request, b.response);

            assertEquals(CTX + "/paises", b.redireccion);
            assertEquals("La API REST Countries no está disponible, intente más tarde", b.atributosSesion.get("flashError"));
        }

        @Test
        void elFlashSeMuestraUnaSolaVez() throws Exception {
            servidor.ok("GET /Paises", "ok", "[]");
            NavegadorFalso b = new NavegadorFalso().conSesion();
            b.atributosSesion.put("flashMensaje", "País eliminado correctamente");

            controller.doGet(b.request, b.response);

            assertEquals("País eliminado correctamente", b.atributo("mensaje"));
            assertFalse(b.atributosSesion.containsKey("flashMensaje"));
        }

        @Test
        void tokenRechazadoVuelveAlLogin() throws Exception {
            servidor.error("GET /Paises", 401, "Token inválido o sesión no activa");
            NavegadorFalso b = new NavegadorFalso().conSesion();

            controller.doGet(b.request, b.response);

            assertTrue(b.sesionInvalidada);
            assertEquals(CTX + "/login?sesion=expirada", b.redireccion);
            assertNull(b.vista);
        }

        @Test
        void registrarDuplicadoQuedaEnElFormularioConLosResultados() throws Exception {
            servidor.error("POST /Paises", 409, "Ya existe un país con ese nombre")
                    .ok("GET /Paises/externo/germany", "Consulta en la API exitosa", "[" + ALEMANIA + "]");
            NavegadorFalso b = new NavegadorFalso().conSesion()
                    .parametro("consulta", "germany").parametro("nombre", "Alemania");

            new NuevoPaisController(paisService).doPost(b.request, b.response);

            assertEquals("Ya existe un país con ese nombre", b.atributo("error"));
            assertEquals("nuevo.jsp", b.vista);
            assertTrue(servidor.peticiones.get(0).cuerpo().contains("\"consulta\":\"germany\""));
        }

        @Test
        void registrarCorrectoVuelveALaTabla() throws Exception {
            servidor.responder("POST /Paises", 201, "OK", "País registrado correctamente", ALEMANIA);
            NavegadorFalso b = new NavegadorFalso().conSesion()
                    .parametro("consulta", "germany").parametro("nombre", "Alemania");

            new NuevoPaisController(paisService).doPost(b.request, b.response);

            assertEquals(CTX + "/paises", b.redireccion);
            assertEquals("País registrado correctamente", b.atributosSesion.get("flashMensaje"));
        }

        @Test
        void nuevoValidaElNombreSinLlamarAlServidor() throws Exception {
            NavegadorFalso b = new NavegadorFalso().conSesion().parametro("consulta", "x".repeat(101));
            new NuevoPaisController(paisService).doGet(b.request, b.response);
            assertEquals("El nombre no puede superar 100 caracteres", b.atributo("error"));
            assertTrue(servidor.peticiones.isEmpty());
        }
    }

    // ================================================================== ECUD3 / ECUD2

    @Nested
    class Consulta {

        private final ConsultaController controller = new ConsultaController(paisService);

        @Test
        @SuppressWarnings("unchecked")
        void consultarMuestraLosResultadosConGuardado() throws Exception {
            servidor.ok("GET /Paises/externo/ecuador", "Consulta en la API exitosa",
                    "[" + ECUADOR.replace("}", ",\"guardado\":true}") + "]");
            NavegadorFalso b = new NavegadorFalso().conSesion().parametro("nombre", "ecuador");

            controller.doGet(b.request, b.response);

            List<Pais> resultados = (List<Pais>) b.atributo("resultados");
            assertTrue(resultados.get(0).isGuardado());
            assertEquals("18.001.000", resultados.get(0).getPoblacionFormateada());
        }

        @Test
        void sinCoincidenciasConservaElTexto() throws Exception {
            servidor.error("GET /Paises/externo/zzz", 404, "No se encontraron países con ese nombre en la API");
            NavegadorFalso b = new NavegadorFalso().conSesion().parametro("nombre", "zzz");

            controller.doGet(b.request, b.response);

            assertEquals("No se encontraron países con ese nombre en la API", b.atributo("error"));
            assertEquals("zzz", b.atributo("nombre"));
        }

        @Test
        void guardarConfirmaYLuegoImporta() throws Exception {
            NavegadorFalso b = new NavegadorFalso().conSesion().parametro("accion", "guardar")
                    .parametro("paso", "revisar").parametro("consulta", "ecuador").parametro("nombre", "Ecuador");
            controller.doPost(b.request, b.response);
            assertEquals("guardar", b.atributo("confirmar"));
            assertTrue(servidor.peticiones.isEmpty());

            servidor.ok("POST /Paises/importar", "País importado correctamente",
                    "{\"pais\":" + ECUADOR + ",\"accion\":\"registrado\"}");
            b = new NavegadorFalso().conSesion().parametro("accion", "guardar")
                    .parametro("paso", "confirmar").parametro("consulta", "ecuador").parametro("nombre", "Ecuador");
            controller.doPost(b.request, b.response);

            assertEquals(CTX + "/consulta?nombre=ecuador", b.redireccion);
            assertEquals("País importado correctamente (registrado)", b.atributosSesion.get("flashMensaje"));
        }

        @Test
        void importarTodosMuestraElResumen() throws Exception {
            servidor.ok("POST /Paises/importar/todos", "Importación finalizada",
                    "{\"recibidos\":250,\"registrados\":240,\"actualizados\":10}");
            NavegadorFalso b = new NavegadorFalso().conSesion()
                    .parametro("accion", "importarTodos").parametro("paso", "confirmar");

            controller.doPost(b.request, b.response);

            assertEquals("Importación finalizada: 250 recibidos, 240 registrados, 10 actualizados",
                    b.atributosSesion.get("flashMensaje"));
        }
    }

    // ================================================================== ECUD5 / ECUD7

    @Test
    void buscarEnviaLosCriteriosCodificados() throws Exception {
        servidor.ok("GET /Paises/buscar?capital=san%20jos%C3%A9&region=Am%C3%A9rica", "Búsqueda realizada correctamente",
                "{\"paises\":[],\"total\":0}");
        NavegadorFalso b = new NavegadorFalso().conSesion().parametro("buscar", "1")
                .parametro("nombre", "").parametro("capital", "san josé").parametro("region", "América");

        new BusquedaController(paisService).doGet(b.request, b.response);

        assertEquals("0 países encontrados", b.atributo("total"));
        assertEquals("Búsqueda realizada correctamente", b.atributo("mensaje"));
    }

    @Test
    void busquedaSinEnviarNoLlamaAlServidor() throws Exception {
        NavegadorFalso b = new NavegadorFalso().conSesion();
        new BusquedaController(paisService).doGet(b.request, b.response);
        assertEquals("busqueda.jsp", b.vista);
        assertTrue(servidor.peticiones.isEmpty());
    }

    @Test
    void informesConGraficoYSinPaises() throws Exception {
        servidor.ok("GET /Reportes", "Informes generados correctamente", "{\"paisMasPoblado\":" + ALEMANIA
                + ",\"paisesPorRegion\":[{\"cantidad\":2,\"region\":\"Europa\"},{\"cantidad\":1,\"region\":\"Asia\"}],"
                + "\"promedioPorRegion\":[{\"promedioPoblacion\":18001000,\"region\":\"América\"}]}");
        NavegadorFalso b = new NavegadorFalso().conSesion();
        InformesController controller = new InformesController(new ReporteService(servidor.cliente()));
        controller.doGet(b.request, b.response);

        Informe informe = (Informe) b.atributo("informe");
        assertEquals("Alemania", informe.getPaisMasPoblado().getNombre());
        assertEquals(2, informe.getBarras().size());
        assertEquals("18.001.000", informe.getPromedioPorRegion().get(0).getPromedioFormateado());

        servidor.responder("GET /Reportes", 200, "ERROR", "No hay países registrados para generar informes", "null");
        b = new NavegadorFalso().conSesion();
        controller.doGet(b.request, b.response);
        assertEquals(true, b.atributo("sinPaises"));
        assertNull(b.atributo("informe"));
    }
}
