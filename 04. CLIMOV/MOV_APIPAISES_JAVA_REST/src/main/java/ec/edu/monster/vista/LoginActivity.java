package ec.edu.monster.vista;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import ec.edu.monster.AplicacionAPIPaises;
import ec.edu.monster.BuildConfig;
import ec.edu.monster.R;
import ec.edu.monster.controlador.LoginControlador;

/** ECUD1 Iniciar Sesión: usuario y clave (oculta), con indicador de progreso mientras se valida. */
public class LoginActivity extends AppCompatActivity {

    private LoginControlador controlador;
    private TextInputLayout tilUsuario;
    private TextInputLayout tilClave;
    private TextInputEditText txtUsuario;
    private TextInputEditText txtClave;
    private TextView lblError;
    private MaterialButton btnIngresar;
    private CircularProgressIndicator prgCargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.light(Color.WHITE, Color.WHITE));
        setContentView(R.layout.activity_login);
        AjusteBordes.contenidoInferior(findViewById(R.id.contenido));

        controlador = AplicacionAPIPaises.de(this).crearLoginControlador();
        if (controlador.haySesion()) {
            abrirPrincipal();
            return;
        }

        tilUsuario = findViewById(R.id.tilUsuario);
        tilClave = findViewById(R.id.tilClave);
        txtUsuario = findViewById(R.id.txtUsuario);
        txtClave = findViewById(R.id.txtClave);
        lblError = findViewById(R.id.lblError);
        btnIngresar = findViewById(R.id.btnIngresar);
        prgCargando = findViewById(R.id.prgCargando);
        TextView lblServidor = findViewById(R.id.lblServidor);
        lblServidor.setText(getString(R.string.login_servidor, BuildConfig.URL_SERVIDOR));

        btnIngresar.setOnClickListener(v -> ingresar());
        txtClave.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_GO) {
                ingresar();
                return true;
            }
            return false;
        });
    }

    private void ingresar() {
        String usuario = texto(txtUsuario);
        String clave = texto(txtClave);
        tilUsuario.setError(null);
        tilClave.setError(null);
        lblError.setVisibility(View.GONE);

        // ECUD1 alterno 2: con un campo vacío no se envía la petición
        LoginControlador.Campo campo = controlador.campoInvalido(usuario, clave);
        if (campo != null) {
            (campo == LoginControlador.Campo.USUARIO ? tilUsuario : tilClave).setError(LoginControlador.mensaje(campo));
            return;
        }

        cargando(true);
        AplicacionAPIPaises.de(this).getTareas().ejecutar(() -> controlador.iniciarSesion(usuario, clave), resultado -> {
            cargando(false);
            if (resultado.isExito()) {
                abrirPrincipal();
                return;
            }
            // ECUD1 alternos 1 y 3: mensaje, se borra la clave y se mantiene el formulario
            lblError.setText(resultado.getMensaje());
            lblError.setVisibility(View.VISIBLE);
            txtClave.setText("");
        }, error -> {
            cargando(false);
            lblError.setText(R.string.error_inesperado);
            lblError.setVisibility(View.VISIBLE);
        });
    }

    private void cargando(boolean activo) {
        btnIngresar.setEnabled(!activo);
        btnIngresar.setText(activo ? "" : getString(R.string.login_ingresar));
        prgCargando.setVisibility(activo ? View.VISIBLE : View.GONE);
        txtUsuario.setEnabled(!activo);
        txtClave.setEnabled(!activo);
    }

    private void abrirPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString();
    }
}
