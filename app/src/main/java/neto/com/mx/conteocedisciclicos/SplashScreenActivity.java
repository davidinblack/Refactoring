package neto.com.mx.conteocedisciclicos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neto.com.mx.conteocedisciclicos.mvp.entityes.DispositivoID;
import neto.com.mx.conteocedisciclicos.mvp.gateways.Gateway;
import neto.com.mx.conteocedisciclicos.mvp.Presenters.Presentador;
import neto.com.mx.conteocedisciclicos.mvp.vista.Vista;
import neto.com.mx.conteocedisciclicos.mvp.gateways.GatewayActualizaciones;
import neto.com.mx.conteocedisciclicos.mvp.Presenters.PantallaInicialPresenter;

public class SplashScreenActivity extends AppCompatActivity implements Vista {
    private static final int UPDATEINSTALL_CODE = 0;

    @BindView(R.id.ambiente)
    TextView txtAmbiente;

    @BindView(R.id.iniciar)
    Button btnIniciar;

    @BindView(R.id.versionAppText)
    TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_000_splash_screen);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();

        ButterKnife.bind(this);

        btnIniciar.setVisibility(View.INVISIBLE);

        Gateway modelo = null;
        try {
            modelo = GatewayActualizaciones.getInstancia()
                    .agregarContexto(getApplicationContext())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Presentador presentador = new PantallaInicialPresenter(this, modelo);

        presentador.leerDatosDispositivo(getString(R.string.EndpointRest));
        presentador.validarUsoAplicacion();
    }



    @OnClick(R.id.iniciar)
    public void iniciaApp(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        Intent mainIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        SplashScreenActivity.this.startActivity(mainIntent);
        SplashScreenActivity.this.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == UPDATEINSTALL_CODE){
            if (resultCode == DescargaUltimaVersionDialog.RESULT_ERROR) {//Descarga con error
                btnIniciar.setVisibility(View.VISIBLE);
            } else if (resultCode == DescargaUltimaVersionDialog.RESULT_ACCESO_DENEGADO) {//Acceso denegado
            } else if (resultCode == DescargaUltimaVersionDialog.RESULT_OK) {//OK
                btnIniciar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void mostrarVersion() {
        String version= "0.0.0";
        try {
            version=getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionText.setText("© Todos los derechos reservados      v".concat(version));
    }

    @Override
    public void mostrarAmbiente(String ambiente) {
        txtAmbiente.setText(ambiente);
    }

    public void iniciaDescargaVersion(){
        Intent intentVersion = new Intent(this,
                DescargaUltimaVersionDialog.class);
        startActivityForResult(intentVersion, UPDATEINSTALL_CODE);
    }
}
