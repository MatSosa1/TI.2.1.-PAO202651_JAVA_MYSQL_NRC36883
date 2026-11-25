package ec.edu.monster.vista;

import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import ec.edu.monster.AplicacionAPIPaises;
import ec.edu.monster.controlador.ControladorConSesion;
import ec.edu.monster.controlador.PaisesControlador;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.servicios.EjecutorTareas;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Pestaña de la pantalla principal: revisa la sesión cada vez que se muestra, ejecuta las
 * operaciones en segundo plano y vuelve al inicio de sesión si el servidor rechaza el token.
 */
public abstract class PestanaFragment extends Fragment {

    private PaisesControlador controlador;

    /** Se llama cada vez que la pestaña se vuelve visible con una sesión vigente. */
    protected abstract void alMostrar();

    protected PaisesControlador controlador() {
        if (controlador == null) {
            controlador = AplicacionAPIPaises.de(requireContext()).crearPaisesControlador();
        }
        return controlador;
    }

    protected EjecutorTareas tareas() {
        return AplicacionAPIPaises.de(requireContext()).getTareas();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            verificarSesionYMostrar();
        }
    }

    @Override
    public void onHiddenChanged(boolean oculto) {
        super.onHiddenChanged(oculto);
        if (!oculto && isResumed()) {
            verificarSesionYMostrar();
        }
    }

    private void verificarSesionYMostrar() {
        if (!controlador().haySesion()) {
            NavegacionSesion.volverAlLogin(getActivity(), ControladorConSesion.MENSAJE_SESION_NO_VALIDA);
            return;
        }
        alMostrar();
    }

    /**
     * Ejecuta la operación en segundo plano y entrega el resultado en el hilo principal solo si la
     * vista sigue activa. Con la sesión vencida vuelve al inicio de sesión (ECUD8).
     */
    protected <T> void ejecutar(Callable<Resultado<T>> operacion, Consumer<Resultado<T>> alTerminar) {
        tareas().ejecutar(operacion, resultado -> {
            if (!vistaActiva()) {
                return;
            }
            if (resultado.isSesionExpirada()) {
                NavegacionSesion.volverAlLogin(getActivity(), resultado.getMensaje());
                return;
            }
            alTerminar.accept(resultado);
        }, error -> {
            if (vistaActiva()) {
                alTerminar.accept(Resultado.error("Ocurrió un error inesperado, intente nuevamente"));
            }
        });
    }

    protected boolean vistaActiva() {
        return isAdded() && getView() != null;
    }

    protected PrincipalActivity principal() {
        return (PrincipalActivity) requireActivity();
    }

    /** Aviso de error en la pantalla (ERS: mensajes de error destacados). */
    protected static void mostrarError(TextView aviso, String mensaje) {
        aviso.setText(mensaje);
        aviso.setVisibility(mensaje == null || mensaje.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Mensaje de confirmación breve (por ejemplo «País eliminado correctamente»). */
    protected void avisar(String mensaje) {
        View raiz = getView();
        if (raiz != null && mensaje != null && !mensaje.isEmpty()) {
            Snackbar aviso = Snackbar.make(raiz, mensaje, Snackbar.LENGTH_LONG);
            // Sobre las pestañas inferiores, para no taparlas
            View pestanas = requireActivity().findViewById(ec.edu.monster.R.id.barraPestanas);
            if (pestanas != null && pestanas.isShown()) {
                aviso.setAnchorView(pestanas);
            }
            aviso.show();
        }
    }
}
