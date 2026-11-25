package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.ConteoRegion;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * Gráfico de barras con la cantidad de países por región (ECUD7, paso 8), dibujado con
 * Graphics2D para no depender de librerías externas. Usa los colores de Estilos.
 */
class GraficoBarras extends JComponent {

    private static final int MARGEN = 16;
    private static final int ALTO_ETIQUETA = 22;

    private List<ConteoRegion> datos = new ArrayList<>();

    GraficoBarras() {
        setPreferredSize(new Dimension(360, 150));
        setOpaque(false);
    }

    void setDatos(List<ConteoRegion> datos) {
        this.datos = new ArrayList<>(datos == null ? List.of() : datos);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (datos.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth() - 2 * MARGEN;
        int base = getHeight() - ALTO_ETIQUETA - 4;
        int altoMaximo = base - MARGEN - 18;
        int maximo = Math.max(1, datos.stream().mapToInt(ConteoRegion::cantidad).max().orElse(1));
        int columna = ancho / datos.size();
        int barra = Math.max(8, (int) (columna * 0.6));

        // Línea base
        g2.setColor(Estilos.BORDE);
        g2.drawLine(MARGEN, base, MARGEN + ancho, base);

        g2.setFont(Estilos.FUENTE.deriveFont(12f));
        FontMetrics medidas = g2.getFontMetrics();
        for (int i = 0; i < datos.size(); i++) {
            ConteoRegion dato = datos.get(i);
            int alto = (int) Math.round((double) dato.cantidad() / maximo * altoMaximo);
            int x = MARGEN + i * columna + (columna - barra) / 2;

            g2.setColor(Estilos.PRIMARIO);
            g2.fillRoundRect(x, base - alto, barra, alto, 4, 4);

            String cantidad = String.valueOf(dato.cantidad());
            g2.setColor(Estilos.TEXTO);
            g2.drawString(cantidad, x + (barra - medidas.stringWidth(cantidad)) / 2, base - alto - 4);

            String region = recortar(dato.region(), medidas, columna - 4);
            g2.setColor(Estilos.TEXTO);
            g2.drawString(region, MARGEN + i * columna + (columna - medidas.stringWidth(region)) / 2,
                    base + medidas.getAscent() + 4);
        }
        g2.dispose();
    }

    private static String recortar(String texto, FontMetrics medidas, int ancho) {
        if (medidas.stringWidth(texto) <= ancho) {
            return texto;
        }
        String corto = texto;
        while (corto.length() > 1 && medidas.stringWidth(corto + "…") > ancho) {
            corto = corto.substring(0, corto.length() - 1);
        }
        return corto + "…";
    }
}
