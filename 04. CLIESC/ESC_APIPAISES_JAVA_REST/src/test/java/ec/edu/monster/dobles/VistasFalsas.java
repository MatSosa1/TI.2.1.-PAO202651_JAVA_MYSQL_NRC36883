package ec.edu.monster.dobles;

import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.vista.Vistas;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Vistas en memoria: guardan lo que el controlador les mostró y simulan las acciones del usuario. */
public final class VistasFalsas {

    private VistasFalsas() {
    }

    public static class Login implements Vistas.LoginVista {
        private Runnable ingresar = () -> { };
        public String usuario = "";
        public String clave = "";
        public String error;
        public boolean claveLimpia;
        public boolean menuAbierto;

        public void ingresar(String usuario, String clave) {
            this.usuario = usuario;
            this.clave = clave;
            ingresar.run();
        }

        @Override
        public String getUsuario() {
            return usuario;
        }

        @Override
        public String getClave() {
            return clave;
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void limpiarClave() {
            claveLimpia = true;
        }

        @Override
        public void abrirMenu() {
            menuAbierto = true;
        }

        @Override
        public void alIngresar(Runnable accion) {
            ingresar = accion;
        }
    }

    public static class Paises implements Vistas.PaisesVista {
        public Consumer<String> cargar;
        public Consumer<String> actualizar;
        public Consumer<String> eliminar;
        public Consumer<String> consultarApi;
        public BiConsumer<String, String> registrar;
        public List<Pais> paises;
        public String region;
        public List<Pais> resultadosApi;
        public final List<String> mensajes = new ArrayList<>();
        public String error;
        public String errorRegistro;
        public String registroCompletado;

        @Override
        public void alCargar(Consumer<String> accion) {
            cargar = accion;
        }

        @Override
        public void alActualizar(Consumer<String> accion) {
            actualizar = accion;
        }

        @Override
        public void alEliminar(Consumer<String> accion) {
            eliminar = accion;
        }

        @Override
        public void alConsultarApi(Consumer<String> accion) {
            consultarApi = accion;
        }

        @Override
        public void alRegistrar(BiConsumer<String, String> accion) {
            registrar = accion;
        }

        @Override
        public void mostrarPaises(List<Pais> paises, String region) {
            this.paises = paises;
            this.region = region;
        }

        @Override
        public void mostrarResultadosApi(List<Pais> paises) {
            resultadosApi = paises;
        }

        @Override
        public void registroCompletado(String mensaje) {
            registroCompletado = mensaje;
        }

        @Override
        public void mostrarMensaje(String mensaje) {
            mensajes.add(mensaje);
        }

        @Override
        public void mostrarError(String mensaje) {
            error = mensaje;
        }

        @Override
        public void mostrarErrorRegistro(String mensaje) {
            errorRegistro = mensaje;
        }
    }

    public static class ConsultaApi implements Vistas.ConsultaApiVista {
        public Consumer<String> consultar;
        public BiConsumer<String, String> guardar;
        public Runnable importarTodos;
        public List<Pais> resultados;
        public String guardado;
        public String mensaje;
        public String error;

        @Override
        public void alConsultar(Consumer<String> accion) {
            consultar = accion;
        }

        @Override
        public void alGuardar(BiConsumer<String, String> accion) {
            guardar = accion;
        }

        @Override
        public void alImportarTodos(Runnable accion) {
            importarTodos = accion;
        }

        @Override
        public void mostrarResultados(List<Pais> paises) {
            resultados = paises;
        }

        @Override
        public void marcarGuardado(String nombre) {
            guardado = nombre;
        }

        @Override
        public void mostrarMensaje(String texto) {
            mensaje = texto;
        }

        @Override
        public void mostrarError(String texto) {
            error = texto;
        }
    }

    public static class Busqueda implements Vistas.BusquedaVista {
        public Consumer<Criterios> buscar;
        public List<Pais> paises;
        public String mensaje;
        public String error;

        @Override
        public void alBuscar(Consumer<Criterios> accion) {
            buscar = accion;
        }

        @Override
        public void mostrarResultado(List<Pais> paises, String texto) {
            this.paises = paises;
            mensaje = texto;
        }

        @Override
        public void mostrarError(String texto) {
            error = texto;
        }
    }

    public static class Informes implements Vistas.InformesVista {
        public Runnable generar;
        public Informe informe;
        public String mensaje;
        public String error;

        @Override
        public void alGenerar(Runnable accion) {
            generar = accion;
        }

        @Override
        public void mostrarInforme(Informe datos, String texto) {
            informe = datos;
            mensaje = texto;
        }

        @Override
        public void mostrarError(String texto) {
            error = texto;
        }
    }
}
