package ec.edu.monster.vista;

import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Formatos;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Tabla de países (el «DataGrid» del ERS): Nombre, Capital, Región, Población y Moneda, y en
 * la consulta a la API la columna «Guardado» (ECUD3, paso 7). La población lleva separador de miles.
 */
class ModeloTablaPaises extends AbstractTableModel {

    private static final String[] COLUMNAS = {"Nombre", "Capital", "Región", "Población", "Moneda", "Guardado"};
    private static final int POBLACION = 3;

    private final boolean conGuardado;
    private List<Pais> paises = new ArrayList<>();

    ModeloTablaPaises(boolean conGuardado) {
        this.conGuardado = conGuardado;
    }

    void setPaises(List<Pais> paises) {
        this.paises = new ArrayList<>(paises == null ? List.of() : paises);
        fireTableDataChanged();
    }

    Pais get(int fila) {
        return fila < 0 || fila >= paises.size() ? null : paises.get(fila);
    }

    /** ECUD2 paso 9: el país recién guardado se marca en la tabla. */
    void marcarGuardado(String nombre) {
        for (int i = 0; i < paises.size(); i++) {
            if (paises.get(i).nombre().equals(nombre)) {
                paises.set(i, paises.get(i).comoGuardado());
                fireTableRowsUpdated(i, i);
            }
        }
    }

    @Override
    public int getRowCount() {
        return paises.size();
    }

    @Override
    public int getColumnCount() {
        return conGuardado ? COLUMNAS.length : COLUMNAS.length - 1;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Pais pais = paises.get(fila);
        return switch (columna) {
            case 0 -> pais.nombre();
            case 1 -> pais.capital();
            case 2 -> pais.region();
            case POBLACION -> Formatos.poblacion(pais.poblacion());
            case 4 -> pais.moneda();
            default -> pais.guardado() ? "Sí" : "No";
        };
    }

    /** Población alineada a la derecha y anchos razonables. */
    static void ajustar(JTable tabla) {
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(POBLACION).setCellRenderer(derecha);
        int[] anchos = {170, 140, 100, 110, 220, 95};
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        if (tabla.getColumnCount() > 5) {
            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(JLabel.CENTER);
            tabla.getColumnModel().getColumn(5).setCellRenderer(centro);
        }
    }
}
