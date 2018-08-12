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
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;


public class DetalleConteoActivity extends AppCompatActivity {

    CredencialBean credencial;
    TextView txtTotArtSinDiferencia ;
    TextView txtTotArtSinContar ;
    TextView txtTotArtConDiferencia ;
    TextView txtTotArtParaReconteo ;

    TextView txtUsuarioNombre;
    TextView txtFecha;
    TextView txtIdInventarioActual;

    List<ArticuloBean> articulosCargados;
    ObjectMapper oMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_08_detalle_conteo);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        txtTotArtSinDiferencia =(TextView)findViewById(R.id.txtTotArtSinDiferencia);
        txtTotArtSinContar =(TextView)findViewById(R.id.txtTotArtSinContar);
        txtTotArtConDiferencia =(TextView)findViewById(R.id.txtTotArtConDiferencia);
        txtTotArtParaReconteo =(TextView)findViewById(R.id.txtTotArtParaReconteo);

        txtUsuarioNombre = (TextView)findViewById(R.id.txtUsuarioNombre);
        txtFecha = (TextView)findViewById(R.id.txtFecha);
        txtIdInventarioActual = (TextView)findViewById(R.id.txtIdInventarioActual);

        Button btn = (Button)findViewById(R.id.btnZona);
        btn.setText(getIntent().getStringExtra("nombre_zona"));

        credencial = (CredencialBean)getIntent().getSerializableExtra("credencial");

        txtUsuarioNombre.setText(credencial.getNombreUsuario());
        txtIdInventarioActual.setText(getIntent().getStringExtra("idInventario"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFecha.setText(sdf.format(new Date()));

        //consultaResumenConteo();

        articulosCargados = GlobalShare.getInstace().getArticulos().get(
          getIntent().getStringExtra("idZona")
        );

        /*System.out.println("Zonas con artículos: ");
        Map<String, List<ArticuloBean>> map = GlobalShare.getInstace().getArticulos();
        for ( String zona : GlobalShare.getInstace().getArticulos().keySet()) {
            System.out.println("> "+zona+" > "+ map.get(zona).size());
        }*/

        if( articulosCargados == null || articulosCargados.size()== 0 ){
            articulosCargados = new ArrayList<>();
            consultaArticulosXZonaXTienda();
        }/*else{
            System.out.println("Ya hay articulos cargados...");
        }*/

    }

    public void consultaResumenConteo(){

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "Int", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "String", getIntent().getStringExtra("idZona")));
        CuerpoPeticion.add(new ParametroCuerpo(5, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(6, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSRESUMENCONTEO", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        this,
                        "Consultando resumen de conteo...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(RespuestaDinamica respuesta) {
                                try
                                {
                                    int idxArtSinDiferencia = 0, idxArtConDiferencia = 1, idxArtSinContar = 2, idxArtParaReconteo=3,
                                            idxCurSalida = 5, idxIdError = 6, idxMensajeError = 7;

                                    ViewDialog dialogo = new ViewDialog(DetalleConteoActivity.this);
                                    if (respuesta == null
                                            || respuesta.getDatosSalida() == null
                                            || respuesta.getDatosSalida().size() == 0) {
                                        dialogo.showDialog(DetalleConteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                                        dialogo.showDialog(DetalleConteoActivity.this,
                                                respuesta.getDatosSalida().get(idxMensajeError),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (respuesta.getCursoresSalida().size() == 0) {
                                        System.out.println(respuesta.getDatosSalida().get(idxMensajeError));

                                        dialogo.showDialog(DetalleConteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);

                                    if (cursor.getCantRegistros() > 0) {
                                        List<String> row = cursor.getRegistros().get(0);

                                        txtTotArtSinDiferencia.setText(row.get(idxArtSinDiferencia));
                                        txtTotArtSinContar.setText(row.get(idxArtSinContar));
                                        txtTotArtConDiferencia.setText(row.get(idxArtConDiferencia));
                                        txtTotArtParaReconteo.setText(row.get(idxArtParaReconteo));
                                    }
                                }catch(Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                            " consultaResumenConteo : manejarExitoVolley > "+e.getMessage(), e);
                                }
                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                txtTotArtSinDiferencia.setText("0");
                                txtTotArtSinContar.setText("0");
                                txtTotArtConDiferencia.setText("0");
                                Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                        " consultaResumenConteo : manejarErrorVolley > "+error.getMessage(), error);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);
    }

    public void consultaArticulosXZonaXTienda(){

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "int", "1"));//Pais
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "String", "0"));//getIntent().getStringExtra("idUbicacion")));
        CuerpoPeticion.add(new ParametroCuerpo(5, "String","0"));//getIntent().getStringExtra("idZona")));
        CuerpoPeticion.add(new ParametroCuerpo(6, ":ArrOra.T_ARR_CODBARRA_XART|T_OBJ_CODBARRA_XART", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, ":Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(8, ":String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CICLICO.CONSARTXZONAXTIENDA", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        DetalleConteoActivity.this,
                        "Consultando artículos de la zona...",
                        solicitud,
                        new HandlerRespuestasVolley() {
                            @Override
                            public void manejarExitoVolley(RespuestaDinamica respuesta) {

                                try
                                {
                                    int idxIdArticulo = 0, idxNombreArticulo = 1, idxCodigosbarra = 2,
                                            idxCurSalida = 6, idxIdError = 7, idxMensajeError = 8,
                                            idxCodbarrsInArray = 1;

                                    ViewDialog dialogo = new ViewDialog(DetalleConteoActivity.this);
                                    if (respuesta == null
                                            || respuesta.getDatosSalida() == null
                                            || respuesta.getDatosSalida().size() == 0) {
                                        dialogo.showDialog(DetalleConteoActivity.this,
                                                "Error en central al procesar la solicitud...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                                        dialogo.showDialog(DetalleConteoActivity.this,
                                                respuesta.getDatosSalida().get(idxMensajeError),
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    } else if (respuesta.getCursoresSalida().size() == 0) {
                                        dialogo.showDialog(DetalleConteoActivity.this,
                                                "No se enviaron datos de respuesta...",
                                                null,
                                                TiposAlert.ERROR);
                                        return;
                                    }

                                    Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);
                                    for (List<String> registro : cursor.getRegistros()) {
                                        try {
                                            long idArticulo = Long.parseLong(registro.get(idxIdArticulo));
                                            String nomArticulo = registro.get(idxNombreArticulo);
                                            ArticuloBean articulo = new ArticuloBean(idArticulo, nomArticulo);

                                            if (registro.get(idxCodigosbarra) != null &&
                                                    !registro.get(idxCodigosbarra).trim().isEmpty()) {

                                                List<ArrayList<String>> codigos =
                                                        oMapper.readValue(registro.get(idxCodigosbarra), new TypeReference<List<ArrayList<String>>>() {
                                                        });
                                                for (List<String> codigoBarras : codigos) {
                                                    articulo.getCodigos().add(codigoBarras.get(idxCodbarrsInArray));
                                                }
                                            }
                                            if (articulo.getCodigos().size() > 0) {
                                                articulosCargados.add(articulo);
                                                GlobalShare.getInstace().getArticulos().put(
                                                        getIntent().getStringExtra("idZona"),
                                                        articulosCargados
                                                );
                                            } else {
                                                Log.w(GlobalShare.logAplicaion, getClass().getName()+
                                                        " : consultaArticulosXZonaXTienda : manejarExitoVolley > Artículo sin código de barras >>"+articulo);
                                            }
                                        } catch (IOException e) {
                                            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                                    " : consultaArticulosXZonaXTienda : manejarExitoVolley > "+e.getMessage(), e);
                                        }
                                    }
                                }catch(Exception e){
                                    Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                            " : consultaArticulosXZonaXTienda : manejarExitoVolley >"+e.getMessage(), e);
                                }
                            }

                            @Override
                            public void manejarErrorVolley(VolleyError error) {
                                Log.e(GlobalShare.logAplicaion, getClass().getName()+
                                        " : consultaArticulosXZonaXTienda : manejarErrorVolley >"+error.getMessage(), error);
                            }
                        }
                );
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);
    }

    public void verArticulosSinContar(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent i = creaIntentExtrasCompleto(AriculosConDiferenciaActivity.class);
        i.putExtra("titulo", "Artículos Sin Contar");
        startActivity(i);
    }

    public void verArticulosConDiferencia(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent i = creaIntentExtrasCompleto(AriculosConDiferenciaActivity.class);
        i.putExtra("titulo", "Artículos Con Diferencia");
        startActivity(i);
    }
    public void verArticulosSinDiferencia(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent i = creaIntentExtrasCompleto(AriculosConDiferenciaActivity.class);
        i.putExtra("titulo", "Artículos Sin Diferencia");
        startActivity(i);
    }

    public void verArticulosParaReconteo(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent i = creaIntentExtrasCompleto(AriculosConDiferenciaActivity.class);
        i.putExtra("titulo", "Artículos Para Reconteo");
        startActivity(i);
    }

    private Intent creaIntentExtrasCompleto(Class cls){
        Intent intent = new Intent(this, cls);

        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda"))  );
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")) );
        intent.putExtra( "idUbicacion", new String(getIntent().getStringExtra("idUbicacion")) );
        intent.putExtra( "nombre_ubicacion", new String(getIntent().getStringExtra("nombre_ubicacion")) );
        intent.putExtra( "idZona", new String(getIntent().getStringExtra("idZona")) );
        intent.putExtra( "nombre_zona", new String(getIntent().getStringExtra("nombre_zona")) );
        intent.putExtra( "esReconteo", (Boolean) getIntent().getBooleanExtra("esReconteo", false));

        return  intent;
    }

    public void regresarZonas(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        finish();
    }

    public void onResume(){
        super.onResume();
        consultaResumenConteo();
    }

}
