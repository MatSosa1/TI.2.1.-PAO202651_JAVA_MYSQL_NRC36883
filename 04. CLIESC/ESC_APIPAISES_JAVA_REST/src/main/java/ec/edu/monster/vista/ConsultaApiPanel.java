package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.Pais;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * ECUD3 Consultar País en la API y ECUD2 Importar: campo de búsqueda, tabla de resultados que
 * indica si cada país ya está guardado, botón «Guardar» y «Importar todos».
 */
public class ConsultaApiPanel extends JPanel implements Vistas.ConsultaApiVista {

    private final JTextField campoNombre = Estilos.entrada(new JTextField());
    private final ModeloTablaPaises modelo = new ModeloTablaPaises(true);
    private final JTable tabla = Estilos.tabla(modelo);
    private final JLabel mensaje = Estilos.lineaMensaje();
    private final JProgressBar progreso = Estilos.progreso();
    private final JButton botonConsultar = Estilos.botonPrimario("Consultar");
    private final JButton botonGuardar = Estilos.botonPrimario("Guardar");
    private final JButton botonImportarTodos = Estilos.botonSecundario("Importar todos");

    private Consumer<String> accionConsultar = nombre -> { };
    private BiConsumer<String, String> accionGuardar = (consulta, nombre) -> { };
    private Runnable accionImportarTodos = () -> { };
    private volatile String ultimaConsulta = "";

    public ConsultaApiPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JPanel superior = new JPanel(new BorderLayout(0, 14));
        superior.setOpaque(false);
        superior.add(Estilos.encabezado("Consultar en la API",
                "Consulte un país en REST Countries y guárdelo en la base de datos local"), BorderLayout.NORTH);
        superior.add(construirBusqueda(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);

        ModeloTablaPaises.ajustar(tabla);
        add(Estilos.desplazamiento(tabla), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        botonGuardar.setEnabled(false);
        campoNombre.addActionListener(e -> consultar());
        botonConsultar.addActionListener(e -> consultar());
        botonGuardar.addActionListener(e -> guardar());
        botonImportarTodos.addActionListener(e -> importarTodos());
    }

    private JPanel construirBusqueda() {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new BorderLayout(10, 6));
        tarjeta.add(Estilos.etiqueta("Nombre del país"), BorderLayout.NORTH);
        tarjeta.add(campoNombre, BorderLayout.CENTER);
        botonConsultar.setPreferredSize(new Dimension(120, 36));
        tarjeta.add(botonConsultar, BorderLayout.EAST);
        return tarjeta;
    }

    private JPanel construirPie() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(mensaje, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(progreso);
        botones.add(botonImportarTodos);
        botones.add(botonGuardar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void consultar() {
        String nombre = campoNombre.getText();
        ultimaConsulta = nombre;
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, () -> accionConsultar.accept(nombre), this::mostrarError, controles());
    }

    /** ECUD2 pasos 1 a 3: país elegido y confirmación. */
    private void guardar() {
        Pais pais = modelo.get(tabla.getSelectedRow());
        if (pais == null) {
            mostrarError("Seleccione un país de los resultados");
            return;
        }
        if (!Estilos.confirmar(this, "¿Guardar el país " + pais.nombre() + " en la base de datos local?")) {
            return;
        }
        String consulta = ultimaConsulta;
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, () -> accionGuardar.accept(consulta, pais.nombre()), this::mostrarError,
                controles());
    }

    private void importarTodos() {
        if (!Estilos.confirmar(this, "¿Importar todos los países desde la API REST Countries?")) {
            return;
        }
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, accionImportarTodos, this::mostrarError, controles());
    }

    private javax.swing.JComponent[] controles() {
        return new javax.swing.JComponent[] {botonConsultar, botonGuardar, botonImportarTodos, campoNombre};
    }

    // ------------------------------------------------------------------ ConsultaApiVista

    @Override
    public void alConsultar(Consumer<String> accion) {
        this.accionConsultar = accion;
    }

    @Override
    public void alGuardar(BiConsumer<String, String> accion) {
        this.accionGuardar = accion;
    }

    @Override
    public void alImportarTodos(Runnable accion) {
        this.accionImportarTodos = accion;
    }

    @Override
    public void mostrarResultados(List<Pais> paises) {
        Estilos.enEdt(() -> {
            modelo.setPaises(paises);
            if (!paises.isEmpty()) {
                tabla.setRowSelectionInterval(0, 0);
            }
            botonGuardar.setEnabled(!paises.isEmpty());
        });
    }

    @Override
    public void marcarGuardado(String nombre) {
        Estilos.enEdt(() -> modelo.marcarGuardado(nombre));
    }

    @Override
    public void mostrarMensaje(String texto) {
        Estilos.mostrar(mensaje, texto, false);
    }

    @Override
    public void mostrarError(String texto) {
        Estilos.enEdt(() -> {
            // ECUD3 alterno 2: se conserva el texto para corregirlo
            Estilos.mostrar(mensaje, texto, true);
            botonGuardar.setEnabled(modelo.getRowCount() > 0);
        });
    }
}
