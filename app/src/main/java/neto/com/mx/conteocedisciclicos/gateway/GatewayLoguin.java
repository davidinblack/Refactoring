package neto.com.mx.conteocedisciclicos.gateway;

import neto.com.mx.conteocedisciclicos.beans.CredencialBean;

public interface GatewayLoguin {
    CredencialBean autenticarUsuario(String usuario, String clave);
}
