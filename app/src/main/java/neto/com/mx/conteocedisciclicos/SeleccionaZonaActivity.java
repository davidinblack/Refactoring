package neto.com.mx.conteocedisciclicos;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neto.com.mx.conteocedisciclicos.beans.CredencialBean;
import neto.com.mx.conteocedisciclicos.beans.ZonaBean;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaServicio;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;


public class SeleccionaZonaActivity extends AppCompatActivity {
    Intent intentSelArticulo ;
    private ObjectMapper oMapper = new ObjectMapper();
    private int[][] arrgloColores = new int[][]{
            {89, 169, 194},{155, 89, 182},{60,179,113},{100,149,237},
            {75,0,130},{72,61,139},{210,105,30},{188,143,143}
    };

    private Intent getInstanciaIntent(Class cls){
        Intent intent_ = new Intent(this, cls);

        intent_.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent_.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent_.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda")));
        intent_.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));

        intent_.putExtra("idUbicacion", new String(getIntent().getStringExtra("idUbicacion")) );
        intent_.putExtra("nombre_ubicacion", new String(getIntent().getStringExtra("nombre_ubicacion")) );
        intent_.putExtra("esReconteo", (Boolean) getIntent().getBooleanExtra("esReconteo", false));

        return intent_;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_04_selecciona_zona);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        String nombreUbicacion = new String( getIntent().getStringExtra("nombre_ubicacion"));
        int idTienda = Integer.parseInt( getIntent().getStringExtra("idTienda"));

        intentSelArticulo = getInstanciaIntent(SeleccionArticuloActivity.class);

        TextView txtNombreUbicacion = (TextView)findViewById(R.id.txtNombreUbicacion);

        txtNombreUbicacion.setText(nombreUbicacion);

        if( GlobalShare.getInstace().getZonas().size() == 0 ) {
            consultaZonasXTiendaWS(idTienda);
        }else{
            crearBotonesZonas(GlobalShare.getInstace().getZonas());
        }
    }

    private void crearBotonesZonas(List<ZonaBean> zonas){
        try {
            int cantElemtentosXRow = 2;
            GridLayout gridEnScroll = (GridLayout) findViewById(R.id.gridEnScroll);

            int rowActual = 0;
            int colActual = 0;
            for (ZonaBean zona : zonas) {
                Button btnUbi = new Button(this);
                btnUbi.setText(zona.getNombre());
                btnUbi.setTextSize((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 24,
                        getResources().getDisplayMetrics()));
                btnUbi.setAllCaps(false);
                btnUbi.setTextColor(getResources().getColor(R.color.colorBlanco));
                Drawable drawCircular = getResources().getDrawable(R.drawable.boton_circulo_azul).mutate();

                drawCircular.setColorFilter(
                        Color.parseColor(zona.getColor()),
                        PorterDuff.Mode.SRC_ATOP);

                btnUbi.setBackground(drawCircular);
                btnUbi.setWidth((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 100,
                        getResources().getDisplayMetrics()));

                btnUbi.setHeight((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 100,
                        getResources().getDisplayMetrics()));

                btnUbi.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(100);

                        Button b = (Button) v;
                        intentSelArticulo.putExtra("idZona", b.getText());
                        intentSelArticulo.putExtra("nombre_zona", b.getText());

                        if (getIntent().getBooleanExtra("esReconteo", false)) {
                            Intent i = getInstanciaIntent(DetalleConteoActivity.class);
                            i.putExtra("idZona", b.getText());
                            i.putExtra("nombre_zona", b.getText());
                            startActivity(i);
                        } else {
                            startActivity(intentSelArticulo);
                        }

                    }
                });

                if (cantElemtentosXRow == colActual) {
                    rowActual++;
                    colActual = 0;
                }
                GridLayout.LayoutParams layoutParams =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(rowActual, GridLayout.CENTER),
                                GridLayout.spec(colActual++, GridLayout.CENTER)
                        );

                layoutParams.setMargins(20, 10, 20, 10);
                gridEnScroll.addView(
                        btnUbi,
                        layoutParams);

            }
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : crearBotonesZonas > "+e.getMessage(), e);
        }
    }

    public void seleccionaTodasZonas(View v){
        Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(100);

        intentSelArticulo.putExtra("idZona", "0" );
        intentSelArticulo.putExtra("nombre_zona", "Todas las Zonas" );

        if( getIntent().getBooleanExtra( "esReconteo", false) ){
            Intent i = getInstanciaIntent(DetalleConteoActivity.class);
            i.putExtra("idZona", "0" );
            i.putExtra("nombre_zona", "Todas las Zonas" );
            startActivity(i);
        }else{
            startActivity(intentSelArticulo);
        }
    }

    public void consultaZonasXTiendaWS(final int idTienda_) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {


                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Consultando Zonas...");
                mDialog.setCancelable(false);
                mDialog.setInverseBackgroundForced(false);
                mDialog.show();

                final int idxUbicacion = 1, idxCursor = 2, idxCodigoError = 3, idxMensaje = 4;
                StringRequest strRequest = new StringRequest(
                        Request.Method.POST,
                        getString(R.string.EndpointRest),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String respuesta)
                            {
                                Log.i(GlobalShare.logAplicaion, getClass().getName() + " : consultaZonasXTiendaWS : onResponse : " + respuesta);

                                try {
                                    mDialog.dismiss();
                                    ViewDialog dialogo = new ViewDialog(SeleccionaZonaActivity.this);

                                    if (respuesta == null || respuesta.equals("")) {
                                        dialogo.showDialog(SeleccionaZonaActivity.this,
                                                getString(R.string.request_sinrespuesta),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    RespuestaServicio respuestaObject = null;
                                    try {
                                        respuestaObject = oMapper.readValue(respuesta, RespuestaServicio.class);
                                        if (respuestaObject == null || respuestaObject.isError()) {
                                            Log.i(GlobalShare.logAplicaion, getClass().getName()+" : consultaZonasXTiendaWS : onResponse : oMapper 1> " + respuestaObject.getResultado() );
                                            dialogo.showDialog(SeleccionaZonaActivity.this,
                                                    "No se obtubo información de las zonas", null,
                                                    TiposAlert.ERROR);
                                            return;
                                        }
                                    } catch (IOException e) {
                                        dialogo.showDialog(SeleccionaZonaActivity.this,
                                                "Se presentó un error al procesar la respuesta 1.", null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    RespuestaDinamica resDinamica = null;
                                    try {
                                        resDinamica = oMapper.readValue(
                                                respuestaObject.getResultado(), RespuestaDinamica.class);
                                        if (!resDinamica.getStackTrace().isEmpty()) {
                                            Log.i(GlobalShare.logAplicaion, getClass().getName()+" : consultaZonasXTiendaWS: onResponse : oMapper 2>" + resDinamica.getStackTrace());
                                            dialogo.showDialog(SeleccionaZonaActivity.this,
                                                    "Se presentó un error al procesar la respuesta 2.", null,
                                                    TiposAlert.ERROR);
                                            return;
                                        }
                                    } catch (IOException e) {
                                        Log.i(GlobalShare.logAplicaion, getClass().getName()+" : consultaZonasXTiendaWS: onResponse : " + e.getMessage(), e);
                                        dialogo.showDialog(SeleccionaZonaActivity.this,
                                                getString(R.string.loguin_error_gen),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }
                                    //Presentamos el error de central.
                                    if (resDinamica != null && !resDinamica.getDatosSalida().get(idxCodigoError).equals("0")) {
                                        dialogo.showDialog(SeleccionaZonaActivity.this,
                                                resDinamica.getDatosSalida().get(idxMensaje), null,
                                                TiposAlert.ERROR);
                                        return;
                                    }
                                    List<ZonaBean> zonas = new ArrayList<ZonaBean>();

                                    Cursor cursor = resDinamica.getCursoresSalida().get(idxCursor);
                                    if (cursor.getCantRegistros() == 0) {
                                        mostrarDialogoGenerico("No se obtubieron zonas de la consulta.", TiposAlert.ERROR);
                                        return;
                                    }

                                    int idxId = 0, idxDescripcion = 0, idxColor = 1;
                                    for (ArrayList<String> zona : cursor.getRegistros()) {
                                        zonas.add(new ZonaBean(
                                                zona.get(idxId),
                                                zona.get(idxDescripcion),
                                                zona.get(idxColor)));
                                    }
                                    crearBotonesZonas(zonas);
                                    GlobalShare.getInstace().setZonas(zonas);
                                } catch (Exception e) {
                                    Log.e(GlobalShare.logAplicaion, getClass().getName() + " : consultaZonasXTiendaWS : onResponse : " + e.getMessage(), e);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(GlobalShare.logAplicaion, getClass().getName() + " : consultaZonasXTiendaWS > " + error.getMessage(), error);

                                mDialog.dismiss();
                                ViewDialog dialogo = new ViewDialog(SeleccionaZonaActivity.this);
                                dialogo.showDialog(SeleccionaZonaActivity.this, error.getMessage(), null, TiposAlert.ERROR);
                            }
                        }) {
                    @Override
                    public Map<String, String> getParams() {
                        Map<String, String> parametros = new HashMap<String, String>();

                        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
                        CuerpoPeticion.add(new ParametroCuerpo(1, "long", "" + idTienda_));
                        CuerpoPeticion.add(new ParametroCuerpo(2, "CUR_SALIDA", "0"));
                        CuerpoPeticion.add(new ParametroCuerpo(3, "::Int", "0"));
                        CuerpoPeticion.add(new ParametroCuerpo(4, "::String", "0"));

                        SolicitudServicio solicitud = new SolicitudServicio("CONSZONASXTIENDA", CuerpoPeticion);

                        try {
                            String strSolicitud = oMapper.writeValueAsString(solicitud);
                            parametros.put("solicitud", strSolicitud);

                            Log.i(GlobalShare.logAplicaion, getClass().getName() + " : consultaZonasXTiendaWS : getParams > " + strSolicitud);
                        } catch (IOException e) {
                            Log.e(GlobalShare.logAplicaion, getClass().getName() + " : consultaZonasXTiendaWS : getParams > " + e.getMessage(), e);
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
                        50000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                AppController.getInstance().addToRequestQueue(strRequest, "tag");


            } else {
                // Mostrar errores
                EditText editText = (EditText) findViewById(R.id.folioPedidoText);
                editText.setInputType(InputType.TYPE_NULL);
                editText.requestFocus();
                editText.setText("");
                ViewDialog alert = new ViewDialog(SeleccionaZonaActivity.this);
                alert.showDialog(SeleccionaZonaActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
            }
        } catch(Exception me) {
            Log.e("Logueo", "Error consultando zonas: " + me.getMessage());
            ViewDialog alert = new ViewDialog(SeleccionaZonaActivity.this);
            alert.showDialog(SeleccionaZonaActivity.this, "URL no disponible: validar conexión a Internet.", null, TiposAlert.ERROR);
        }
    }

    private void mostrarDialogoGenerico(String mensaje, TiposAlert tipo) {
        ViewDialog alert = new ViewDialog(SeleccionaZonaActivity.this);
        alert.showDialog(SeleccionaZonaActivity.this, mensaje, null, tipo);
    }

    public void regresarSeleccionUbicacion(View view) {
        Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(100);

        Boolean esReconteo =(Boolean) getIntent().getBooleanExtra("esReconteo", false);
        if( esReconteo ){
            Intent intent = getInstanciaIntent(AvanceInventarioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            Intent intent = getInstanciaIntent(SeleccionaUbicacionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
