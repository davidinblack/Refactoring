package neto.com.mx.conteocedisciclicos.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dramirezr on 15/01/2018.
 */

public class UbicacionBean {
    int id;
    String nombre;
    String color;

    List<ZonaBean> zonas;

    public UbicacionBean() {}

    public UbicacionBean(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        zonas = new ArrayList<ZonaBean>();
    }

    public List<ZonaBean> getZonas() {
        return zonas;
    }

    public void setZonas(List<ZonaBean> zonas) {
        this.zonas = zonas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
