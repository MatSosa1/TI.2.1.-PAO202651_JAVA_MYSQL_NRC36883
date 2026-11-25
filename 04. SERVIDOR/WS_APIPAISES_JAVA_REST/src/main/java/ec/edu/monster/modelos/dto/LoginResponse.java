package ec.edu.monster.modelos.dto;

/** Datos de un inicio de sesión correcto: el token y el nombre del usuario (ECUD1, paso 6). */
public class LoginResponse {

    private String token;
    private String usuario;

    public LoginResponse() {
    }

    public LoginResponse(String token, String usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
