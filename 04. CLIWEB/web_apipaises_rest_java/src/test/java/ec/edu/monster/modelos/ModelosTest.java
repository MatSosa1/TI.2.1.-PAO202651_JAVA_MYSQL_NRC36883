package ec.edu.monster.modelos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import ec.edu.monster.fakes.ServidorFalso;
import ec.edu.monster.modelos.dto.Formatos;
import ec.edu.monster.modelos.dto.Informe;
import ec.edu.monster.modelos.servicios.PaisService;
import ec.edu.monster.modelos.servicios.ServicioException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Cliente REST, formatos y cálculo del gráfico. */
class ModelosTest {

    private final ServidorFalso servidor = new ServidorFalso();
    private final PaisService servicio = new PaisService(servidor.cliente());

    @Test
    void sinRedORespuestaIlegible() {
        servidor.sinRed = true;
        assertEquals(ServicioException.SIN_SERVIDOR,
                assertThrows(ServicioException.class, () -> servicio.listar("tok")).getMessage());

        servidor.sinRed = false; // sin respuesta preparada: página 404 de Payara
        ServicioException error = assertThrows(ServicioException.class, () -> servicio.listar("tok"));
        assertEquals(ServicioException.RESPUESTA_INCOMPLETA, error.getMessage());
        assertFalse(error.isSesionNoValida());
    }

    @Test
    void tokenRechazadoEsSesionNoValida() {
        servidor.error("GET /Paises", 401, "Token inválido o sesión no activa");
        assertTrue(assertThrows(ServicioException.class, () -> servicio.listar("tok")).isSesionNoValida());
    }

    @Test
    void rutasMetodosYToken() throws ServicioException {
        servidor.ok("PUT /Paises/Per%C3%BA", "País actualizado correctamente", ServidorFalso.ECUADOR);
        servicio.actualizar("tok", "Perú");
        assertEquals("PUT", servidor.ultima().metodo());
        assertEquals("tok", servidor.ultima().cabeceras().get("Authorization"));
        assertNull(servidor.ultima().cuerpo());
    }

    @Test
    void formatos() {
        assertEquals("18.001.000", Formatos.poblacion(18_001_000));
        assertEquals("1 país encontrado", Formatos.total(1));
        assertEquals("3 países encontrados", Formatos.total(3));
        assertEquals("Europa", Formatos.region(" Europa "));
        assertEquals("", Formatos.region("Todas"));
        assertEquals(Formatos.INGRESE_NOMBRE_PAIS, Formatos.errorNombre(null));
        assertNull(Formatos.errorNombre("x".repeat(100)));
    }

    @Test
    void barrasProporcionalesALaRegionConMasPaises() {
        Informe informe = new Gson().fromJson("{\"paisesPorRegion\":[{\"region\":\"América\",\"cantidad\":4},"
                + "{\"region\":\"Europa\",\"cantidad\":2},{\"region\":\"Asia\",\"cantidad\":0}]}", Informe.class);

        List<Informe.Barra> barras = informe.getBarras();

        assertEquals(3, barras.size());
        assertEquals(160, barras.get(0).getAlto(), "la región con más países usa todo el alto");
        assertEquals(80, barras.get(1).getAlto());
        assertEquals(0, barras.get(2).getAlto());
        assertEquals(informe.getBaseGrafico(), barras.get(0).getY() + barras.get(0).getAlto());
        assertTrue(barras.get(0).getCentro() < barras.get(1).getCentro());
        assertTrue(new Informe().getBarras().isEmpty());
    }
}
