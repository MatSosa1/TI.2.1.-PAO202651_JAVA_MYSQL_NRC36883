package ec.edu.monster.filtros;

import ec.edu.monster.modelos.Mensajes;
import ec.edu.monster.servicios.TokenService;
import ec.edu.monster.vista.JsonResponseView;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

/**
 * Validar Token (RQF-008, ECUD8): toda petición, salvo el inicio de sesión, debe traer en el
 * encabezado Authorization un token de 64 caracteres hexadecimales que esté entre los activos.
 * Si no, la operación no se ejecuta y se responde 401 «Token inválido o sesión no activa».
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TokenFilter implements ContainerRequestFilter {

    /** Única ruta pública. */
    private static final String RUTA_LOGIN = "Login/login";

    /** Propiedad de la petición con el usuario autenticado, por si un controlador lo necesita. */
    public static final String USUARIO = "apipaises.usuario";

    private final TokenService tokenService = new TokenService();

    @Override
    public void filter(ContainerRequestContext peticion) {
        if (esPublica(peticion.getUriInfo().getPath())) {
            return;
        }
        String token = TokenService.extraer(peticion.getHeaderString(HttpHeaders.AUTHORIZATION));
        if (!tokenService.esValido(token)) {
            peticion.abortWith(JsonResponseView.error(401, Mensajes.TOKEN_INVALIDO));
            return;
        }
        peticion.setProperty(USUARIO, tokenService.usuarioDe(token));
    }

    static boolean esPublica(String ruta) {
        String limpia = ruta == null ? "" : ruta.replaceAll("^/+|/+$", "");
        return RUTA_LOGIN.equals(limpia);
    }
}
