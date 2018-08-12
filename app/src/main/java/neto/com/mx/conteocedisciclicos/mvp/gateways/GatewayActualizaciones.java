package neto.com.mx.conteocedisciclicos.mvp.gateways;

import android.content.Context;
import android.content.ContextWrapper;

import neto.com.mx.conteocedisciclicos.R;
import neto.com.mx.conteocedisciclicos.mvp.entityes.DispositivoID;

public class GatewayActualizaciones implements Gateway {
    private Context contexto;
    private String endPoint;
    private DispositivoID dispositivoID;

    private static GatewayActualizaciones instancia;
    private GatewayActualizaciones(){}

    public static GatewayActualizaciones getInstancia(){
        if( instancia == null ){
            instancia = new GatewayActualizaciones();
        }
        return instancia;
    }

    public GatewayActualizaciones agregarContexto(Context contexto){
        instancia.endPoint = contexto.getString(R.string.EndpointRest);
        return instancia;
    }

    public GatewayActualizaciones build() throws Exception {
        if( dispositivoID == null ){
            throw new Exception("No se pudieron leer los datos del dispositivo.");
        }else if( dispositivoID.getAplicacionId() == null  || "".equals(dispositivoID.getAplicacionId()) ){
            throw new Exception("No se pudo leer el id de aplicación.");
        }else if( dispositivoID.getImeii() == null || "".equals(dispositivoID.getImeii()) ){
            throw new Exception("No se pudo leer el iimei de dispositivo.");
        }else if( dispositivoID.getVersionActual() == null || "".equals(dispositivoID.getVersionActual()) ){
            throw new Exception("No se pudo leer la versión de la aplicación.");
        }
        return instancia;
    }

    @Override
    public void validarConexion() {

    }

    @Override
    public void buscarActualización() {

    }

    @Override
    public void validarUsoAplicacion() {

    }


}
