package ec.edu.monster.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.BusquedaControlador;
import ec.edu.monster.controlador.ConsultaApiControlador;
import ec.edu.monster.controlador.InformesControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisesControlador;
import ec.edu.monster.dobles.ServicioFalso;
import ec.edu.monster.dobles.VistasFalsas;
import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.Vistas.BusquedaVista.Criterios;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ControladoresTest {

    private final ServicioFalso servicio = new ServicioFalso();
    private final Sesion sesion = new Sesion();
    private final AtomicInteger expiraciones = new AtomicInteger();

    private void conSesion() {
        sesion.iniciar("admin", new DatosSesion(ServicioFalso.TOKEN, "Administrador"));
    }

    // ================================================================== ECUD1

    @Nested
    class Login {

        private final VistasFalsas.Login vista = new VistasFalsas.Login();

        @BeforeEach
        void conectar() {
            new LoginControlador(vista, servicio, sesion);
        }

        @Test
        void camposVaciosNoLlamanAlServidor() {
            vista.ingresar(" ", "x");
            assertEquals("Ingrese el usuario", vista.error);
            vista.ingresar("admin", "");
            assertEquals("Ingrese la clave", vista.error);
            assertTrue(servicio.llamadas.isEmpty());
            assertFalse(vista.menuAbierto);
        }

        @Test
        void credencialesCorrectasAbrenElMenu() {
            vista.ingresar(" admin ", "admin123");

            assertEquals(List.of("login admin admin123"), servicio.llamadas);
            assertTrue(vista.menuAbierto);
            assertTrue(sesion.estaActiva());
            assertEquals("Administrador", sesion.getNombre());
            assertEquals(ServicioFalso.TOKEN, sesion.getToken());
        }

        @Test
        void credencialesIncorrectasMuestranElMensajeYBorranLaClave() {
            servicio.fallarCon("Credenciales incorrectas");
            vista.ingresar("admin", "mala");

            assertEquals("Credenciales incorrectas", vista.error);
            assertTrue(vista.claveLimpia, "ECUD1 alterno 1");
            assertFalse(vista.menuAbierto);
            assertFalse(sesion.estaActiva());
        }
    }

    // ================================================================== ECUD4 / ECUD6

    @Nested
    class Paises {

        private final VistasFalsas.Paises vista = new VistasFalsas.Paises();

        @BeforeEach
        void conectar() {
            conSesion();
            new PaisesControlador(vista, servicio, sesion, expiraciones::incrementAndGet);
        }

        @Test
        void listarSinRegionYFiltrarConRegion() {
            vista.cargar.accept("");
            assertEquals(List.of("listar"), servicio.llamadas);
            assertEquals(2, vista.paises.size());
            assertEquals(ServicioFalso.TOKEN, servicio.tokenRecibido);

            vista.cargar.accept("Europa");
            assertEquals("buscar null|null|Europa", servicio.llamadas.get(1));
            assertEquals(List.of(ServicioFalso.ALEMANIA), vista.paises);
            assertEquals("Europa", vista.region);

            vista.cargar.accept("Todas");
            assertEquals("listar", servicio.llamadas.get(2), "«Todas» quita el filtro");
        }

        @Test
        void actualizarYEliminarRecarganConElFiltroActivo() {
            vista.cargar.accept("Europa");
            vista.actualizar.accept("Alemania");
            vista.eliminar.accept("Alemania");

            assertEquals(List.of("buscar null|null|Europa", "actualizar Alemania", "buscar null|null|Europa",
                    "eliminar Alemania", "buscar null|null|Europa"), servicio.llamadas);
            assertEquals(List.of("País actualizado correctamente", "País eliminado correctamente"), vista.mensajes);
        }

        @Test
        void registrarConsultaLaApiYRecarga() {
            vista.consultarApi.accept(" ecuador ");
            assertEquals(List.of(ServicioFalso.ECUADOR), vista.resultadosApi);

            vista.registrar.accept("ecuador", "Ecuador");
            assertEquals("País registrado correctamente", vista.registroCompletado);
            assertEquals(List.of("consultarApi ecuador", "registrar ecuador|Ecuador", "listar"), servicio.llamadas);
        }

        @Test
        void validacionDelNombreEnElDialogo() {
            vista.consultarApi.accept("  ");
            assertEquals("Ingrese el nombre del país", vista.errorRegistro);
            vista.consultarApi.accept("x".repeat(101));
            assertEquals("El nombre no puede superar 100 caracteres", vista.errorRegistro);
            assertTrue(servicio.llamadas.isEmpty());
        }

        @Test
        void duplicadoSeMuestraEnElDialogo() {
            servicio.fallarCon("Ya existe un país con ese nombre");
            vista.registrar.accept("ecuador", "Ecuador");
            assertEquals("Ya existe un país con ese nombre", vista.errorRegistro);
            assertNull(vista.registroCompletado);
        }

        @Test
        void tokenRechazadoCierraLaSesion() {
            servicio.rechazarToken();
            vista.cargar.accept("");

            assertEquals(1, expiraciones.get(), "ECUD8: se vuelve al inicio de sesión");
            assertFalse(sesion.estaActiva());
            assertNull(vista.error, "la aplicación muestra el aviso en el login");
        }
    }

    // ================================================================== ECUD3 / ECUD2

    @Nested
    class ConsultaApi {

        private final VistasFalsas.ConsultaApi vista = new VistasFalsas.ConsultaApi();

        @BeforeEach
        void conectar() {
            conSesion();
            new ConsultaApiControlador(vista, servicio, sesion, expiraciones::incrementAndGet);
        }

        @Test
        void consultarYGuardarMarcaElPais() {
            vista.consultar.accept("ecuador");
            assertEquals(List.of(ServicioFalso.ECUADOR), vista.resultados);

            vista.guardar.accept("ecuador", "Ecuador");
            assertEquals("País importado correctamente (registrado)", vista.mensaje);
            assertEquals("Ecuador", vista.guardado);
        }

        @Test
        void apiNoDisponibleMuestraElMensajeDelServidor() {
            servicio.fallarCon("La API REST Countries no está disponible, intente más tarde");
            vista.consultar.accept("ecuador");
            assertEquals("La API REST Countries no está disponible, intente más tarde", vista.error);
        }

        @Test
        void nombreVacioNoSeEnvia() {
            vista.consultar.accept("");
            assertEquals("Ingrese el nombre del país", vista.error);
            assertTrue(servicio.llamadas.isEmpty());
        }

        @Test
        void importarTodosMuestraElResumen() {
            vista.importarTodos.run();
            assertEquals("Importación finalizada: 250 recibidos, 240 registrados, 10 actualizados", vista.mensaje);
        }
    }

    // ================================================================== ECUD5 / ECUD7

    @Test
    void buscarEnviaLosTresCriterios() {
        conSesion();
        VistasFalsas.Busqueda vista = new VistasFalsas.Busqueda();
        new BusquedaControlador(vista, servicio, sesion, expiraciones::incrementAndGet);

        vista.buscar.accept(new Criterios("ecua", "", "América"));
        assertEquals("buscar ecua||América", servicio.llamadas.get(0));
        assertEquals("Búsqueda realizada correctamente", vista.mensaje);

        vista.buscar.accept(new Criterios("", "", "Todas"));
        assertEquals("buscar |||", servicio.llamadas.get(1).replace("buscar ||", "buscar |||"));

        vista.buscar.accept(new Criterios("x".repeat(101), "", "Todas"));
        assertEquals("El nombre no puede superar 100 caracteres", vista.error);
        assertEquals(2, servicio.llamadas.size());
    }

    @Test
    void informesYSinPaises() {
        conSesion();
        VistasFalsas.Informes vista = new VistasFalsas.Informes();
        new InformesControlador(vista, servicio, sesion, expiraciones::incrementAndGet);

        vista.generar.run();
        assertEquals("Informes generados correctamente", vista.mensaje);
        assertEquals("Alemania", vista.informe.paisMasPoblado().nombre());

        servicio.fallarCon("No hay países registrados para generar informes");
        vista.generar.run();
        assertTrue(vista.error.startsWith("No hay países registrados para generar informes"));
        assertTrue(vista.error.contains("Consultar en la API"), "ECUD7 alterno 2: sugiere guardar países");
    }
}
