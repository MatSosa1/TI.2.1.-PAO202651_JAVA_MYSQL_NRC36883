package ec.edu.monster.dao;

import ec.edu.monster.config.Configuracion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conexiones JDBC a APIPaisesDB. Cada operación del DAO abre su conexión y la cierra al
 * terminar (try-with-resources).
 */
public class ConexionBD {

    private static final Logger LOG = Logger.getLogger(ConexionBD.class.getName());

    static {
        // Dentro de Payara el driver viene en el war; se registra explícitamente porque
        // DriverManager no siempre lo descubre desde el classloader de la aplicación.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "No se encontró el driver de MySQL (mysql-connector-j)", e);
        }
    }

    private final String url;
    private final Properties propiedades = new Properties();

    public ConexionBD() {
        this(Configuracion.get());
    }

    public ConexionBD(Configuracion configuracion) {
        this(configuracion.getDbUrl(), configuracion.getDbUsuario(), configuracion.getDbClave());
    }

    public ConexionBD(String url, String usuario, String clave) {
        this.url = url;
        propiedades.setProperty("user", usuario);
        propiedades.setProperty("password", clave);
        // Si MySQL no responde, se falla pronto y el cliente recibe «Servicio no disponible»
        propiedades.setProperty("connectTimeout", "5000");
        propiedades.setProperty("socketTimeout", "30000");
    }

    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, propiedades);
    }
}
