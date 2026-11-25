package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Pantalla de inicio: saludo y una tarjeta por opción del menú, como el menú de EurekaBank. */
public class InicioPanel extends JPanel {

    public InicioPanel(String nombre, Consumer<String> alSeleccionar) {
        super(new BorderLayout(0, 22));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));

        add(Estilos.encabezado("Bienvenido, " + nombre, "Seleccione una opción para continuar."),
                BorderLayout.NORTH);

        JPanel tarjetas = new JPanel(new GridLayout(2, 2, 16, 16));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjeta(PrincipalFrame.PAISES,
                "Liste, registre, actualice y elimine los países guardados", alSeleccionar));
        tarjetas.add(tarjeta(PrincipalFrame.CONSULTA,
                "Consulte un país en REST Countries y guárdelo en la base local", alSeleccionar));
        tarjetas.add(tarjeta(PrincipalFrame.BUSQUEDA,
                "Busque por nombre, capital o región", alSeleccionar));
        tarjetas.add(tarjeta(PrincipalFrame.INFORMES,
                "Países por región, país más poblado y promedio poblacional", alSeleccionar));
        add(tarjetas, BorderLayout.CENTER);
    }

    private JButton tarjeta(String titulo, String descripcion, Consumer<String> alSeleccionar) {
        JButton boton = new JButton("<html><div style='width:220px'><b>" + titulo
                + "</b><br><br>" + descripcion + "</div></html>");
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setVerticalAlignment(SwingConstants.TOP);
        boton.setBackground(Estilos.BLANCO);
        boton.setForeground(Estilos.TEXTO);
        boton.setFont(Estilos.FUENTE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> alSeleccionar.accept(titulo));
        return boton;
    }
}
