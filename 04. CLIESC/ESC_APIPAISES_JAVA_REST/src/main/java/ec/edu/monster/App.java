package ec.edu.monster;

import ec.edu.monster.controlador.BusquedaControlador;
import ec.edu.monster.controlador.ConsultaApiControlador;
import ec.edu.monster.controlador.EstadoControlador;
import ec.edu.monster.controlador.InformesControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisesControlador;
import ec.edu.monster.modelo.ClienteRest;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.ServiciosRest;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.modelo.TransporteHttp;
import ec.edu.monster.vista.LoginFrame;
import ec.edu.monster.vista.PrincipalFrame;
import java.util.concurrent.CompletableFuture;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Raíz de composición: crea el modelo una sola vez y conecta cada ventana con sus
 * controladores. Maneja la navegación entre el inicio de sesión y la ventana principal.
 */
public final class App {

    private static final String TITULO = "APIPaises";

    private final Sesion sesion = new Sesion();
    private final ServiciosRest servicios;
    private final EstadoServidor estado;
    private PrincipalFrame principal;

    private App(Configuracion configuracion) {
        ClienteRest cliente = new ClienteRest(configuracion.getUrlBase(), new TransporteHttp());
        this.servicios = new ServiciosRest(cliente);
        this.estado = new EstadoServidor(cliente.getUrlBase());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::iniciar);
    }

    private static void iniciar() {
        Configuracion configuracion;
        try {
            configuracion = Configuracion.cargar();
        } catch (RuntimeException e) {
            error(e.getMessage());
            return;
        }
        new App(configuracion).mostrarLogin(null);
    }

    /** @param aviso mensaje a mostrar al abrir (sesión no válida), o null */
    private void mostrarLogin(String aviso) {
        LoginFrame login = new LoginFrame(this::mostrarPrincipal);
        new LoginControlador(login, servicios, sesion);
        new EstadoControlador(login.getIndicadorEstado(), estado);
        login.setVisible(true);
        if (aviso != null) {
            login.mostrarError(aviso);
        }
    }

    private void mostrarPrincipal() {
        principal = new PrincipalFrame(sesion.getNombre(), this::cerrarSesion);
        new PaisesControlador(principal.getPaisesPanel(), servicios, sesion, this::sesionNoValida);
        new ConsultaApiControlador(principal.getConsultaApiPanel(), servicios, sesion, this::sesionNoValida);
        new BusquedaControlador(principal.getBusquedaPanel(), servicios, sesion, this::sesionNoValida);
        new InformesControlador(principal.getInformesPanel(), servicios, sesion, this::sesionNoValida);
        new EstadoControlador(principal.getIndicadorEstado(), estado);
        principal.setVisible(true);
    }

    /** ECUD1 alterno 4: se borra la sesión y se vuelve al inicio aunque el servidor no responda. */
    private void cerrarSesion() {
        String token = sesion.getToken();
        sesion.cerrar();
        CompletableFuture.runAsync(() -> {
            try {
                servicios.cerrarSesion(token);
            } catch (ServicioException e) {
                // El token se descarta igual en el cliente
            }
        });
        principal = null;
        mostrarLogin(null);
    }

    /** ECUD8 alterno 2: el token ya no vale; se cierra la ventana principal y se vuelve al login. */
    private void sesionNoValida() {
        SwingUtilities.invokeLater(() -> {
            if (principal == null) {
                return;
            }
            principal.dispose();
            principal = null;
            mostrarLogin(ServicioException.SESION_NO_VALIDA);
        });
    }

    private static void error(String mensaje) {
        JOptionPane.showOptionDialog(null, mensaje, TITULO, JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, new Object[] {"Aceptar"}, "Aceptar");
    }
}
