package neto.com.mx.conteocedisciclicos.mensajes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by dramirezr on 13/01/2018.
 */

@JsonPropertyOrder({"error","vacia","resultado"})
public class RespuestaServicio {
    @JsonProperty("error")
    private boolean error;

    @JsonProperty("vacia")
    private boolean vacia;

    @JsonProperty("resultado")
    private String resultado;

    private final static long serialVersionUID = 9021738568503059758L;

    public RespuestaServicio() {}

    public RespuestaServicio(boolean error, boolean vacia, String resultado) {
        super();
        this.error = error;
        this.vacia = vacia;
        this.resultado = resultado;
    }

    @JsonProperty("error")
    public boolean isError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(boolean error) {
        this.error = error;
    }

    @JsonProperty("vacia")
    public boolean isVacia() {
        return vacia;
    }

    @JsonProperty("vacia")
    public void setVacia(boolean vacia) {
        this.vacia = vacia;
    }

    @JsonProperty("resultado")
    public String getResultado() {
        return resultado;
    }

    @JsonProperty("resultado")
    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String toJSON(){
        ObjectMapper mapper = new ObjectMapper();
        String res = "";
        try {
            res = mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
