package neto.com.mx.conteocedisciclicos.mensajes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * 
 * @author dramirezr
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"indice","tipoDato","valor"})
public class ParametroCuerpo implements Serializable {
    @JsonProperty("indice")
    private int indice;
    @JsonProperty("tipoDato")
    private String tipoDato;
    @JsonProperty("valor")
    private String valor;
    private final static long serialVersionUID = -8070836895723993550L;
    
    public ParametroCuerpo() {}
    public ParametroCuerpo(int indice, String tipoDato, String valor) {
        super();
        this.indice = indice;
        this.tipoDato = tipoDato;
        this.valor = valor;
    }
    @JsonProperty("indice")
    public long getIndice() {
        return indice;
    }

    @JsonProperty("indice")
    public void setIndice(int indice) {
        this.indice = indice;
    }

    @JsonProperty("tipoDato")
    public String getTipoDato() {
        return tipoDato;
    }

    @JsonProperty("tipoDato")
    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    @JsonProperty("valor")
    public String getValor() {
        return valor;
    }

    @JsonProperty("valor")
    public void setValor(String valor) {
        this.valor = valor;
    }
}
