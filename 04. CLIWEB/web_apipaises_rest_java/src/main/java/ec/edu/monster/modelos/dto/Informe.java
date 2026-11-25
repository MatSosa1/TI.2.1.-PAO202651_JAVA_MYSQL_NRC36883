package ec.edu.monster.modelos.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Los tres informes de GET /api/Reportes (ECUD7) y las barras del gráfico de países por
 * región, calculadas aquí para dibujarlas en la JSP como SVG (sin librerías de JavaScript).
 */
public class Informe {

    /** Medidas del gráfico en unidades del viewBox del SVG. */
    public static final int ANCHO_GRAFICO = 600;
    public static final int ALTO_GRAFICO = 240;
    private static final int BASE = 200;
    private static final int ALTO_MAXIMO = 160;

    private List<ConteoRegion> paisesPorRegion;
    private Pais paisMasPoblado;
    private List<PromedioRegion> promedioPorRegion;

    public List<ConteoRegion> getPaisesPorRegion() {
        return paisesPorRegion == null ? List.of() : paisesPorRegion;
    }

    public Pais getPaisMasPoblado() {
        return paisMasPoblado;
    }

    public List<PromedioRegion> getPromedioPorRegion() {
        return promedioPorRegion == null ? List.of() : promedioPorRegion;
    }

    public int getAnchoGrafico() {
        return ANCHO_GRAFICO;
    }

    public int getAltoGrafico() {
        return ALTO_GRAFICO;
    }

    public int getBaseGrafico() {
        return BASE;
    }

    /** Una barra por región, proporcional a la región con más países. */
    public List<Barra> getBarras() {
        List<ConteoRegion> datos = getPaisesPorRegion();
        List<Barra> barras = new ArrayList<>();
        if (datos.isEmpty()) {
            return barras;
        }
        int maximo = Math.max(1, datos.stream().mapToInt(ConteoRegion::getCantidad).max().orElse(1));
        int columna = ANCHO_GRAFICO / datos.size();
        int ancho = (int) (columna * 0.6);
        for (int i = 0; i < datos.size(); i++) {
            ConteoRegion dato = datos.get(i);
            int alto = (int) Math.round((double) dato.getCantidad() / maximo * ALTO_MAXIMO);
            int x = i * columna + (columna - ancho) / 2;
            barras.add(new Barra(dato.getRegion(), dato.getCantidad(), x, BASE - alto, ancho, alto,
                    i * columna + columna / 2));
        }
        return barras;
    }

    /** Cantidad de países de una región. */
    public static class ConteoRegion {
        private String region;
        private int cantidad;

        public String getRegion() {
            return region;
        }

        public int getCantidad() {
            return cantidad;
        }
    }

    /** Promedio de población de una región. */
    public static class PromedioRegion {
        private String region;
        private long promedioPoblacion;

        public String getRegion() {
            return region;
        }

        public String getPromedioFormateado() {
            return Formatos.poblacion(promedioPoblacion);
        }
    }

    /** Rectángulo de una barra y posición de sus textos (centro de la columna). */
    public static class Barra {
        private final String region;
        private final int cantidad;
        private final int x;
        private final int y;
        private final int ancho;
        private final int alto;
        private final int centro;

        Barra(String region, int cantidad, int x, int y, int ancho, int alto, int centro) {
            this.region = region;
            this.cantidad = cantidad;
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.centro = centro;
        }

        public String getRegion() {
            return region;
        }

        public int getCantidad() {
            return cantidad;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getAncho() {
            return ancho;
        }

        public int getAlto() {
            return alto;
        }

        public int getCentro() {
            return centro;
        }
    }
}
