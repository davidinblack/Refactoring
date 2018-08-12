package neto.com.mx.conteocedisciclicos;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import neto.com.mx.conteocedisciclicos.beans.CredencialBean;
import neto.com.mx.conteocedisciclicos.cliente.ClienteConsultaGenerica;
import neto.com.mx.conteocedisciclicos.cliente.HandlerRespuestasVolley;
import neto.com.mx.conteocedisciclicos.globales.GlobalShare;
import neto.com.mx.conteocedisciclicos.mensajes.Cursor;
import neto.com.mx.conteocedisciclicos.mensajes.ParametroCuerpo;
import neto.com.mx.conteocedisciclicos.mensajes.RespuestaDinamica;
import neto.com.mx.conteocedisciclicos.mensajes.SolicitudServicio;
import neto.com.mx.conteocedisciclicos.dialogos.ViewDialog;
import neto.com.mx.conteocedisciclicos.utiles.TiposAlert;



public class LoginActivity extends AppCompatActivity implements HandlerRespuestasVolley{
    private String usuario;
    private String pass;

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);
    private ObjectMapper oMapper = new ObjectMapper();

    EditText passwordText;
    EditText usuarioText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        //setContentView(R.layout.activity_000_splash_screen);
        setContentView(R.layout.activity_00_login);
        getSupportActionBar().hide();

        ImageView logoImage = (ImageView) findViewById(R.id.logoImage);
        logoImage.animate().setDuration(2000);
        logoImage.animate().alpha(1f);

        Button loginBoton = (Button) findViewById(R.id.loginBoton);
        loginBoton.animate().setDuration(2000);
        loginBoton.animate().alpha(1f);

        usuarioText = (EditText) findViewById(R.id.usuarioText);
        usuarioText.setText("");
        /*usuarioText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ){
                    //Ocultar espacio
                }
            }
        });*/

        passwordText = (EditText) findViewById(R.id.passwordText);
        passwordText.setText("");
        /*passwordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ){
                    //Ocultar espacio
                }
            }
        });*/

        //verificarVersion();
    }

    public void iniciaLogueo(View view) {

        view.startAnimation(buttonClick);
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrador.vibrate(100);

        EditText usuarioText = (EditText) findViewById(R.id.usuarioText);
        usuario = usuarioText.getText().toString();

        EditText passwordText = (EditText) findViewById(R.id.passwordText);
        pass = passwordText.getText().toString();

        if(usuario.equals("")) {
            ViewDialog alert = new ViewDialog(LoginActivity.this);
            alert.showDialog(LoginActivity.this, "Debes introducir tu usuario: ", null, TiposAlert.ERROR);
            return;
        }

        if(pass.equals("")) {
            ViewDialog alert = new ViewDialog(LoginActivity.this);
            alert.showDialog(LoginActivity.this, "Debes introducir tu clave", null, TiposAlert.ERROR);
            return;
        }

        loguearseCentralREST();
    }

    public void loguearseCentralREST(){
        List<ParametroCuerpo> CuerpoPeticion = new ArrayList<ParametroCuerpo>();
        CuerpoPeticion.add(new ParametroCuerpo(1, "long", usuario ));
        CuerpoPeticion.add(new ParametroCuerpo(2, "String", convierteMD5(pass) ));
        CuerpoPeticion.add(new ParametroCuerpo(3, ":String", "0"));//Nombre
        CuerpoPeticion.add(new ParametroCuerpo(4, ":String", "0"));//Apellido 1
        CuerpoPeticion.add(new ParametroCuerpo(5, ":String", "0"));//Apellido 2
        CuerpoPeticion.add(new ParametroCuerpo(6, "::Int", "0"));
        CuerpoPeticion.add(new ParametroCuerpo(7, "::String", "0"));

        SolicitudServicio solicitud = new SolicitudServicio("VALIDARUSUARIO", CuerpoPeticion);
        ClienteConsultaGenerica clienteConsultaGenerica =
                new ClienteConsultaGenerica(
                        getString(R.string.EndpointRest),
                        this,
                        LoginActivity.this,
                        "Validando credenciales...",
                        solicitud,
                        this
                );
        clienteConsultaGenerica.setManejarCodigoError(true);
        clienteConsultaGenerica.ejecutarConsultaWS(6, 7);

        EditText usuarioText = (EditText) findViewById(R.id.usuarioText);
        usuarioText.setText("");

        EditText passwordText = (EditText) findViewById(R.id.passwordText);
        passwordText.setText("");
    }

    public String convierteMD5(String pass) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(pass.getBytes());
            byte messageDigest[] = digest.digest();

            return bytesToHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            Log.e(GlobalShare.logAplicaion, "loguearseCentralREST : convierteMD5 : Imposible cifrar el password.", e);
            ViewDialog alert = new ViewDialog(LoginActivity.this);
            alert.showDialog(LoginActivity.this, "Imposible cifrar el parámetro de entrada", null, TiposAlert.ERROR);
        }
        return "";
    }

    private static String bytesToHex (byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;
        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10)
                strbuf.append("0");
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }

    @Override
    public void manejarExitoVolley(RespuestaDinamica respuesta) {
        ViewDialog dialogo = new ViewDialog(LoginActivity.this );
        final int idxIdError=6,idxMensajeError=7;

        try {
            if (respuesta == null
                    || respuesta.getDatosSalida() == null
                    || respuesta.getDatosSalida().size() == 0) {
                dialogo.showDialog(LoginActivity.this,
                        "Error en central al procesar la solicitud...",
                        null,
                        TiposAlert.ERROR);
                return;
            } else if (!respuesta.getDatosSalida().get(idxIdError).equals("0")) {
                /*dialogo.showDialog(LoginActivity.this,
                        respuesta.getDatosSalida().get(idxMensajeError),
                        null,
                        TiposAlert.ERROR);*/
                return;
            }
            Intent intent = new Intent(this, CargaFolioConteoActivity.class);
            String nombre = respuesta.getDatosSalida().get(3) + " " +
                    respuesta.getDatosSalida().get(4) + " " +
                    respuesta.getDatosSalida().get(5);
            intent.putExtra("credencial", new CredencialBean(usuario, nombre));
            Log.i(GlobalShare.logAplicaion, getClass().getName()+" : loguearseCentralREST : manejarExitoVolley : credencial :: usuario > " + usuario + ", "+nombre);
            startActivity(intent);
        }catch(Exception e){
            Log.e(GlobalShare.logAplicaion, getClass().getName()+" : loguearseCentralREST : "+e.getMessage(), e);
        }
    }

    @Override
    public void manejarErrorVolley(VolleyError error) {
        Log.e(GlobalShare.logAplicaion, "loguearseCentralREST : manejarErrorVolley : "+error.getMessage(), error);
    }

    /*public void verificarVersion() {
        Intent i = new Intent(this, DescargaUltimaVersionDialog.class);
        startActivity(i);
    }*/

}



