package ec.edu.monster.vista;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.utils.Formato;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * ECUD4 Registrar País (hoja inferior): consultar el nombre en la API, elegir un resultado y
 * «Registrar»; «Cancelar» cierra sin enviar nada (alterno 6).
 */
class HojaNuevoPais extends BottomSheetDialog {

    private final TextInputEditText txtConsulta;
    private final TextView lblError;
    private final TextView lblElija;
    private final View prgCargando;
    private final RadioGroup grupoResultados;
    private final View btnConsultar;
    private final View btnRegistrar;
    private String consulta = "";

    HojaNuevoPais(Context contexto, Consumer<String> alConsultar, BiConsumer<String, String> alRegistrar) {
        super(contexto);
        View vista = LayoutInflater.from(contexto).inflate(R.layout.hoja_nuevo, null);
        setContentView(vista);
        getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        getBehavior().setSkipCollapsed(true);

        txtConsulta = vista.findViewById(R.id.txtConsulta);
        lblError = vista.findViewById(R.id.lblError);
        lblElija = vista.findViewById(R.id.lblElija);
        prgCargando = vista.findViewById(R.id.prgCargando);
        grupoResultados = vista.findViewById(R.id.grupoResultados);
        btnConsultar = vista.findViewById(R.id.btnConsultar);
        btnRegistrar = vista.findViewById(R.id.btnRegistrar);

        Runnable consultar = () -> {
            consulta = txtConsulta.getText() == null ? "" : txtConsulta.getText().toString();
            alConsultar.accept(consulta);
        };
        btnConsultar.setOnClickListener(v -> consultar.run());
        txtConsulta.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                consultar.run();
                return true;
            }
            return false;
        });
        btnRegistrar.setOnClickListener(v -> {
            View elegido = grupoResultados.findViewById(grupoResultados.getCheckedRadioButtonId());
            if (elegido != null) {
                alRegistrar.accept(consulta, (String) elegido.getTag());
            }
        });
        vista.findViewById(R.id.btnCancelar).setOnClickListener(v -> dismiss());
    }

    void cargando(boolean activo) {
        prgCargando.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnConsultar.setEnabled(!activo);
        btnRegistrar.setEnabled(!activo && grupoResultados.getChildCount() > 0);
        if (activo) {
            lblError.setVisibility(View.GONE);
        }
    }

    /** ECUD4 Registrar paso 4: resultados para elegir; se habilita «Registrar». */
    void mostrarResultados(List<Pais> paises) {
        grupoResultados.removeAllViews();
        for (int i = 0; i < paises.size(); i++) {
            Pais pais = paises.get(i);
            RadioButton opcion = new RadioButton(getContext());
            opcion.setId(View.generateViewId());
            opcion.setTag(pais.getNombre());
            opcion.setText(pais.getNombre() + "\n" + Formato.texto(pais.getCapital()) + " · "
                    + Formato.texto(pais.getRegion()) + " · " + Formato.poblacion(pais.getPoblacion())
                    + (pais.isGuardado() ? " · " + getContext().getString(R.string.guardado) : ""));
            opcion.setPadding(16, 16, 0, 16);
            grupoResultados.addView(opcion);
            if (i == 0) {
                opcion.setChecked(true);
            }
        }
        lblElija.setVisibility(paises.isEmpty() ? View.GONE : View.VISIBLE);
        btnRegistrar.setEnabled(!paises.isEmpty());
    }

    void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisibility(View.VISIBLE);
    }
}
