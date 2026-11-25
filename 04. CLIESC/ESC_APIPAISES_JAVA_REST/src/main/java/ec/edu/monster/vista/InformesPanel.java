package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.ConteoRegion;
import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Datos.PromedioRegion;
import ec.edu.monster.modelo.Formatos;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * ECUD7 Generar Informes: tabla de países por región, país más poblado, tabla de promedio
 * poblacional por región y gráfico de barras con la cantidad de países por región.
 */
public class InformesPanel extends JPanel implements Vistas.InformesVista {

    private final DefaultTableModel modeloConteo = modeloSoloLectura("Región", "Países");
    private final DefaultTableModel modeloPromedio = modeloSoloLectura("Región", "Promedio de población");
    private final GraficoBarras grafico = new GraficoBarras();
    private final JLabel masPobladoNombre = Estilos.subtitulo(" ");
    private final JLabel masPobladoDetalle = Estilos.texto(" ");
    private final JLabel mensaje = Estilos.lineaMensaje();
    private final JProgressBar progreso = Estilos.progreso();
    private final JButton botonGenerar = Estilos.botonPrimario("Generar informes");
    private final JPanel contenido = new JPanel(new BorderLayout(0, 16));

    private Runnable accionGenerar = () -> { };

    public InformesPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // Encabezado con el botón a la derecha para dejar el alto a las tablas
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(Estilos.encabezado("Informes", "Estadísticas de los países guardados en la base local"),
                BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(progreso);
        botones.add(botonGenerar);
        superior.add(botones, BorderLayout.EAST);
        add(superior, BorderLayout.NORTH);

        // Arriba: las dos tablas con las seis regiones (alto completo); abajo: país más poblado y gráfico
        contenido.setOpaque(false);
        JPanel filaSuperior = new JPanel(new GridLayout(1, 2, 16, 0));
        filaSuperior.setOpaque(false);
        filaSuperior.add(tarjetaMasPoblado());
        filaSuperior.add(tarjeta("Cantidad de países por región", grafico));
        JPanel filaTablas = new JPanel(new GridLayout(1, 2, 16, 0));
        filaTablas.setOpaque(false);
        filaTablas.add(tarjeta("Países por región", Estilos.desplazamiento(tablaDe(modeloConteo))));
        filaTablas.add(tarjeta("Promedio poblacional por región", Estilos.desplazamiento(tablaDe(modeloPromedio))));
        contenido.add(filaTablas, BorderLayout.NORTH);
        contenido.add(filaSuperior, BorderLayout.CENTER);
        contenido.setVisible(false);
        add(contenido, BorderLayout.CENTER);
        add(mensaje, BorderLayout.SOUTH);

        botonGenerar.addActionListener(e -> generar());
        // Los informes se generan al abrir la pantalla
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                generar();
            }
        });
    }

    private void generar() {
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, accionGenerar, this::mostrarError, botonGenerar);
    }

    private static DefaultTableModel modeloSoloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private static JTable tablaDe(DefaultTableModel modelo) {
        JTable tabla = Estilos.tabla(modelo);
        tabla.setRowHeight(24);
        // Alto para las seis regiones sin barra de desplazamiento
        tabla.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 24 * 6));
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(1).setCellRenderer(derecha);
        return tabla;
    }

    private static JPanel tarjeta(String titulo, java.awt.Component cuerpo) {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.add(Estilos.etiqueta(titulo), BorderLayout.NORTH);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel tarjetaMasPoblado() {
        JPanel datos = new JPanel(new GridLayout(2, 1, 0, 4));
        datos.setOpaque(false);
        datos.add(masPobladoNombre);
        datos.add(masPobladoDetalle);
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(datos, BorderLayout.NORTH);
        JPanel tarjeta = tarjeta("País más poblado", arriba);
        tarjeta.setBackground(Estilos.FONDO_RESULTADO);
        return tarjeta;
    }

    // ------------------------------------------------------------------ InformesVista

    @Override
    public void alGenerar(Runnable accion) {
        this.accionGenerar = accion;
    }

    @Override
    public void mostrarInforme(Informe informe, String texto) {
        Estilos.enEdt(() -> {
            modeloConteo.setRowCount(0);
            for (ConteoRegion conteo : informe.paisesPorRegion()) {
                modeloConteo.addRow(new Object[] {conteo.region(), conteo.cantidad()});
            }
            modeloPromedio.setRowCount(0);
            for (PromedioRegion promedio : informe.promedioPorRegion()) {
                modeloPromedio.addRow(new Object[] {promedio.region(), Formatos.poblacion(promedio.promedioPoblacion())});
            }
            Pais mayor = informe.paisMasPoblado();
            masPobladoNombre.setText(mayor == null ? "—" : mayor.nombre());
            masPobladoDetalle.setText(mayor == null ? " "
                    : Formatos.poblacion(mayor.poblacion()) + " habitantes · " + mayor.region());
            grafico.setDatos(informe.paisesPorRegion());
            contenido.setVisible(true);
            revalidate();
            repaint();
        });
        Estilos.mostrar(mensaje, texto, false);
    }

    @Override
    public void mostrarError(String texto) {
        Estilos.enEdt(() -> contenido.setVisible(false));
        Estilos.mostrar(mensaje, texto, true);
    }
}
