package ec.edu.monster.modelos.dto;

/** Datos de un inicio de sesión correcto: el token y el nombre del usuario (ECUD1, paso 6). */
public record LoginDatos(String token, String usuario) {
}
