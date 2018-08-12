package neto.com.mx.conteocedisciclicos.beans;

import java.io.Serializable;

/**
 * Created by dramirezr on 18/01/2018.
 */

public class CredencialBean implements Serializable {
    String idUsuario;
    String nombreUsuario;

    public CredencialBean(){}
    public CredencialBean(String idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
