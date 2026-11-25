package ec.edu.monster.modelos.servicios;

import java.util.Map;

/** Iniciar y cerrar sesión en el servidor (ECUD1). Única clase que conoce /api/Login. */
public class LoginService {

    /** Datos de un inicio de sesión correcto: el token y el nombre del usuario. */
    public static class Sesion {
        private String token;
        private String usuario;

        public String getToken() {
            return token;
        }

        public String getUsuario() {
            return usuario;
        }
    }

    private final ClienteRest cliente;

    public LoginService() {
        this(new ClienteRest());
    }

    public LoginService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public Sesion iniciarSesion(String usuario, String clave) throws ServicioException {
        Resultado<Sesion> resultado = cliente.enviar("POST", "/Login/login", null,
                Map.of("usuario", usuario, "clave", clave), Sesion.class, ClienteRest.ESPERA);
        if (resultado.datos() == null || resultado.datos().getToken() == null) {
            throw new ServicioException(ServicioException.RESPUESTA_INCOMPLETA);
        }
        return resultado.datos();
    }

    /** El cliente cierra su sesión aunque el servidor no responda (ECUD1, alterno 4). */
    public void cerrarSesion(String token) {
        try {
            cliente.enviar("POST", "/Login/logout", token, null, Object.class, ClienteRest.ESPERA);
        } catch (ServicioException e) {
            // Sin servidor o token ya vencido: la sesión local se borra igual
        }
    }
}
