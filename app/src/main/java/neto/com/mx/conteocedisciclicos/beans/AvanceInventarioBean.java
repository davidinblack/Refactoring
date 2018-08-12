package neto.com.mx.conteocedisciclicos.beans;

import java.io.Serializable;

/**
 * Created by dramirezr on 18/01/2018.
 */

public class AvanceInventarioBean implements Serializable {

    String reservaNombre;
    Integer reservaPorcentaje;
    Long   reservaCantArt;

    String activoNombre;
    Integer activoPorcentaje;
    Long txtActivoCantArt;

    Long totArticulos;
    Long totCajas;
    Integer porcentajeAvanceTotal;

    public AvanceInventarioBean() {}

    public AvanceInventarioBean(String reservaNombre, Integer reservaPorcentaje, Long reservaCantArt, String activoNombre, Integer activoPorcentaje, Long txtActivoCantArt, Long totArticulos, Long totCajas, Integer porcentajeAvanceTotal) {

        this.reservaNombre = reservaNombre;
        this.reservaPorcentaje = reservaPorcentaje;
        this.reservaCantArt = reservaCantArt;
        this.activoNombre = activoNombre;
        this.activoPorcentaje = activoPorcentaje;
        this.txtActivoCantArt = txtActivoCantArt;
        this.totArticulos = totArticulos;
        this.totCajas = totCajas;
        this.porcentajeAvanceTotal = porcentajeAvanceTotal;
    }

    public String getReservaNombre() {
        return reservaNombre;
    }

    public void setReservaNombre(String reservaNombre) {
        this.reservaNombre = reservaNombre;
    }

    public Integer getReservaPorcentaje() {
        return reservaPorcentaje;
    }

    public void setReservaPorcentaje(Integer reservaPorcentaje) {
        this.reservaPorcentaje = reservaPorcentaje;
    }

    public Long getReservaCantArt() {
        return reservaCantArt;
    }

    public void setReservaCantArt(Long reservaCantArt) {
        this.reservaCantArt = reservaCantArt;
    }

    public String getActivoNombre() {
        return activoNombre;
    }

    public void setActivoNombre(String activoNombre) {
        this.activoNombre = activoNombre;
    }

    public Integer getActivoPorcentaje() {
        return activoPorcentaje;
    }

    public void setActivoPorcentaje(Integer activoPorcentaje) {
        this.activoPorcentaje = activoPorcentaje;
    }

    public Long getTxtActivoCantArt() {
        return txtActivoCantArt;
    }

    public void setTxtActivoCantArt(Long txtActivoCantArt) {
        this.txtActivoCantArt = txtActivoCantArt;
    }

    public Long getTotArticulos() {
        return totArticulos;
    }

    public void setTotArticulos(Long totArticulos) {
        this.totArticulos = totArticulos;
    }

    public Long getTotCajas() {
        return totCajas;
    }

    public void setTotCajas(Long totCajas) {
        this.totCajas = totCajas;
    }

    public Integer getPorcentajeAvanceTotal() {
        return porcentajeAvanceTotal;
    }

    public void setPorcentajeAvanceTotal(Integer porcentajeAvanceTotal) {
        this.porcentajeAvanceTotal = porcentajeAvanceTotal;
    }

    @Override
    public String toString() {
        return "AvanceInventarioBean{" +
                "reservaNombre='" + reservaNombre + '\'' +
                ", reservaPorcentaje=" + reservaPorcentaje +
                ", reservaCantArt=" + reservaCantArt +
                ", activoNombre='" + activoNombre + '\'' +
                ", activoPorcentaje=" + activoPorcentaje +
                ", txtActivoCantArt=" + txtActivoCantArt +
                ", totArticulos=" + totArticulos +
                ", totCajas=" + totCajas +
                ", porcentajeAvanceTotal=" + porcentajeAvanceTotal +
                '}';
    }
}
