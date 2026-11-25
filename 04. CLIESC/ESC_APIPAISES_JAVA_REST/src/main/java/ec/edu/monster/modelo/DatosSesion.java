package ec.edu.monster.modelo;

/** Respuesta de un inicio de sesión correcto: el token y el nombre del usuario (ECUD1, paso 6). */
public record DatosSesion(String token, String usuario) {
}
