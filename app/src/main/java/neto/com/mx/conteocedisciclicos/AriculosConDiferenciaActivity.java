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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import neto.com.mx.conteocedisciclicos.ReconteoActivity;
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


public class AriculosConDiferenciaActivity extends AppCompatActivity implements HandlerRespuestasVolley{

    private static final int cantItemsXCarga = 50;
    ArrayList<ArticuloBean> articulosCargados;
    ArrayList<ArticuloBean> articulosRecibidos;

    ArrayList<String> vistaArticulosCargados;
    ArrayAdapter listAdapter;

    ListView listView;
    EditText lectorCodigos;

    TextView txtUsuarioNombre;
    TextView txtTituloVista;
    TextView txtIdInventarioActual;
    ObjectMapper oMapper = new ObjectMapper();

    String tipoConsulta=null;

    boolean cargandoItemsLista = false;
    ProgressDialog pdCargandoArticulos;


    int idxActualArtCargados = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_10_ariculos_con_diferencia);
        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        getSupportActionBar().hide();

        Button btn =(Button)findViewById(R.id.btnZona);
        btn.setText(getIntent().getStringExtra("nombre_zona"));
        listView = (ListView)findViewById(R.id.listViewVista);
        txtTituloVista = (TextView) findViewById(R.id.txtTituloVista);
        txtIdInventarioActual= (TextView) findViewById(R.id.txtIdInventarioActual);
        lectorCodigos = (EditText)findViewById(R.id.editTextBuscador);
        txtUsuarioNombre = (TextView)findViewById(R.id.txtUsuarioNombre);

        TextView txtFecha = (TextView) findViewById(R.id.txtFecha);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        txtFecha.setText(sdf.format(new Date()));


        lectorCodigos.setText("");

        lectorCodigos.setInputType(InputType.TYPE_NULL);
        lectorCodigos.requestFocus();
        lectorCodigos.setOnEditorActionListener(escaneaListener);

        articulosRecibidos = (ArrayList<ArticuloBean>)GlobalShare.getInstace().getArticulos().get(getIntent().getStringExtra("idZona"));//(ArrayList<ArticuloBean>)getIntent().getSerializableExtra("articulosXZona");
        articulosCargados = new ArrayList<ArticuloBean>();

        txtTituloVista.setText(getIntent().getStringExtra("titulo"));
        txtIdInventarioActual.setText(getIntent().getStringExtra("idInventario"));

        CredencialBean credencial = (CredencialBean)getIntent().getSerializableExtra("credencial");
        txtUsuarioNombre.setText(credencial.getNombreUsuario());

        pdCargandoArticulos = new ProgressDialog(this);
        pdCargandoArticulos.setMessage("Cargando más artículos...");
        pdCargandoArticulos.setCancelable(false);
        pdCargandoArticulos.setInverseBackgroundForced(false);

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
                        Intent i = creaIntentExtrasCompleto(ReconteoActivity.class);
                        i.putExtra("articuloSeleccionado", articuloSel);
                        startActivity(i);
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

        cargarArticulosXTipoDetalle();
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
                                if (idxActualArtCargados >= articulosCargados.size()) {
                                    listAdapter.add("");
                                    listAdapter.add("");
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
                buscarArticulo(new String(lectorCodigos.getText().toString().trim()));
            }
            return true;
        }
    };

    private void buscarArticulo(String codigoBarras){
        try {
            lectorCodigos.setText("");
            ArticuloBean articuloEncontrado = null;

            forPrincipal:
            for (ArticuloBean a : articulosRecibidos) {

                for (String cod : a.getCodigos()) {
                    if (codigoBarras.equals(cod)) {
                        articuloEncontrado = a;
                        break forPrincipal;
                    }
                }
            }

            int idxArt = articulosCargados.indexOf(articuloEncontrado);
            if( idxArt<0 )
                articuloEncontrado = null;

            if (articuloEncontrado == null) {
                ViewDialog dialogo = new ViewDialog(this);
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
                return;
            }

            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(100);

            Intent intent = creaIntentExtrasCompleto(ReconteoActivity.class);
            intent.putExtra("articuloSeleccionado", articuloEncontrado);
            intent.putExtra("regresar_a", "articulos_sin_contar");
            startActivity(intent);
        }catch (Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : buscarArticulo > "+e.getMessage(), e);
        }
    }

    private void cargarArticulosXTipoDetalle() {
        idxActualArtCargados=0;
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();

        switch(getIntent().getStringExtra("titulo")){
            case "Artículos Sin Diferencia":
                tipoConsulta = "1";
                break;
            case "Artículos Con Diferencia":
                tipoConsulta = "2";
                break;
            case "Artículos Sin Contar":
                tipoConsulta = "3";
                break;
            case "Artículos Para Reconteo":
                tipoConsulta = "4";
                break;
        }

        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", "1"));
        CuerpoPeticion.add(new ParametroCuerpo(2, "long", getIntent().getStringExtra("idTienda")));
        CuerpoPeticion.add(new ParametroCuerpo(3, "long", getIntent().getStringExtra("idInventario")));
        CuerpoPeticion.add(new ParametroCuerpo(4, "Int", tipoConsulta ));
        CuerpoPeticion.add(new ParametroCuerpo(5, "String", getIntent().getStringExtra("idZona")));
        CuerpoPeticion.add(new ParametroCuerpo(6, "CUR_SALIDA", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(8, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("CONSARTXTIPODETALLE", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        AriculosConDiferenciaActivity.this,
                        "Consultando articulos...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.ejecutarConsultaWS(7, 8);
    }

    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {
        try
        {

            lectorCodigos.setText("");
            articulosCargados.clear();

            ViewDialog dialogo = new ViewDialog(this);
            int idxIdArticulo = 0, idxNombreArticulo = 1,
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
                long idArticulo = Long.parseLong(registro.get(idxIdArticulo));
                String nomArticulo = registro.get(idxNombreArticulo);
                ArticuloBean articulo = new ArticuloBean(idArticulo, nomArticulo);

                articulosCargados.add(articulo);
            }

            cargarMasItems();

        }catch (Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : cargarArticulosXTipoDetalle > "+e.getMessage(), e);
        }
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        Log.e(GlobalShare.logAplicaion, getClass().getName()+
                " : cargarArticulosXTipoDetalle > "+error.getMessage(), error);
    }

    public void irAResumen(View view){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = creaIntentExtrasCompleto(AvanceInventarioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void regresar(View view){
        finish();
    }

    public void regresarAIndicador(View v){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(100);

        Intent intent = creaIntentExtrasCompleto( DetalleConteoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
        //intent.putExtra( "articulosXZona", articulosRecibidos);
        intent.putExtra( "esReconteo", (Boolean) getIntent().getBooleanExtra("esReconteo", false));

        return  intent;
    }

    protected void onResume(){
        try {
            //cargarArticulosXTipoDetalle();
            if( vistaArticulosCargados != null && vistaArticulosCargados.size()>0) {
                for (ArticuloBean artBean : GlobalShare.getInstace().getUltimosArticulosContados()) {

                    int idxArt = vistaArticulosCargados.indexOf(artBean.getNombreArticulo());
                    if (idxArt >= 0) {
                        //Log.d(GlobalShare.logAplicaion, getClass().getName()+ " : onResume : idxArt >= 0 > : Encontrado y eliminado...");
                        GlobalShare.getInstace().removeUltimoArticuloContado(artBean);
                        vistaArticulosCargados.remove(idxArt);
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


    @Override
    protected void onStop() {
        lectorCodigos.setText("");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        GlobalShare.getInstace().resetUltimosArticulosContados();
        super.onDestroy();
    }
}
