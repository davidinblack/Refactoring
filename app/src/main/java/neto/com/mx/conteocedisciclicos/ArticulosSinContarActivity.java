package neto.com.mx.conteocedisciclicos;

import android.app.ProgressDialog;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
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


public class ArticulosSinContarActivity extends AppCompatActivity implements HandlerRespuestasVolley {
    private static final int cantItemsXCarga = 100;

    private ObjectMapper oMapper = new ObjectMapper();

    List<ArticuloBean> articulosCargados;
    List<String> vistaArticulosCargados;
    ArrayAdapter listAdapter;

    ListView listView;
    EditText lectorCodigos;

    TextView txtArtContados;
    TextView txtTotArticulosContados;
    TextView txtUbicacion;
    Button btnZona;
    ProgressDialog progresDialogCargandoItems;
    int idxUltArtCargado;
    boolean cargandoItemsLista=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_07_articulos_sin_contar);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        listView = (ListView)findViewById(R.id.listViewArticulosSinContar);

        txtArtContados = (TextView)findViewById(R.id.txtArtContados);
        txtTotArticulosContados = (TextView)findViewById(R.id.txtTotArticulosContados);
        txtUbicacion= (TextView)findViewById(R.id.txtUbicacion);
        btnZona= (Button)findViewById(R.id.btnZona);

        btnZona.setText(getIntent().getStringExtra("nombre_zona"));

        lectorCodigos = (EditText)findViewById(R.id.codigoBarrasLeido);
        lectorCodigos.setText("");
        lectorCodigos.setInputType(InputType.TYPE_NULL);
        lectorCodigos.requestFocus();
        lectorCodigos.setOnEditorActionListener(escaneaListener);

        txtUbicacion.setText(getIntent().getStringExtra("nombre_ubicacion"));

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView lv, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int ultimoEnPantalla = firstVisibleItem + visibleItemCount;
                if ((ultimoEnPantalla == totalItemCount) && !(cargandoItemsLista)) {
                    cargarMasElementos();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String nombreArtSeleccionado = (String) listView.getItemAtPosition(position);
                    Log.d(GlobalShare.logAplicaion, getClass().getName()+
                            " : setOnItemClickListener > nombreArtSeleccionado > ["+nombreArtSeleccionado+"]");
                    ArticuloBean articuloSel = null;
                    if (nombreArtSeleccionado == null || nombreArtSeleccionado.equals("")){
                        Log.d(GlobalShare.logAplicaion, getClass().getName()+
                                " : setOnItemClickListener > nombre de artículo nulo o vacío : ["+nombreArtSeleccionado+"]");
                        return;
                    }
                    Log.d(GlobalShare.logAplicaion, getClass().getName()+
                            " : setOnItemClickListener > articulosCargados.size() > ["+articulosCargados.size()+"]");
                    for (ArticuloBean articulo : articulosCargados) {
                        if (articulo.getNombreArticulo().trim().equals(nombreArtSeleccionado.trim())) {
                            articuloSel = articulo;
                            break;
                        }
                    }

                    Log.d(GlobalShare.logAplicaion, getClass().getName()+
                            " : setOnItemClickListener > articuloSel > ["+articuloSel+"]");
                    if( articuloSel == null )
                        return;

                    Intent intent = new Intent(ArticulosSinContarActivity.this, ActualizaConteoActivity.class);
                    intent.putExtra("credencial", (CredencialBean) getIntent().getSerializableExtra("credencial"));
                    intent.putExtra("idInventario", new String(getIntent().getStringExtra("idInventario")));
                    intent.putExtra("idTienda", new String(getIntent().getStringExtra("idTienda")));
                    intent.putExtra("nombreTienda", new String(getIntent().getStringExtra("nombreTienda")));
                    intent.putExtra("idUbicacion", new String(getIntent().getStringExtra("idUbicacion")));
                    intent.putExtra("nombre_ubicacion", new String(getIntent().getStringExtra("nombre_ubicacion")));
                    intent.putExtra("idZona", new String(getIntent().getStringExtra("idZona")));
                    intent.putExtra("nombre_zona", new String(getIntent().getStringExtra("nombre_zona")));
                    //intent.putExtra( "articulosXZona", articulosCargados );
                    intent.putExtra("articuloSeleccionado", articuloSel);
                    startActivity(intent);
                }catch(Exception e){
                    Log.e(GlobalShare.logAplicaion, getClass().getName()+" : setOnItemClickListener > "+e.getMessage(), e);
                }
            }
        });

        articulosCargados = new ArrayList<ArticuloBean>();
        vistaArticulosCargados = new ArrayList<String>();
        progresDialogCargandoItems = new ProgressDialog(this);
        progresDialogCargandoItems.setCancelable(false);
        progresDialogCargandoItems.setInverseBackgroundForced(false);
        progresDialogCargandoItems.setMessage("Cargando más artículos...");

        listAdapter = new ArrayAdapter<String>(
                this, R.layout.simple_row, vistaArticulosCargados);
        listView.setAdapter(listAdapter);

        cargarArticulosSinContar();
    }


    private void cargarMasElementos(){

        if ( articulosCargados != null && idxUltArtCargado < articulosCargados.size() ) {
            final ProgressDialog dialog = new ProgressDialog(this);
            new AsyncTask<String, Void, String>() {
                @Override
                protected void onPreExecute() {
                    dialog.show();
                }

                @Override
                protected String doInBackground(String... params) {
                    try
                    {
                        final int maxArtCargar = idxUltArtCargado + cantItemsXCarga;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                while (idxUltArtCargado < articulosCargados.size() &&
                                        idxUltArtCargado <= maxArtCargar) {
                                    ArticuloBean art = articulosCargados.get(idxUltArtCargado++);
                                    listAdapter.add(art.getNombreArticulo());
                                }

                                listAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (Exception e) {
                        Log.e(GlobalShare.logAplicaion, getClass().getName() + " : doInBackground :: " + e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String x) {
                    dialog.dismiss();
                }

                @Override
                protected void onCancelled() {
                    dialog.dismiss();
                }
            }.execute();
        }
    }

    TextView.OnEditorActionListener escaneaListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView txtV, int actionId, KeyEvent event){
            if( actionId == EditorInfo.IME_NULL &&
                    event.getAction() == KeyEvent.ACTION_DOWN){
                //Búsqueda de código de barras
                buscarArticulo(new String(lectorCodigos.getText().toString().trim()));
            }
            return true;
        }
    };

    private void buscarArticulo(String codigoBarras){
        lectorCodigos.setText("");
        ArticuloBean articuloEncontrado = null;

        for (ArticuloBean a : articulosCargados ){
            for( String cod : a.getCodigos() ){
                if( codigoBarras.equals(cod) ){
                    articuloEncontrado = a;
                    break;
                }
            }
        }

        if( articuloEncontrado == null ){
            ViewDialog dialogo = new ViewDialog(this );
            dialogo.showDialogDismiss(this,
                    "No se encontró el artículo escaneado...",
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

    private void cargarArticulosSinContar() {

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "Int", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "String", getIntent().getStringExtra("idZona")));
        CuerpoPeticion.add(new ParametroCuerpo(5, ":Long", "0" ));
        CuerpoPeticion.add(new ParametroCuerpo(6, ":Long", "0" ));
        CuerpoPeticion.add(new ParametroCuerpo(7, ":ArrOra.T_ARR_CODBARRA_XART|T_OBJ_CODBARRA_XART", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(8, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(9, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSARTPENDIENTECONTAR", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        ArticulosSinContarActivity.this,
                        "Consultando artículos sin contar...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.ejecutarConsultaWS(8, 9);
    }

    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {
        try {
            final ProgressDialog dialogoTemp = new ProgressDialog(this);
            runOnUiThread(new Runnable() {
                public void run() {
                    dialogoTemp.setMessage("Procesando artículos recibidos...");
                    dialogoTemp.setCancelable(false);
                    dialogoTemp.show();
                }
            });

            lectorCodigos.setText("");
            articulosCargados = new ArrayList<ArticuloBean>();
            ViewDialog dialogo = new ViewDialog(this);
            int idxIdArticulo = 0, idxNombreArticulo = 1,
                    idxCodigosbarra = 2,
                    idxTotArtContados = 6, idxTotArticulos = 5,
                    idxCurSalida = 7, idxIdError = 8, idxMensajeError = 9,
                    idxCodbarrsInArray = 1;

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

            String articulosContados = respuesta.getDatosSalida().get(idxTotArtContados);
            String articulosXContar = respuesta.getDatosSalida().get(idxTotArticulos);

            txtArtContados.setText(articulosContados);
            txtTotArticulosContados.setText(articulosXContar);

            Cursor cursor = respuesta.getCursoresSalida().get(idxCurSalida);
            for (List<String> registro : cursor.getRegistros()) {

                long idArticulo = Long.parseLong(registro.get(idxIdArticulo));
                String nomArticulo = registro.get(idxNombreArticulo);
                ArticuloBean articulo = new ArticuloBean(idArticulo, nomArticulo);

                if (registro.get(idxCodigosbarra) != null &&
                        !registro.get(idxCodigosbarra).trim().isEmpty()) {
                    List<ArrayList<String>> codigos = null;
                    try {
                        codigos = oMapper.readValue(registro.get(idxCodigosbarra),
                                new TypeReference<List<ArrayList<String>>>() {
                                });
                    } catch (IOException e) {
                        codigos = new ArrayList<ArrayList<String>>();
                        Log.e(GlobalShare.logAplicaion, getClass().getName() + " : Error convirtiendo el código de barras > " + e.getMessage(), e);
                    }
                    for (List<String> codigoBarras : codigos) {
                        articulo.getCodigos().add(codigoBarras.get(idxCodbarrsInArray));
                    }
                }

                articulosCargados.add(articulo);
            }

            runOnUiThread(new Runnable() {
                public void run() {
                    dialogoTemp.dismiss();
                }
            });

            cargarMasElementos();
            /*if (idxUltArtCargado == articulosCargados.size() - 1) {
                vistaArticulosCargados.add("");
                vistaArticulosCargados.add("");
            } else {
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView lv, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int ultimoEnPantalla = firstVisibleItem + visibleItemCount;
                        if ((ultimoEnPantalla == totalItemCount) && !(cargandoItemsLista)) {
                            cargarMasElementos();
                        }
                    }
                });
            }

            listAdapter = new ArrayAdapter<String>(
                    this, R.layout.simple_row, vistaArticulosCargados);
            listView.setAdapter(listAdapter);

            runOnUiThread(new Runnable() {
                public void run() {
                    dialogoTemp.dismiss();
                }
            });*/

            cargarMasElementos();

        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : cargarArticulosSinContar : manejarExitoVolley > "+e.getMessage(), e);
        }
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        lectorCodigos.setText("");
        Log.i(GlobalShare.logAplicaion, getClass().getName()+
                " : cargarArticulosSinContar : manejarErrorVolley > "+error.getMessage(), error);
    }

    public void irAResumen(View view){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = new Intent(this, AvanceInventarioActivity.class);
        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda"))  );
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")) );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void regresar(View view){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = new Intent(this, SeleccionaUbicacionActivity.class);
        intent.putExtra( "credencial", (CredencialBean)getIntent().getSerializableExtra("credencial") );
        intent.putExtra( "idInventario", new String(getIntent().getStringExtra("idInventario")) );
        intent.putExtra( "idTienda", new String(getIntent().getStringExtra("idTienda"))  );
        intent.putExtra( "nombreTienda", new String(getIntent().getStringExtra("nombreTienda")) );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        regresar(null);
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

                    int idxArt = vistaArticulosCargados.indexOf(artBean.getNombreArticulo());
                    if (idxArt >= 0) {
                        //Log.d(GlobalShare.logAplicaion, getClass().getName()+ " : onResume : idxArt >= 0 > : Encontrado y eliminado...");
                        GlobalShare.getInstace().removeUltimoArticuloContado(artBean);
                        vistaArticulosCargados.remove(idxArt);
                        //vistaArticulosCargados.add(artBean.getNombreArticulo());
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

    protected void onDestroy(){
        GlobalShare.getInstace().resetUltimosArticulosContados();
        super.onDestroy();
    }
}
