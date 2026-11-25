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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ValidadorDatos;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.utils.Formato;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * ECUD4 Gestionar Países con ECUD5 Buscar y ECUD6 Filtrar: búsqueda por nombre y capital, lista,
 * filtros por región en la parte inferior (filtran al elegir) y «Nuevo». Al tocar un país se
 * abre su detalle con Actualizar y Eliminar.
 */
public class PaisesFragment extends PestanaFragment {

    private TextInputEditText txtNombre;
    private TextInputEditText txtCapital;
    private TextView lblTotal;
    private TextView lblError;
    private SwipeRefreshLayout refrescar;
    private ChipGroup grupoRegiones;
    private PaisesAdaptador adaptador;
    private final List<String> opcionesRegion = new ArrayList<>();
    private HojaPais hojaAbierta;
    private HojaNuevoPais hojaNuevo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflador, @Nullable ViewGroup contenedor, @Nullable Bundle estado) {
        View vista = inflador.inflate(R.layout.fragment_paises, contenedor, false);
        txtNombre = vista.findViewById(R.id.txtNombre);
        txtCapital = vista.findViewById(R.id.txtCapital);
        lblTotal = vista.findViewById(R.id.lblTotal);
        lblError = vista.findViewById(R.id.lblError);
        refrescar = vista.findViewById(R.id.refrescar);
        grupoRegiones = vista.findViewById(R.id.grupoRegiones);
        refrescar.setColorSchemeResources(R.color.ap_primario);
        refrescar.setOnRefreshListener(this::buscar);

        RecyclerView lista = vista.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        adaptador = new PaisesAdaptador(false, this::abrirDetalle);
        lista.setAdapter(adaptador);

        TextView.OnEditorActionListener alBuscar = (v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) {
                ocultarTeclado(v);
                buscar();
                return true;
            }
            return false;
        };
        txtNombre.setOnEditorActionListener(alBuscar);
        txtCapital.setOnEditorActionListener(alBuscar);

        crearFiltros(inflador);
        vista.findViewById(R.id.btnNuevo).setOnClickListener(v -> abrirNuevo());
        return vista;
    }

    /** Lista de regiones: «Todas» y las seis del catálogo; al elegir se filtra de inmediato (ECUD6). */
    private void crearFiltros(LayoutInflater inflador) {
        opcionesRegion.add("");
        opcionesRegion.addAll(ValidadorDatos.REGIONES);
        for (int i = 0; i < opcionesRegion.size(); i++) {
            Chip chip = new Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle);
            chip.setId(View.generateViewId());
            chip.setText(i == 0 ? getString(R.string.todas) : opcionesRegion.get(i));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(R.color.chip_fondo);
            chip.setTextColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.chip_texto));
            chip.setChipStrokeColorResource(R.color.chip_borde);
            chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
            chip.setTag(opcionesRegion.get(i));
            grupoRegiones.addView(chip);
            if (i == 0) {
                chip.setChecked(true);
            }
        }
        grupoRegiones.setOnCheckedStateChangeListener((grupo, ids) -> buscar());
    }

    @Override
    protected void alMostrar() {
        buscar();
    }

    /** Pone el foco en la búsqueda (acceso «Buscar países» del inicio). */
    void enfocarBusqueda() {
        if (txtNombre != null) {
            txtNombre.requestFocus();
        }
    }

    private String regionElegida() {
        View chip = grupoRegiones.findViewById(grupoRegiones.getCheckedChipId());
        return chip == null ? "" : (String) chip.getTag();
    }

    /** Listar, Buscar o Filtrar según los criterios escritos y la región elegida. */
    private void buscar() {
        String nombre = texto(txtNombre);
        String capital = texto(txtCapital);
        String region = regionElegida();
        boolean conCriterios = !nombre.isEmpty() || !capital.isEmpty();
        refrescar.setRefreshing(true);
        ejecutar(() -> controlador().buscar(nombre, capital, region), resultado -> {
            refrescar.setRefreshing(false);
            if (!resultado.isExito()) {
                mostrarError(lblError, resultado.getMensaje());
                return;
            }
            mostrarError(lblError, null);
            List<Pais> paises = resultado.getDatos();
            adaptador.mostrar(paises);
            if (paises.isEmpty()) {
                lblTotal.setText(conCriterios ? R.string.sin_resultados
                        : region.isEmpty() ? R.string.sin_paises : R.string.sin_paises_region);
            } else {
                lblTotal.setText(Formato.total(paises.size()));
            }
        });
    }

    private void abrirDetalle(Pais pais) {
        hojaAbierta = new HojaPais(requireContext(), pais, () -> actualizar(pais), () -> confirmarEliminar(pais));
        hojaAbierta.show();
    }

    /** ECUD4 Actualizar: el servidor vuelve a consultar la API. */
    private void actualizar(Pais pais) {
        hojaAbierta.cargando(true);
        operar(() -> controlador().actualizar(pais.getNombre()));
    }

    /** ECUD4 Eliminar pasos 2 y 3: confirmación antes de enviar. */
    private void confirmarEliminar(Pais pais) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar)
                .setMessage(getString(R.string.eliminar_pregunta, pais.getNombre()))
                .setPositiveButton(R.string.eliminar, (dialogo, boton) -> {
                    hojaAbierta.cargando(true);
                    operar(() -> controlador().eliminar(pais.getNombre()));
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private <T> void operar(Callable<Resultado<T>> operacion) {
        ejecutar(operacion, resultado -> {
            if (resultado.isExito()) {
                hojaAbierta.dismiss();
                avisar(resultado.getMensaje());
                buscar();
            } else {
                hojaAbierta.cargando(false);
                hojaAbierta.mostrarError(resultado.getMensaje());
            }
        });
    }

    /** ECUD4 Registrar: hoja con la consulta a la API y «Registrar». */
    private void abrirNuevo() {
        hojaNuevo = new HojaNuevoPais(requireContext(),
                consulta -> {
                    hojaNuevo.cargando(true);
                    ejecutar(() -> controlador().consultarApi(consulta), resultado -> {
                        hojaNuevo.cargando(false);
                        if (resultado.isExito()) {
                            hojaNuevo.mostrarResultados(resultado.getDatos());
                        } else {
                            hojaNuevo.mostrarError(resultado.getMensaje());
                        }
                    });
                },
                (consulta, nombre) -> {
                    hojaNuevo.cargando(true);
                    ejecutar(() -> controlador().registrar(consulta, nombre), resultado -> {
                        hojaNuevo.cargando(false);
                        if (resultado.isExito()) {
                            // ECUD4 Registrar paso 9: mensaje, se cierra el formulario y se recarga
                            hojaNuevo.dismiss();
                            avisar(resultado.getMensaje());
                            buscar();
                        } else {
                            hojaNuevo.mostrarError(resultado.getMensaje());
                        }
                    });
                });
        hojaNuevo.show();
    }

    private void ocultarTeclado(View vista) {
        InputMethodManager teclado = requireContext().getSystemService(InputMethodManager.class);
        if (teclado != null) {
            teclado.hideSoftInputFromWindow(vista.getWindowToken(), 0);
        }
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
