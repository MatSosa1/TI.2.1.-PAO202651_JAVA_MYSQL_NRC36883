package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Datos.Sesion;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.servicios.SesionServicio;

/** Base de los controladores que trabajan con la sesión abierta. */
public abstract class ControladorConSesion {

    public static final String MENSAJE_SESION_NO_VALIDA = "Su sesión no es válida. Inicie sesión nuevamente.";

    protected final SesionServicio sesion;

    protected ControladorConSesion(SesionServicio sesion) {
        this.sesion = sesion;
    }

    protected String getToken() {
        return sesion.getToken();
    }

    public boolean haySesion() {
        return sesion.estaAutenticado();
    }

    public Sesion getSesion() {
        return sesion.getSesion();
    }

    /** ECUD8 alterno 2: si el servidor rechazó el token, se borra la sesión local. */
    protected <T> Resultado<T> revisarSesion(Resultado<T> resultado) {
        if (resultado.isSesionExpirada()) {
            sesion.limpiar();
        }
        return resultado;
    }
}
