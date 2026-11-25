package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.Pais;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * ECUD4 Registrar País, pasos 2 a 6: el usuario consulta un nombre en la API, elige un país de
 * los resultados y presiona «Registrar». «Cancelar» cierra sin enviar nada (alterno 6).
 */
class NuevoPaisDialog extends JDialog {

    private final JTextField campoConsulta = Estilos.entrada(new JTextField());
    private final ModeloTablaPaises modelo = new ModeloTablaPaises(true);
    private final JTable tabla = Estilos.tabla(modelo);
    private final JLabel mensaje = Estilos.lineaMensaje();
    private final JProgressBar progreso = Estilos.progreso();
    private final JButton botonConsultar = Estilos.botonSecundario("Consultar");
    private final JButton botonRegistrar = Estilos.botonPrimario("Registrar");
    private final JButton botonCancelar = Estilos.botonSecundario("Cancelar");
    private volatile String consulta = "";

    NuevoPaisDialog(Window padre, Consumer<String> alConsultar, BiConsumer<String, String> alRegistrar) {
        super(padre, "Nuevo país", ModalityType.APPLICATION_MODAL);
        setSize(820, 480);
        setLocationRelativeTo(padre);

        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(Estilos.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        raiz.add(construirBusqueda(), BorderLayout.NORTH);
        ModeloTablaPaises.ajustar(tabla);
        raiz.add(Estilos.desplazamiento(tabla), BorderLayout.CENTER);
        raiz.add(construirPie(), BorderLayout.SOUTH);
        setContentPane(raiz);

        botonRegistrar.setEnabled(false);
        getRootPane().setDefaultButton(botonConsultar);
        botonConsultar.addActionListener(e -> {
            consulta = campoConsulta.getText();
            String texto = consulta;
            Estilos.limpiar(mensaje);
            Estilos.conProgreso(progreso, () -> alConsultar.accept(texto), this::mostrarError,
                    botonConsultar, botonRegistrar);
        });
        botonRegistrar.addActionListener(e -> {
            Pais pais = modelo.get(tabla.getSelectedRow());
            if (pais == null) {
                mostrarError("Seleccione un país de los resultados");
                return;
            }
            String texto = consulta;
            Estilos.limpiar(mensaje);
            Estilos.conProgreso(progreso, () -> alRegistrar.accept(texto, pais.nombre()), this::mostrarError,
                    botonConsultar, botonRegistrar);
        });
        botonCancelar.addActionListener(e -> dispose());
    }

    private JPanel construirBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(Estilos.subtitulo("Nuevo país"), BorderLayout.NORTH);
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        fila.add(Estilos.etiqueta("Nombre del país"), BorderLayout.WEST);
        fila.add(campoConsulta, BorderLayout.CENTER);
        botonConsultar.setPreferredSize(new Dimension(120, 36));
        fila.add(botonConsultar, BorderLayout.EAST);
        panel.add(fila, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPie() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(mensaje, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(progreso);
        botones.add(botonCancelar);
        botones.add(botonRegistrar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    /** ECUD4 Registrar paso 4: se muestran los resultados y se habilita «Registrar». */
    void mostrarResultados(List<Pais> paises) {
        Estilos.enEdt(() -> {
            modelo.setPaises(paises);
            if (!paises.isEmpty()) {
                tabla.setRowSelectionInterval(0, 0);
            }
            botonRegistrar.setEnabled(!paises.isEmpty());
            getRootPane().setDefaultButton(botonRegistrar);
        });
    }

    void mostrarError(String texto) {
        Estilos.mostrar(mensaje, texto, true);
    }
}
