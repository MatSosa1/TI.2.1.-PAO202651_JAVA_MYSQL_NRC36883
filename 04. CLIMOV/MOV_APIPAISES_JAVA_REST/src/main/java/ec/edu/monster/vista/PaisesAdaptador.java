package ec.edu.monster.vista;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.utils.Formato;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lista de países. En «Países» al tocar una fila se abre su detalle; en la consulta a la API
 * cada fila muestra «Guardado» y el botón «Guardar» (ECUD3 paso 7, ECUD2).
 */
class PaisesAdaptador extends RecyclerView.Adapter<PaisesAdaptador.Fila> {

    private final boolean modoApi;
    private final Consumer<Pais> alElegir;
    private final List<Pais> paises = new ArrayList<>();

    PaisesAdaptador(boolean modoApi, Consumer<Pais> alElegir) {
        this.modoApi = modoApi;
        this.alElegir = alElegir;
    }

    void mostrar(List<Pais> nuevos) {
        paises.clear();
        if (nuevos != null) {
            paises.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    void marcarGuardado(String nombre) {
        for (int i = 0; i < paises.size(); i++) {
            if (paises.get(i).getNombre().equals(nombre)) {
                paises.get(i).setGuardado(true);
                notifyItemChanged(i);
            }
        }
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_pais, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Pais pais = paises.get(posicion);
        fila.nombre.setText(pais.getNombre());
        fila.detalle.setText(Formato.texto(pais.getCapital()) + " · " + Formato.texto(pais.getRegion()));
        fila.moneda.setText(Formato.texto(pais.getMoneda()));
        fila.poblacion.setText(Formato.poblacion(pais.getPoblacion()));

        if (modoApi) {
            boolean guardado = pais.isGuardado();
            fila.guardado.setVisibility(View.VISIBLE);
            fila.guardado.setText(guardado ? R.string.guardado : R.string.no_guardado);
            int texto = ContextCompat.getColor(fila.itemView.getContext(), guardado ? R.color.ingreso : R.color.texto_secundario);
            int fondo = ContextCompat.getColor(fila.itemView.getContext(), guardado ? R.color.ingreso_fondo : R.color.fondo_app);
            fila.guardado.setTextColor(texto);
            fila.guardado.setBackgroundTintList(ColorStateList.valueOf(fondo));
            fila.guardar.setVisibility(View.VISIBLE);
            fila.guardar.setOnClickListener(v -> alElegir.accept(pais));
            fila.itemView.setOnClickListener(null);
            fila.itemView.setClickable(false);
        } else {
            fila.guardado.setVisibility(View.GONE);
            fila.guardar.setVisibility(View.GONE);
            fila.itemView.setOnClickListener(v -> alElegir.accept(pais));
        }
    }

    @Override
    public int getItemCount() {
        return paises.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView detalle;
        final TextView moneda;
        final TextView poblacion;
        final TextView guardado;
        final MaterialButton guardar;

        Fila(View vista) {
            super(vista);
            nombre = vista.findViewById(R.id.lblNombre);
            detalle = vista.findViewById(R.id.lblDetalle);
            moneda = vista.findViewById(R.id.lblMoneda);
            poblacion = vista.findViewById(R.id.lblPoblacion);
            guardado = vista.findViewById(R.id.lblGuardado);
            guardar = vista.findViewById(R.id.btnGuardar);
        }
    }
}
