package ec.edu.monster.modelos.entidades;

/** Fila de la tabla Usuarios. La clave nunca se guarda en claro (RNF-006). */
public class Usuario {

    private int idUsuario;
    private String usuario;
    private String claveHash;
    private String nombre;

    public Usuario() {
    }

    public Usuario(int idUsuario, String usuario, String claveHash, String nombre) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.claveHash = claveHash;
        this.nombre = nombre;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
