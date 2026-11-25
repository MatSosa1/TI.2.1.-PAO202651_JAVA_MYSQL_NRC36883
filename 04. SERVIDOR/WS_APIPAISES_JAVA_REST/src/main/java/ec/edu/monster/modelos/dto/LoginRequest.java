package ec.edu.monster.modelos.dto;

/** Cuerpo de POST /api/Login/login. */
public class LoginRequest {

    private String usuario;
    private String clave;

    public LoginRequest() {
    }

    public LoginRequest(String usuario, String clave) {
        this.usuario = usuario;
        this.clave = clave;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
