package ec.edu.monster.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import ec.edu.monster.R;
import ec.edu.monster.modelos.data.Datos.ConteoRegion;
import java.util.ArrayList;
import java.util.List;

/**
 * Gráfico de barras con la cantidad de países por región (ECUD7, paso 8), dibujado en un Canvas
 * para no depender de librerías externas. Usa los colores de la paleta.
 */
public class GraficoBarrasView extends View {

    private final Paint barra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eje = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint etiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectangulo = new RectF();
    private final List<ConteoRegion> datos = new ArrayList<>();

    public GraficoBarrasView(Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);
        barra.setColor(ContextCompat.getColor(contexto, R.color.ap_primario));
        eje.setColor(ContextCompat.getColor(contexto, R.color.borde_tarjeta));
        eje.setStrokeWidth(dp(1));
        valor.setColor(ContextCompat.getColor(contexto, R.color.texto_oscuro));
        valor.setTextSize(sp(13));
        valor.setFakeBoldText(true);
        valor.setTextAlign(Paint.Align.CENTER);
        etiqueta.setColor(ContextCompat.getColor(contexto, R.color.texto_secundario));
        etiqueta.setTextSize(sp(11));
        etiqueta.setTextAlign(Paint.Align.CENTER);
        setContentDescription(contexto.getString(R.string.informe_grafico));
    }

    public void setDatos(List<ConteoRegion> nuevos) {
        datos.clear();
        if (nuevos != null) {
            datos.addAll(nuevos);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas lienzo) {
        super.onDraw(lienzo);
        if (datos.isEmpty()) {
            return;
        }
        float ancho = getWidth();
        float base = getHeight() - dp(24);
        float altoMaximo = base - dp(24);
        int maximo = 1;
        for (ConteoRegion dato : datos) {
            maximo = Math.max(maximo, dato.getCantidad());
        }
        float columna = ancho / datos.size();
        float anchoBarra = columna * 0.55f;

        lienzo.drawLine(0, base, ancho, base, eje);
        for (int i = 0; i < datos.size(); i++) {
            ConteoRegion dato = datos.get(i);
            float centro = i * columna + columna / 2;
            float alto = dato.getCantidad() / (float) maximo * altoMaximo;
            rectangulo.set(centro - anchoBarra / 2, base - alto, centro + anchoBarra / 2, base);
            lienzo.drawRoundRect(rectangulo, dp(4), dp(4), barra);
            lienzo.drawText(String.valueOf(dato.getCantidad()), centro, base - alto - dp(6), valor);
            CharSequence region = TextUtils.ellipsize(dato.getRegion(), new android.text.TextPaint(etiqueta),
                    columna - dp(4), TextUtils.TruncateAt.END);
            lienzo.drawText(region.toString(), centro, base + dp(16), etiqueta);
        }
    }

    private float dp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics());
    }

    private float sp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, getResources().getDisplayMetrics());
    }
}
