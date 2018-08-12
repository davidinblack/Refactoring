package neto.com.mx.conteocedisciclicos.mensajes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * Created by dramirezr on 23/01/2018.
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"valor"})
public class ParametroSimple implements Serializable {
    private final static long serialVersionUID = -8070836895723993551L;

    @JsonProperty("valor")
    private String valor;

    public ParametroSimple() {}
    public ParametroSimple(String valor) {
        super();
        this.valor = valor;
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
