package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import neto.com.mx.conteocedisciclicos.beans.ArticuloBean;
import neto.com.mx.conteocedisciclicos.beans.CredencialBean;
import neto.com.mx.conteocedisciclicos.cliente.ClienteConsultaGenerica;
import neto.com.mx.conteocedisciclicos.cliente.HandlerRespuestasVolley;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroTipo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;

import neto.com.mx.conteocedisciclicos.R;

public class ReconteoActivity extends AppCompatActivity implements HandlerRespuestasVolley{
    ObjectMapper oMapper = new ObjectMapper();

    SimpleDateFormat sdf;

    LinearLayout linearActivo;
    LinearLayout linearReserva;

    TextView txtNombreArticulo;
    EditText editCantidadActivo;
    EditText editCantidadReserva;

    TextView txtCantidadContada;

    CredencialBean credencial;
    ArticuloBean articuloActual;

    Button btncantContada;
    Button btnGuardar;

    CheckBox chkActivo;
    CheckBox chkReserva;

    TextView textCantActivo;
    TextView textCantReserva;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*ListenerKeyboradLayout listenerKeyboradLayout =
                new ListenerKeyboradLayout(this, null ,R.layout.activity_09_reconteo, this);
        setContentView(listenerKeyboradLayout);*/
        setContentView(R.layout.activity_09_reconteo);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        sdf = new SimpleDateFormat("dd/MM/yyyy");

        articuloActual = (ArticuloBean)getIntent().getSerializableExtra("articuloSeleccionado");
        credencial = (CredencialBean) getIntent().getSerializableExtra("credencial");

        linearActivo = (LinearLayout) findViewById(R.id.linearActivo);
        linearReserva= (LinearLayout) findViewById(R.id.linearReserva);

        txtNombreArticulo = (TextView)findViewById(R.id.txtNombreArticulo);
        editCantidadActivo = (EditText ) findViewById(R.id.txtCantidadActivo);
        editCantidadReserva = (EditText )findViewById(R.id.txtCantidadReserva);

        btnGuardar= (Button )findViewById(R.id.btnGuardar);
        btncantContada= (Button )findViewById(R.id.btncantContada);

        chkActivo = (CheckBox)findViewById(R.id.checkBoxActivo);
        chkReserva = (CheckBox)findViewById(R.id.checkBoxReserva);

        textCantActivo = (TextView)findViewById(R.id.textCantActivo);
        textCantReserva = (TextView)findViewById(R.id.textCantReserva);
        txtCantidadContada = (TextView)findViewById(R.id.txtCantidadContada);

        /*chkActivo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean isChecked){
                if( isChecked ){
                    editCantidadActivo.setVisibility(View.VISIBLE);
                    textCantActivo.setVisibility(View.GONE);
                }else{
                    editCantidadActivo.setText( textCantActivo.getText().toString() );
                    editCantidadActivo.setVisibility(View.GONE);
                    textCantActivo.setVisibility(View.VISIBLE);
                }
            }
        });
        chkReserva.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean isChecked){
                if( isChecked ){
                    editCantidadReserva.setVisibility(View.VISIBLE);
                    textCantReserva.setVisibility(View.GONE);
                }else{
                    editCantidadReserva.setText( textCantReserva.getText().toString() );
                    editCantidadReserva.setVisibility(View.GONE);
                    textCantReserva.setVisibility(View.VISIBLE);
                }
            }
        });
*/

        editCantidadActivo.setText("");
        editCantidadReserva.setText("");
        editCantidadReserva.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //if( event.getAction() == KeyEvent.ACTION_UP){
                    if( event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                        regresar(null);
                    }
                //}
                return false;
            }
        });
        editCantidadReserva.setOnKeyListener(new TextView.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //if( event.getAction() == KeyEvent.ACTION_DOWN){
                TextView txtV = (TextView)v;
                    //System.out.println("Editado editCantidadReserva...");
                    //if( !txtV.getText().toString().trim().isEmpty() ) {
                        int valorActual = Integer.parseInt("0" + txtV.getText().toString().trim());
                        int valorAnterior = Integer.parseInt("0" + txtV.getHint().toString().trim());
                        //System.out.println("valorActual : "+valorActual+", valorAnterior: "+valorAnterior);
                if( txtV.getText().toString().trim().isEmpty() ) {
                    chkReserva.setChecked(false);
                }else{
                    chkReserva.setChecked(valorActual != valorAnterior );
                }
                    //}
                //}
                return false;
            }
        });
        editCantidadActivo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //if( event.getAction() == KeyEvent.ACTION_UP){
                    if( event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                        regresar(null);
                    }
                //}
                return false;
            }
        });
        editCantidadActivo.setOnKeyListener(new TextView.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //if( keyCode == KeyEvent.KEYCODE_NUM ){
                TextView txtV = (TextView)v;
                    //System.out.println("Editado editCantidadActivo...");
                    //if( !txtV.getText().toString().trim().isEmpty() ) {
                int valorActual = Integer.parseInt("0" + txtV.getText().toString().trim());
                int valorAnterior = Integer.parseInt("0" + txtV.getHint().toString().trim());
                        //System.out.println("valorActual : "+valorActual+", valorAnterior: "+valorAnterior);
                if( txtV.getText().toString().trim().isEmpty() ) {
                    chkActivo.setChecked(false);
                }else{
                    chkActivo.setChecked(valorActual != valorAnterior );
                }
                    //}
                //}
                return false;
            }
        });
        editCantidadActivo.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if( actionId == EditorInfo.IME_ACTION_DONE){
                    return true;
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tecladoAbierto = false;
                            cambiarVista(false);
                        }
                    }, 2000);*/
                }
                return false;
            }
        });

        editCantidadReserva.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if( actionId == EditorInfo.IME_ACTION_DONE){
                    return true;/*
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tecladoAbierto = false;
                            cambiarVista(false);
                        }
                    }, 2000);*/
                }
                return false;
            }
        });

        btncantContada.setText("0");

        txtNombreArticulo.setText(articuloActual.getNombreArticulo());

        obtenerConteosXArticulo();

        /*int60dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        int30dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        int100dp= (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int200dp= (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());*/
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DEL ||
                (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)){

            EditText editActual = null;
            CheckBox checkActual = null;

            if( editCantidadReserva.isFocused() ) {
                editActual = editCantidadReserva;
                checkActual = chkReserva;
            }else if(editCantidadActivo.isFocused() ) {
                editActual = editCantidadActivo;
                checkActual = chkActivo;
            }

            checkActual.setChecked( !editActual.getText().toString().trim().isEmpty() );
            /*int valorActual = Integer.parseInt("0" + editActual.getText().toString().trim());
            int valorAnterior = Integer.parseInt("0" + editActual.getHint().toString().trim());
            System.out.println("valorActual: "+valorActual+", valorAnterior: "+valorAnterior);
            if( editActual.getText().toString().trim().isEmpty() ) {
                checkActual.setChecked(false);
            }else{
                checkActual.setChecked(valorActual != valorAnterior );
            }*/

            if( chkReserva.isChecked() || chkActivo.isChecked() ){
                btnGuardar.setVisibility(View.VISIBLE);
            }else{
                btnGuardar.setVisibility(View.GONE);
            }

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void obtenerConteosXArticulo() {

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "long", articuloActual.getIdArticulo()+"" ));
        CuerpoPeticion.add(new ParametroCuerpo(5, "String", getIntent().getStringExtra("idUbicacion")));
        CuerpoPeticion.add(new ParametroCuerpo(6, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(8, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSRESUMENRECONTEO", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        ReconteoActivity.this,
                        "Consultando inventario...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.ejecutarConsultaWS(7, 8);
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        Log.e(GlobalShare.logAplicaion, getClass().getName()+
                " : obtenerConteosXArticulo : manejarExitoVolley > "+error.getMessage(), error);
    }
    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {
        try {
            ViewDialog dialogo = new ViewDialog(this);
            int idxIdUbicacion = 0, idxDescUbi = 1, idxCantidad = 2,
                    idxCurSalida = 6,
                    idxIdError = 7, idxMensajeError = 8;

            if (respuesta == null
                    || respuesta.getDatosSalida() == null
                    || respuesta.getDatosSalida().size() == 0) {
                dialogo.showDialog(this,
                        "Error en central al procesar la solicitud...",
                        null,
                        TiposAlert.ERROR);
                return;
            } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                dialogo.showDialog(this,
                        respuesta.getDatosSalida().get(idxMensajeError),
                        null,
                        TiposAlert.ERROR);
                return;
            } else if (respuesta.getCursoresSalida().size() == 0) {
                dialogo.showDialog(this,
                        "Error en central al procesar la solicitud...",
                        null,
                        TiposAlert.ERROR);
                return;
            }

            Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);
            for (List<String> registro : cursor.getRegistros()) {
                if (Integer.parseInt(registro.get(idxIdUbicacion)) == 0) {
                    btncantContada.setText(registro.get(idxCantidad));
                } else if (Integer.parseInt(registro.get(idxIdUbicacion)) == 1) {
                    editCantidadActivo.setText("");//registro.get(idxCantidad));
                    editCantidadActivo.setHint(registro.get(idxCantidad));
                    //textCantActivo.setText(registro.get(idxCantidad));
                } else if (Integer.parseInt(registro.get(idxIdUbicacion)) == 2) {
                    editCantidadReserva.setText("");//(registro.get(idxCantidad));
                    editCantidadReserva.setHint(registro.get(idxCantidad));
                    //textCantReserva.setText(registro.get(idxCantidad));
                }
            }
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : obtenerConteosXArticulo : manejarExitoVolley > "+e.getMessage(), e);
        }
    }

    public void actualizarReconteo(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        int cantidadContadaActivo = Integer.parseInt("0"+ editCantidadActivo.getText().toString().trim());
        int cantidadContadaReserva = Integer.parseInt("0"+ editCantidadReserva.getText().toString().trim());
        ViewDialog dialogo = new ViewDialog(this);

        //Creamos nivel2
        String strParamsNivel2 = null;
        List<ParametroTipo[]> paramsNivel2 = new ArrayList<ParametroTipo[]>();
        if( chkActivo.isChecked() ) {
            paramsNivel2.add(
                    new ParametroTipo[]{
                            new ParametroTipo("", "1"),//Activo
                            new ParametroTipo("", cantidadContadaActivo + ""),
                            new ParametroTipo("", "Modificacion desde reconteo"),
                            new ParametroTipo("", sdf.format(new Date()))
                    });
        }
        if( chkReserva.isChecked() ) {
            paramsNivel2.add(
                    new ParametroTipo[]{
                            new ParametroTipo("", "2"),//Reserva
                            new ParametroTipo("", cantidadContadaReserva + ""),
                            new ParametroTipo("", "Modificacion desde reconteo"),
                            new ParametroTipo("", sdf.format(new Date()))
                    });
        }
        try{
            strParamsNivel2 = oMapper.writeValueAsString(paramsNivel2);
        }catch(IOException e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : actualizarReconteo > strParamsNivel2 >> "+e.getMessage(), e);
            dialogo.showDialog(this,
                    "Error al construir solicitud de almacenamiento Nivel2...",null,
                    TiposAlert.ALERT,
                    3000);
            return ;
        }

        //Creamos nivel1 del arreglo
        String strParamsNivel1 = null;
        List<ParametroTipo[]> paramsNivel1 = new ArrayList<ParametroTipo[]>();
        paramsNivel1.add(
                new ParametroTipo[]
                        {
                                new ParametroTipo( "", articuloActual.getIdArticulo()+""),
                                new ParametroTipo( "", sdf.format(new Date())),
                                new ParametroTipo( "ARREGLO > T_OBJ_CONTEOXUBI, T_ARR_CONTEOXUBI",
                                        strParamsNivel2)
                        }
        );

        try{
            strParamsNivel1 = oMapper.writeValueAsString(paramsNivel1);
        }catch(IOException e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : actualizarReconteo > strParamsNivel1 >> "+e.getMessage(), e);
            dialogo.showDialog(this,
                    "Se presentó un error al construir la solicitud de almacenamiento Nivel1...",null,
                    TiposAlert.ALERT);
            return ;
        }

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "int", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "long", credencial.getIdUsuario()));
        CuerpoPeticion.add(new ParametroCuerpo(5, "ARREGLO > T_OBJ_ARTCONTADO, T_ARR_ARTCONTADO", strParamsNivel1));
        CuerpoPeticion.add(new ParametroCuerpo(6, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CICLICO.EDITINDICRECONTEO", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        ReconteoActivity.this,
                        "Actualizando conteo...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(final RespuestaDinamica respuesta) {
                                ViewDialog dialogo = new ViewDialog(ReconteoActivity.this );
                                final int idxIdError=6,idxMensajeError=7;

                                try
                                {
                                    if (respuesta == null
                                            || respuesta.getDatosSalida() == null
                                            || respuesta.getDatosSalida().size() == 0) {
                                        dialogo.showDialog(ReconteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                                        dialogo.showDialog(ReconteoActivity.this,
                                                respuesta.getDatosSalida().get(idxMensajeError),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    GlobalShare.getInstace().addUltimoArticuloContado(articuloActual);
                                    dialogo.showDialogDismiss(
                                            ReconteoActivity.this,
                                            respuesta.getDatosSalida().get(idxMensajeError),
                                            null,
                                            TiposAlert.CORRECTO,
                                            new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    if( respuesta.getDatosSalida().get(idxIdError).equals("0") ){
                                                        finish();
                                                    }
                                                }
                                            }, 3000);
                                }catch(Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                            " : actualizarReconteo > manejarExitoVolley >> "+e.getMessage(), e);
                                }

                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                        " : actualizarReconteo > manejarErrorVolley >> "+error.getMessage(), error);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);
    }

    public void regresar(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);
        finish();
    }

    @Override
    public void onBackPressed() {
        regresar(null);
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            regresar(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /* boolean tecladoAbierto = false;

    @Override
    public void softKeyboardHiddenChanged(boolean visible) {
        System.out.println("softKeyboardHiddenChanged");
        if( !tecladoAbierto && (editCantidadActivo.isFocused() || editCantidadReserva.isFocused())) {
            cambiarVista(true);
            tecladoAbierto = true;
        }
    }*/

    /*int int60dp = 0, int30dp=0, int100dp=0, int200dp=0;
    private void cambiarVista(boolean tecladoVisible){
        System.out.println("cambiarVista tecladoVisible: "+tecladoVisible);
        if( !tecladoVisible ){
            btncantContada.setHeight(int60dp);
            txtCantidadContada.setWidth(int100dp);
            linearActivo.setOrientation(LinearLayout.VERTICAL);
            linearActivo.setWeightSum(0);
            linearReserva.setOrientation(LinearLayout.VERTICAL);
            linearReserva.setWeightSum(0);
        }else{
            btncantContada.setHeight(int30dp);
            txtCantidadContada.setWidth(int100dp);
            linearActivo.setOrientation(LinearLayout.HORIZONTAL);
            linearActivo.setWeightSum(0.5f);
            linearReserva.setOrientation(LinearLayout.HORIZONTAL);
            linearReserva.setWeightSum(0.5f);

            //tecladoAbierto = false;
        }
    }*/

}
