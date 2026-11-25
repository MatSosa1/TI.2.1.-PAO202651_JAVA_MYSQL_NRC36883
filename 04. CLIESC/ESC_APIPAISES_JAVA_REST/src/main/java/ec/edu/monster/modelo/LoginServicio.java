package ec.edu.monster.modelo;

/** Iniciar y cerrar sesión (ECUD1). */
public interface LoginServicio {

    DatosSesion iniciarSesion(String usuario, String clave) throws ServicioException;

    void cerrarSesion(String token) throws ServicioException;
}
