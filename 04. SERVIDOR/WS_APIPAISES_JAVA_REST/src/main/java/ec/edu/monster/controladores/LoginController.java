package ec.edu.monster.controladores;

import ec.edu.monster.dao.UsuarioDAO;
import ec.edu.monster.modelos.Mensajes;
import ec.edu.monster.modelos.dto.LoginRequest;
import ec.edu.monster.modelos.dto.LoginResponse;
import ec.edu.monster.modelos.entidades.Usuario;
import ec.edu.monster.servicios.HashUtil;
import ec.edu.monster.servicios.TokenService;
import ec.edu.monster.utilidades.Textos;
import ec.edu.monster.vista.JsonResponseView;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Iniciar y cerrar sesión (RQF-001, ECUD1). */
@Path("Login")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class LoginController extends ControladorBase {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final TokenService tokenService = new TokenService();
    private final HashUtil hashUtil = new HashUtil();

    /**
     * POST /api/Login/login {usuario, clave}. Compara el SHA-256 de la clave con el guardado y,
     * si coincide, entrega un token nuevo y el nombre del usuario. No indica qué dato falló.
     */
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest solicitud) {
        String usuario = solicitud == null ? null : solicitud.getUsuario();
        String clave = solicitud == null ? null : solicitud.getClave();
        if (Textos.vacio(usuario)) {
            return JsonResponseView.error(400, Mensajes.INGRESE_USUARIO);
        }
        if (clave == null || clave.isEmpty()) {
            return JsonResponseView.error(400, Mensajes.INGRESE_CLAVE);
        }

        return responder("login", () -> {
            Usuario registrado = usuarioDAO.buscarPorUsuario(usuario.trim());
            if (registrado == null || !hashUtil.coincide(clave, registrado.getClaveHash())) {
                return JsonResponseView.error(401, Mensajes.CREDENCIALES_INCORRECTAS);
            }
            String token = tokenService.generarToken(registrado.getUsuario());
            return JsonResponseView.ok(Mensajes.LOGIN_EXITOSO, new LoginResponse(token, registrado.getNombre()));
        });
    }

    /** POST /api/Login/logout. El TokenFilter ya comprobó que el token está activo. */
    @POST
    @Path("logout")
    public Response logout(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        tokenService.invalidar(TokenService.extraer(authorization));
        return JsonResponseView.ok(Mensajes.SESION_CERRADA, null);
    }
}
