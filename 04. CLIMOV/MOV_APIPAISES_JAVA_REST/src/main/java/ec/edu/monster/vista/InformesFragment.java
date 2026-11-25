package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.ConteoRegion;
import ec.edu.monster.modelos.data.Datos.Informe;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.modelos.data.Datos.PromedioRegion;
import ec.edu.monster.utils.Formato;

/**
 * ECUD7 Informes: país más poblado, gráfico de barras con la cantidad de países por región y las
 * tablas de países por región y promedio poblacional por región.
 */
public class InformesFragment extends PestanaFragment {

    /** Mensaje del servidor sin países guardados (ECUD7, alterno 2). */
    private static final String SIN_PAISES = "No hay países registrados para generar informes";

    private SwipeRefreshLayout refrescar;
    private TextView lblError;
    private View contenido;
    private TextView lblMasPoblado;
    private TextView lblMasPobladoDetalle;
    private GraficoBarrasView grafico;
    private LinearLayout tablaConteo;
    private LinearLayout tablaPromedio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        View vista = inflador.inflate(R.layout.fragment_informes, contenedor, false);
        refrescar = vista.findViewById(R.id.refrescar);
        lblError = vista.findViewById(R.id.lblError);
        contenido = vista.findViewById(R.id.contenido);
        lblMasPoblado = vista.findViewById(R.id.lblMasPoblado);
        lblMasPobladoDetalle = vista.findViewById(R.id.lblMasPobladoDetalle);
        grafico = vista.findViewById(R.id.grafico);
        tablaConteo = vista.findViewById(R.id.tablaConteo);
        tablaPromedio = vista.findViewById(R.id.tablaPromedio);
        refrescar.setColorSchemeResources(R.color.ap_primario);
        refrescar.setOnRefreshListener(this::generar);
        return vista;
    }

    @Override
    protected void alMostrar() {
        generar();
    }

    private void generar() {
        refrescar.setRefreshing(true);
        ejecutar(() -> controlador().informes(), resultado -> {
            refrescar.setRefreshing(false);
            if (!resultado.isExito() || resultado.getDatos() == null) {
                contenido.setVisibility(View.GONE);
                String mensaje = resultado.getMensaje();
                mostrarError(lblError, SIN_PAISES.equals(mensaje)
                        ? mensaje + ". " + getString(R.string.informe_sugerencia) : mensaje);
                return;
            }
            mostrarError(lblError, null);
            mostrar(resultado.getDatos());
        });
    }

    private void mostrar(Informe informe) {
        Pais mayor = informe.getPaisMasPoblado();
        lblMasPoblado.setText(mayor == null ? "—" : mayor.getNombre());
        lblMasPobladoDetalle.setText(mayor == null ? ""
                : Formato.poblacion(mayor.getPoblacion()) + " habitantes · " + Formato.texto(mayor.getRegion()));
        grafico.setDatos(informe.getPaisesPorRegion());

        tablaConteo.removeAllViews();
        for (ConteoRegion conteo : informe.getPaisesPorRegion()) {
            HojaPais.dato(tablaConteo, conteo.getRegion(), String.valueOf(conteo.getCantidad()));
        }
        tablaPromedio.removeAllViews();
        for (PromedioRegion promedio : informe.getPromedioPorRegion()) {
            HojaPais.dato(tablaPromedio, promedio.getRegion(), Formato.poblacion(promedio.getPromedioPoblacion()));
        }
        contenido.setVisibility(View.VISIBLE);
    }
}
