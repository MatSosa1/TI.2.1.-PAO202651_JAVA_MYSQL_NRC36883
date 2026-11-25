package ec.edu.monster.modelo;

/** Operación correcta: el mensaje del servidor (por ejemplo «País registrado correctamente») y los datos. */
public record Resultado<T>(String mensaje, T datos) {
}
