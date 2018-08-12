package neto.com.mx.conteocedisciclicos;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

//import conteo.mx.com.neto.inventariotienda.cliente.ClienteSSLConsultaGenerica;
//import conteo.mx.com.neto.inventariotienda.cliente.HandlerRespuestasVolley;
//import conteo.mx.com.neto.inventariotienda.globales.Constantes;
//import conteo.mx.com.neto.inventariotienda.globales.GlobalShare;
//import conteo.mx.com.neto.inventariotienda.mensajes.ParametroCuerpo;
//import conteo.mx.com.neto.inventariotienda.mensajes.RespuestaDinamica;
//import conteo.mx.com.neto.inventariotienda.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.cliente.ClienteConsultaGenerica;
import neto.com.mx.conteocedisciclicos.cliente.HandlerRespuestasVolley;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;


/**
 * Created by dramirezr on 16/02/2018.
 */

public class DescargaUltimaVersionDialog extends Activity {
    public static final int RESULT_ERROR = 1;
    public static final int RESULT_OK = 0;
    public static final int RESULT_ACCESO_DENEGADO = -1;
    public static final int RESULT_NOREGISTRADO = 3;
    public static final int MAX_INTENTOS_DESCARGA = 300;

    private static final int TIME_WAIT_CLOSE_ACTIVITY_SHORT = 2000;
    private static final int TIME_WAIT_CLOSE_ACTIVITY_MEDIUM = 3000;
    private static final int TIME_WAIT_CLOSE_ACTIVITY_LONG = 5000;
    private static final int TIEMPO_REVISAR_DESCARGA = 500;
    private static final int PRIMER_ELEMENTO = 0;

    private static class RespuestaCentral {
        public static final int ACCESO_DENEGADO_A_APP = -1;
        public static final int NO_ES_NECESARIO_ACTUALIZAR = 0;
        public static final int ACTUALIZAR_NUEVA_VERSION = 1;
        public static final int ACTUALIZAR_VERSION_ESTABLE = 2;
        public static final int INICIO_RESPUESTAS_ERROR = 3;
        public static final int APPLICACION_INACTIVA = 15;
    }

    private String versionDescargar;
    private Handler handler = new Handler();
    private boolean isChequerRunning = false;
    long refrenciaDescarga;
    int intentosDescargaRealizados;
    DownloadManager descargaManager;

    LinearLayout principal;

    LinearLayout contDescarga;
    LinearLayout contInicial;

    ScrollView scroll;
    TextView txtAvisoFinal;
    TextView txtMensaje;
    TextView textMensajeDescarga;
    ProgressBar progresoDesacarga;
    String version;

    private void cerrarActivity(long miliSegs) {
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, miliSegs);
    }

    enum VistaActualizacion {INICIAL, DESCARGA, RESULTADO}

    ;
    VistaActualizacion vistaAnterior;

    private void cambiaVistaYTexto(final VistaActualizacion vista, final String strMensaje) {
        runOnUiThread(new Runnable() {
            public void run() {
                txtMensaje.setText(strMensaje);
                textMensajeDescarga.setText(strMensaje);
                txtAvisoFinal.setText(strMensaje);
                if (vistaAnterior != vista) {
                    switch (vista) {
                        case INICIAL:
                            contInicial.setVisibility(View.VISIBLE);
                            contDescarga.setVisibility(View.GONE);
                            txtAvisoFinal.setVisibility(View.GONE);
                            break;
                        case DESCARGA:
                            contDescarga.setVisibility(View.VISIBLE);
                            contInicial.setVisibility(View.GONE);
                            txtAvisoFinal.setVisibility(View.GONE);
                            break;
                        case RESULTADO:
                            contInicial.setVisibility(View.GONE);
                            contDescarga.setVisibility(View.GONE);
                            txtAvisoFinal.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            }
        });
    }

    private void crearVista() {
        principal = new LinearLayout(this);
        principal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        principal.setBackgroundColor(Color.BLACK);
        principal.setGravity(Gravity.CENTER);
        principal.setOrientation(LinearLayout.VERTICAL);

        int dim9sp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 9, getResources().getDisplayMetrics());
        int dim10sp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
        int dim40sp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        int dim20sp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        principal.setPadding(dim20sp, dim20sp, dim20sp, dim20sp);

        contInicial = new LinearLayout(this);
        contInicial.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        int widthProgressBar = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 100f, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams linearLayoutParamsProgress = new LinearLayout.LayoutParams(
                widthProgressBar, LinearLayout.LayoutParams.WRAP_CONTENT, 0.9f);

        progressBar.setLayoutParams(linearLayoutParamsProgress);

        contInicial.addView(progressBar);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setHorizontalScrollBarEnabled(true);
        scroll.setVerticalScrollBarEnabled(true);

        contInicial.addView(scroll);

        txtMensaje = new TextView(this);
        txtMensaje.setText("Verificando versión de aplicación...");
        txtMensaje.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtMensaje.setGravity(View.TEXT_ALIGNMENT_CENTER);
        txtMensaje.setTextSize(dim9sp);

        txtMensaje.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.9f
        ));
        //contInicial.addView(txtMensaje);
        scroll.addView(txtMensaje);

        principal.addView(contInicial);

        contDescarga = new LinearLayout(this);
        contDescarga.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        contDescarga.setOrientation(LinearLayout.VERTICAL);

        textMensajeDescarga = new TextView(this);
        textMensajeDescarga.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textMensajeDescarga.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        textMensajeDescarga.setTextColor(Color.WHITE);
        textMensajeDescarga.setTextSize(dim9sp);

        contDescarga.addView(textMensajeDescarga);
        int progressHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        progresoDesacarga = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progresoDesacarga.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, progressHeight
        ));
        progresoDesacarga.setMax(100);
        progresoDesacarga.setProgress(0);
        progresoDesacarga.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        contDescarga.addView(progresoDesacarga);
        contDescarga.setVisibility(View.GONE);

        principal.addView(contDescarga);

        txtAvisoFinal = new TextView(this);
        txtAvisoFinal.setTextSize(dim10sp);
        txtAvisoFinal.setText("No se podrá continuar usando la aplicación hasta que se haya instalado la nueva versión.");
        txtAvisoFinal.setTextColor(Color.YELLOW);
        txtAvisoFinal.setVisibility(View.GONE);
        txtAvisoFinal.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        principal.addView(txtAvisoFinal);


        setContentView(principal);
        setFinishOnTouchOutside(false);

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ne) {
            Log.e(GlobalShare.logAplicaion, "Error al obtener la versión : " + ne.getMessage());
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        crearVista();

        IntentFilter filtroDescargaCompleta = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(broadcastReceiverDownload, filtroDescargaCompleta);

        setResult(Activity.RESULT_OK);

        verificarVersion();
    }

    /**/
    public void onBackPressed() {
        return;
    }

    private void checarProgreso() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(~(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL));
        Cursor cur = descargaManager.query(query);
        if (!cur.moveToFirst()) {
            cur.close();
            return;
        }
        do {
            long referenciaActual = cur.getLong(cur.getColumnIndex(DownloadManager.COLUMN_ID));
            if (referenciaActual == refrenciaDescarga) {

                int statusDescarga = cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (statusDescarga == DownloadManager.STATUS_FAILED) {
                    Log.e(GlobalShare.logAplicaion, getLocalClassName() + " : checarProgreso : La descarga no se completo debido a un error");

                    cambiaVistaYTexto(VistaActualizacion.RESULTADO, "Se presentó un problema con la descarga de la " +
                            "nueva versión, se intnteará descargar en el futuro.");

                    cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_LONG);
                    return;
                }

                Long bytes_total = cur.getLong(cur.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                Long bytes_so_far = cur.getLong(cur.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                final Float progreso = bytes_so_far == 0l ? 0f : ((bytes_so_far * 1f / bytes_total * 1f) * 100f);

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            progresoDesacarga.setProgress(progreso.intValue());
                            textMensajeDescarga.setText("Descargando versión " + versionDescargar + " %" +
                                    String.format(new Locale("es", "MX"), "%3.0f", progreso));
                        } catch (Exception e) {
                            Log.e(GlobalShare.logAplicaion, getClass() + " : checarProgreso : " + e.getMessage(), e);
                        }
                    }
                });

                if (statusDescarga == DownloadManager.STATUS_SUCCESSFUL) {
                } else {
                    int reazon = cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_REASON));
                    Log.e(GlobalShare.logAplicaion, getClass() +
                            " : checarProgreso : Revisión de estatus de descarga : " +
                            " : Intento núm: " + intentosDescargaRealizados + " - " +
                            "estatusId: " + statusDescarga + ", razon: " + getDownloadErrorReazon(statusDescarga, reazon));
                    if (intentosDescargaRealizados++ == MAX_INTENTOS_DESCARGA) {
                        if (statusDescarga == DownloadManager.STATUS_PAUSED &&
                                reazon == DownloadManager.PAUSED_WAITING_TO_RETRY) {
                            cambiaVistaYTexto(VistaActualizacion.RESULTADO, "Se presentó un problema con la descarga, " +
                                    "se intentará descargar en el futuro.");
                            descargaManager.remove(referenciaActual);
                            setResult(RESULT_ERROR);
                            cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_LONG);
                        }
                    }
                }
            }

        } while (cur.moveToNext());
        if (!descargaTerminada)
            handler.postDelayed(chequer, TIEMPO_REVISAR_DESCARGA);
    }

    private Runnable chequer = new Runnable() {
        @Override
        public void run() {
            try {
                checarProgreso();
            } catch (Exception e) {
                handler.postDelayed(chequer, TIEMPO_REVISAR_DESCARGA);
            }
        }
    };

    public void iniciaChequeoProgreso() {
        intentosDescargaRealizados = 0;
        cambiaVistaYTexto(VistaActualizacion.DESCARGA,
                "Descargando versión " + versionDescargar + "       %" + String.format("%3.1f", 0f));
        if (!isChequerRunning) {
            chequer.run();
            isChequerRunning = true;
        }
    }

    public void verificarVersion() {
        if (GlobalShare.getInstace().getVersionVerificado()) {
            setResult(RESULT_OK);
            Log.w(GlobalShare.logAplicaion, "Versión y acceso verificados...");
            cambiaVistaYTexto(VistaActualizacion.RESULTADO, "Versión y acceso verificados...");
            cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_SHORT);
            return;
        }

        String mensajeError = null;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        String aplicacionId = getResources().getString(R.string.app_id);
        /**/if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //return;
        }
        String imeii = telephonyManager.getDeviceId();
        String version = null;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            mensajeError = e.getLocalizedMessage();
            Log.e(GlobalShare.logAplicaion, getClass().getName() + " : Obteniendo la vesion actual.", e);
        }
        final String versionActual = version;
        //Eliminar apks con version actual en su nombre

        boolean errorConf = true;
        if (aplicacionId == null || aplicacionId.isEmpty()) {
            mensajeError = "No se ha configurado un id para esta aplicación.";
        } else if (versionActual == null || versionActual.isEmpty()) {
            mensajeError = "No ha configurado la versión actual de esta aplicación.";
        } else if (imeii == null || imeii.isEmpty()) {
            mensajeError = "No fue posible leer el IMEII de este dispositivo.";
        } else {
            errorConf = false;
        }

        if (errorConf) {
            cambiaVistaYTexto(VistaActualizacion.RESULTADO, mensajeError);
            cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_LONG);
            return;
        }

        File archivos = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        Log.d(GlobalShare.logAplicaion, "Buscando al archivo que contenga: "+versionActual + ".apk");
        File[] encontrados = archivos.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains(versionActual+".apk");
            }
        });

        if( encontrados != null && encontrados.length > 0) {
            Log.d(GlobalShare.logAplicaion, "Archivo de versión anterior encontrado, procediendo a elimnar...");
            for (File arcActual : encontrados) {
                Log.d(GlobalShare.logAplicaion, "Elimnando archivo: " + arcActual.getAbsolutePath());
                try {
                    arcActual.delete();
                } catch (Exception e) {
                    Log.e(GlobalShare.logAplicaion, getClass().getName() + " : Error al eliminar archivo...", e);
                    mensajeError = e.getLocalizedMessage();
                }
            }
        }else{
            Log.d(GlobalShare.logAplicaion, "Archivo de versión anterior NO encontrado...");
        }

        //contInicial.setVisibility(View.VISIBLE);
        List<ParametroCuerpo> cuerpoPeticion = new ArrayList<>();

        final int idxVersion = 4, idxUrlDescarga = 5, idxIdError = 6, idxMensaje = 7;

        GlobalShare.getInstace().setVersionVerificado(false);

        cuerpoPeticion.add(new ParametroCuerpo(1, "Long", imeii));//IMEII
        cuerpoPeticion.add(new ParametroCuerpo(2, "Long", aplicacionId));//IDAPP
        cuerpoPeticion.add(new ParametroCuerpo(3, "String", versionActual));//VERSIONACTUAL
        cuerpoPeticion.add(new ParametroCuerpo(idxVersion, ":String", ""));//Version por actualizar
        cuerpoPeticion.add(new ParametroCuerpo(idxUrlDescarga, ":String", ""));//URL Desacarga
        cuerpoPeticion.add(new ParametroCuerpo(idxIdError, "::Int", "0"));
        cuerpoPeticion.add(new ParametroCuerpo(idxMensaje, "::String", "0"));

        runOnUiThread(new Runnable() {
            public void run() {
                textMensajeDescarga.setText("Validando acceso de dispositivo...");
                txtMensaje.setText("Validando acceso de dispositivo...");
            }
        });

        SolicitudServicio solicitud = new SolicitudServicio("VERIFICAVERSION", cuerpoPeticion);
        ClienteConsultaGenerica cliente = new ClienteConsultaGenerica(
                getString(R.string.EndpointRest),
                this,
                solicitud,
                new HandlerRespuestasVolley() {
                    @Override
                    public void manejarExitoVolley(final RespuestaDinamica respuesta) {
                        try {

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textMensajeDescarga.setText("Procesando respuesta de servicio...");
                                    txtMensaje.setText("Procesando respuesta de servicio...");
                                }
                            });

                            if (respuesta.getStackTrace() != null && !respuesta.getStackTrace().isEmpty()) {
                                setResult(RESULT_ERROR);
                                Log.d(GlobalShare.logAplicaion, getClass().getName() + " : verificarVersion : Respuesta indica error.");
                                cambiaVistaYTexto(VistaActualizacion.RESULTADO,
                                        "Se presentó un problema al validar la versión... " + respuesta.toString());
                                cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_MEDIUM);
                            } else if (respuesta == null
                                    || respuesta.getDatosSalida() == null
                                    || respuesta.getDatosSalida().size() == 0) {
                                setResult(RESULT_ERROR);
                                Log.d(GlobalShare.logAplicaion, getClass().getName() + " : verificarVersion : manejarExitoVolley :" +
                                        "Respuesta vacía");
                                cambiaVistaYTexto(VistaActualizacion.RESULTADO,
                                        "Respuesta de servicio vacía... " + respuesta.toString());

                                cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_MEDIUM);
                            }

                            int idOperacionRealizar = Integer.parseInt(respuesta.getDatosSalida().get(idxIdError));
                            String descripOpRealizar = respuesta.getDatosSalida().get(idxMensaje);
                            txtAvisoFinal.setText(descripOpRealizar);

                            //0 := No es necesario descargar versión
                            //1 := Descargar versión anterior
                            //2 := Descargar nueva versión

                            GlobalShare.getInstace().setAccesoVerificado(true);
                            if (idOperacionRealizar == RespuestaCentral.NO_ES_NECESARIO_ACTUALIZAR) {//ok
                                GlobalShare.getInstace().setVersionVerificado(true);
                                setResult(RESULT_OK);//Log.d(GlobalShare.logAplicaion, "No es necesario actualizar la versión");
                                cambiaVistaYTexto(VistaActualizacion.RESULTADO, "No es necesario actualizar la versión.");
                                cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_SHORT);
                                return;
                            } else if (idOperacionRealizar == RespuestaCentral.ACTUALIZAR_VERSION_ESTABLE) {//cambiaVistaYTexto(VistaActualizacion.INICIAL, "Se requiere instalar actualización");
                                Log.w(GlobalShare.logAplicaion, descripOpRealizar);
                                cambiaVistaYTexto(VistaActualizacion.INICIAL, getPrimerElementoPipeOTodoNoPipe(descripOpRealizar));
                            } else if (idOperacionRealizar == RespuestaCentral.ACTUALIZAR_NUEVA_VERSION) {//cambiaVistaYTexto(VistaActualizacion.INICIAL, "Se requiere hacer Downgrade...");
                                Log.w(GlobalShare.logAplicaion, descripOpRealizar);
                                cambiaVistaYTexto(VistaActualizacion.INICIAL, getPrimerElementoPipeOTodoNoPipe(descripOpRealizar));
                            } else if (idOperacionRealizar == RespuestaCentral.ACCESO_DENEGADO_A_APP ||
                                    idOperacionRealizar == RespuestaCentral.APPLICACION_INACTIVA) {
                                setResult(RESULT_ACCESO_DENEGADO);
                                GlobalShare.getInstace().setAccesoVerificado(false);
                                //cambiaVistaYTexto(VistaActualizacion.INICIAL, "Acceso denegado para este dispositivo...");
                                cambiaVistaYTexto(VistaActualizacion.RESULTADO, getPrimerElementoPipeOTodoNoPipe(descripOpRealizar));
                                //cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_LONG);
                                return;
                            } else if (idOperacionRealizar >= RespuestaCentral.INICIO_RESPUESTAS_ERROR) {
                                setResult(RESULT_ERROR);
                                Log.w(GlobalShare.logAplicaion, descripOpRealizar);
                                cambiaVistaYTexto(VistaActualizacion.RESULTADO, getPrimerElementoPipeOTodoNoPipe(descripOpRealizar));
                                cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_LONG);
                                return;
                            } else {//Casos no contemplados pasan como [ OK ]
                                setResult(RESULT_OK);
                                Log.w(GlobalShare.logAplicaion, descripOpRealizar);
                                cambiaVistaYTexto(VistaActualizacion.INICIAL, getPrimerElementoPipeOTodoNoPipe(descripOpRealizar));
                                cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_SHORT);
                                return;
                            }

                            //cambiaVistaYTexto(VistaActualizacion.RESULTADO, "Acceso denegado para este dispositivo...");

                            versionDescargar = respuesta.getDatosSalida().get(idxVersion);
                            final String urlVerSig = respuesta.getDatosSalida().get(idxUrlDescarga);

                            try {
                                String[] nombre = urlVerSig.split(Matcher.quoteReplacement("/"));
                                String nombreAPK = nombre[nombre.length - 1];

                                Log.w(GlobalShare.logAplicaion, getClass().getName() +
                                        " : Buscar archivo por nombre : ["+nombreAPK+"]");

                                descargaManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                Uri uriDescarga = Uri.parse(urlVerSig);

                                File archivo = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), nombreAPK);

                                if (archivo.exists()) {
                                    cambiaVistaYTexto(VistaActualizacion.RESULTADO, "Se instalará desde las descargas. No será posible usar esta aplicación sin la versión más actual.");
                                    Log.d(GlobalShare.logAplicaion, getClass().getName() +
                                            " : Archivo encontrado > se instalará desde las descargas...");

                                    Uri uriArchivoEncontrado = null;
                                    Intent intentInstaller = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Log.d(GlobalShare.logAplicaion, getClass().getName() + " : SDK_INT >= N  ...");
                                        uriArchivoEncontrado = FileProvider.getUriForFile(
                                                getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", archivo);
                                        intentInstaller = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                        intentInstaller.setData(uriArchivoEncontrado);
                                        intentInstaller.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    }else{
                                        Log.d(GlobalShare.logAplicaion, getClass().getName() + " : ELSE ...");
                                        uriArchivoEncontrado = Uri.fromFile(archivo);
                                        intentInstaller = new Intent(Intent.ACTION_VIEW);
                                        intentInstaller.setDataAndType(
                                                uriArchivoEncontrado,
                                                "application/vnd.android.package-archive");
                                        intentInstaller.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    }

                                    Log.d(GlobalShare.logAplicaion, getClass().getName() +
                                            " : Iniciando la instalación de la ultima versión ...");

                                    startActivity(intentInstaller);
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            txtAvisoFinal.setText("Iniciando la descarga...");
                                            txtMensaje.setText("Iniciando la descarga...");
                                        }
                                    });

                                    Log.d(GlobalShare.logAplicaion, getClass().getName() + " : Archivo no encontrado > se descargará...");
                                    DownloadManager.Request req = new DownloadManager.Request(uriDescarga);
                                    req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                    req.setAllowedOverRoaming(false);
                                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                    //req.setVisibleInDownloadsUi(false);
                                    req.setTitle("Actualización");
                                    req.setDescription("Descargando nueva versión...");
                                    req.setDestinationInExternalFilesDir(
                                            DescargaUltimaVersionDialog.this,
                                            Environment.DIRECTORY_DOWNLOADS,
                                            nombreAPK
                                    );

                                    refrenciaDescarga = descargaManager.enqueue(req);

                                    iniciaChequeoProgreso();
                                }
                            } catch (Exception e) {
                                setResult(RESULT_ERROR);
                                cambiaVistaYTexto(VistaActualizacion.RESULTADO, "Ocurrió un error durante el proceso de actualización...");
                                Log.e(GlobalShare.logAplicaion, e.getMessage(), e);
                            }
                        } catch (Exception e) {
                            setResult(RESULT_ERROR);
                            Log.e(GlobalShare.logAplicaion, getClass().getName() + " : verificarVersion : " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void manejarErrorVolley(final VolleyError error) {
                        String errorMostrar = "Se presentó un problema al intentar realizar la consulta.";
                        setResult(RESULT_ERROR);

                        if( error != null ) {
                            errorMostrar = "No se tiene acceso al servidor.";
                            if (error.networkResponse != null) {
                                if (error.networkResponse.statusCode >= 500) {
                                    errorMostrar = "[" + error.networkResponse.statusCode + ", " +
                                            error.networkResponse.toString() + "]  \nEl servidor presenta un problema al recibir conexiónes.";
                                } else if (error.networkResponse.statusCode >= 400 &&
                                        error.networkResponse.statusCode < 500) {
                                    errorMostrar = "[" + error.networkResponse.statusCode + ", " +
                                            error.networkResponse.data.toString() + "] \nSe presentó un problema al intentar conectarse con el servidor.";
                                } else if (error.networkResponse.statusCode >= 300 &&
                                        error.networkResponse.statusCode < 400) {
                                    errorMostrar = "[" + error.networkResponse.statusCode + ", " +
                                            error.networkResponse.toString() + "]  \nSe presentó un problema al enviar los datos al servidor.";
                                }else{
                                    errorMostrar = "[" + error.networkResponse.statusCode + ", " +
                                            error.networkResponse.toString() + "]  \nSe presentó un problema inesperado al enviar los datos al servidor.";
                                }
                            }
                        }

                        cambiaVistaYTexto(VistaActualizacion.RESULTADO, errorMostrar);


                        Log.e(GlobalShare.logAplicaion, getClass().getName() +
                                " : verificarVersion : manejarErrorVolley :" + error.getMessage(), error);

                        cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_SHORT);
                    }
                }
        );

        runOnUiThread(new Runnable() {
            public void run() {
                textMensajeDescarga.setText("Consultando info de servicio...");
            }
        });

        cliente.setManejarCodigoError(false);
        //final String respuesta =
        cliente.ejecutarConsultaWSSinDialogos(idxIdError, idxMensaje);
        /*if( respuesta != null && !respuesta.isEmpty())
        {
            runOnUiThread(new Runnable(){
                public void run() {
                    textMensajeDescarga.setText(respuesta);
                    txtMensaje.setText(respuesta);
                    txtAvisoFinal.setText(respuesta);
                }
            });
        }*/
    }

    boolean descargaTerminada = false;
    private BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {
        public void onReceive(Context contexto, Intent intent) {
            long idReferencia = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            intent.getIntExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS, -1);
            Uri uriURLDescarga = null;
            if (refrenciaDescarga == idReferencia) {
                uriURLDescarga = descargaManager.getUriForDownloadedFile(refrenciaDescarga);
                if( uriURLDescarga != null ){
                    cambiaVistaYTexto(VistaActualizacion.RESULTADO,"Versión " + versionDescargar + " descargada con éxito.");
                    progresoDesacarga.setProgress(100);

                    Intent intentInstaller = new Intent(Intent.ACTION_VIEW);
                    intentInstaller.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intentInstaller.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentInstaller.setDataAndType(
                            uriURLDescarga,"application/vnd.android.package-archive");

                    Log.e(GlobalShare.logAplicaion,
                            getClass().getName() + uriURLDescarga.getPath());

                    descargaTerminada = true;

                    Log.e(GlobalShare.logAplicaion, getClass().getName()+ " : Iniciando la instalación de la ultima versión...");
                    //cambiaVistaYTexto(VistaActualizacion.RESULTADO,"Inicia instalación...");
                    cambiaVistaYTexto(VistaActualizacion.RESULTADO,"Iniciando instalación, No será posible seguir usando la aplicación hasta que se realice la instalación...");
                    startActivity(intentInstaller);
                }else{
                    cambiaVistaYTexto(VistaActualizacion.RESULTADO,
                            "No es posible descargar la actualización, es posible que los archivos ya no esten disponibles en la ubicación registrada.");
                    cerrarActivity(TIME_WAIT_CLOSE_ACTIVITY_LONG);
                    return;
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        try {
            this.unregisterReceiver(broadcastReceiverDownload);
        } catch (Exception e) {
            Log.e(GlobalShare.logAplicaion, getClass().getName() + " : onDestroy : " + e.getMessage(), e);
        }
        super.onDestroy();
    }

    private String getDownloadErrorReazon(int estatus, int idRazon) {
        String statusName = null;
        String failedReason = null;
        String pausedReason = null;

        if( estatus == DownloadManager.STATUS_FAILED ){
            statusName = "STATUS_FAILED";
            switch (idRazon) {
                case DownloadManager.ERROR_CANNOT_RESUME:
                    failedReason = "ERROR_CANNOT_RESUME";
                    break;
                case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                    failedReason = "ERROR_DEVICE_NOT_FOUND";
                    break;
                case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                    failedReason = "ERROR_FILE_ALREADY_EXISTS";
                    break;
                case DownloadManager.ERROR_FILE_ERROR:
                    failedReason = "ERROR_FILE_ERROR";
                    break;
                case DownloadManager.ERROR_HTTP_DATA_ERROR:
                    failedReason = "ERROR_HTTP_DATA_ERROR";
                    break;
                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    failedReason = "ERROR_INSUFFICIENT_SPACE";
                    break;
                case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                    failedReason = "ERROR_TOO_MANY_REDIRECTS";
                    break;
                case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                    failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                    break;
                case DownloadManager.ERROR_UNKNOWN:
                    failedReason = "ERROR_UNKNOWN";
                    break;
            }
        }else if( estatus == DownloadManager.STATUS_PAUSED){
            statusName = "STATUS_PAUSED";
            switch (idRazon) {
                case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                    pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                    break;
                case DownloadManager.PAUSED_UNKNOWN:
                    pausedReason = "PAUSED_UNKNOWN";
                    break;
                case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                    pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                    break;
                case DownloadManager.PAUSED_WAITING_TO_RETRY:
                    pausedReason = "PAUSED_WAITING_TO_RETRY";
                    break;
            }
        }else if( estatus == DownloadManager.STATUS_PENDING){
            statusName = "PENDING";
        }else if( estatus == DownloadManager.STATUS_RUNNING){
            statusName = "RUNNING";
        }
        Date ahora = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(ahora)+" :: "+statusName + " > "+ (failedReason!=null?failedReason:"")+ (pausedReason!=null? pausedReason:"") + ".";
    }

    private String getPrimerElementoPipeOTodoNoPipe(String texto){
        /*String res = texto.split(Pattern.quote("|"))[PRIMER_ELEMENTO];
        if( res != null && !res.isEmpty() )
            return res;
        else
            return texto;*/
        return texto;
    }
}

