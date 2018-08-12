package neto.com.mx.conteocedisciclicos.mensajes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by dramirezr on 09/01/2018.
 */


@JsonPropertyOrder({"datosSalida","cursoresSalida", "stackTrace"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RespuestaDinamica {
    @JsonProperty("datosSalida")
    Map<Integer, String> datosSalida;
    @JsonProperty("cursoresSalida")
    Map<Integer, Cursor> cursoresSalida;
    @JsonProperty("stackTrace")
    String stackTrace;

    public RespuestaDinamica(Map<Integer, String> datosSalida, Map<Integer, Cursor> cursoresSalida, String stackTrace) {
        this.datosSalida = datosSalida;
        this.cursoresSalida = cursoresSalida;
        this.stackTrace = stackTrace;
    }

    public RespuestaDinamica(){
        datosSalida = new HashMap<Integer, String>();
        cursoresSalida = new  HashMap<Integer, Cursor>();
        stackTrace = new String();
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String StackTrace) {
        this.stackTrace = StackTrace;
    }

    public Map<Integer, Cursor> getCursoresSalida() {
        return cursoresSalida;
    }

    public void setCursoresSalida(Map<Integer, Cursor> cursoresSalida) {
        this.cursoresSalida = cursoresSalida;
    }

    public Map<Integer, String> getDatosSalida() {
        return datosSalida;
    }

    public void setDatosSalida(Map<Integer, String> datosSalida) {
        this.datosSalida = datosSalida;
    }

    public String toJSON() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "RespuestaDinamica{" + "datosSalida=" + datosSalida + ", cursoresSalida=" + cursoresSalida + ", stackTrace=" + stackTrace + '}';
    }
}
