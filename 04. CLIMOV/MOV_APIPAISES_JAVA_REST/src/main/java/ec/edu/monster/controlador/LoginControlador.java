package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Datos.Sesion;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.repositorios.ApiPaisesRepositorio;
import ec.edu.monster.servicios.SesionServicio;

/** ECUD1 Iniciar Sesión y cerrar sesión. */
public class LoginControlador extends ControladorConSesion {

    /** Campo que tiene el error de validación, para marcarlo en la pantalla. */
    public enum Campo { USUARIO, CLAVE }

    private final ApiPaisesRepositorio repositorio;

    public LoginControlador(ApiPaisesRepositorio repositorio, SesionServicio sesion) {
        super(sesion);
        this.repositorio = repositorio;
    }

    /** ECUD1 alterno 2: campo con error, o null si ambos tienen datos. */
    public Campo campoInvalido(String usuario, String clave) {
        if (ValidadorDatos.limpiar(usuario).isEmpty()) {
            return Campo.USUARIO;
        }
        return clave == null || clave.isEmpty() ? Campo.CLAVE : null;
    }

    public static String mensaje(Campo campo) {
        return campo == Campo.USUARIO ? ValidadorDatos.INGRESE_USUARIO : ValidadorDatos.INGRESE_CLAVE;
    }

    /** ECUD1 pasos 4 a 7. Se ejecuta en segundo plano. */
    public Resultado<Sesion> iniciarSesion(String usuario, String clave) {
        Campo campo = campoInvalido(usuario, clave);
        if (campo != null) {
            return Resultado.error(mensaje(campo));
        }
        Resultado<Sesion> resultado = repositorio.login(ValidadorDatos.limpiar(usuario), clave);
        if (resultado.isExito()) {
            sesion.iniciar(resultado.getDatos());
        }
        return resultado;
    }

    /**
     * ECUD1 alterno 4: borra la sesión local de inmediato, para volver al inicio de sesión aunque el
     * servidor no responda. @return el token, para avisar al servidor con {@link #notificarCierre}.
     */
    public String cerrarSesion() {
        String token = getToken();
        sesion.limpiar();
        return token;
    }

    /** POST /Login/logout en segundo plano; su resultado no cambia el flujo. */
    public Resultado<Object> notificarCierre(String token) {
        if (token == null || token.isEmpty()) {
            return Resultado.ok(null, "");
        }
        return repositorio.logout(token);
    }
}
