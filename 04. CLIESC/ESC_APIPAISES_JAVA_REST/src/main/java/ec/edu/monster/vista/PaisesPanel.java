package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Formatos;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * ECUD4 Gestionar Países: tabla con los países guardados, botones Nuevo, Actualizar y Eliminar,
 * y la lista de filtros por región en la parte inferior (ECUD6, ERS 9.1.1), que filtra la
 * tabla de inmediato al elegir una región.
 */
public class PaisesPanel extends JPanel implements Vistas.PaisesVista {

    private final ModeloTablaPaises modelo = new ModeloTablaPaises(false);
    private final JTable tabla = Estilos.tabla(modelo);
    private final JLabel total = Estilos.texto(" ");
    private final JLabel mensaje = Estilos.lineaMensaje();
    private final JProgressBar progreso = Estilos.progreso();
    private final JButton botonNuevo = Estilos.botonPrimario("Nuevo");
    private final JButton botonActualizar = Estilos.botonSecundario("Actualizar");
    private final JButton botonEliminar = Estilos.botonSecundario("Eliminar");
    private final List<JToggleButton> filtros = new ArrayList<>();
    private NuevoPaisDialog dialogo;

    private Consumer<String> accionCargar = region -> { };
    private Consumer<String> accionActualizar = nombre -> { };
    private Consumer<String> accionEliminar = nombre -> { };
    private Consumer<String> accionConsultarApi = consulta -> { };
    private BiConsumer<String, String> accionRegistrar = (consulta, nombre) -> { };
    private String region = "";

    public PaisesPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        add(Estilos.encabezado("Países", "Países guardados en la base de datos local"), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        botonNuevo.addActionListener(e -> abrirNuevo());
        botonActualizar.addActionListener(e -> actualizar());
        botonEliminar.addActionListener(e -> eliminar());

        // La tabla se recarga cada vez que se abre la pantalla
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargar(region);
            }
        });
    }

    private JPanel construirTabla() {
        JPanel tarjeta = Estilos.panelTarjeta();
        tarjeta.setLayout(new BorderLayout(0, 10));
        ModeloTablaPaises.ajustar(tabla);
        tarjeta.add(Estilos.desplazamiento(tabla), BorderLayout.CENTER);

        // Lista de filtros en la parte inferior (ERS 9.1.1): etiqueta arriba y una opción por región
        JPanel lineaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lineaFiltros.setOpaque(false);
        JPanel bloqueFiltros = new JPanel(new BorderLayout(0, 6));
        bloqueFiltros.setOpaque(false);
        bloqueFiltros.add(Estilos.etiqueta("Filtrar por región"), BorderLayout.NORTH);
        bloqueFiltros.add(lineaFiltros, BorderLayout.CENTER);
        ButtonGroup grupo = new ButtonGroup();
        List<String> opciones = new ArrayList<>();
        opciones.add(Formatos.TODAS);
        opciones.addAll(Formatos.REGIONES);
        for (String opcion : opciones) {
            JToggleButton boton = new JToggleButton(opcion, Formatos.TODAS.equals(opcion));
            boton.setFont(Estilos.FUENTE_PEQUENA);
            boton.setFocusPainted(false);
            boton.setMargin(new java.awt.Insets(4, 10, 4, 10));
            boton.addActionListener(e -> cargar(Formatos.region(opcion)));
            grupo.add(boton);
            filtros.add(boton);
            lineaFiltros.add(boton);
        }
        tarjeta.add(bloqueFiltros, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel construirPie() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        JPanel lineas = new JPanel(new BorderLayout());
        lineas.setOpaque(false);
        lineas.add(total, BorderLayout.WEST);
        lineas.add(mensaje, BorderLayout.CENTER);
        mensaje.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        panel.add(lineas, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(progreso);
        botones.add(botonEliminar);
        botones.add(botonActualizar);
        botones.add(botonNuevo);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------------ Acciones del usuario

    private void cargar(String regionElegida) {
        region = regionElegida;
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, () -> accionCargar.accept(regionElegida), this::mostrarError, controles());
    }

    private void actualizar() {
        Pais pais = seleccionado();
        if (pais == null) {
            return;
        }
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, () -> accionActualizar.accept(pais.nombre()), this::mostrarError, controles());
    }

    /** ECUD4 Eliminar pasos 2 y 3: se pide confirmar antes de enviar. */
    private void eliminar() {
        Pais pais = seleccionado();
        if (pais == null || !Estilos.confirmar(this, "¿Eliminar el país " + pais.nombre() + "?")) {
            return;
        }
        Estilos.limpiar(mensaje);
        Estilos.conProgreso(progreso, () -> accionEliminar.accept(pais.nombre()), this::mostrarError, controles());
    }

    private void abrirNuevo() {
        dialogo = new NuevoPaisDialog(SwingUtilities.getWindowAncestor(this), accionConsultarApi, accionRegistrar);
        dialogo.setVisible(true);
    }

    private Pais seleccionado() {
        Pais pais = modelo.get(tabla.getSelectedRow());
        if (pais == null) {
            Estilos.mostrar(mensaje, "Seleccione un país de la tabla", true);
        }
        return pais;
    }

    private javax.swing.JComponent[] controles() {
        List<javax.swing.JComponent> lista = new ArrayList<>(List.of(botonNuevo, botonActualizar, botonEliminar));
        lista.addAll(filtros);
        return lista.toArray(new javax.swing.JComponent[0]);
    }

    // ------------------------------------------------------------------ PaisesVista

    @Override
    public void alCargar(Consumer<String> accion) {
        this.accionCargar = accion;
    }

    @Override
    public void alActualizar(Consumer<String> accion) {
        this.accionActualizar = accion;
    }

    @Override
    public void alEliminar(Consumer<String> accion) {
        this.accionEliminar = accion;
    }

    @Override
    public void alConsultarApi(Consumer<String> accion) {
        this.accionConsultarApi = accion;
    }

    @Override
    public void alRegistrar(BiConsumer<String, String> accion) {
        this.accionRegistrar = accion;
    }

    @Override
    public void mostrarPaises(List<Pais> paises, String regionActual) {
        Estilos.enEdt(() -> {
            modelo.setPaises(paises);
            if (paises.isEmpty()) {
                // ECUD4 alterno 7 y ECUD6 alterno 3
                total.setText(regionActual.isEmpty()
                        ? "No hay países registrados. Use «Consultar en la API» para guardarlos"
                        : "No se encontraron países en esa región");
            } else {
                total.setText(Formatos.total(paises.size()));
            }
        });
    }

    @Override
    public void mostrarResultadosApi(List<Pais> paises) {
        if (dialogo != null) {
            dialogo.mostrarResultados(paises);
        }
    }

    @Override
    public void registroCompletado(String texto) {
        // ECUD4 Registrar paso 9: se muestra el mensaje, se cierra el formulario y se recarga la tabla
        Estilos.enEdt(() -> {
            if (dialogo != null) {
                dialogo.dispose();
                dialogo = null;
            }
        });
        mostrarMensaje(texto);
    }

    @Override
    public void mostrarMensaje(String texto) {
        Estilos.mostrar(mensaje, texto, false);
    }

    @Override
    public void mostrarError(String texto) {
        Estilos.mostrar(mensaje, texto, true);
    }

    @Override
    public void mostrarErrorRegistro(String texto) {
        if (dialogo != null) {
            dialogo.mostrarError(texto);
        } else {
            mostrarError(texto);
        }
    }
}
