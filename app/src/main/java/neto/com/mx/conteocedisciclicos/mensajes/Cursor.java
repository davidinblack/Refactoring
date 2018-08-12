package neto.com.mx.conteocedisciclicos.mensajes;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * Created by dramirezr on 09/01/2018.
 */

public class Cursor {
    @JsonProperty("encabezados")
    List<String> encabezados;
    @JsonProperty("registros")
    List<ArrayList<String>> registros;
    @JsonProperty("cantRegistros")
    int cantRegistros;

    public Cursor(@JsonProperty("encabezados") List<String> encabezados,
                  @JsonProperty("registros") List<ArrayList<String>> registros,
                  @JsonProperty("cantRegistros") int cantRegistros) {
        this.encabezados = encabezados;
        this.registros = registros;
        this.cantRegistros = cantRegistros;
    }

    public Cursor()
    {
        this.encabezados = new ArrayList();
        this.registros = new ArrayList();
    }

    public List<String> getEncabezados() {
        return this.encabezados;
    }

    public void setEncabezados(List<String> encabezados) { this.encabezados = encabezados; }

    public List<ArrayList<String>> getRegistros() {
        return this.registros;
    }

    public void setRegistros(List<ArrayList<String>> registros) { this.registros = registros; }

    public int getCantRegistros() {
        return this.cantRegistros;
    }

    public void setCantRegistros(int cantRegistros) {
        this.cantRegistros = cantRegistros;
    }

    @Override
    public String toString() {
        return "Cursor{" + "encabezados=" + encabezados + ", registros=" + registros + ", cantRegistros=" + cantRegistros + '}';
    }
}
