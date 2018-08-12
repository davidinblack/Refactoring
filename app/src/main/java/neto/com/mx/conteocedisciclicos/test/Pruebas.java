package neto.com.mx.conteocedisciclicos.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import neto.com.mx.conteocedisciclicos.mvp.gateways.Gateway;
import neto.com.mx.conteocedisciclicos.mvp.Presenters.Presentador;
import neto.com.mx.conteocedisciclicos.mvp.vista.Vista;
import neto.com.mx.conteocedisciclicos.mvp.Presenters.PantallaInicialPresenter;


public class Pruebas {

    @Mock
    Vista vista;

    @Mock
    Gateway modelo;

    Presentador presentador;

    @Before
    public void setUp(){
        presentador = new PantallaInicialPresenter(vista, modelo);
    }

    @Test
    public void prueba(){
        //Given

        //
        //presentador.leerDatosDispositivo();

        //then
        Assert.assertEquals(1,1);
    }
}
