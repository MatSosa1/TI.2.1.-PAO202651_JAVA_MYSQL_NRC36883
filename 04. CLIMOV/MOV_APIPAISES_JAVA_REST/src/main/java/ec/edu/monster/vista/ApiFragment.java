package ec.edu.monster.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.Pais;

/**
 * ECUD3 Consultar País en la API: resultados con «Guardado» y «Guardar» (ECUD2, con
 * confirmación) e «Importar todos».
 */
public class ApiFragment extends PestanaFragment {

    private TextInputEditText txtNombre;
    private TextView lblError;
    private TextView lblAyuda;
    private MaterialButton btnConsultar;
    private MaterialButton btnImportarTodos;
    private CircularProgressIndicator prgCargando;
    private PaisesAdaptador adaptador;
    private String consulta = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        View vista = inflador.inflate(R.layout.fragment_api, contenedor, false);
        txtNombre = vista.findViewById(R.id.txtNombre);
        lblError = vista.findViewById(R.id.lblError);
        lblAyuda = vista.findViewById(R.id.lblAyuda);
        btnConsultar = vista.findViewById(R.id.btnConsultar);
        btnImportarTodos = vista.findViewById(R.id.btnImportarTodos);
        prgCargando = vista.findViewById(R.id.prgCargando);

        RecyclerView lista = vista.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        adaptador = new PaisesAdaptador(true, this::confirmarGuardar);
        lista.setAdapter(adaptador);

        btnConsultar.setOnClickListener(v -> consultar());
        txtNombre.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                consultar();
                return true;
            }
            return false;
        });
        btnImportarTodos.setOnClickListener(v -> confirmarImportarTodos());
        return vista;
    }

    @Override
    protected void alMostrar() {
        // La consulta se hace a pedido del usuario
    }

    private void consultar() {
        consulta = txtNombre.getText() == null ? "" : txtNombre.getText().toString();
        ocultarTeclado();
        cargando(true);
        ejecutar(() -> controlador().consultarApi(consulta), resultado -> {
            cargando(false);
            if (resultado.isExito()) {
                mostrarError(lblError, null);
                lblAyuda.setVisibility(View.GONE);
                adaptador.mostrar(resultado.getDatos());
            } else {
                // ECUD3 alterno 2: se conserva el texto para corregirlo
                mostrarError(lblError, resultado.getMensaje());
            }
        });
    }

    /** ECUD2 pasos 2 y 3. */
    private void confirmarGuardar(Pais pais) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.guardar)
                .setMessage(getString(R.string.guardar_pregunta, pais.getNombre()))
                .setPositiveButton(R.string.guardar, (dialogo, boton) -> guardar(pais))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void guardar(Pais pais) {
        cargando(true);
        ejecutar(() -> controlador().importar(consulta, pais.getNombre()), resultado -> {
            cargando(false);
            if (resultado.isExito()) {
                mostrarError(lblError, null);
                adaptador.marcarGuardado(pais.getNombre());
                avisar(resultado.getMensaje());
            } else {
                mostrarError(lblError, resultado.getMensaje());
            }
        });
    }

    private void confirmarImportarTodos() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.importar_todos)
                .setMessage(R.string.importar_todos_pregunta)
                .setPositiveButton(R.string.importar_todos, (dialogo, boton) -> importarTodos())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void importarTodos() {
        cargando(true);
        ejecutar(() -> controlador().importarTodos(), resultado -> {
            cargando(false);
            if (resultado.isExito()) {
                mostrarError(lblError, null);
                avisar(resultado.getMensaje());
            } else {
                mostrarError(lblError, resultado.getMensaje());
            }
        });
    }

    private void cargando(boolean activo) {
        prgCargando.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnConsultar.setText(activo ? "" : getString(R.string.consultar));
        btnConsultar.setEnabled(!activo);
        btnImportarTodos.setEnabled(!activo);
    }

    private void ocultarTeclado() {
        InputMethodManager teclado = requireContext().getSystemService(InputMethodManager.class);
        if (teclado != null) {
            teclado.hideSoftInputFromWindow(txtNombre.getWindowToken(), 0);
        }
    }
}
