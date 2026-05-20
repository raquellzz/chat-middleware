package br.ufrn.middleware.invocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.ufrn.middleware.exceptions.RemotingException;

public class Marshaller {

    private final Invoker invoker;
    private final Gson gson;

    public Marshaller(Invoker invoker) {
        this.invoker = invoker;
        this.gson = new Gson();
    }

    /**
     * Pega na requisição HTTP bruta da rede e traduz para o Invoker
     */
    public String unmarshalAndInvoke(String rawHttpRequest) {
        try {
            String[] parts = rawHttpRequest.split("\r\n\r\n", 2);
            String headerPart = parts[0];
            String bodyPart = parts.length > 1 ? parts[1] : "";

            String[] headerLines = headerPart.split("\r\n");
            String[] requestLine = headerLines[0].split(" ");
            
            if (requestLine.length < 2) {
                throw new RemotingException(400, "Requisicao HTTP malformada");
            }

            String httpMethod = requestLine[0];
            String route = requestLine[1];

            Map<String, Object> params = new HashMap<>();
            
            if ("POST".equalsIgnoreCase(httpMethod) && !bodyPart.isEmpty()) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                params = gson.fromJson(bodyPart.replace("\0", "").trim(), type);
                
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof Double aDouble) {
                        entry.setValue(aDouble.intValue());
                    }
                }
            } else if ("GET".equalsIgnoreCase(httpMethod) && route.contains("?")) {
                String[] routeParts = route.split("\\?");
                route = routeParts[0];
                String[] queryParams = routeParts[1].split("&");
                for (String param : queryParams) {
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        params.put(kv[0], kv[1]);
                    }
                }
            }

            Object result = invoker.invoke(httpMethod, route, params);

            return marshalResult(result);

        } catch (RemotingException e) {
            // Pega o código (404, 400, 500) e manda a resposta amigável formatada!
            System.err.println("[REMOTING ERROR] " + e.getHttpStatusCode() + " - " + e.getMessage());
            return buildHttpResponse(e.getHttpStatusCode(), "{\"erro\": \"" + e.getMessage() + "\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            return buildHttpResponse(500, "{\"erro\": \"Internal Server Error\"}");
        }
    }

    /**
     * Formata qualquer objeto Java de volta para uma string HTTP válida
     */
    private String marshalResult(Object result) {
        String jsonResponse = gson.toJson(result);
        return buildHttpResponse(200, jsonResponse);
    }

    private String buildHttpResponse(int statusCode, String body) {
        String statusText = statusCode == 200 ? "OK" : (statusCode == 404 ? "Not Found" : "Error");
        return "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
               "Content-Type: application/json\r\n" +
               "Content-Length: " + body.getBytes().length + "\r\n" +
               "\r\n" + body;
    }
}