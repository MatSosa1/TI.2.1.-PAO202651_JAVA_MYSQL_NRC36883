package ec.edu.monster;

import android.app.Application;
import android.content.Context;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.controlador.PaisesControlador;
import ec.edu.monster.modelos.repositorios.ApiPaisesRepositorio;
import ec.edu.monster.modelos.repositorios.ClienteRest;
import ec.edu.monster.modelos.repositorios.TransporteHttp;
import ec.edu.monster.servicios.EjecutorTareas;
import ec.edu.monster.servicios.RedAndroid;
import ec.edu.monster.servicios.SesionServicio;
import java.util.concurrent.Executors;

/** Raíz de composición: crea una sola vez la sesión, el repositorio y el ejecutor de tareas. */
public class AplicacionAPIPaises extends Application {

    private SesionServicio sesion;
    private ApiPaisesRepositorio repositorio;
    private EjecutorTareas tareas;

    public static AplicacionAPIPaises de(Context contexto) {
        return (AplicacionAPIPaises) contexto.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sesion = new SesionServicio();
        repositorio = new ApiPaisesRepositorio(
                new ClienteRest(BuildConfig.URL_SERVIDOR, new TransporteHttp(), new RedAndroid(this)));
        tareas = new EjecutorTareas(Executors.newCachedThreadPool());
    }

    public EjecutorTareas getTareas() {
        return tareas;
    }

    public LoginControlador crearLoginControlador() {
        return new LoginControlador(repositorio, sesion);
    }

    public PaisesControlador crearPaisesControlador() {
        return new PaisesControlador(repositorio, sesion);
    }
}
