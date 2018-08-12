package neto.com.mx.conteocedisciclicos.utiles;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import neto.com.mx.conteocedisciclicos.beans.RespuestaHTTPEntity;

/**
 * Created by yruizm on 21/10/16.
 */

public class Util {
    public static List<RespuestaHTTPEntity> codigosHTTP = new ArrayList<>();
    static{
        codigosHTTP.add( new RespuestaHTTPEntity(300, "Multiple Choices",
                "Indica opciones múltiples para el URI que el cliente podría seguir. Esto podría ser utilizado, " +
                        "por ejemplo, para presentar distintas opciones de formato para video, " +
                        "listar archivos con distintas extensiones o word sense disambiguation."));

        codigosHTTP.add( new RespuestaHTTPEntity(301, "Moved Permanently",
                "Esta y todas las peticiones futuras deberían ser dirigidas a la URI dada."));

        codigosHTTP.add( new RespuestaHTTPEntity(302, "Found",
                "Este es el código de redirección más popular, " +
                        "pero también un ejemplo de las prácticas de la industria contradiciendo el estándar. " +
                        "La especificación HTTP/1.0 (RFC 1945) requería que el cliente realizara una redirección temporal " +
                        "(la frase descriptiva original fue 'Moved Temporarily'), " +
                        "pero los navegadores populares lo implementaron como 303 See Other. Por tanto," +
                        " HTTP/1.1 añadió códigos de estado 303 y 307 para eliminar la ambigüedad entre ambos comportamientos. " +
                        "Sin embargo, la mayoría de aplicaciones web y bibliotecas de desarrollo aún utilizan el código de respuesta " +
                        "302 como si fuera el 303."));

        codigosHTTP.add( new RespuestaHTTPEntity(303, "See Other (desde HTTP/1.1)",
                "La respuesta a la petición puede ser encontrada bajo otra URI utilizando el método GET."));

        codigosHTTP.add( new RespuestaHTTPEntity(304, "Not Modified",
                "Indica que la petición a la URL no ha sido modificada desde que fue requerida por última vez. " +
                        "Típicamente, el cliente HTTP provee un encabezado como If-Modified-Since para indicar una fecha y hora " +
                        "contra la cual el servidor pueda comparar. El uso de este encabezado ahorra ancho de banda y reprocesamiento " +
                        "tanto del servidor como del cliente."));

        codigosHTTP.add( new RespuestaHTTPEntity(305, "Use Proxy (desde HTTP/1.1)",
                "Muchos clientes HTTP (como Mozilla3​ e Internet Explorer) no se apegan al estándar al procesar respuestas " +
                        "con este código, principalmente por motivos de seguridad."));

        codigosHTTP.add( new RespuestaHTTPEntity(306, "Switch Proxy",
                "Este código se utilizaba en las versiones antiguas de HTTP pero ya no se usa (aunque está reservado para usos futuros).​"));

        codigosHTTP.add( new RespuestaHTTPEntity(307, "Temporary Redirect (desde HTTP/1.1)",
                "Se trata de una redirección que debería haber sido hecha con otra URI, sin embargo aún puede ser procesada " +
                        "con la URI proporcionada. En contraste con el código 303, el método de la petición no debería ser cambiado cuando el " +
                        "cliente repita la solicitud. Por ejemplo, una solicitud POST tiene que ser repetida utilizando otra petición POST."));

        codigosHTTP.add( new RespuestaHTTPEntity(308, "Permanent Redirect",
                "El recurso solicitado por el navegador se encuentra en otro lugar y este cambio es permanente. A diferencia del código 301," +
                        " no se permite cambiar el método HTTP para la nueva petición (así por ejemplo, " +
                        "si envías un formulario a un recurso que ha cambiado de lugar, todo seguirá funcionando bien).​ "));





        codigosHTTP.add( new RespuestaHTTPEntity(400, "Bad Request",
                "La solicitud contiene sintaxis errónea y no debería repetirse."));
        codigosHTTP.add( new RespuestaHTTPEntity(401, "Unauthorized",
                "Similar al 403 Forbidden, pero específicamente para su uso cuando la autentificación es posible pero ha fallado o aún " +
                        "no ha sido provista. Vea autenticación HTTP básica y Digest access authentication."));
        codigosHTTP.add( new RespuestaHTTPEntity(402, "Payment Required",
                "La intención original era que este código pudiese ser usado como parte de alguna forma o esquema de Dinero electrónico o " +
                        "micropagos, pero eso no sucedió, y este código nunca se utilizó."));
        codigosHTTP.add( new RespuestaHTTPEntity(403, "Forbidden",
                "La solicitud fue legal, pero el servidor rehúsa responderla dado que el cliente no tiene los privilegios para hacerla. " +
                        "En contraste a una respuesta 401 No autorizado, la autenticación no haría la diferencia."));
        codigosHTTP.add( new RespuestaHTTPEntity(404, "Not Found",
                "Recurso no encontrado. Se utiliza cuando el servidor web no encuentra la página o recurso solicitado."));
        codigosHTTP.add( new RespuestaHTTPEntity(405, "Method Not Allowed",
                "Una petición fue hecha a una URI utilizando un método de solicitud no soportado por dicha URI; por ejemplo, " +
                        "cuando se utiliza GET en un formulario que requiere que los datos sean presentados vía POST, " +
                        "o utilizando PUT en un recurso de solo lectura."));
        codigosHTTP.add( new RespuestaHTTPEntity(406, "Not Acceptable",
                "El servidor no es capaz de devolver los datos en ninguno de los formatos aceptados por el cliente, " +
                        "indicados por éste en la cabecera 'Accept' de la petición."));
        codigosHTTP.add( new RespuestaHTTPEntity(407, "Proxy Authentication Required",
                ""));
        codigosHTTP.add( new RespuestaHTTPEntity(408, "Request Timeout",
                "El cliente falló al continuar la petición - excepto durante la ejecución de videos Adobe Flash cuando solo significa que" +
                        " el usuario cerró la ventana de video o se movió a otro."));
        codigosHTTP.add( new RespuestaHTTPEntity(409 , "Conflict",
                "Indica que la solicitud no pudo ser procesada debido a un conflicto con el estado actual del recurso que esta identifica."));
        codigosHTTP.add( new RespuestaHTTPEntity(410, "Gone",
                "Indica que el recurso solicitado ya no está disponible y no lo estará de nuevo. Debería ser utilizado cuando un " +
                        "recurso ha sido quitado de forma permanente.\n" +
                        "   Si un cliente recibe este código no debería volver a solicitar el recurso en el futuro. Por ejemplo un buscador lo eliminará " +
                        "de sus índices y lo hará más rápidamente que utilizando un código 404."));
        codigosHTTP.add( new RespuestaHTTPEntity(411, "Length Required",
                "El servidor rechaza la petición del navegador porque no incluye la cabecera Content-Length adecuada.​"));
        codigosHTTP.add( new RespuestaHTTPEntity(412, "Precondition Failed",
                "El servidor no es capaz de cumplir con algunas de las condiciones impuestas por el navegador en su petición.​"));
        codigosHTTP.add( new RespuestaHTTPEntity(413, "Request Entity Too Large",
                "La petición del navegador es demasiado grande y por ese motivo el servidor no la procesa2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(414, "Request-URI Too Long",
                "La URI de la petición del navegador es demasiado grande y por ese motivo el servidor no la procesa (esta condición se" +
                        " produce en muy raras ocasiones y casi siempre porque el navegador envía como GET una petición que debería ser POST).​"));
        codigosHTTP.add( new RespuestaHTTPEntity(415, "Unsupported Media Type",
                "La petición del navegador tiene un formato que no entiende el servidor y por eso no se procesa.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(416, "Requested Range Not Satisfiable",
                "El cliente ha preguntado por una parte de un archivo, pero el servidor no puede proporcionar esa parte, por ejemplo," +
                        " si el cliente preguntó por una parte de un archivo que está más allá de los límites del fin del archivo."));
        codigosHTTP.add( new RespuestaHTTPEntity(417, "Expectation Failed",
                "La petición del navegador no se procesa porque el servidor no es capaz de cumplir con los requerimientos de la cabecera " +
                        "Expect de la petición.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(418, "I'm a teapot",
                "Soy una tetera. Este código fue definido en 1998 como una inocentada, en el Protocolo de Transmisión de Hipertexto de " +
                        "Cafeteras (RFC-2324). No se espera que los servidores web implementen realmente este código de error, " +
                        "pero es posible encontrar sitios que devuelvan este código HTTP."));
        codigosHTTP.add( new RespuestaHTTPEntity(422, "Unprocessable Entity (WebDAV - RFC 4918)",
                "La solicitud está bien formada pero fue imposible seguirla debido a errores semánticos."));
        codigosHTTP.add( new RespuestaHTTPEntity(423, "Locked (WebDAV - RFC 4918)",
                "El recurso al que se está teniendo acceso está bloqueado."));
        codigosHTTP.add( new RespuestaHTTPEntity(424, "Failed Dependency (WebDAV) (RFC 4918)",
                "La solicitud falló debido a una falla en la solicitud previa."));
        codigosHTTP.add( new RespuestaHTTPEntity(425, "Unassigned",
                "Definido en los drafts de WebDav Advanced Collections, pero no está presente en 'Web Distributed Authoring and Versioning " +
                        "(WebDAV) Ordered Collections Protocol' (RFC 3648)."));
        codigosHTTP.add( new RespuestaHTTPEntity(426, "Upgrade Required (RFC 7231)",
                "El cliente debería cambiarse a TLS/1.0."));
        codigosHTTP.add( new RespuestaHTTPEntity(428, "Precondition Required",
                "El servidor requiere que la petición del navegador sea condicional (este tipo de peticiones evitan los problemas producidos " +
                        "al modificar con PUT un recurso que ha sido modificado por otra parte).2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(429, "Too Many Requests",
                "Hay muchas conexiones desde esta dirección de internet."));
        codigosHTTP.add( new RespuestaHTTPEntity(431, "Request Header Fields Too Large)",
                "El servidor no puede procesar la petición porque una de las cabeceras de la petición es demasiado grande. " +
                        "Este error también se produce cuando la suma del tamaño de todas las peticiones es demasiado grande.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(449, "",
                "Una extensión de Microsoft: La petición debería ser reintentada después de hacer la acción apropiada."));
        codigosHTTP.add( new RespuestaHTTPEntity(451, "Unavailable for Legal Reasons",
                "El contenido ha sido eliminado como consecuencia de una orden judicial o sentencia emitida por un tribunal. "));




        codigosHTTP.add( new RespuestaHTTPEntity(500, "Internal Server Error",
                "Es un código comúnmente emitido por aplicaciones empotradas en servidores web, mismas que generan contenido dinámicamente, " +
                        "por ejemplo aplicaciones montadas en IIS o Tomcat, cuando se encuentran con situaciones de error ajenas a la naturaleza del servidor web."));
        codigosHTTP.add( new RespuestaHTTPEntity(501, "Not Implemented",
                "El servidor no soporta alguna funcionalidad necesaria para responder a la solicitud del navegador " +
                        "(como por ejemplo el método utilizado para la petición).2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(502, "Bad Gateway",
                "El servidor está actuando de proxy o gateway y ha recibido una respuesta inválida del otro servidor," +
                        " por lo que no puede responder adecuadamente a la petición del navegador.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(503, "Service Unavailable",
                "El servidor no puede responder a la petición del navegador porque está congestionado o está realizando tareas de mantenimiento.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(504, "Gateway Timeout",
                "El servidor está actuando de proxy o gateway y no ha recibido a tiempo una respuesta del otro servidor, " +
                        "por lo que no puede responder adecuadamente a la petición del navegador.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(505, "HTTP Version Not Supported",
                "El servidor no soporta o no quiere soportar la versión del protocolo HTTP utilizada en la petición del navegador.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(506, "Variant Also Negotiates (RFC 2295)",
                "El servidor ha detectado una referencia circular al procesar la parte de la negociación del contenido de la petición.​"));
        codigosHTTP.add( new RespuestaHTTPEntity(507, "Insufficient Storage (WebDAV - RFC 4918)",
                "El servidor no puede crear o modificar el recurso solicitado porque no hay suficiente espacio de almacenamiento libre.​"));
        codigosHTTP.add( new RespuestaHTTPEntity(508, "Loop Detected (WebDAV)",
                "La petición no se puede procesar porque el servidor ha encontrado un bucle infinito al intentar procesarla.​"));
        codigosHTTP.add( new RespuestaHTTPEntity(509, "Bandwidth Limit Exceeded",
                "Límite de ancho de banda excedido. Este código de estatus, a pesar de ser utilizado por muchos servidores, no es oficial."));
        codigosHTTP.add( new RespuestaHTTPEntity(510, "Not Extended (RFC 2774)",
                "La petición del navegador debe añadir más extensiones para que el servidor pueda procesarla.2​"));
        codigosHTTP.add( new RespuestaHTTPEntity(511, "Network Authentication Required",
                "El navegador debe autenticarse para poder realizar peticiones (se utiliza por ejemplo con los portales cautivos que te " +
                        "obligan a autenticarte antes de empezar a navegar).​"));
        codigosHTTP.add( new RespuestaHTTPEntity(512, "Not updated",
                "Este error prácticamente es inexistente en la red, pero indica que el servidor está en una operación de actualizado y " +
                        "no puede tener conexión. "));

    }

    public RespuestaHTTPEntity getHTTPRespuesta(int codigo){
        for (RespuestaHTTPEntity resp: codigosHTTP ){
            if( resp.getCodigo() == codigo ){
                return resp;
            }
        }
        return null;
    }

    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }


}
