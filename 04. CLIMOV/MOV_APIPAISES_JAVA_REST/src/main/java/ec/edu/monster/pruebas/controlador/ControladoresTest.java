package ec.edu.monster.pruebas.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisesControlador;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.modelos.data.Datos.Sesion;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.pruebas.apoyo.ServidorFalso;
import ec.edu.monster.servicios.SesionServicio;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/** Controladores de la app con el servidor simulado y una sesión en memoria. */
public class ControladoresTest {

    private final ServidorFalso servidor = new ServidorFalso();
    private final SesionServicio sesion = new SesionServicio();
    private LoginControlador login;
    private PaisesControlador paises;

    @Before
    public void preparar() {
        login = new LoginControlador(servidor.repositorio(), sesion);
        paises = new PaisesControlador(servidor.repositorio(), sesion);
    }

    private void conSesion() {
        sesion.iniciar(new Sesion(ServidorFalso.TOKEN, "Administrador"));
    }

    // ------------------------------------------------------------------ ECUD1

    @Test
    public void camposVaciosNoLlamanAlServidor() {
        assertEquals(LoginControlador.Campo.USUARIO, login.campoInvalido(" ", "x"));
        assertEquals(LoginControlador.Campo.CLAVE, login.campoInvalido("admin", ""));
        assertNull(login.campoInvalido("admin", "admin123"));
        assertEquals(ValidadorDatos.INGRESE_CLAVE, login.iniciarSesion("admin", "").getMensaje());
        assertTrue(servidor.peticiones.isEmpty());
    }

    @Test
    public void loginCorrectoGuardaLaSesionYLogoutLaBorra() {
        servidor.ok("POST /Login/login", "Ingreso de sesión exitoso",
                "{\"token\":\"" + ServidorFalso.TOKEN + "\",\"usuario\":\"Administrador\"}");
        assertTrue(login.iniciarSesion(" admin ", "admin123").isExito());
        assertTrue(login.haySesion());
        assertTrue(servidor.ultima().cuerpo.contains("\"usuario\":\"admin\""));

        String token = login.cerrarSesion();
        assertEquals(ServidorFalso.TOKEN, token);
        assertFalse("ECUD1 alterno 4: la sesión local se borra de inmediato", login.haySesion());

        servidor.servidorApagado = true;
        assertFalse(login.notificarCierre(token).isExito());
    }

    // ------------------------------------------------------------------ ECUD4 / ECUD5 / ECUD6

    @Test
    public void sinCriteriosListaYConCriteriosBusca() {
        conSesion();
        servidor.ok("GET /Paises", "ok", "[" + ServidorFalso.ECUADOR + "]");
        assertEquals(1, paises.buscar("", " ", "").getDatos().size());
        assertEquals("GET", servidor.ultima().metodo);
        assertEquals("/Paises", servidor.ultima().ruta);

        servidor.ok("GET /Paises/buscar?capital=quito&region=Am%C3%A9rica", "Búsqueda realizada correctamente",
                "{\"paises\":[" + ServidorFalso.ECUADOR + "],\"total\":1}");
        Resultado<List<Pais>> resultado = paises.buscar("", "quito", "América");
        assertEquals("Búsqueda realizada correctamente", resultado.getMensaje());
        assertEquals("Ecuador", resultado.getDatos().get(0).getNombre());
    }

    @Test
    public void regionFueraDeLaListaSeIgnoraYCriterioLargoSeRechaza() {
        conSesion();
        servidor.ok("GET /Paises", "ok", "[]");
        paises.buscar(null, null, "Marte");
        assertEquals("/Paises", servidor.ultima().ruta);

        assertEquals(ValidadorDatos.NOMBRE_MUY_LARGO, paises.buscar("x".repeat(101), null, null).getMensaje());
        assertEquals(1, servidor.peticiones.size());
    }

    @Test
    public void tokenRechazadoBorraLaSesion() {
        conSesion();
        servidor.error("GET /Paises/buscar?region=Europa", 401, "Token inválido o sesión no activa");

        Resultado<List<Pais>> resultado = paises.buscar(null, null, "Europa");

        assertTrue(resultado.isSesionExpirada());
        assertFalse(paises.haySesion());
    }

    // ------------------------------------------------------------------ ECUD3 / ECUD2

    @Test
    public void consultarValidaElNombre() {
        conSesion();
        assertEquals(ValidadorDatos.INGRESE_NOMBRE_PAIS, paises.consultarApi(" ").getMensaje());
        assertEquals(ValidadorDatos.NOMBRE_MUY_LARGO, paises.consultarApi("x".repeat(101)).getMensaje());
        assertTrue(servidor.peticiones.isEmpty());
    }

    @Test
    public void importarAgregaLaAccionAlMensaje() {
        conSesion();
        servidor.ok("POST /Paises/importar", "País importado correctamente",
                "{\"pais\":" + ServidorFalso.ECUADOR + ",\"accion\":\"actualizado\"}");
        assertEquals("País importado correctamente (actualizado)", paises.importar("ecuador", "Ecuador").getMensaje());
    }

    @Test
    public void importarTodosMuestraElResumen() {
        conSesion();
        servidor.ok("POST /Paises/importar/todos", "Importación finalizada",
                "{\"recibidos\":250,\"registrados\":240,\"actualizados\":10}");
        assertEquals("Importación finalizada: 250 recibidos, 240 registrados, 10 actualizados",
                paises.importarTodos().getMensaje());
    }

    @Test
    public void informesSinPaisesEsError() {
        conSesion();
        servidor.responder("GET /Reportes", 200, "ERROR", "No hay países registrados para generar informes", "null");
        Resultado<?> resultado = paises.informes();
        assertFalse(resultado.isExito());
        assertFalse(resultado.isSesionExpirada());
        assertEquals("No hay países registrados para generar informes", resultado.getMensaje());
    }
}
