package neto.com.mx.conteocedisciclicos.beans;

/**
 * Created by dramirezr on 15/01/2018.
 */

public class SesionConteo {
    long idUsuario;
    String nombreUsuario;

    long idInventario;
    int idUbicación;
    String nombreUbicación;

    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public long getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(long idInventario) {
        this.idInventario = idInventario;
    }

    public int getIdUbicación() {
        return idUbicación;
    }

    public void setIdUbicación(int idUbicación) {
        this.idUbicación = idUbicación;
    }

    public String getNombreUbicación() {
        return nombreUbicación;
    }

    public void setNombreUbicación(String nombreUbicación) {
        this.nombreUbicación = nombreUbicación;
    }
}
