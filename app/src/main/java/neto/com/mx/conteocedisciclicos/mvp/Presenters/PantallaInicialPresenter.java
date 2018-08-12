package neto.com.mx.conteocedisciclicos.mvp.Presenters;

import neto.com.mx.conteocedisciclicos.mvp.gateways.Gateway;
import neto.com.mx.conteocedisciclicos.mvp.vista.Vista;

public class PantallaInicialPresenter implements Presentador {
    Vista vista;
    Gateway modelo;

    public PantallaInicialPresenter(Vista vista, Gateway modelo) {
        this.vista = vista;
        this.modelo = modelo;
    }

    @Override
    public void leerDatosDispositivo(String endPoint) {
        vista.mostrarVersion();
        String ambiente = calculaAmbiente(endPoint);
        vista.mostrarAmbiente(ambiente);
    }

    @Override
    public void validarUsoAplicacion() {
        modelo.validarUsoAplicacion();
    }

    @Override
    public void actualizarAplicacion() {

    }


    public String calculaAmbiente(String endPoint) {
        String res="";//PRODUCCION
        if( endPoint.contains("10.81.12.46") ){
            res = "QA";
        }else if( endPoint.contains("10.81.12.45") ||
                endPoint.contains("10.37.140.202")){
            res = "DESARROLLO";
        }
        return res;
    }


}
