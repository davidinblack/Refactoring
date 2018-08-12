package neto.com.mx.conteocedisciclicos.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by dramirezr on 19/01/2018.
 */

public class ArticuloBean implements Serializable {
    long idArticulo;
    String nombreArticulo;
    private ArrayList<String> codigos;

    public ArticuloBean() {
        codigos = new ArrayList<>();
    }

    public ArticuloBean(long idArticulo, String nombreArticulo) {
        this();
        this.idArticulo = idArticulo;
        this.nombreArticulo = nombreArticulo;
    }
    public ArticuloBean(long idArticulo, String nombreArticulo, ArrayList<String> cods) {
        this(idArticulo, nombreArticulo);
        this.codigos = cods;
    }

    @Override
    public int hashCode() {
        return (idArticulo+"").hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if( obj == null )
            return false;
        if( !(obj instanceof ArticuloBean) )
            return  false;
        ArticuloBean art = (ArticuloBean)obj;
        return art.idArticulo == this.idArticulo;
    }

    public ArrayList<String> getCodigos() {
        return codigos;
    }

    public void setCodigos(ArrayList<String> codigos) {
        this.codigos = codigos;
    }

    public long getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(long idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    @Override
    public String toString() {
        return "ArticuloBean{" +
                "idArticulo=" + idArticulo +
                ", nombreArticulo='" + nombreArticulo + '\'' +
                ", codigos=" + Arrays.toString(codigos.toArray()) +
                '}';
    }
}
