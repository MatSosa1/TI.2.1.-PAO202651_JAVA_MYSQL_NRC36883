package ec.edu.monster.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.utils.Formato;

/** Detalle de un país guardado (hoja inferior) con Actualizar y Eliminar (ECUD4). */
class HojaPais extends BottomSheetDialog {

    private final TextView lblError;
    private final View prgCargando;
    private final View btnActualizar;
    private final View btnEliminar;

    HojaPais(Context contexto, Pais pais, Runnable alActualizar, Runnable alEliminar) {
        super(contexto);
        View vista = LayoutInflater.from(contexto).inflate(R.layout.hoja_pais, null);
        setContentView(vista);

        ((TextView) vista.findViewById(R.id.lblNombre)).setText(pais.getNombre());
        LinearLayout datos = vista.findViewById(R.id.datos);
        dato(datos, contexto.getString(R.string.dato_capital), Formato.texto(pais.getCapital()));
        dato(datos, contexto.getString(R.string.dato_region), Formato.texto(pais.getRegion()));
        dato(datos, contexto.getString(R.string.dato_poblacion), Formato.poblacion(pais.getPoblacion()));
        dato(datos, contexto.getString(R.string.dato_moneda), Formato.texto(pais.getMoneda()));

        lblError = vista.findViewById(R.id.lblError);
        prgCargando = vista.findViewById(R.id.prgCargando);
        btnActualizar = vista.findViewById(R.id.btnActualizar);
        btnEliminar = vista.findViewById(R.id.btnEliminar);
        btnActualizar.setOnClickListener(v -> alActualizar.run());
        btnEliminar.setOnClickListener(v -> alEliminar.run());
    }

    void cargando(boolean activo) {
        prgCargando.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnActualizar.setEnabled(!activo);
        btnEliminar.setEnabled(!activo);
        if (activo) {
            lblError.setVisibility(View.GONE);
        }
    }

    void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisibility(View.VISIBLE);
    }

    /** Fila «etiqueta · valor» (item_dato de EurekaBank). */
    static void dato(ViewGroup contenedor, String etiqueta, String valor) {
        View fila = LayoutInflater.from(contenedor.getContext()).inflate(R.layout.item_dato, contenedor, false);
        ((TextView) fila.findViewById(R.id.lblEtiqueta)).setText(etiqueta);
        ((TextView) fila.findViewById(R.id.lblValor)).setText(valor);
        contenedor.addView(fila);
    }
}
