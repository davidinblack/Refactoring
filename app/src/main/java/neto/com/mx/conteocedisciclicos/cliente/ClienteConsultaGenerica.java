package neto.com.mx.conteocedisciclicos.cliente;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import neto.com.mx.conteocedisciclicos.AppController;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaServicio;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;
import neto.com.mx.conteocedisciclicos.R;

/**
 * Created by dramirezr on 18/01/2018.
 */

public class ClienteConsultaGenerica {
    private final Context contexto;
    private final Activity activity;
    private final String descOperacion;
    private final SolicitudServicio solicitud;
    private String generado;
    private final HandlerRespuestasVolley handler;
    private final String endPointConsulta;
    private ObjectMapper oMapper = new ObjectMapper();
    private boolean manejarCodigoError;

    public ClienteConsultaGenerica(
            String endPoint,
            Context contextoApp,
            AppCompatActivity activityApp,
            String descripcionOperacion,
            SolicitudServicio solicitudGenerica,
            HandlerRespuestasVolley handler)
    {
        this.endPointConsulta = endPoint;
        this.contexto = contextoApp;
        this.activity = activityApp;
        this.descOperacion = descripcionOperacion;
        this.solicitud = solicitudGenerica;
        this.handler = handler;
    }

    public ClienteConsultaGenerica(
            String endPoint,
            Context contextoApp,
            AppCompatActivity activityApp,
            String descripcionOperacion,
            String solicitudG,
            HandlerRespuestasVolley handler)
    {
        this.manejarCodigoError = true;
        this.endPointConsulta = endPoint;
        this.contexto = contextoApp;
        this.activity = activityApp;
        this.descOperacion = descripcionOperacion;
        this.solicitud = null;
        generado = solicitudG;
        this.handler = handler;
    }

    boolean sinDialogos= false;
    public ClienteConsultaGenerica(
            String endPoint,
            Context contextoApp,
            SolicitudServicio solicitud,
            HandlerRespuestasVolley handler)
    {
        sinDialogos = true;
        this.manejarCodigoError = true;
        this.endPointConsulta = endPoint;
        this.contexto = contextoApp;
        this.activity = null;
        this.descOperacion = null;
        this.solicitud = solicitud;
        generado = null;
        this.handler = handler;
    }


    public void setManejarCodigoError(boolean manejarCodigoError) {
        this.manejarCodigoError = manejarCodigoError;
    }

    public void ejecutarConsultaWS() {
        ejecutarConsultaWS(0, 1);
    }
    public void ejecutarConsultaWS(final int idxCodigoError, final int idxMensaje) {
        Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : Creando ConectivityManager...");
        try {
            ConnectivityManager connMgr = (ConnectivityManager)
                    contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : Verificando conexiones...");
            if (networkInfo != null && networkInfo.isConnected()) {
                //Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : Sí hay conexión...");

                final ProgressDialog mDialog = new ProgressDialog(contexto);

                    mDialog.setMessage(descOperacion);
                    mDialog.setCancelable(false);
                    mDialog.setInverseBackgroundForced(false);
                    mDialog.show();

                Log.i(GlobalShare.logAplicaion, endPointConsulta);
                StringRequest strRequest = new StringRequest(
                        Request.Method.POST,
                        endPointConsulta,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String respuesta) {
                                Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : respuesta > "+respuesta);

                                    mDialog.dismiss();

                                ViewDialog dialogo = new ViewDialog(contexto);

                                if (respuesta == null || respuesta.equals("") ) {
                                        dialogo.showDialog(activity,
                                                contexto.getString(R.string.request_sinrespuesta),
                                                null,
                                                TiposAlert.ERROR);
                                    return;
                                }

                                RespuestaServicio respuestaObject = null;
                                try {
                                    respuestaObject = oMapper.readValue(respuesta, RespuestaServicio.class);
                                    RespuestaDinamica resDinamica = oMapper.readValue(respuestaObject.getResultado(), RespuestaDinamica.class);

                                    Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : "+
                                            " idxCodigoError: " + idxCodigoError + ", idxMensaje: " + idxMensaje );
                                    if (idxCodigoError > 0 && idxMensaje > 0)
                                    {

                                        if (!resDinamica.getStackTrace().isEmpty()) {
                                            Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : WebService : "+resDinamica.getStackTrace());
                                                dialogo.showDialog(activity,
                                                        resDinamica.getStackTrace(),
                                                        null,
                                                        TiposAlert.ERROR);
                                            return;
                                        } else if (!resDinamica.getDatosSalida().get(idxCodigoError).equals("0"))
                                        {
                                            Log.w(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : WebService : "+
                                                    " idError: " + resDinamica.getDatosSalida().get(idxCodigoError)+
                                                    " Mensaje: " + resDinamica.getDatosSalida().get(idxMensaje) );
                                            if (manejarCodigoError) {
                                                String mensaje = null;
                                                if (resDinamica.getDatosSalida().get(idxMensaje) != null &&
                                                    resDinamica.getDatosSalida().get(idxMensaje).indexOf('|') >= 0 )
                                                {
                                                    mensaje = resDinamica.getDatosSalida().get(idxMensaje).split(Pattern.quote("|"))[0];
                                                } else {
                                                    mensaje = resDinamica.getDatosSalida().get(idxMensaje);
                                                }
                                                if (mensaje != null) {
                                                    dialogo.showDialog(activity, mensaje, null, TiposAlert.ERROR);
                                                }else{
                                                    Log.d(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : WebService : "+
                                                            "mensaje NULO.");
                                                }
                                                return;
                                            } else {
                                                Log.w(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : solicitud > Código de error mandejado desde el invocador...");
                                            }
                                        }
                                    }

                                    handler.manejarExitoVolley(resDinamica);
                                } catch (IOException e) {
                                    Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : "+e.getMessage(), e);
                                        dialogo.showDialog(activity,
                                                contexto.getString(R.string.error_gen_cliente),
                                                null,
                                                TiposAlert.ERROR);
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMostrar=null;
                        Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onErrorResponse : VolleyError : "+error );
                        if( error.getCause() instanceof ConnectException ){
                            error.getMessage().contains("Connection");
                            errorMostrar = "Se excedio el tiempo de espera para la respuesta.";

                        }else if( error.networkResponse != null )
                        {
                            if( error.networkResponse.statusCode == 500) {
                                errorMostrar = "[500] Error en servidor.";
                            }else if( error.networkResponse.statusCode >= 400) {
                                errorMostrar = "["+error.networkResponse.statusCode+"] Error en cliente.";
                            }
                        }else{
                            errorMostrar = error.getMessage();
                        }

                            mDialog.dismiss();

                            ViewDialog dialogo = new ViewDialog(contexto);
                            dialogo.showDialog(activity, errorMostrar, null, TiposAlert.ERROR);

                        handler.manejarErrorVolley(error);
                    }
                }) {
                    @Override
                    public Map<String, String> getParams() {
                        Map<String, String> parametros = new HashMap<String, String>();

                        if (solicitud == null) {
                            Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : getParams : solicitud > " + generado);
                            parametros.put("solicitud", generado);
                        } else {
                            try {
                                String strSolicitud = oMapper.writeValueAsString(solicitud);
                                parametros.put("solicitud", strSolicitud);

                                Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : getParams : solicitud > " + strSolicitud);
                            } catch (IOException e) {
                                Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : getParams : "+e.getMessage(), e );
                            }
                        }
                        return parametros;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Contet-Type", "text/plain");
                        headers.put("Contet-Encodig", "UTF-8");
                        return headers;
                    }
                };

                strRequest.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                AppController.getInstance().addToRequestQueue(strRequest, "ClienteDinamico");

            } else {
                Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : networkInfo == null ó networkInfo no esta conectado...");
                    ViewDialog alert = new ViewDialog(contexto);
                    alert.showDialog(activity, "No hay conexión HTTP", null, TiposAlert.ERROR);
                    handler.manejarErrorVolley(new VolleyError("No fue posible conectarse a la red",
                            new Exception("networkInfo es null o  networkInfo.isConnected regresó false")));
            }
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : "+e.getMessage(), e);
        }

    }

    public String ejecutarConsultaWSSinDialogos(final int idxCodigoError, final int idxMensaje) {

        Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : Creando ConectivityManager...");
        try {
            ConnectivityManager connMgr = (ConnectivityManager)
                    contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : Verificando conexiones...");
            if (networkInfo != null && networkInfo.isConnected()) {
                //Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : Sí hay conexión...");

                Log.i(GlobalShare.logAplicaion, endPointConsulta);
                StringRequest strRequest = new StringRequest(
                        Request.Method.POST,
                        endPointConsulta,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String respuesta) {
                                Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : respuesta > "+respuesta);

                                if (respuesta == null || respuesta.equals("") ) {
                                    return;
                                }

                                RespuestaServicio respuestaObject = null;
                                try {
                                    respuestaObject = oMapper.readValue(respuesta, RespuestaServicio.class);
                                    RespuestaDinamica resDinamica = oMapper.readValue(respuestaObject.getResultado(), RespuestaDinamica.class);

                                    Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : "+
                                            " idxCodigoError: " + idxCodigoError + ", idxMensaje: " + idxMensaje );
                                    if (idxCodigoError > 0 && idxMensaje > 0)
                                    {

                                        if (!resDinamica.getStackTrace().isEmpty()) {
                                            Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : WebService : "+resDinamica.getStackTrace());
                                            //return;
                                        } else if (!resDinamica.getDatosSalida().get(idxCodigoError).equals("0"))
                                        {
                                            Log.w(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : WebService : "+
                                                    " idError: " + resDinamica.getDatosSalida().get(idxCodigoError)+
                                                    " Mensaje: " + resDinamica.getDatosSalida().get(idxMensaje) );
                                            if (manejarCodigoError) {
                                                String mensaje = null;
                                                if (resDinamica.getDatosSalida().get(idxMensaje) != null &&
                                                        resDinamica.getDatosSalida().get(idxMensaje).indexOf('|') >= 0 )
                                                {
                                                    mensaje = resDinamica.getDatosSalida().get(idxMensaje).split(Pattern.quote("|"))[0];
                                                } else {
                                                    mensaje = resDinamica.getDatosSalida().get(idxMensaje);
                                                }
                                                if (mensaje != null) {

                                                }else{
                                                    Log.d(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : WebService : "+
                                                            "mensaje NULO.");
                                                }
                                                //return;
                                            } else {
                                                Log.w(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : solicitud > Código de error mandejado desde el invocador...");
                                            }
                                        }
                                    }

                                    handler.manejarExitoVolley(resDinamica);
                                } catch (IOException e) {
                                    Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : onResponse : "+e.getMessage(), e);
                                    RespuestaDinamica resDina = new RespuestaDinamica();
                                    Map<Integer, String> datosSalida = new HashMap<>();
                                    datosSalida.put(idxCodigoError, "Excepcion al construir respuesta.");
                                    resDina.setDatosSalida(datosSalida);
                                    resDina.setStackTrace("Error manejado:"+e.getLocalizedMessage());
                                    handler.manejarExitoVolley(new RespuestaDinamica());
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : onErrorResponse : VolleyError : "+error );
                        handler.manejarErrorVolley(error);
                    }
                }) {
                    @Override
                    public Map<String, String> getParams() {
                        Map<String, String> parametros = new HashMap<String, String>();

                        if (solicitud == null) {
                            Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : getParams : solicitud > " + generado);
                            parametros.put("solicitud", generado);
                        } else {
                            try {
                                String strSolicitud = oMapper.writeValueAsString(solicitud);
                                parametros.put("solicitud", strSolicitud);

                                Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : getParams : solicitud > " + strSolicitud);
                            } catch (IOException e) {
                                Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : getParams : "+e.getMessage(), e );
                            }
                        }
                        return parametros;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Contet-Type", "text/plain");
                        headers.put("Contet-Encodig", "UTF-8");
                        return headers;
                    }
                };

                strRequest.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                AppController.getInstance().addToRequestQueue(strRequest, "ClienteDinamico");

            } else {
                Log.i(GlobalShare.logAplicaion, "ejecutarConsultaWS : networkInfo == null ó networkInfo no esta conectado...");
                return "No hay conexión";
            }
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, "ejecutarConsultaWS : "+e.getMessage(), e);
            return "Excepcion durante la construccion de llamada.";
        }
        return null;
    }


}

