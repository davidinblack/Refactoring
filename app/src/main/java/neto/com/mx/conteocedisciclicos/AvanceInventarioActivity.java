package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import neto.com.mx.conteocedisciclicos.beans.AvanceInventarioBean;
import neto.com.mx.conteocedisciclicos.beans.CredencialBean;
import neto.com.mx.conteocedisciclicos.beans.UbicacionBean;
import neto.com.mx.conteocedisciclicos.cliente.ClienteConsultaGenerica;
import neto.com.mx.conteocedisciclicos.cliente.HandlerRespuestasVolley;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;


public class AvanceInventarioActivity extends AppCompatActivity implements HandlerRespuestasVolley{
    private CredencialBean credencial;

    private boolean esNecesarioCargarDatosWS = false;
    private ObjectMapper oMapper = new ObjectMapper();

    //EditText editText;

    TextView txtUsuarioNombre;
    TextView txtIdInventarioActual;

    TextView txtNombreTienda = null;
    TextView txtReserva = null;
    TextView txtReservaPorcent= null;
    TextView txtReservaCantArt= null;

    TextView txtActivo= null;
    TextView txtActivoPorcent= null;
    TextView txtActivoCantArt= null;

    TextView txtTotArticulos= null;
    TextView txtTotCajas= null;
    TextView txtProgresBarAvanceTotal= null;

    ProgressBar progressBarCombinado;
    ProgressBar progresBarAvanceTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_02_avance_inventario);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        txtUsuarioNombre = (TextView) findViewById( R.id.txtUsuarioNombre);
        txtIdInventarioActual = (TextView) findViewById( R.id.txtIdInventarioActual);

        txtNombreTienda = (TextView) findViewById( R.id.txtNombreTienda);

        txtReserva = (TextView) findViewById( R.id.txtReserva);
        txtReservaPorcent = (TextView)  findViewById( R.id.txtReservaPorcent);
        txtReservaCantArt = (TextView)  findViewById( R.id.txtReservaCantArt);

        txtActivo = (TextView) findViewById( R.id.txtActivo);
        txtActivoPorcent = (TextView)  findViewById( R.id.txtActivoPorcent);
        txtActivoCantArt = (TextView) findViewById( R.id.txtActivoCantArt);

        txtTotArticulos= (TextView) findViewById( R.id.txtTotArticulos);
        txtTotCajas= (TextView) findViewById( R.id.txtTotCajas);
        txtProgresBarAvanceTotal= (TextView) findViewById( R.id.txtProgresBarAvanceTotal);

        progressBarCombinado = (ProgressBar) findViewById(R.id.progressBarCombinado);
        progresBarAvanceTotal = (ProgressBar) findViewById(R.id.progresBarAvanceTotal);

        TextView txtFecha = (TextView) findViewById(R.id.txtFecha);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFecha.setText(sdf.format(new Date()));

        credencial = (CredencialBean) getIntent().getSerializableExtra("credencial");

        txtUsuarioNombre.setText( credencial.getNombreUsuario() );
        txtIdInventarioActual.setText( getIntent().getStringExtra("idInventario"));

        txtNombreTienda.setText(getIntent().getStringExtra("nombreTienda"));


        Button btnContar = (Button)findViewById(R.id.btnContar);
        btnContar.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                mostrarUbicaciones();
            }
        });

        Button btnReconteo = (Button)findViewById(R.id.btnReconteo);
        btnReconteo.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                iniciaReconteo();
            }
        });

        AvanceInventarioBean avance = (AvanceInventarioBean) getIntent().getSerializableExtra("avanceInventario");
        if( avance == null ){
            resetAvanceConteo();
            consultarAvanceInventarioREST();
        }else{
            resetAvanceConteo();
            actualizaAvanceInventario(avance);
        }

        //Cargar sólo si no hay datos en las globales
        if( GlobalShare.getInstace().getUbicaciones().size() == 0 ){
            consultaUbicacionesWS();
        }

    }



    public void consultarAvanceInventarioREST(){

        Log.i(GlobalShare.logAplicaion,"consultarAvanceInventarioREST : Consultado datos de avance de inventario...");

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();

        CuerpoPeticion.add(new ParametroCuerpo(1, "long", new String( getIntent().getStringExtra("idInventario") ) ));
        CuerpoPeticion.add(new ParametroCuerpo(2, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(3, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(4, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSRESUMENINVETARIOCD", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        AvanceInventarioActivity.this,
                        "Consultando inventario...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.ejecutarConsultaWS(3, 4);
    }

    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {
        try {
            ViewDialog dialogo = new ViewDialog(this);
            int idxTxtReserva = 0, idxPorcRecerva = 1, idxCantArticulosReserva = 2,
                    idxTxtActivo = 3, idxPorcActivo = 4, idxCantArticulosActivo = 5,
                    idxPorcentajeAvance = 6, idxCantTotArticulos = 7, idxTotCantCajasContadas = 8,
                    idxCurSalida = 2,
                    idxIdError = 3, idxMensajeError = 4,
                    idxPrimerRow = 0;

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
            } else if (respuesta.getCursoresSalida().size() == 0 ||
                    respuesta.getCursoresSalida().get(idxCurSalida).getCantRegistros() == 0) {
                dialogo.showDialog(this,
                        "No se recibieron datos del inventario...",
                        null,
                        TiposAlert.ERROR);
                return;
            }


            Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);
            List<String> registro = cursor.getRegistros().get(idxPrimerRow);


            String reservaNombre = registro.get(idxTxtReserva);
            Integer reservaPorcentaje = Integer.parseInt(registro.get(idxPorcRecerva));
            Long reservaCantArt = Long.parseLong(registro.get(idxCantArticulosReserva));

            String activoNombre = registro.get(idxTxtActivo);
            Integer activoPorcentaje = Integer.parseInt(registro.get(idxPorcActivo));
            Long txtActivoCantArt = Long.parseLong(registro.get(idxCantArticulosActivo));

            Long totArticulos = Long.parseLong(registro.get(idxCantTotArticulos));
            Long totCajas = Long.parseLong(registro.get(idxTotCantCajasContadas));
            Integer porcentajeAvanceTotal = Integer.parseInt(registro.get(idxPorcentajeAvance));

            AvanceInventarioBean avanceInventario = new AvanceInventarioBean(
                    reservaNombre, reservaPorcentaje,
                    reservaCantArt, activoNombre, activoPorcentaje, txtActivoCantArt,
                    totArticulos, totCajas, porcentajeAvanceTotal);

            actualizaAvanceInventario(avanceInventario);
        }catch (Exception e){
            Log.e(GlobalShare.logAplicaion, "consultarAvanceInventarioREST : manejarExitoVolley : "+e.getMessage(), e);
        }
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        Log.e(GlobalShare.logAplicaion, "consultarAvanceInventarioREST : manejarErrorVolley : "+error.getMessage(), error);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        esNecesarioCargarDatosWS =  true;
    }

    private void mostrarUbicaciones(){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);
        Intent intent = new Intent(this, SeleccionaUbicacionActivity.class);

        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda")));
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));

        startActivity(intent);
    }

    private void resetAvanceConteo(){

        txtReservaCantArt.setText("0");
        txtActivoCantArt.setText("0");

        txtTotCajas.setText("0");
        txtTotArticulos.setText("0");

        txtProgresBarAvanceTotal.setText("0%");

        progresBarAvanceTotal.setProgress(0);
        progressBarCombinado.setSecondaryProgress(0);
        progressBarCombinado.setProgress(0);
    }



    public void regresarSeleccionUbicacion(View view) {

        Intent intent = new Intent(this, SeleccionaUbicacionActivity.class);

        intent.putExtra( "credencial", getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda")));
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario").trim()) );

        startActivity(intent);
    }

    private void actualizaAvanceInventario(AvanceInventarioBean avanceInventario){

        txtReserva.setText(avanceInventario.getReservaNombre());
        txtReservaPorcent.setText(avanceInventario.getReservaPorcentaje().toString()+"%");
        txtReservaCantArt.setText(avanceInventario.getReservaCantArt().toString());

        txtActivo.setText(avanceInventario.getActivoNombre());
        txtActivoPorcent.setText(avanceInventario.getActivoPorcentaje().toString()+"%");
        txtActivoCantArt.setText(avanceInventario.getTxtActivoCantArt().toString());

        txtTotArticulos.setText(avanceInventario.getTotArticulos().toString());
        txtTotCajas.setText(avanceInventario.getTotCajas().toString());
        txtProgresBarAvanceTotal.setText(avanceInventario.getPorcentajeAvanceTotal().toString()+"%");

        progressBarCombinado.setProgress(avanceInventario.getReservaPorcentaje());
        progressBarCombinado.setSecondaryProgress(
        avanceInventario.getReservaPorcentaje() + avanceInventario.getActivoPorcentaje());
        progresBarAvanceTotal.setProgress(avanceInventario.getPorcentajeAvanceTotal().intValue());

    }

    public void iniciaReconteo(){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);
        Intent intent = new Intent(this, DetalleConteoActivity.class);

        intent.putExtra( "credencial", getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda")));
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario").trim()) );
        intent.putExtra( "esReconteo", true );

        intent.putExtra( "idUbicacion", "0");
        intent.putExtra( "nombre_ubicacion", "Reconteo");

        intent.putExtra("idZona", "0");
        intent.putExtra("nombre_zona", "Todas las Zonas");

        startActivity(intent);
    }



    private void mostrarDialogoGenerico(String mensaje, TiposAlert tipo) {
        ViewDialog alert = new ViewDialog(AvanceInventarioActivity.this);
        alert.showDialog(this, mensaje, null, tipo);
    }

    public void consultaUbicacionesWS() {

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(3, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSUBICACIONESCD", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        AvanceInventarioActivity.this,
                        "Consultando ubicaciones...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(RespuestaDinamica respuesta) {
                                final int idxCursor=1, idxCodigoError=2;
                                try {

                                    Cursor cursor = respuesta.getCursoresSalida().get(idxCursor);

                                    List<UbicacionBean> ubicaciones = new ArrayList<UbicacionBean>();
                                    for (ArrayList<String> ubicacion : cursor.getRegistros()) {
                                        ubicaciones.add(new UbicacionBean(
                                                Integer.parseInt(ubicacion.get(0)),
                                                ubicacion.get(1)));
                                    }
                                    GlobalShare.getInstace().setUbicaciones(ubicaciones);

                                    if (cursor.getCantRegistros() == 0) {
                                        mostrarDialogoGenerico("No hay datos de ubicaciones en respuesta...", TiposAlert.ERROR);
                                        return;
                                    }
                                    if (respuesta.getDatosSalida().get(idxCodigoError).equals("0")) {
                                        Toast.makeText(getApplicationContext(), "Ubicaciones cargadas...", Toast.LENGTH_SHORT);
                                        return;
                                    }
                                }catch(Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+" :: consultaUbicacionesWS : manejarExitoVolley : "+e.getMessage(), e);
                                }
                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Error consultando ubicaciones...", Toast.LENGTH_SHORT);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(3, 4);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent i = new Intent(AvanceInventarioActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
