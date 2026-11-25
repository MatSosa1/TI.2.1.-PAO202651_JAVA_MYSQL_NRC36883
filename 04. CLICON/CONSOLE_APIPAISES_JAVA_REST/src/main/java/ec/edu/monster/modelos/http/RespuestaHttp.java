package ec.edu.monster.modelos.http;

/** Código HTTP y cuerpo de la respuesta (el servidor responde 400/401/404/409/502 con un cuerpo válido). */
public record RespuestaHttp(int codigo, String cuerpo) {
}
