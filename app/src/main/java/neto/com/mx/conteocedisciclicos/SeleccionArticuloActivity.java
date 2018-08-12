package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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


public class SeleccionArticuloActivity extends AppCompatActivity implements HandlerRespuestasVolley {
    private static final int cantItemsXCarga = 50;
    private ObjectMapper oMapper = new ObjectMapper();

    List<String> vistaArticulosCargados;

    EditText lectorCodigos;
    Intent intent;
    CredencialBean credencial;
    List<ArticuloBean> articulosCargados ;
    ListView listView;
    ArrayAdapter listAdapter;
    int idxActualArtCargados = 0;
    boolean cargandoItemsLista = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_05_seleccion_articulo);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        RelativeLayout rel = (RelativeLayout)findViewById(R.id.activity_selecciona_articulo);
        //LinearLayout contedorZona = (LinearLayout)findViewById(R.id.contedorZona);
        lectorCodigos = (EditText) findViewById(R.id.codigoBarrasLeido);
        lectorCodigos.setText("");
        lectorCodigos.setInputType(InputType.TYPE_NULL);
        lectorCodigos.requestFocus();
        lectorCodigos.setOnEditorActionListener(escaneaListener);

        TextView txtFecha = (TextView) findViewById(R.id.txtFecha);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFecha.setText(sdf.format(new Date()));

        if( getIntent().getStringExtra("nombre_ubicacion").toLowerCase().equals("reserva") ){
            rel.setBackgroundColor(getResources().getColor(R.color.colorAzulReserva));
            lectorCodigos.setBackgroundColor(getResources().getColor(R.color.colorAzulReserva));
            //contedorZona.setVisibility(View.GONE);
        }else{
            rel.setBackgroundColor(getResources().getColor(R.color.colorMoradoActivo));
            lectorCodigos.setBackgroundColor(getResources().getColor(R.color.colorMoradoActivo));
            //contedorZona.setVisibility(View.VISIBLE);
        }

        listView = (ListView)findViewById(R.id.listViewVista);

        TextView txtUbicacion = (TextView)findViewById(R.id.txtUbicacion);
        //Button btnZona = (Button)findViewById(R.id.btnZona);
        TextView txtUsuarioNombre = (TextView) findViewById( R.id.txtUsuarioNombre);

        intent = getInstanciaIntent(ActualizaConteoActivity.class);

        credencial = (CredencialBean) getIntent().getSerializableExtra("credencial");


        txtUsuarioNombre.setText(credencial.getNombreUsuario());
        txtUbicacion.setText(new String(getIntent().getStringExtra("nombre_ubicacion")));
        //btnZona.setText(new String(getIntent().getStringExtra("nombre_zona")));

        /*articulosCargados = GlobalShare.getInstace()
                .getArticulos().get(
                        getIntent().getStringExtra("idZona"));*/


        //if( articulosCargados == null || articulosCargados.size() == 0 ){
            articulosCargados = new ArrayList<ArticuloBean>();
            consultaArticulosXZonaXTiendaREST();
        //}


        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView v, int scrollState) {}
            @Override
            public void onScroll(AbsListView lv, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int ultimoItemEnPantalla = firstVisibleItem + visibleItemCount;
                if( (ultimoItemEnPantalla == totalItemCount) && !(cargandoItemsLista)){
                    cargarMasItems();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String nombreArtSeleccionado = (String) listView.getItemAtPosition(position);
                    if (nombreArtSeleccionado== null || nombreArtSeleccionado.equals("")){
                        Log.d(GlobalShare.logAplicaion, getClass().getName()+
                                " : setOnItemClickListener > nombre de artículo nulo o vacío : ["+nombreArtSeleccionado+"]");
                        return;
                    }

                    ArticuloBean articuloSel = null;
                    for (ArticuloBean articulo : articulosCargados) {
                        if (articulo.getNombreArticulo().trim().equals(nombreArtSeleccionado.trim())) {
                            articuloSel = articulo;
                            break;
                        }
                    }
                    if (articuloSel != null) {
                        iniciaActualizacionConteo(articuloSel);
                    }
                }catch (Exception e){
                    Log.e(GlobalShare.logAplicaion, getClass().getName()+" : setOnItemClickListener > "+e.getMessage(), e);
                }
            }
        });

        vistaArticulosCargados = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(
                this, R.layout.simple_row, vistaArticulosCargados);
        listView.setAdapter(listAdapter);

    }

    private void cargarMasItems() {

        if( articulosCargados != null &&
                idxActualArtCargados < articulosCargados.size() ) {

            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... parametros) {
                    try {
                        Thread.sleep(1000);
                        final int maxArtCargar = idxActualArtCargados + cantItemsXCarga;
                        runOnUiThread(new Runnable(){
                            public void run() {
                                while (idxActualArtCargados < articulosCargados.size() &&
                                        idxActualArtCargados < maxArtCargar) {
                                    ArticuloBean articulo = articulosCargados.get(idxActualArtCargados++);
                                    listAdapter.add(articulo.getNombreArticulo());
                                }

                                listAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (Exception e) {
                        Log.e(GlobalShare.logAplicaion, getClass().getName() + " : doInBackground :: " + e.getMessage(), e);
                    }
                    return "Ok";
                }
            }.execute();
        }
    }

    TextView.OnEditorActionListener escaneaListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView txtV, int actionId, KeyEvent event){
            if( actionId == EditorInfo.IME_NULL &&
                    event.getAction() == KeyEvent.ACTION_DOWN){
                buscarArticulo(new String (lectorCodigos.getText().toString().trim()));
            }
            return true;
        }
    };

    public void consultaArticulosXZonaXTiendaREST(){
        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "int", "1"));//Pais
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "String", getIntent().getStringExtra("idUbicacion")));
        CuerpoPeticion.add(new ParametroCuerpo(5, "String", getIntent().getStringExtra("idZona")));
        CuerpoPeticion.add(new ParametroCuerpo(6, ":ArrOra.T_ARR_CODBARRA_XART|T_OBJ_CODBARRA_XART", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, ":Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(8, ":String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CICLICO.CONSARTXZONAXTIENDA", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        SeleccionArticuloActivity.this,
                        "Consultando artículos de la ubicación...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.ejecutarConsultaWS(7, 8);
    }

    public void regresarPantallaAnterior(View view) {
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = null;
        //if( getIntent().getStringExtra("nombre_ubicacion").trim().toLowerCase().equals("reserva") ){
            intent = getInstanciaIntent(SeleccionaUbicacionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        /*}else if( getIntent().getStringExtra("nombre_ubicacion").trim().toLowerCase().equals("activo") ){
            intent = getInstanciaIntent(ArticulosSinContarActivity.class);
        }*/
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        regresarPantallaAnterior(null);
    }

    private Intent getInstanciaIntent(Class cls){
        intent = new Intent(this, cls);

        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda")));
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));

        intent.putExtra("idUbicacion", new String(getIntent().getStringExtra("idUbicacion")) );
        intent.putExtra("nombre_ubicacion", new String(getIntent().getStringExtra("nombre_ubicacion")) );
        intent.putExtra("idZona", new String(getIntent().getStringExtra("idZona")) );
        intent.putExtra("nombre_zona", new String(getIntent().getStringExtra("nombre_zona")) );

        return intent;
    }

    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {

        int idxIdArticulo = 0, idxNombreArticulo = 1, idxCodigosbarra= 2,
            idxCurSalida = 6, idxIdError=7,idxMensajeError=8,
            idxCodbarrsInArray=1;

        try {
            ViewDialog dialogo = new ViewDialog(this);
            if (respuesta == null
                    || respuesta.getDatosSalida() == null
                    || respuesta.getDatosSalida().size() == 0) {
                dialogo.showDialog(this,
                        "Error en central al procesar la solicitud...",
                        null,
                        TiposAlert.ERROR);
                return;
            } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                return;
            } else if (respuesta.getCursoresSalida().size() == 0) {
                dialogo.showDialog(this,
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
                    if (articulo.getCodigos().size() > 0)
                        articulosCargados.add(articulo);
                    else
                        Log.i(GlobalShare.logAplicaion, getClass().getName()+" manejarExitoVolley > Artículo sin códigos de barras >" + articulo.toString());

                    /*GlobalShare.getInstace().getArticulos().put(
                            getIntent().getStringExtra("idZona"),
                            articulosCargados
                    );*/

                } catch (IOException e) {
                    Log.e(GlobalShare.logAplicaion, getClass().getName()+"Error: " + e.getLocalizedMessage());
                }
            }

            cargarMasItems();

        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : consultaArticulosXZonaXTiendaREST : manejarExitoVolley > "+e.getMessage(), e);
        }
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        Log.e(GlobalShare.logAplicaion, getClass().getName()+" : consultaArticulosXZonaXTiendaREST : manejarErrorVolley > "+error.getMessage(), error);
    }

    private void buscarArticulo(String codigoBarras){
        lectorCodigos.setText("");

        ArticuloBean articuloEncontrado = null;

        forPrincipal:
        {
            for (ArticuloBean a : articulosCargados) {
                for (String cod : a.getCodigos()) {
                    if (codigoBarras.equals(cod)) {
                        articuloEncontrado = a;
                        break forPrincipal;
                    }
                }
            }
        }

        if( articuloEncontrado == null ){
            ViewDialog dialogo = new ViewDialog(this );
            dialogo.showDialogDismiss(this,
                    "No se encontró el artículo...",
                    null,
                    TiposAlert.ERROR,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            lectorCodigos.setText("");
                        }
                    });
            return ;
        }

        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        iniciaActualizacionConteo(articuloEncontrado);
    }

    private void iniciaActualizacionConteo(ArticuloBean articuloEncontrado){
        Intent intent = new Intent(this, ActualizaConteoActivity.class);
        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda"))  );
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")) );
        intent.putExtra( "idUbicacion", new String(getIntent().getStringExtra("idUbicacion")) );
        intent.putExtra( "nombre_ubicacion", new String(getIntent().getStringExtra("nombre_ubicacion")) );
        intent.putExtra( "idZona", new String(getIntent().getStringExtra("idZona")) );
        intent.putExtra( "nombre_zona", new String(getIntent().getStringExtra("nombre_zona")) );
        intent.putExtra( "articuloSeleccionado", articuloEncontrado );

        startActivity(intent);
    }

    @Override
    protected void onStop() {
        lectorCodigos.setText("");
        super.onStop();
    }

    protected void onResume(){
        try {
            if( vistaArticulosCargados != null && vistaArticulosCargados.size()>0) {

                for (ArticuloBean artBean : GlobalShare.getInstace().getUltimosArticulosContados()) {

                    int idxArtVista = vistaArticulosCargados.indexOf(artBean.getNombreArticulo());
                    //int idxArtEscan = articulosCargados.indexOf(artBean);
                    if (idxArtVista >= 0) {
                        //Log.d(GlobalShare.logAplicaion, getClass().getName()+ " : onResume : idxArt >= 0 > : Encontrado y eliminado...");
                        GlobalShare.getInstace().removeUltimoArticuloContado(artBean);
                        vistaArticulosCargados.remove(idxArtVista);//Remover de articulos para vista
                        //articulosCargados.remove(idxArtEscan);//Remover de artículos para escanear

                        listAdapter.notifyDataSetChanged();
                        break;
                    }
                }

            }
        }catch(Exception error){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+
                    " : onResume > "+error.getMessage(), error);
        }
        super.onResume();
    }
}
