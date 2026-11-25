package ec.edu.monster.modelo;

/** Datos de la sesión abierta: el token se guarda mientras dure la sesión (ECUD1, paso 7). */
public class Sesion {

    private String usuario;
    private String nombre;
    private String token;

    public void iniciar(String usuario, DatosSesion datos) {
        this.usuario = usuario;
        this.nombre = datos.usuario();
        this.token = datos.token();
    }

    public void cerrar() {
        usuario = null;
        nombre = null;
        token = null;
    }

    public boolean estaActiva() {
        return token != null && !token.isBlank();
    }

    public String getUsuario() {
        return usuario;
    }

    /** Nombre que devolvió el servidor, o el usuario si no vino. */
    public String getNombre() {
        return nombre == null || nombre.isBlank() ? usuario : nombre;
    }

    public String getToken() {
        return token;
    }
}
