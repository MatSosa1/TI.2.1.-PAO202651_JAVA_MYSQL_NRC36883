package ec.edu.monster.modelos.servicios;

/** Operación correcta: el mensaje del servidor (p. ej. «País registrado correctamente») y los datos. */
public record Resultado<T>(String mensaje, T datos) {
}
