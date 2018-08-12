package neto.com.mx.conteocedisciclicos.beans;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class RespuestaHTTPEntity {
    private int codigo;
    private String mensaje;
    private String descripcion;

    public RespuestaHTTPEntity(int codigo) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.descripcion = descripcion;
    }

    public RespuestaHTTPEntity(int codigo, String mensaje, String descripcion) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "" + codigo +
                ", " + mensaje +
                ", " + descripcion ;
    }
}
