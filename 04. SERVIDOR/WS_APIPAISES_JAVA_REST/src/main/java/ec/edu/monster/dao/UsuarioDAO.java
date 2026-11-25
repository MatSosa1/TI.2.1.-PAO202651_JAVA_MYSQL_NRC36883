package ec.edu.monster.dao;

import ec.edu.monster.modelos.entidades.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a la tabla Usuarios (ECUD1, paso 5). */
public class UsuarioDAO {

    private static final String BUSCAR =
            "select IdUsuario, Usuario, ClaveHash, Nombre from Usuarios where Usuario = ?";

    private final ConexionBD conexionBD;

    public UsuarioDAO() {
        this(new ConexionBD());
    }

    public UsuarioDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /** Usuario registrado con ese nombre de usuario, o null si no existe. */
    public Usuario buscarPorUsuario(String usuario) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(BUSCAR)) {
            sentencia.setString(1, usuario);
            try (ResultSet fila = sentencia.executeQuery()) {
                if (!fila.next()) {
                    return null;
                }
                return new Usuario(fila.getInt("IdUsuario"), fila.getString("Usuario"),
                        fila.getString("ClaveHash"), fila.getString("Nombre"));
            }
        }
    }
}
