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
import neto.com.mx.conteocedisciclicos.beans.UbicacionBean;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaServicio;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;
import neto.com.mx.conteocedisciclicos.R;


public class SeleccionaUbicacionActivity extends AppCompatActivity {

    private ObjectMapper oMapper = new ObjectMapper();
    private int[][] arregloColores = new int[][]{
            {155, 89, 182},{64, 134, 194},{60,179,113},{100,149,237},
            {75,0,130},{72,61,139},{210,105,30},{188,143,143}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_03_selecciona_ubicacion);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        if( GlobalShare.getInstace().getUbicaciones().size() == 0 ) {
            consultaUbicacionesWS();
        }else{
            crearBotonesUbicacion( GlobalShare.getInstace().getUbicaciones() );
        }
    }

    private void crearBotonesUbicacion(List<UbicacionBean> ubicaciones){
        try {
            GridLayout gridEnScroll = (GridLayout) findViewById(R.id.gridEnScroll);

            int rowActual = 0;
            for (UbicacionBean ubica : ubicaciones) {
                Button btnUbi = new Button(this);
                btnUbi.setId(ubica.getId());
                btnUbi.setText(ubica.getNombre());
                btnUbi.setTextSize(24);
                btnUbi.setAllCaps(false);
                btnUbi.setTextColor(getResources().getColor(R.color.colorBlanco));
                Drawable drawCircular = getResources().getDrawable(R.drawable.boton_circulo_azul).mutate();

                drawCircular.setColorFilter(
                        Color.rgb(arregloColores[rowActual][0],
                                arregloColores[rowActual][1],
                                arregloColores[rowActual][2]),
                        PorterDuff.Mode.SRC_ATOP);

                btnUbi.setBackground(drawCircular);
                btnUbi.setWidth((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 150,
                        getResources().getDisplayMetrics()));

                btnUbi.setHeight((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 150,
                        getResources().getDisplayMetrics()));

                btnUbi.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(100);

                        Button b = (Button) v;

                        Log.i(SeleccionaUbicacionActivity.class.getName(),
                                "Ubicación seleccionada idUbicacion: " + b.getId() + ", nombre_ubicacion: " + b.getText());

                        Intent intent = null;
                        //if (b.getText().toString().toLowerCase().trim().equals("reserva")) {
                            intent = getInstanciaIntent(SeleccionArticuloActivity.class);
                            intent.putExtra("idZona", new String("0"));
                            intent.putExtra("nombre_zona", new String("Todas las zonas"));
                        //} else if (b.getText().toString().toLowerCase().trim().equals("activo")) {
                        //    intent = getInstanciaIntent(SeleccionaZonaActivity.class);
                        //}

                        intent.putExtra("idUbicacion", new String("" + b.getId()));
                        intent.putExtra("nombre_ubicacion", new String("" + b.getText().toString()));

                        startActivity(intent);
                    }
                });

                GridLayout.LayoutParams layoutParams =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(rowActual++, GridLayout.CENTER),
                                GridLayout.spec(1, GridLayout.CENTER)
                        );

                layoutParams.setMargins(20, 20, 20, 20);
                gridEnScroll.addView(
                        btnUbi,
                        layoutParams);

            }
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : crearBotonesUbicacion > "+e.getMessage(), e);
        }
    }

    public void consultaUbicacionesWS() {
        try
        {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
            {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Consultando ubicaciones...");
                mDialog.setCancelable(false);
                mDialog.setInverseBackgroundForced(false);
                mDialog.show();

                final int idxCursor=1, idxCodigoError=2, idxMensaje=3;
                StringRequest strRequest = new StringRequest(
                        Request.Method.POST,
                        getString(R.string.EndpointRest),
                        new Response.Listener<String>(){
                            @Override
                            public void onResponse(String respuesta){
                                Log.i(GlobalShare.logAplicaion, getClass().getName()+" : consultaUbicacionesWS > respuesta : "+respuesta);

                                mDialog.dismiss();
                                ViewDialog dialogo = new ViewDialog(SeleccionaUbicacionActivity.this );

                                if( respuesta == null || respuesta.equals("")){
                                    dialogo.showDialog(SeleccionaUbicacionActivity.this,
                                            getString(R.string.request_sinrespuesta),
                                            null,
                                            TiposAlert.ERROR);
                                    return ;
                                }

                                RespuestaServicio respuestaObject= null;
                                try {
                                    respuestaObject = oMapper.readValue(respuesta, RespuestaServicio.class);
                                    RespuestaDinamica resDinamica= oMapper.readValue(respuestaObject.getResultado(), RespuestaDinamica.class);

                                    if( !resDinamica.getDatosSalida().get(idxCodigoError).equals("0") ){
                                        dialogo.showDialog(SeleccionaUbicacionActivity.this,
                                                resDinamica.getDatosSalida().get(idxMensaje),null,
                                                TiposAlert.ERROR);
                                        return ;
                                    }
                                    List<UbicacionBean> ubicaciones = new ArrayList<UbicacionBean>();

                                    Cursor cursor = resDinamica.getCursoresSalida().get(idxCursor);

                                    for ( ArrayList<String> ubicacion : cursor.getRegistros()){
                                        ubicaciones.add(new UbicacionBean(
                                                Integer.parseInt(ubicacion.get(0)),
                                                ubicacion.get(1)));
                                    }

                                    GlobalShare.getInstace().setUbicaciones(ubicaciones);
                                    crearBotonesUbicacion(ubicaciones);

                                    if( cursor.getCantRegistros() == 0 ){
                                        mostrarDialogoGenerico("No se obtubieron ubicaciones de la consulta.", TiposAlert.ERROR);
                                    }

                                } catch (IOException e) {
                                    Log.i(GlobalShare.logAplicaion, getClass().getName()+" : consultaUbicacionesWS > "+e.getLocalizedMessage());
                                    dialogo.showDialog(SeleccionaUbicacionActivity.this,
                                            getString(R.string.loguin_error_gen),
                                            null,
                                            TiposAlert.ERROR);
                                }

                            }
                        }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e(GlobalShare.logAplicaion, getClass().getName()+": consultaUbicacionesWS : onErrorResponse: "+error.getMessage(), error);

                        mDialog.dismiss();
                        ViewDialog dialogo = new ViewDialog(SeleccionaUbicacionActivity.this);
                        dialogo.showDialog(SeleccionaUbicacionActivity.this, error.getMessage(),null, TiposAlert.ERROR);
                    }
                }){
                    @Override
                    public Map<String, String> getParams(){
                        Map<String, String> parametros = new HashMap<String, String>();

                        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
                        CuerpoPeticion.add(new ParametroCuerpo(1, "CUR_SALIDA", "0"));
                        CuerpoPeticion.add(new ParametroCuerpo(2, "::Int", "0"));
                        CuerpoPeticion.add(new ParametroCuerpo(3, "::String", "0"));

                        SolicitudServicio solicitud = new SolicitudServicio("CONSUBICACIONESCD", CuerpoPeticion);

                        try {
                            String strSolicitud = oMapper.writeValueAsString(solicitud);
                            parametros.put("solicitud", strSolicitud);
                            Log.i(GlobalShare.logAplicaion, getClass().getName()+": consultaUbicacionesWS : getParams : " + strSolicitud);
                        } catch (IOException e) {
                            Log.i(GlobalShare.logAplicaion, getClass().getName()+": consultaUbicacionesWS : getParams : " + e.getMessage(), e);
                        }

                        return parametros;
                    }

                    @Override
                    public Map<String, String> getHeaders(){
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Contet-Type","text/plain");
                        headers.put("Contet-Encodig","UTF-8");
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
            mostrarDialogoGenerico("No hay conexión HTTP", TiposAlert.ERROR);
        }
        } catch(Exception me) {
            Log.e("Consulta ubicaciones", me.getLocalizedMessage());
            mostrarDialogoGenerico("URL no disponible: validar conexión a Internet.", TiposAlert.ERROR);
        }
    }

    private Intent getInstanciaIntent(Class cls){
        Intent intent = new Intent(this, cls);

        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda")));
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));

        return intent;
    }


    private void mostrarDialogoGenerico(String mensaje, TiposAlert tipo) {
        ViewDialog alert = new ViewDialog(SeleccionaUbicacionActivity.this);
        alert.showDialog(this, mensaje, null, tipo);
    }

    public void regresarAvanceInventario(View view) {
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = getInstanciaIntent(AvanceInventarioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
