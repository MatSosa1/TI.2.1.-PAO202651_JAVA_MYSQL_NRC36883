package ec.edu.monster.servicios;

import android.content.Context;
import android.net.ConnectivityManager;
import ec.edu.monster.modelos.interfaces.VerificadorRed;

/** Red activa según Android. Ante cualquier duda se intenta la petición igual. */
public class RedAndroid implements VerificadorRed {

    private final ConnectivityManager conectividad;

    public RedAndroid(Context contexto) {
        conectividad = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean hayRed() {
        try {
            return conectividad == null || conectividad.getActiveNetwork() != null;
        } catch (RuntimeException e) {
            return true;
        }
    }
}
