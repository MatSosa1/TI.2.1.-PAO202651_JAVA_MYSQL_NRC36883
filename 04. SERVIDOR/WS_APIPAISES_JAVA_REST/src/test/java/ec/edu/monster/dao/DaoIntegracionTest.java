package ec.edu.monster.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ec.edu.monster.modelos.dto.ConteoRegion;
import ec.edu.monster.modelos.dto.PromedioRegion;
import ec.edu.monster.modelos.dto.ResumenImportacion;
import ec.edu.monster.modelos.entidades.Pais;
import ec.edu.monster.modelos.entidades.Usuario;
import ec.edu.monster.servicios.HashUtil;
import ec.edu.monster.utilidades.Textos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los DAO contra la base real de 03. BDD (docker compose up -d). Si MySQL no
 * está disponible, se omiten. Solo tocan países cuyo nombre empieza por «ZZ Prueba» y los
 * borran al terminar, así que no alteran los datos importados.
 */
class DaoIntegracionTest {

    private static final String PREFIJO = "ZZ Prueba";

    private static final ConexionBD CONEXION = new ConexionBD();
    private final PaisDAO paisDAO = new PaisDAO(CONEXION);
    private final RegionDAO regionDAO = new RegionDAO(CONEXION);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO(CONEXION);

    @BeforeAll
    static void requiereBaseDeDatos() {
        try (Connection conexion = CONEXION.obtenerConexion()) {
            assumeTrue(conexion.isValid(2));
        } catch (SQLException e) {
            assumeTrue(false, "MySQL no disponible: " + e.getMessage());
        }
    }

    @AfterEach
    void borrarPaisesDePrueba() throws SQLException {
        try (Connection conexion = CONEXION.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement("delete from Paises where Nombre like ?")) {
            sentencia.setString(1, PREFIJO + "%");
            sentencia.executeUpdate();
        }
    }

    private static Pais pais(String sufijo, String capital, String region, long poblacion) {
        return new Pais(PREFIJO + " " + sufijo, capital, region, poblacion, "Moneda de prueba (XXX)");
    }

    // ------------------------------------------------------------------ Usuarios y regiones

    @Test
    void buscaUsuarioDeInitSqlYSuHashCoincide() throws SQLException {
        Usuario admin = usuarioDAO.buscarPorUsuario("admin");

        assertNotNull(admin);
        assertEquals("Administrador", admin.getNombre());
        assertTrue(new HashUtil().coincide("admin123", admin.getClaveHash()));
        assertNull(usuarioDAO.buscarPorUsuario("no-existe"));
    }

    @Test
    void regionesDelCatalogo() throws SQLException {
        assertEquals(6, regionDAO.listar().size());
        assertTrue(regionDAO.existe("Europa"));
        assertTrue(regionDAO.existe("europa"), "sin distinguir mayúsculas");
        assertTrue(regionDAO.existe("Africa"), "sin distinguir tildes");
        assertFalse(regionDAO.existe("Marte"));
        assertEquals(-1, regionDAO.obtenerIdPorNombre(""));
    }

    // ------------------------------------------------------------------ Mantenimiento (ECUD4)

    @Test
    void registrarActualizarEliminar() throws SQLException {
        Pais pais = pais("Uno", "Capital uno", "Europa", 100);
        paisDAO.registrar(pais);
        assertTrue(paisDAO.existe(pais.getNombre()));

        assertThrows(SQLIntegrityConstraintViolationException.class,
                () -> paisDAO.registrar(pais("UNO", "Otra", "Asia", 1)), "nombre repetido: 409 en el controlador");

        pais.setCapital("Capital nueva");
        pais.setRegion("Asia");
        pais.setPoblacion(200);
        assertEquals(1, paisDAO.actualizar(pais));
        Pais leido = paisDAO.buscar(pais.getNombre(), null, null).get(0);
        assertEquals("Capital nueva", leido.getCapital());
        assertEquals("Asia", leido.getRegion());
        assertEquals(200, leido.getPoblacion());

        assertEquals(1, paisDAO.actualizar(pais), "sin cambios sigue contando la fila encontrada");
        assertEquals(1, paisDAO.eliminar(pais.getNombre()));
        assertEquals(0, paisDAO.eliminar(pais.getNombre()), "país inexistente: 404 en el controlador");
        assertEquals(0, paisDAO.actualizar(pais));
    }

    @Test
    void registrarConRegionInvalidaFalla() {
        assertThrows(IllegalArgumentException.class, () -> paisDAO.registrar(pais("Dos", "X", "Marte", 1)));
    }

    // ------------------------------------------------------------------ Búsqueda (ECUD5, ECUD6)

    @Test
    void buscarPorNombreCapitalYRegion() throws SQLException {
        paisDAO.registrar(pais("Andino", "San Francisco de Quito", "América", 10));
        paisDAO.registrar(pais("Europeo", "Lisboa", "Europa", 20));
        paisDAO.registrar(pais("Asiatico", "Tokio", "Asia", 30));

        assertEquals(3, paisDAO.buscar(PREFIJO, null, null).size());
        assertEquals(1, paisDAO.buscar(null, "quito", null).size(), "coincidencia parcial sin mayúsculas");
        assertEquals(1, paisDAO.buscar(PREFIJO, null, "america").size(), "región sin tilde");
        assertEquals(0, paisDAO.buscar(PREFIJO, "lisboa", "Asia").size(), "todos los criterios a la vez");
        assertEquals(0, paisDAO.buscar("%", null, null).stream()
                .filter(p -> p.getNombre().startsWith(PREFIJO)).count(), "el comodín del usuario se escapa");

        List<Pais> ordenados = paisDAO.buscar(PREFIJO, null, null);
        assertEquals(PREFIJO + " Andino", ordenados.get(0).getNombre(), "ordenados por nombre");
    }

    // ------------------------------------------------------------------ Importación (ECUD2)

    @Test
    void importarRegistraYLuegoActualiza() throws SQLException {
        Pais pais = pais("Importado", "Capital", "Oceanía", 5);
        assertEquals(PaisDAO.REGISTRADO, paisDAO.importar(pais));

        pais.setPoblacion(6);
        assertEquals(PaisDAO.ACTUALIZADO, paisDAO.importar(pais));
        assertEquals(6, paisDAO.buscar(pais.getNombre(), null, null).get(0).getPoblacion());
    }

    @Test
    void importarTodosSinDuplicarYEnUnaTransaccion() throws SQLException {
        paisDAO.registrar(pais("Existente", "Vieja", "Europa", 1));

        ResumenImportacion resumen = paisDAO.importarTodos(List.of(
                pais("Existente", "Nueva", "Europa", 2),
                pais("Nuevo", "Capital", "África", 3),
                pais("nuevo", "Repetido por la collation", "África", 4)));

        assertEquals(3, resumen.getRecibidos());
        assertEquals(1, resumen.getRegistrados());
        assertEquals(1, resumen.getActualizados());
        assertEquals("Nueva", paisDAO.buscar(PREFIJO + " Existente", null, null).get(0).getCapital());
        assertEquals(2, paisDAO.buscar(PREFIJO, null, null).size());
    }

    @Test
    void importarTodosDeshaceTodoSiFallaUnPais() throws SQLException {
        String demasiadoLargo = "x".repeat(150); // no cabe en VARCHAR(100)
        List<Pais> lote = List.of(pais("Valido", "Capital", "Asia", 1),
                new Pais(PREFIJO + " Invalido", demasiadoLargo, "Asia", 1, "M"));

        assertThrows(SQLException.class, () -> paisDAO.importarTodos(lote));
        assertFalse(paisDAO.existe(PREFIJO + " Valido"), "ECUD2, alterno 5: nada queda a medias");
    }

    @Test
    void clavesGuardadasParaMarcarLosPaisesDeLaApi() throws SQLException {
        paisDAO.registrar(pais("Marcado", "C", "Asia", 1));
        assertTrue(paisDAO.clavesGuardadas().contains(Textos.clave(PREFIJO + " MARCADO")));
    }

    // ------------------------------------------------------------------ Informes (ECUD7)

    @Test
    void informesIncluyenLasSeisRegionesYElMasPoblado() throws SQLException {
        paisDAO.registrar(pais("Gigante", "C", "Antártida", 9_000_000_000_000L));
        paisDAO.registrar(pais("Gigante bis", "C", "Antártida", 9_000_000_000_000L));

        List<ConteoRegion> conteos = paisDAO.contarPorRegion();
        assertEquals(6, conteos.size(), "regiones sin países incluidas");
        for (int i = 1; i < conteos.size(); i++) {
            assertTrue(conteos.get(i - 1).getCantidad() >= conteos.get(i).getCantidad(), "de mayor a menor");
        }

        Pais masPoblado = paisDAO.paisMasPoblado();
        assertEquals(PREFIJO + " Gigante", masPoblado.getNombre(), "empate: el primero por nombre");
        assertEquals("Antártida", masPoblado.getRegion());

        List<PromedioRegion> promedios = paisDAO.promedioPorRegion();
        assertEquals(6, promedios.size());
        assertTrue(promedios.stream().anyMatch(p -> p.getRegion().equals("Antártida")
                && p.getPromedioPoblacion() >= 9_000_000_000_000L / 2));
        assertTrue(paisDAO.contar() >= 2);
    }
}
