package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Ventana principal después del inicio de sesión (ECUD1 paso 7): encabezado con la marca, el
 * sello del servicio y el usuario; menú lateral con Inicio, Países, Consultar en la API, Buscar
 * países e Informes; y la línea de estado del servidor. Mismo diseño que EurekaBank.
 */
public class PrincipalFrame extends JFrame {

    static final String INICIO = "Inicio";
    static final String PAISES = "Países";
    static final String CONSULTA = "Consultar en la API";
    static final String BUSQUEDA = "Buscar países";
    static final String INFORMES = "Informes";

    private final CardLayout pantallas = new CardLayout();
    private final JPanel contenido = new JPanel(pantallas);
    private final Map<String, JButton> navegacion = new LinkedHashMap<>();
    private final PaisesPanel paisesPanel = new PaisesPanel();
    private final ConsultaApiPanel consultaApiPanel = new ConsultaApiPanel();
    private final BusquedaPanel busquedaPanel = new BusquedaPanel();
    private final InformesPanel informesPanel = new InformesPanel();
    private final IndicadorEstado indicadorEstado = new IndicadorEstado("Sesión activa · ");

    public PrincipalFrame(String nombre, Runnable alCerrarSesion) {
        super(Estilos.TITULO_VENTANA);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(980, 700));
        setLocationRelativeTo(null);

        contenido.setOpaque(false);
        contenido.add(new InicioPanel(nombre, this::seleccionar), INICIO);
        contenido.add(paisesPanel, PAISES);
        contenido.add(consultaApiPanel, CONSULTA);
        contenido.add(busquedaPanel, BUSQUEDA);
        contenido.add(informesPanel, INFORMES);

        setJMenuBar(construirMenu(alCerrarSesion));
        indicadorEstado.setBorder(BorderFactory.createEmptyBorder(8, 30, 14, 30));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilos.FONDO);
        raiz.add(construirEncabezado(nombre, alCerrarSesion), BorderLayout.NORTH);
        raiz.add(construirNavegacion(), BorderLayout.WEST);
        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(indicadorEstado, BorderLayout.SOUTH);
        setContentPane(raiz);
        seleccionar(INICIO);
    }

    public PaisesPanel getPaisesPanel() {
        return paisesPanel;
    }

    public ConsultaApiPanel getConsultaApiPanel() {
        return consultaApiPanel;
    }

    public BusquedaPanel getBusquedaPanel() {
        return busquedaPanel;
    }

    public InformesPanel getInformesPanel() {
        return informesPanel;
    }

    public Vistas.EstadoVista getIndicadorEstado() {
        return indicadorEstado;
    }

    /** Muestra la pantalla y resalta su opción en el menú lateral. */
    void seleccionar(String pantalla) {
        pantallas.show(contenido, pantalla);
        navegacion.forEach((nombre, boton) -> {
            boolean activa = nombre.equals(pantalla);
            boton.setBackground(activa ? Estilos.FONDO_RESULTADO : Estilos.BLANCO);
            boton.setForeground(activa ? Estilos.PRIMARIO : Estilos.TEXTO);
            boton.setFont(activa ? Estilos.FUENTE_NEGRITA : Estilos.FUENTE);
        });
    }

    private JMenuBar construirMenu(Runnable alCerrarSesion) {
        JMenu archivo = Estilos.menu("Archivo");
        for (String pantalla : new String[] {INICIO, PAISES, CONSULTA, BUSQUEDA, INFORMES}) {
            archivo.add(Estilos.opcion(pantalla, () -> seleccionar(pantalla)));
        }
        archivo.addSeparator();
        archivo.add(Estilos.opcion("Cerrar sesión", () -> cerrarSesion(alCerrarSesion)));
        archivo.add(Estilos.opcion("Salir",
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))));
        return Estilos.barraMenu(archivo, Estilos.menuAyuda(this));
    }

    private void cerrarSesion(Runnable alCerrarSesion) {
        try {
            alCerrarSesion.run();
        } finally {
            dispose();
        }
    }

    private JPanel construirEncabezado(String nombre, Runnable alCerrarSesion) {
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);

        JLabel marca = new JLabel("AP");
        marca.setOpaque(true);
        marca.setBackground(Estilos.PRIMARIO);
        marca.setForeground(Estilos.BLANCO);
        marca.setFont(Estilos.FUENTE_NEGRITA);
        marca.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        izquierda.add(marca);
        izquierda.add(Estilos.subtitulo("APIPaises"));

        JLabel servicio = new JLabel("REST / Java");
        servicio.setFont(Estilos.FUENTE_PEQUENA);
        servicio.setForeground(Estilos.PRIMARIO);
        servicio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.PRIMARIO),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        izquierda.add(servicio);

        JPanel usuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        usuario.setOpaque(false);
        JLabel nombreUsuario = new JLabel(nombre);
        nombreUsuario.setFont(Estilos.FUENTE_NEGRITA);
        nombreUsuario.setForeground(Estilos.TEXTO);
        usuario.add(nombreUsuario);
        JLabel rol = new JLabel("Usuario");
        rol.setFont(Estilos.FUENTE_PEQUENA);
        rol.setForeground(Estilos.APAGADO);
        usuario.add(rol);

        JButton cerrar = Estilos.botonSecundario("Cerrar sesión");
        cerrar.addActionListener(e -> cerrarSesion(alCerrarSesion));
        usuario.add(cerrar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilos.BLANCO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilos.BORDE),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(usuario, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel construirNavegacion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Estilos.BLANCO);
        panel.setPreferredSize(new Dimension(210, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));

        JLabel titulo = new JLabel("MENÚ");
        titulo.setFont(Estilos.FUENTE_PEQUENA);
        titulo.setForeground(Estilos.APAGADO);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 0));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titulo);

        for (String pantalla : new String[] {INICIO, PAISES, CONSULTA, BUSQUEDA, INFORMES}) {
            JButton boton = Estilos.botonNavegacion(pantalla);
            boton.setAlignmentX(LEFT_ALIGNMENT);
            boton.addActionListener(e -> seleccionar(pantalla));
            navegacion.put(pantalla, boton);
            panel.add(boton);
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }
}
