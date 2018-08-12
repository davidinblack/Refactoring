package neto.com.mx.conteocedisciclicos.cliente;

import com.android.volley.VolleyError;

import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaServicio;

/**
 * Created by dramirezr on 18/01/2018.
 */

public interface HandlerRespuestasVolley {
    public void manejarExitoVolley(RespuestaDinamica respuesta);
    public void manejarErrorVolley(VolleyError error);
}
