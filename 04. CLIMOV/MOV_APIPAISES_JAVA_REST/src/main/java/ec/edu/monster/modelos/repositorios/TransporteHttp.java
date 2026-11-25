package ec.edu.monster.modelos.repositorios;

import ec.edu.monster.modelos.interfaces.Transporte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Transporte real con HttpURLConnection (java.net.http no existe en Android 7). Lee el cuerpo
 * también en los códigos de error: el servidor responde 400/401/404/409/502 con JSON.
 */
public class TransporteHttp implements Transporte {

    private static final int ESPERA_CONEXION_MS = 5000;

    @Override
    public Respuesta enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, int esperaMs)
            throws IOException {
        HttpURLConnection conexion = (HttpURLConnection) new URL(url).openConnection();
        try {
            conexion.setRequestMethod(metodo);
            conexion.setConnectTimeout(ESPERA_CONEXION_MS);
            conexion.setReadTimeout(esperaMs);
            conexion.setUseCaches(false);
            for (Map.Entry<String, String> cabecera : cabeceras.entrySet()) {
                conexion.setRequestProperty(cabecera.getKey(), cabecera.getValue());
            }
            if (cuerpo != null) {
                byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
                conexion.setDoOutput(true);
                conexion.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(bytes);
                }
            }
            int codigo = conexion.getResponseCode();
            InputStream entrada = codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            return new Respuesta(codigo, leer(entrada));
        } finally {
            conexion.disconnect();
        }
    }

    private static String leer(InputStream entrada) throws IOException {
        if (entrada == null) {
            return "";
        }
        try (InputStream flujo = entrada; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] bloque = new byte[8192];
            int leidos;
            while ((leidos = flujo.read(bloque)) != -1) {
                bytes.write(bloque, 0, leidos);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
