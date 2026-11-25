package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import java.util.function.Consumer;

/**
 * Base de los controladores que trabajan con la sesión abierta. Si el servidor rechaza el
 * token (ECUD8), se borra la sesión y se avisa a la aplicación para volver al inicio de sesión.
 */
abstract class ControladorConSesion {

    /** Operación que llama al servidor. */
    @FunctionalInterface
    interface Operacion {
        void ejecutar() throws ServicioException;
    }

    protected final Sesion sesion;
    private final Runnable alSesionNoValida;

    protected ControladorConSesion(Sesion sesion, Runnable alSesionNoValida) {
        this.sesion = sesion;
        this.alSesionNoValida = alSesionNoValida;
    }

    protected void ejecutar(Operacion operacion, Consumer<String> mostrarError) {
        try {
            operacion.ejecutar();
        } catch (ServicioException e) {
            if (e.isSesionNoValida()) {
                sesion.cerrar();
                alSesionNoValida.run();
            } else {
                mostrarError.accept(e.getMessage());
            }
        }
    }
}
