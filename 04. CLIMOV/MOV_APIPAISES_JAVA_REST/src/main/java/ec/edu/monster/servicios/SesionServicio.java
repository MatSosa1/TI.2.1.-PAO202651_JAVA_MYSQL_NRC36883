package ec.edu.monster.servicios;

import ec.edu.monster.modelos.data.Datos.Sesion;

/** Guarda el token y el nombre del usuario mientras dura la sesión (ECUD1, paso 7). */
public class SesionServicio {

    private volatile Sesion sesion;

    public Sesion getSesion() {
        return sesion;
    }

    public String getToken() {
        Sesion actual = sesion;
        return actual != null ? actual.getToken() : null;
    }

    public boolean estaAutenticado() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void iniciar(Sesion nueva) {
        sesion = nueva;
    }

    public void limpiar() {
        sesion = null;
    }
}
