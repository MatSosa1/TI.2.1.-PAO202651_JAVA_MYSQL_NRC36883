package ec.edu.monster.controlador;

import ec.edu.monster.modelo.DatosSesion;
import ec.edu.monster.modelo.Formatos;
import ec.edu.monster.modelo.LoginServicio;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.Vistas.LoginVista;

/** ECUD1 Iniciar Sesión. */
public class LoginControlador {

    private final LoginVista vista;
    private final LoginServicio servicio;
    private final Sesion sesion;

    public LoginControlador(LoginVista vista, LoginServicio servicio, Sesion sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
        vista.alIngresar(this::ingresar);
    }

    private void ingresar() {
        String usuario = vista.getUsuario();
        String clave = vista.getClave();
        // ECUD1 alterno 2: con un campo vacío no se envía la petición
        if (usuario == null || usuario.isBlank()) {
            vista.mostrarError(Formatos.INGRESE_USUARIO);
            return;
        }
        if (clave == null || clave.isEmpty()) {
            vista.mostrarError(Formatos.INGRESE_CLAVE);
            return;
        }
        try {
            DatosSesion datos = servicio.iniciarSesion(usuario.trim(), clave);
            sesion.iniciar(usuario.trim(), datos);
            vista.abrirMenu();
        } catch (ServicioException e) {
            // ECUD1 alternos 1 y 3: se muestra el mensaje y se borra la clave
            vista.limpiarClave();
            vista.mostrarError(e.getMessage());
        }
    }
}
