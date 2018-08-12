package neto.com.mx.conteocedisciclicos.mensajes;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/** @author dramirezr */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"propiedadSQL","cuerpoPeticion"})
public class SolicitudServicio implements Serializable {
    
    @JsonProperty("propiedadSQL")
    private String propiedadSQL;
    
    @JsonProperty("cuerpoPeticion")
    private List<ParametroCuerpo> cuerpoPeticion = null;
    
    private final static long serialVersionUID = -3914124486943387687L;
    
    public SolicitudServicio() {}

    /**
    * @param cuerpoPeticion
    * @param propiedadSQL
    */
    public SolicitudServicio(String propiedadSQL, List<ParametroCuerpo> cuerpoPeticion) {
        super();
        this.propiedadSQL = propiedadSQL;
        this.cuerpoPeticion = cuerpoPeticion;
    }
    
    @JsonProperty("propiedadSQL")
    public String getPropiedadSQL() {
        return propiedadSQL;
    }

    @JsonProperty("propiedadSQL")
    public void setPropiedadSQL(String propiedadSQL) {
        this.propiedadSQL = propiedadSQL;
    }

    @JsonProperty("cuerpoPeticion")
    public List<ParametroCuerpo> getCuerpoPeticion() {
     return cuerpoPeticion;
    }

    @JsonProperty("cuerpoPeticion")
    public void setCuerpoPeticion(List<ParametroCuerpo> cuerpoPeticion) {
        this.cuerpoPeticion = cuerpoPeticion;
    }
}
