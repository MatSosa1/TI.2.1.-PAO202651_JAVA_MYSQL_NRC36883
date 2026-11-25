package ec.edu.monster.vista;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.AplicacionAPIPaises;
import ec.edu.monster.R;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelos.data.Datos.Sesion;

/**
 * Menú principal (ECUD1 paso 7): Inicio, Países (con Buscar y Filtrar), Consultar en la API e
 * Informes en pestañas inferiores, y «Cerrar sesión» en la barra superior.
 */
public class PrincipalActivity extends AppCompatActivity {

    private static final String PESTANA_INICIO = "inicio";
    private static final String PESTANA_PAISES = "paises";
    private static final String PESTANA_API = "api";
    private static final String PESTANA_INFORMES = "informes";

    private LoginControlador controlador;
    private MaterialToolbar barra;
    private BottomNavigationView barraPestanas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.light(Color.WHITE, Color.WHITE));
        setContentView(R.layout.activity_principal);

        controlador = AplicacionAPIPaises.de(this).crearLoginControlador();
        Sesion sesion = controlador.getSesion();
        if (sesion == null) {
            NavegacionSesion.volverAlLogin(this, null);
            return;
        }

        barra = findViewById(R.id.barraSuperior);
        barra.setSubtitle(sesion.getNombre());
        barra.inflateMenu(R.menu.menu_sesion);
        barra.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.accion_cerrar_sesion) {
                confirmarCierreSesion();
                return true;
            }
            return false;
        });
        AjusteBordes.barraSuperior(barra);

        barraPestanas = findViewById(R.id.barraPestanas);
        barraPestanas.setOnItemSelectedListener(item -> {
            mostrarPestana(item.getItemId());
            return true;
        });
        barraPestanas.setOnItemReselectedListener(item -> { });
        if (savedInstanceState == null) {
            mostrarPestana(R.id.pestana_inicio);
        } else {
            barra.setTitle(titulo(barraPestanas.getSelectedItemId()));
        }

        // Con el teclado abierto se ocultan las pestañas para dejar espacio al formulario
        View raiz = findViewById(R.id.raizPrincipal);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, bordes) -> {
            boolean teclado = bordes.isVisible(WindowInsetsCompat.Type.ime());
            int alto = bordes.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            barraPestanas.setVisibility(teclado ? View.GONE : View.VISIBLE);
            vista.setPadding(0, 0, 0, teclado ? alto : 0);
            return bordes;
        });
    }

    /** Accesos del inicio a las demás pestañas. */
    void irA(@IdRes int pestana) {
        barraPestanas.setSelectedItemId(pestana);
    }

    /** «Buscar países» del inicio: abre Países con el foco en la búsqueda. */
    void irABuscar() {
        irA(R.id.pestana_paises);
        Fragment paises = getSupportFragmentManager().findFragmentByTag(PESTANA_PAISES);
        if (paises instanceof PaisesFragment) {
            ((PaisesFragment) paises).enfocarBusqueda();
        }
    }

    private String titulo(@IdRes int pestana) {
        if (pestana == R.id.pestana_paises) {
            return getString(R.string.pestana_paises);
        }
        if (pestana == R.id.pestana_api) {
            return getString(R.string.titulo_api);
        }
        if (pestana == R.id.pestana_informes) {
            return getString(R.string.pestana_informes);
        }
        return getString(R.string.app_name);
    }

    private void mostrarPestana(@IdRes int pestana) {
        String etiqueta = pestana == R.id.pestana_paises ? PESTANA_PAISES
                : pestana == R.id.pestana_api ? PESTANA_API
                : pestana == R.id.pestana_informes ? PESTANA_INFORMES
                : PESTANA_INICIO;
        barra.setTitle(titulo(pestana));
        FragmentManager gestor = getSupportFragmentManager();
        FragmentTransaction transaccion = gestor.beginTransaction().setReorderingAllowed(true);
        Fragment destino = gestor.findFragmentByTag(etiqueta);
        if (destino == null) {
            destino = crearPestana(etiqueta);
            transaccion.add(R.id.contenedorPestanas, destino, etiqueta);
        }
        for (Fragment abierta : gestor.getFragments()) {
            if (abierta != destino) {
                transaccion.hide(abierta);
            }
        }
        transaccion.show(destino).commitNow();
    }

    private static Fragment crearPestana(String etiqueta) {
        switch (etiqueta) {
            case PESTANA_PAISES:
                return new PaisesFragment();
            case PESTANA_API:
                return new ApiFragment();
            case PESTANA_INFORMES:
                return new InformesFragment();
            default:
                return new InicioFragment();
        }
    }

    private void confirmarCierreSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cerrar_sesion)
                .setMessage(R.string.cerrar_sesion_pregunta)
                .setPositiveButton(R.string.si, (dialogo, boton) -> cerrarSesion())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /** ECUD1 alterno 4: se borra la sesión local y se avisa al servidor en segundo plano. */
    private void cerrarSesion() {
        String token = controlador.cerrarSesion();
        AplicacionAPIPaises.de(this).getTareas().ejecutar(
                () -> controlador.notificarCierre(token), resultado -> { }, error -> { });
        NavegacionSesion.volverAlLogin(this, null);
    }
}
