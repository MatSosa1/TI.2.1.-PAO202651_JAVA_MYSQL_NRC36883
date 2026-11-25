package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Formatos;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * ECUD5 Buscar Países: campos Nombre y Capital y la lista de regiones. Al cambiar la región se
 * filtra de inmediato, sin presionar «Buscar», con los textos ya escritos (ECUD6).
 */
public class BusquedaPanel extends JPanel implements Vistas.BusquedaVista {

    private final JTextField campoNombre = Estilos.entrada(new JTextField());
    private final JTextField campoCapital = Estilos.entrada(new JTextField());
    private final JComboBox<String> comboRegion = Estilos.entrada(new JComboBox<>());
    private final ModeloTablaPaises modelo = new ModeloTablaPaises(false);
    private final JTable tabla = Estilos.tabla(modelo);
    private final JLabel total = Estilos.texto(" ");
    private final JLabel mensaje = Estilos.lineaMensaje();
    private final JProgressBar progreso = Estilos.progreso();
    private final JButton botonBuscar = Estilos.botonPrimario("Buscar");
    private final JButton botonLimpiar = Estilos.botonSecundario("Limpiar");

    private Consumer<Criterios> accionBuscar = criterios -> { };

    public BusquedaPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JPanel superior = new JPanel(new BorderLayout(0, 14));
        superior.setOpaque(false);
        superior.add(Estilos.encabezado("Buscar países", "Busque por nombre, capital o región"), BorderLayout.NORTH);
        superior.add(construirFormulario(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);

        ModeloTablaPaises.ajustar(tabla);
        add(Estilos.desplazamiento(tabla), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        comboRegion.addItem(Formatos.TODAS);
        Formatos.REGIONES.forEach(comboRegion::addItem);
        comboRegion.addActionListener(e -> buscar());
        campoNombre.addActionListener(e -> buscar());
        campoCapital.addActionListener(e -> buscar());
        botonBuscar.addActionListener(e -> buscar());
        botonLimpiar.addActionListener(e -> limpiar());
    }

    private JPanel construirFormulario() {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 4, 12);
        c.gridy = 0;
        c.weightx = 1;
        c.gridx = 0;
        tarjeta.add(Estilos.etiqueta("Nombre"), c);
        c.gridx = 1;
        tarjeta.add(Estilos.etiqueta("Capital"), c);
        c.gridx = 2;
        c.weightx = 0.6;
        tarjeta.add(Estilos.etiqueta("Región"), c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 12);
        c.weightx = 1;
        c.gridx = 0;
        tarjeta.add(campoNombre, c);
        c.gridx = 1;
        tarjeta.add(campoCapital, c);
        c.gridx = 2;
        c.weightx = 0.6;
        tarjeta.add(comboRegion, c);
        c.gridx = 3;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(botonBuscar, c);
        return tarjeta;
    }

    private JPanel construirPie() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        JPanel lineas = new JPanel(new BorderLayout());
        lineas.setOpaque(false);
        lineas.add(total, BorderLayout.WEST);
        mensaje.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        lineas.add(mensaje, BorderLayout.CENTER);
        panel.add(lineas, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(progreso);
        botones.add(botonLimpiar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void buscar() {
        Criterios criterios = new Criterios(campoNombre.getText(), campoCapital.getText(),
                (String) comboRegion.getSelectedItem());
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, () -> accionBuscar.accept(criterios), this::mostrarError,
                botonBuscar, botonLimpiar);
    }

    private void limpiar() {
        campoNombre.setText("");
        campoCapital.setText("");
        comboRegion.setSelectedIndex(0); // dispara una búsqueda sin criterios: todos los países
    }

    // ------------------------------------------------------------------ BusquedaVista

    @Override
    public void alBuscar(Consumer<Criterios> accion) {
        this.accionBuscar = accion;
    }

    @Override
    public void mostrarResultado(List<Pais> paises, String texto) {
        Estilos.enEdt(() -> {
            modelo.setPaises(new ArrayList<>(paises));
            // ECUD5 alterno 4: sin resultados se muestra la tabla vacía y un mensaje
            total.setText(paises.isEmpty() ? "No se encontraron países con esos criterios"
                    : Formatos.total(paises.size()));
        });
        Estilos.mostrar(mensaje, texto, false);
    }

    @Override
    public void mostrarError(String texto) {
        Estilos.mostrar(mensaje, texto, true);
    }
}
