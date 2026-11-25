package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.Datos.Pais;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contratos entre las pantallas y sus controladores. Las pantallas registran las acciones del
 * usuario («al…») y los controladores responden con «mostrar…». Las implementaciones Swing
 * actualizan la interfaz en el EDT; las pruebas usan implementaciones falsas.
 */
public final class Vistas {

    private Vistas() {
    }

    /** ECUD1: formulario de inicio de sesión. */
    public interface LoginVista {
        String getUsuario();

        String getClave();

        void mostrarError(String mensaje);

        /** ECUD1 alterno 1: se borra la clave y se mantiene el formulario. */
        void limpiarClave();

        void abrirMenu();

        void alIngresar(Runnable accion);
    }

    /** Indicador «Servicio disponible / no disponible». */
    public interface EstadoVista {
        void mostrarEstado(boolean disponible);

        void alRevisarEstado(Runnable accion);
    }

    /** ECUD4 Gestionar Países y ECUD6 Filtrar. */
    public interface PaisesVista {
        /** Cargar la tabla con la región indicada ("" = todas). */
        void alCargar(Consumer<String> accion);

        void alActualizar(Consumer<String> nombre);

        void alEliminar(Consumer<String> nombre);

        /** Diálogo «Nuevo»: consultar en la API (ECUD4 Registrar, pasos 2 y 3). */
        void alConsultarApi(Consumer<String> consulta);

        /** Diálogo «Nuevo»: registrar el país elegido (consulta, nombre). */
        void alRegistrar(BiConsumer<String, String> accion);

        void mostrarPaises(List<Pais> paises, String region);

        void mostrarResultadosApi(List<Pais> paises);

        void registroCompletado(String mensaje);

        void mostrarMensaje(String mensaje);

        void mostrarError(String mensaje);

        void mostrarErrorRegistro(String mensaje);
    }

    /** ECUD3 Consultar país en la API y ECUD2 Importar. */
    public interface ConsultaApiVista {
        void alConsultar(Consumer<String> nombre);

        /** Guardar el país elegido (consulta, nombre). */
        void alGuardar(BiConsumer<String, String> accion);

        void alImportarTodos(Runnable accion);

        void mostrarResultados(List<Pais> paises);

        void marcarGuardado(String nombre);

        void mostrarMensaje(String mensaje);

        void mostrarError(String mensaje);
    }

    /** ECUD5 Buscar Países (y ECUD6 al cambiar la región). */
    public interface BusquedaVista {
        record Criterios(String nombre, String capital, String region) {
        }

        void alBuscar(Consumer<Criterios> accion);

        void mostrarResultado(List<Pais> paises, String mensaje);

        void mostrarError(String mensaje);
    }

    /** ECUD7 Generar Informes Estadísticos. */
    public interface InformesVista {
        void alGenerar(Runnable accion);

        void mostrarInforme(Informe informe, String mensaje);

        void mostrarError(String mensaje);
    }
}
