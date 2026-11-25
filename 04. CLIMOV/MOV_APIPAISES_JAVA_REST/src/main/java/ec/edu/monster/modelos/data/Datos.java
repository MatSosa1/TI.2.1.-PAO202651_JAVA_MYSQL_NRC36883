package ec.edu.monster.modelos.data;

import java.util.Collections;
import java.util.List;

/**
 * Datos que devuelve el servidor REST, con los mismos nombres de campo que su JSON (Gson los
 * llena directamente). Clases simples para Android 7 (minSdk 24).
 */
public final class Datos {

    private Datos() {
    }

    /** País guardado o consultado; {@code guardado} solo llega en la consulta a la API (ECUD3). */
    public static class Pais {
        private String nombre;
        private String capital;
        private String region;
        private long poblacion;
        private String moneda;
        private boolean guardado;

        public Pais() {
        }

        public Pais(String nombre, String capital, String region, long poblacion, String moneda, boolean guardado) {
            this.nombre = nombre;
            this.capital = capital;
            this.region = region;
            this.poblacion = poblacion;
            this.moneda = moneda;
            this.guardado = guardado;
        }

        public String getNombre() {
            return nombre;
        }

        public String getCapital() {
            return capital;
        }

        public String getRegion() {
            return region;
        }

        public long getPoblacion() {
            return poblacion;
        }

        public String getMoneda() {
            return moneda;
        }

        public boolean isGuardado() {
            return guardado;
        }

        public void setGuardado(boolean guardado) {
            this.guardado = guardado;
        }
    }

    /** Datos de un inicio de sesión correcto: el token y el nombre del usuario (ECUD1 paso 6). */
    public static class Sesion {
        private String token;
        private String usuario;

        public Sesion() {
        }

        public Sesion(String token, String usuario) {
            this.token = token;
            this.usuario = usuario;
        }

        public String getToken() {
            return token;
        }

        public String getNombre() {
            return usuario;
        }
    }

    /** ECUD5 y ECUD6: países encontrados y total. */
    public static class Busqueda {
        private List<Pais> paises;
        private int total;

        public List<Pais> getPaises() {
            return paises == null ? Collections.emptyList() : paises;
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

    /** «Importar todos»: recibidos de la API, registrados y actualizados. */
    public static class Resumen {
        private int recibidos;
        private int registrados;
        private int actualizados;

        public Resumen() {
        }

        public Resumen(int recibidos, int registrados, int actualizados) {
            this.recibidos = recibidos;
            this.registrados = registrados;
            this.actualizados = actualizados;
        }

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

    /** ECUD7: los tres informes. */
    public static class Informe {
        private List<ConteoRegion> paisesPorRegion;
        private Pais paisMasPoblado;
        private List<PromedioRegion> promedioPorRegion;

        public List<ConteoRegion> getPaisesPorRegion() {
            return paisesPorRegion == null ? Collections.emptyList() : paisesPorRegion;
        }

        public Pais getPaisMasPoblado() {
            return paisMasPoblado;
        }

        public List<PromedioRegion> getPromedioPorRegion() {
            return promedioPorRegion == null ? Collections.emptyList() : promedioPorRegion;
        }
    }

    public static class ConteoRegion {
        private String region;
        private int cantidad;

        public ConteoRegion() {
        }

        public ConteoRegion(String region, int cantidad) {
            this.region = region;
            this.cantidad = cantidad;
        }

        public String getRegion() {
            return region;
        }

        public int getCantidad() {
            return cantidad;
        }
    }

    public static class PromedioRegion {
        private String region;
        private long promedioPoblacion;

        public String getRegion() {
            return region;
        }

        public long getPromedioPoblacion() {
            return promedioPoblacion;
        }
    }
}
