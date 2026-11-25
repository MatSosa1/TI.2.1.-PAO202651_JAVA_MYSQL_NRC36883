package ec.edu.monster.modelos.dto;

import java.util.List;

/** Datos de las respuestas del servidor que no son un país (Gson los llena por nombre de campo). */
public final class Resultados {

    private Resultados() {
    }

    /** ECUD5 y ECUD6: países encontrados y total. */
    public static class Busqueda {
        private List<Pais> paises;
        private int total;

        public List<Pais> getPaises() {
            return paises == null ? List.of() : paises;
        }

        public int getTotal() {
            return total;
        }
    }

    /** ECUD2: país importado y si fue «registrado» o «actualizado». */
    public static class Importacion {
        private Pais pais;
        private String accion;

        public Pais getPais() {
            return pais;
        }

        public String getAccion() {
            return accion;
        }
    }

    /** «Importar todos»: países recibidos de la API, registrados y actualizados. */
    public static class Resumen {
        private int recibidos;
        private int registrados;
        private int actualizados;

        public int getRecibidos() {
            return recibidos;
        }

        public int getRegistrados() {
            return registrados;
        }

        public int getActualizados() {
            return actualizados;
        }
    }
}
