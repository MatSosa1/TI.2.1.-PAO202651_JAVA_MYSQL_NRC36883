package ec.edu.monster.modelos.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class RespuestaEstructuradaTest {

    @Test
    void okLlevaLosDatos() {
        RespuestaEstructurada<List<String>> respuesta = RespuestaEstructurada.ok("Hecho", List.of("a"));

        assertEquals("OK", respuesta.getEstado());
        assertEquals("Hecho", respuesta.getMensaje());
        assertEquals(List.of("a"), respuesta.getDatos());
    }

    @Test
    void errorDejaLosDatosVacios() {
        RespuestaEstructurada<Object> respuesta = RespuestaEstructurada.error("Falló");

        assertEquals("ERROR", respuesta.getEstado());
        assertEquals("Falló", respuesta.getMensaje());
        assertNull(respuesta.getDatos(), "ECUD10, paso 4: con ERROR los datos quedan vacíos");
    }
}
