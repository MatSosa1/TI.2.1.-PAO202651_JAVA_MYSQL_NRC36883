package ec.edu.monster.dao;

import ec.edu.monster.modelos.entidades.Region;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso al catálogo Regiones. La búsqueda por nombre no distingue mayúsculas ni tildes
 * (collation de la base), así que «europa» y «Europa» son la misma región.
 */
public class RegionDAO {

    private static final String ID_POR_NOMBRE = "select IdRegion from Regiones where Nombre = ?";
    private static final String LISTAR = "select IdRegion, Nombre from Regiones order by IdRegion";

    private final ConexionBD conexionBD;

    public RegionDAO() {
        this(new ConexionBD());
    }

    public RegionDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    /** true si la región está en el catálogo (ECUD5 y ECUD6, «Región no válida»). */
    public boolean existe(String nombre) throws SQLException {
        return obtenerIdPorNombre(nombre) > 0;
    }

    /** Id de la región, o -1 si no existe. */
    public int obtenerIdPorNombre(String nombre) throws SQLException {
        try (Connection conexion = conexionBD.obtenerConexion()) {
            return idPorNombre(conexion, nombre);
        }
    }

    public List<Region> listar() throws SQLException {
        List<Region> regiones = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
                ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                regiones.add(new Region(fila.getInt("IdRegion"), fila.getString("Nombre")));
            }
        }
        return regiones;
    }

    /** Versión que reutiliza una conexión abierta (por ejemplo, dentro de una transacción de PaisDAO). */
    static int idPorNombre(Connection conexion, String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank()) {
            return -1;
        }
        try (PreparedStatement sentencia = conexion.prepareStatement(ID_POR_NOMBRE)) {
            sentencia.setString(1, nombre.trim());
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next() ? fila.getInt(1) : -1;
            }
        }
    }
}
