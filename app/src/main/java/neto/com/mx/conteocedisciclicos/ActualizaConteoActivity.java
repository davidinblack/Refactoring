package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import butterknife.BindView;
import butterknife.ButterKnife;
import neto.com.mx.conteocedisciclicos.beans.ArticuloBean;
import neto.com.mx.conteocedisciclicos.beans.CredencialBean;
import neto.com.mx.conteocedisciclicos.cliente.ClienteConsultaGenerica;
import neto.com.mx.conteocedisciclicos.cliente.HandlerRespuestasVolley;
import neto.com.mx.conteocedisciclicos.dialogos.CalculadoraActivity;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroTipo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;


public class ActualizaConteoActivity extends AppCompatActivity {

    @BindView(R.id.txtNombreArticulo)
    TextView txtNombreArticulo;

    @BindView(R.id.editTextCantidadContada)
    EditText editTextCantidadContada;

    @BindView(R.id.txtHiddenIdUltimoAct)
    TextView txtHiddenIdUltimoAct;

    @BindView(R.id.btnUltCantCont)
    Button btnUltCantCont;

    @BindView(R.id.txtHoraUltAct)
    TextView txtHoraUltAct;

    @BindView(R.id.txtUltCantCont)
    TextView txtUltCantCont;

    @BindView(R.id.tableRowUltimoConteo)
    LinearLayout tableRowUltimoConteo;

    @BindView(R.id.linearBtnGuardar)
    LinearLayout linearBtnGuardar;

    private ObjectMapper oMapper = new ObjectMapper();
    ArticuloBean articuloActual;
    CredencialBean credencial;
    SimpleDateFormat sdf;

    int idUbicacionConteo;
    Boolean esActualizacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_06_actualiza_conteo);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        ButterKnife.bind(this);

        esActualizacion = false;

        linearBtnGuardar.setVisibility(View.INVISIBLE);

        editTextCantidadContada.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( ((TextView)v).getText().toString().isEmpty() )
                    linearBtnGuardar.setVisibility(View.INVISIBLE);
                else
                    linearBtnGuardar.setVisibility(View.VISIBLE);

                return false;
            }
        });



        RelativeLayout rel = (RelativeLayout)findViewById(R.id.activity_actualiza_conteo);
        if( getIntent().getStringExtra("nombre_ubicacion").toLowerCase().equals("reserva") ){
            rel.setBackgroundColor(getResources().getColor(R.color.colorAzulReserva));
        }else{
            rel.setBackgroundColor(getResources().getColor(R.color.colorMoradoActivo));
        }

        articuloActual = (ArticuloBean)getIntent().getSerializableExtra("articuloSeleccionado");

        Log.i(GlobalShare.logAplicaion, getClass().getName()+ " : onCreate : Artículo recibido > " + articuloActual);

        credencial= (CredencialBean) getIntent().getSerializableExtra("credencial");
        sdf = new SimpleDateFormat("dd/MM/yyyy");

        txtNombreArticulo.setText(articuloActual.getNombreArticulo());
        idUbicacionConteo =0;

        consultaUltimaActualizacionArticuloREST();
    }

    public void enviarConteo(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);
        if( !esActualizacion ){
            insertaNuevoConteoArticulosxUbicacionYZonaREST();
        }else{
            actualizarConteoArticulosxUbicacionYZonaREST();
        }
    }

    public void activaEditarUltimoConteo(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        esActualizacion = true;
        tableRowUltimoConteo.setVisibility(View.GONE);
        editTextCantidadContada.setText(txtUltCantCont.getText().toString());
    }

    public void consultaUltimaActualizacionArticuloREST(){
        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", articuloActual.getIdArticulo()+""));//paUsuario
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", credencial.getIdUsuario()));
        CuerpoPeticion.add(new ParametroCuerpo(4, "int", getIntent().getStringExtra("idUbicacion")));
        CuerpoPeticion.add(new ParametroCuerpo(5, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(6, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSULTIMAUPDATEARTI", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        ActualizaConteoActivity.this,
                        "Consultando última actualización del artículo...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(RespuestaDinamica respuesta) {
                                try {
                                    int idxCantidadContada = 4, idxUltimaCantContada = 3, idxUltimaCantHora = 2, idxIdArticulo = 0,
                                            idxCurSalida = 5,
                                            idxIdError = 6, idxMensajeError = 7;
                                    ViewDialog dialogo = new ViewDialog(ActualizaConteoActivity.this);
                                    if (respuesta == null
                                            || respuesta.getDatosSalida() == null
                                            || respuesta.getDatosSalida().size() == 0) {
                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                "Error consultando última actualización del artículo...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                respuesta.getDatosSalida().get(idxMensajeError),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (respuesta.getCursoresSalida().size() == 0) {

                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);

                                    if (cursor.getCantRegistros() > 0) {
                                        int idxUltArticulo = cursor.getRegistros().size() - 1;
                                        if (idxUltArticulo >= 0) {
                                            List<String> row = cursor.getRegistros().get(idxUltArticulo);
                                            txtHiddenIdUltimoAct.setText(row.get(idxIdArticulo));
                                            btnUltCantCont.setText(row.get(idxCantidadContada));
                                            txtHoraUltAct.setText(row.get(idxUltimaCantHora));
                                            txtUltCantCont.setText(row.get(idxUltimaCantContada));

                                            if (row.get(idxUltimaCantHora).equals("00:00 hrs.") &&
                                                    row.get(idxCantidadContada).equals("0")) {
                                                mostrarEdicion(false);
                                            } else {
                                                mostrarEdicion(true);
                                            }

                                        } else {
                                            mostrarEdicion(false);
                                        }
                                    } else {
                                        mostrarEdicion(false);
                                    }

                                    final ScheduledExecutorService excS = Executors.newScheduledThreadPool(1);
                                    excS.schedule(new Runnable() {
                                        public void run() {
                                            editTextCantidadContada.requestFocus();
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.showSoftInput(editTextCantidadContada, InputMethodManager.SHOW_IMPLICIT);
                                        }
                                    }, 1, TimeUnit.SECONDS);

                                }catch (Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+" : consultaUltimaActualizacionArticuloREST : manejarExitoVolley > "+e.getMessage(), e);
                                }
                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                mostrarEdicion(false);
                                btnUltCantCont.setText("0");
                                txtHoraUltAct.setText("00:00 hrs");
                                txtHoraUltAct.setText("0");
                                tableRowUltimoConteo.setVisibility(View.GONE);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);
    }

    private void mostrarEdicion(boolean ver){
        if( ver ) {
            tableRowUltimoConteo.setVisibility(View.VISIBLE);
            linearBtnGuardar.setVisibility(View.VISIBLE);
        }else {
            tableRowUltimoConteo.setVisibility(View.GONE);
        }
    }

    public void insertaNuevoConteoArticulosxUbicacionYZonaREST(){
        int cantidadContada =0;
        try {
            cantidadContada = Integer.parseInt("0" + editTextCantidadContada.getText().toString().trim());
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : insertaNuevoConteoArticulosxUbicacionYZonaREST > oMapper:strParamsNivel2 >>"+e.getMessage(), e);
        }
        ViewDialog dialogo = new ViewDialog(this);

        String strParamsNivel2 = null;
        List<ParametroTipo[]> paramsNivel2 = new ArrayList<ParametroTipo[]>();
        paramsNivel2.add(
                new ParametroTipo[]{
                        new ParametroTipo( "", getIntent().getStringExtra("idUbicacion")),
                        new ParametroTipo( "", cantidadContada+""),
                        new ParametroTipo( "", "-"),
                        new ParametroTipo( "", sdf.format(new Date()))
                });
        try{
            strParamsNivel2 = oMapper.writeValueAsString(paramsNivel2);
        }catch(IOException e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : insertaNuevoConteoArticulosxUbicacionYZonaREST > oMapper:strParamsNivel2 >>"+e.getMessage(), e);
            dialogo.showDialog(this,
                    "Error al construir solicitud de almacenamiento Nivel2...",null,
                    TiposAlert.ALERT);
            return ;
        }

        //Creamos nivel1 del arreglo
        String strParamsNivel1 = null;
        List<ParametroTipo[]> paramsNivel1 = new ArrayList<ParametroTipo[]>();
        paramsNivel1.add(
                new ParametroTipo[]
                        {
                                new ParametroTipo(  "", articuloActual.getIdArticulo()+""),
                                new ParametroTipo(  "", sdf.format(new Date())+""),
                                new ParametroTipo( "ARREGLO > T_OBJ_CONTEOXUBI, T_ARR_CONTEOXUBI",
                                        strParamsNivel2)
                        }
        );

        try{
            strParamsNivel1 = oMapper.writeValueAsString(paramsNivel1);
        }catch(IOException e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : insertaNuevoConteoArticulosxUbicacionYZonaREST > oMapper:paramsNivel1 >>"+e.getMessage(), e);
            dialogo.showDialog(this,
                    "Se presentó un error al construir la solicitud de almacenamiento Nivel1...",null,
                    TiposAlert.ALERT);
            return ;
        }

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "long", credencial.getIdUsuario()));
        CuerpoPeticion.add(new ParametroCuerpo(5, "ARREGLO > T_OBJ_ARTCONTADO, T_ARR_ARTCONTADO",strParamsNivel1));

        CuerpoPeticion.add(new ParametroCuerpo(6, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CICLICO.INSCONTEOARTICULO", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        ActualizaConteoActivity.this,
                        "Enviando nuevo conteo...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(RespuestaDinamica respuesta) {
                                try {
                                    ViewDialog dialogo = new ViewDialog(ActualizaConteoActivity.this);
                                    int idxIdError = 6, idxMensajeError = 7;
                                    if (respuesta == null
                                            || respuesta.getDatosSalida() == null
                                            || respuesta.getDatosSalida().size() == 0) {
                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                respuesta.getDatosSalida().get(idxMensajeError),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    /*Log.d(GlobalShare.logAplicaion, getClass().getName()+
                                            " : insertaNuevoConteoArticulosxUbicacionYZonaREST > "+ articuloActual );*/
                                    GlobalShare.getInstace().addUltimoArticuloContado(articuloActual);

                                    dialogo.showDialogDismiss(
                                            ActualizaConteoActivity.this,
                                            respuesta.getDatosSalida().get(idxMensajeError),
                                            null,
                                            TiposAlert.CORRECTO,
                                            new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    finish();
                                                }
                                            }, 3000);
                                }catch(Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                            " : insertaNuevoConteoArticulosxUbicacionYZonaREST : manejarExitoVolley : "+e.getMessage(), e);
                                }
                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                Log.e(GlobalShare.logAplicaion,getClass().getName()+
                                        " : insertaNuevoConteoArticulosxUbicacionYZonaREST : manejarErrorVolley :"+
                                        error.getMessage(), error);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);
    }

    public void actualizarConteoArticulosxUbicacionYZonaREST(){
        int cantidadContada = Integer.parseInt("0"+editTextCantidadContada.getText().toString().trim());
        ViewDialog dialogo = new ViewDialog(this);

        //Creamos nivel2
        String strParamsNivel2 = null;
        List<ParametroTipo[]> paramsNivel2 = new ArrayList<ParametroTipo[]>();
        paramsNivel2.add(
                new ParametroTipo[]{
                    new ParametroTipo("", getIntent().getStringExtra("idUbicacion")),
                    new ParametroTipo("", cantidadContada+""),
                    new ParametroTipo("", "-"),
                    new ParametroTipo( "", sdf.format(new Date()))
        });
        try{
            strParamsNivel2 = oMapper.writeValueAsString(paramsNivel2);
        }catch(IOException e){
            Log.e(GlobalShare.logAplicaion,getClass().getName()+
                    " : actualizarConteoArticulosxUbicacionYZonaREST > oMapper:strParamsNivel2 > "+e.getMessage(), e);
            dialogo.showDialog(this,
                    "Error al construir solicitud de almacenamiento Nivel2...",null,
                    TiposAlert.ALERT);
            return ;
        }

        //Creamos nivel1 del arreglo
        String strParamsNivel1 = null;
        List<ParametroTipo[]> paramsNivel1 = new ArrayList<ParametroTipo[]>();
        paramsNivel1.add(
           new ParametroTipo[]
                {
                    new ParametroTipo( "", txtHiddenIdUltimoAct.getText().toString()),
                    new ParametroTipo( "", txtHoraUltAct.getText().toString()),
                    new ParametroTipo( "ARREGLO > T_OBJ_CONTEOXUBI, T_ARR_CONTEOXUBI",
                            strParamsNivel2)
                }
        );

        try{
            strParamsNivel1 = oMapper.writeValueAsString(paramsNivel1);
        }catch(IOException e){
            Log.e(GlobalShare.logAplicaion,getClass().getName()+
                    " : actualizarConteoArticulosxUbicacionYZonaREST > oMapper:strParamsNivel2 > "+e.getMessage(), e);
            dialogo.showDialog(this,
                    "Se presentó un error al construir la solicitud de almacenamiento Nivel1...",null,
                    TiposAlert.ALERT);
            return ;
        }

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "long", credencial.getIdUsuario()));
        CuerpoPeticion.add(new ParametroCuerpo(5, "ARREGLO > T_OBJ_ARTCONTADO, T_ARR_ARTCONTADO", strParamsNivel1));
        CuerpoPeticion.add(new ParametroCuerpo(6, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CICLICO.ACTCONTEOARTICULO", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        ActualizaConteoActivity.this,
                        "Consultando artículos de la ubicación y/o zona...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(RespuestaDinamica respuesta) {
                                ViewDialog dialogo = new ViewDialog(ActualizaConteoActivity.this );
                                int idxIdError=6,idxMensajeError=7;

                                try {
                                    if (respuesta == null
                                            || respuesta.getDatosSalida() == null
                                            || respuesta.getDatosSalida().size() == 0) {
                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                                        //System.out.println(" menj: "+ respuesta.getDatosSalida().get(idxMensajeError));
                                        dialogo.showDialog(ActualizaConteoActivity.this,
                                                respuesta.getDatosSalida().get(idxMensajeError),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    Log.d(GlobalShare.logAplicaion, getClass().getName()+
                                            " : actualizarConteoArticulosxUbicacionYZonaREST > "+ articuloActual );
                                    GlobalShare.getInstace().addUltimoArticuloContado(articuloActual);

                                    dialogo.showDialogDismiss(
                                            ActualizaConteoActivity.this,
                                            respuesta.getDatosSalida().get(idxMensajeError),
                                            null,
                                            TiposAlert.CORRECTO,
                                            new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                finish();
                                            }
                                        }, 3000);

                                }catch(Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                            " : actualizarConteoArticulosxUbicacionYZonaREST : manejarExitoVolley > "+e.getMessage(), e);
                                }

                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                        " : actualizarConteoArticulosxUbicacionYZonaREST : manejarErrorVolley > "+error.getMessage(), error);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);
    }

    public void regresar(View view) {
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        finish();
    }

    public void mostrarCalculadora(View view) {
        Intent i = new Intent(this, CalculadoraActivity.class);
        startActivity(i);
    }



}
