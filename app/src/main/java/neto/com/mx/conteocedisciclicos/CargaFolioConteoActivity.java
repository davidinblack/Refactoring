package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.Keyboard;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import neto.com.mx.conteocedisciclicos.beans.AvanceInventarioBean;
import neto.com.mx.conteocedisciclicos.beans.CredencialBean;
import neto.com.mx.conteocedisciclicos.cliente.ClienteConsultaGenerica;
import neto.com.mx.conteocedisciclicos.cliente.HandlerRespuestasVolley;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.dialogos.BienvenidaDialog;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;


public class CargaFolioConteoActivity extends AppCompatActivity implements HandlerRespuestasVolley {

    private static final int SOLICITUD_DESCARGA = 0;
    CredencialBean credencial;
    AvanceInventarioBean avanceInventario;

    Context context;
    String version = "";

    EditText editText;
    String folioConteo;

    Intent intentAvanceInventario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_01_carga_folio_conteo);

        GlobalShare.getInstace().restearVariables();

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        editText = (EditText) findViewById(R.id.folioPedidoText);
        editText.setText("");

        TextView txtFecha = (TextView) findViewById(R.id.txtFecha);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFecha.setText(sdf.format(new Date()));

        context = this.getApplicationContext();
        getSupportActionBar().hide();


        editText.setInputType(InputType.TYPE_NULL);
        editText.requestFocus();
        editText.setOnEditorActionListener(escaneaListener);

        credencial = (CredencialBean) getIntent().getSerializableExtra("credencial");
        avanceInventario = (AvanceInventarioBean) getIntent().getSerializableExtra("avanceInventario");

        TextView txtUsuarioNombre = (TextView) findViewById(R.id.txtUsuarioNombre);
        txtUsuarioNombre.setText(credencial.getNombreUsuario());

        BienvenidaDialog alert = new BienvenidaDialog(CargaFolioConteoActivity.this);
        alert.showDialog(CargaFolioConteoActivity.this, credencial.getNombreUsuario(), new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editText.setText("");
            }
        });

        //consultaIdInventarioWS();

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException ne) {
            Log.e("CARGA_FOLIO_TAG", "Error al obtener la versión: " + ne.getMessage());
        }

    }

    public void salirMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        salirMenu();
    }

    public void regresaMenu() {
        Intent intent = new Intent(getApplicationContext(), CargaFolioConteoActivity.class);
        startActivity(intent);
    }

    public void salirMenu() {
        Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("LOGOUT", true);
        startActivity(intent);
    }

    TextView.OnEditorActionListener escaneaListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                consultarFolioConteoREST(new String(editText.getText().toString().trim()));
            }
            return true;
        }
    };

    public void consultarFolioConteoREST(String folioConteo_){
        folioConteo = folioConteo_.replace(" ","").trim();

        if(folioConteo.equals("")) {
            ViewDialog alert = new ViewDialog(CargaFolioConteoActivity.this);
            alert.showDialogDismiss(CargaFolioConteoActivity.this,
                    "Escanea un folio de inventario",
                    null,
                    TiposAlert.ALERT,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            editText.setText("");
                        }
                    });
            return ;
        }

        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        //Log.i(TagClase, "consultarFolioConteoREST :: Consultado datos de avance de inventario...");

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", folioConteo ));
        CuerpoPeticion.add(new ParametroCuerpo(2, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(3, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(4, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSINVENTARIOCD", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        CargaFolioConteoActivity.this,
                        "Consultando inventario...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.setManejarCodigoError(false);
        clienteConsultaGenerica.ejecutarConsultaWS(3, 4);
    }

    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {
        editText.setText("");

        ViewDialog dialogo = new ViewDialog(this );
        int idxTxtReserva = 0, idxPorcRecerva = 1, idxCantArticulosReserva= 2,
                idxTxtActivo = 3, idxPorcActivo = 4, idxCantArticulosActivo= 5,
                idxPorcentajeAvance=6, idxCantTotArticulos=7, idxTotCantCajasContadas = 8,
                idxTienda = 9, idxNombreTienda = 10,
        idxCurSalida = 2,
                idxIdError=3, idxMensajeError=4;
        try {
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
            } else if (respuesta.getCursoresSalida().get(idxCurSalida).getCantRegistros() == 0) {
                dialogo.showDialog(this,
                        "No se recibieron datos del usuario...",
                        null,
                        TiposAlert.ERROR);
                return;
            }


            Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);
            List<String> registro = cursor.getRegistros().get(0);

            String idInventario = folioConteo;

            String reservaNombre = registro.get(idxTxtReserva);
            Integer reservaPorcentaje = Integer.parseInt(registro.get(idxPorcRecerva));
            Long reservaCantArt = Long.parseLong(registro.get(idxCantArticulosReserva));

            String activoNombre = registro.get(idxTxtActivo);
            Integer activoPorcentaje = Integer.parseInt(registro.get(idxPorcActivo));
            Long txtActivoCantArt = Long.parseLong(registro.get(idxCantArticulosActivo));

            Long totArticulos = Long.parseLong(registro.get(idxCantTotArticulos));
            Long totCajas = Long.parseLong(registro.get(idxTotCantCajasContadas));
            Integer porcentajeAvanceTotal = Integer.parseInt(registro.get(idxPorcentajeAvance));
            Integer idTienda = Integer.parseInt(registro.get(idxTienda));
            String nombreTienda = registro.get(idxNombreTienda);

            AvanceInventarioBean avanceInventario = new AvanceInventarioBean(
                    reservaNombre, reservaPorcentaje,
                    reservaCantArt, activoNombre, activoPorcentaje, txtActivoCantArt,
                    totArticulos, totCajas, porcentajeAvanceTotal);

            Log.d(GlobalShare.logAplicaion,
                    String.format("tiendaId: %d, usuarioId: %s, versionActual: %s",
                            idTienda, credencial.getIdUsuario(), getPackageManager().getPackageInfo(getPackageName(), 0).versionName));

            /*Intent intentVersion = new Intent(this, DescargaUltimaVersionDialog.class);
            intentVersion.putExtra("tiendaId", idTienda);
            intentVersion.putExtra("usuarioId", credencial.getIdUsuario() );
            intentVersion.putExtra("versionActual",
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName );

            startActivityForResult(intentVersion, SOLICITUD_DESCARGA );*/

            intentAvanceInventario = new Intent(getApplicationContext(), AvanceInventarioActivity.class);
            intentAvanceInventario.putExtra("credencial", (CredencialBean) getIntent().getSerializableExtra("credencial"));
            intentAvanceInventario.putExtra("idInventario", new String(folioConteo));
            intentAvanceInventario.putExtra("idTienda", new String(idTienda.toString()));
            intentAvanceInventario.putExtra("nombreTienda", new String(nombreTienda));
            intentAvanceInventario.putExtra("avanceInventario", avanceInventario);

            startActivity(intentAvanceInventario);

        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, "consultarFolioConteoREST : manejarExitoVolley ", e);
        }
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        Log.e(GlobalShare.logAplicaion, "consultarFolioConteoREST : manejarErrorVolley : "+error.getMessage(), error);
        editText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == SOLICITUD_DESCARGA )
            startActivity(intentAvanceInventario);
    }

    public void regresarSeleccionUbicacion(View view) {
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = new Intent(getApplicationContext(), SeleccionaUbicacionActivity.class);

        intent.putExtra( "credencial", credencial );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda"))  );
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")) );

        startActivity(intent);
    }

    @Override
    protected void onStop() {
        editText.setText("");
        GlobalShare.getInstace().restearVariables();
        super.onStop();
    }
}
