package ec.edu.monster.controladores;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Publica los servicios RESTful en http://&lt;servidor&gt;:&lt;puerto&gt;/WS_APIPAISES_JAVA_REST/api.
 * Payara registra automáticamente los controladores (@Path) y los proveedores (@Provider).
 */
@ApplicationPath("api")
public class ApplicationConfig extends Application {
}
