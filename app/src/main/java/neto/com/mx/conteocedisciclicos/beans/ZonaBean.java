package neto.com.mx.conteocedisciclicos.beans;

/**
 * Created by dramirezr on 16/01/2018.
 */

public class ZonaBean {
    String id;
    String nombre;
    String color;

    public ZonaBean() {}
    public ZonaBean(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    public ZonaBean(String id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj == null) return false;
        if(!(obj instanceof ZonaBean)) return false;

        return getId().equals(((ZonaBean) obj).getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
