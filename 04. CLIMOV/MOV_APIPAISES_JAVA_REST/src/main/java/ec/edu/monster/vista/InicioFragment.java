package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.Sesion;

/** Inicio: saludo, cantidad de países guardados y accesos a las opciones del menú. */
public class InicioFragment extends PestanaFragment {

    private TextView lblNombre;
    private TextView lblCantidad;
    private TextView lblError;
    private SwipeRefreshLayout refrescar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        View vista = inflador.inflate(R.layout.fragment_inicio, contenedor, false);
        lblNombre = vista.findViewById(R.id.lblNombre);
        lblCantidad = vista.findViewById(R.id.lblCantidad);
        lblError = vista.findViewById(R.id.lblError);
        refrescar = vista.findViewById(R.id.refrescar);
        refrescar.setColorSchemeResources(R.color.ap_primario);
        refrescar.setOnRefreshListener(this::cargar);

        accion(vista.findViewById(R.id.accionPaises), R.drawable.ic_lista, R.string.accion_paises,
                R.string.accion_paises_detalle, () -> principal().irA(R.id.pestana_paises));
        accion(vista.findViewById(R.id.accionApi), R.drawable.ic_nube, R.string.accion_api,
                R.string.accion_api_detalle, () -> principal().irA(R.id.pestana_api));
        accion(vista.findViewById(R.id.accionBuscar), R.drawable.ic_buscar, R.string.accion_buscar,
                R.string.accion_buscar_detalle, () -> principal().irABuscar());
        accion(vista.findViewById(R.id.accionInformes), R.drawable.ic_grafico, R.string.accion_informes,
                R.string.accion_informes_detalle, () -> principal().irA(R.id.pestana_informes));
        return vista;
    }

    @Override
    protected void alMostrar() {
        Sesion sesion = controlador().getSesion();
        lblNombre.setText(sesion == null ? "" : sesion.getNombre());
        cargar();
    }

    private void cargar() {
        refrescar.setRefreshing(true);
        ejecutar(() -> controlador().buscar(null, null, null), resultado -> {
            refrescar.setRefreshing(false);
            if (resultado.isExito()) {
                lblCantidad.setText(String.valueOf(resultado.getDatos().size()));
                mostrarError(lblError, null);
            } else {
                lblCantidad.setText("—");
                mostrarError(lblError, resultado.getMensaje());
            }
        });
    }

    private static void accion(View fila, @DrawableRes int icono, @StringRes int titulo, @StringRes int detalle,
            Runnable alTocar) {
        ((ImageView) fila.findViewById(R.id.imgAccion)).setImageResource(icono);
        ((TextView) fila.findViewById(R.id.lblAccion)).setText(titulo);
        ((TextView) fila.findViewById(R.id.lblAccionDetalle)).setText(detalle);
        fila.setOnClickListener(v -> alTocar.run());
    }
}
