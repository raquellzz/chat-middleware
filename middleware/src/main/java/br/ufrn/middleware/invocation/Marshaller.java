package br.ufrn.middleware.invocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.ufrn.middleware.exceptions.RemotingException;
import br.ufrn.middleware.remoting.MessageProcessor;
import br.ufrn.middleware.utils.HttpHelper;
import br.ufrn.middleware.utils.HttpHelper.HttpRequestData;

public class Marshaller implements MessageProcessor {

    private final Invoker invoker;
    private final Gson gson;

    public Marshaller(Invoker invoker) {
        this.invoker = invoker;
        this.gson = new Gson();
    }

    /**
     * Pega a requisição bruta, delega o parse HTTP para o Helper,
     * converte o payload JSON (Unmarshal) e traduz para o Invoker
     */
    @Override
    public String processMessage(String rawHttpRequest) {
        try {
            if (rawHttpRequest == null || rawHttpRequest.trim().isEmpty()) return "";

            HttpRequestData req = HttpHelper.parse(rawHttpRequest);

            if (!req.isValid) {
                throw new RemotingException(400, "Requisicao malformada ou nao suportada");
            }

            Map<String, Object> params = new HashMap<>();
            String routeToInvoke = req.route;
            
            // Deserialização (Unmarshal): Transforma o JSON ou Query Params em Objetos Java
            if ("POST".equalsIgnoreCase(req.method) && !req.body.isEmpty()) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                params = gson.fromJson(req.body, type);
                
                if (params != null) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getValue() instanceof Double aDouble) {
                            entry.setValue(aDouble.intValue());
                        }
                    }
                }
            } else if ("GET".equalsIgnoreCase(req.method) && routeToInvoke.contains("?")) {
                String[] routeParts = routeToInvoke.split("\\?");
                routeToInvoke = routeParts[0];
                String[] queryParams = routeParts[1].split("&");
                for (String param : queryParams) {
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        params.put(kv[0], kv[1]);
                    }
                }
            }

            // Invoca a Regra de Negócio (Storage ou Validator)
            Object result = invoker.invoke(req.method, routeToInvoke, params);

            // Formata a resposta (Marshal)
            return marshalResult(result);

        } catch (RemotingException e) {
            System.err.println("[REMOTING ERROR] " + e.getHttpStatusCode() + " - " + e.getMessage());
            return HttpHelper.buildResponse(e.getHttpStatusCode(), "{\"erro\": \"" + e.getMessage() + "\"}");
            
        } catch (Exception e) {
            System.err.println("[MARSHALLER FATAL ERROR] " + e.getMessage());
            e.printStackTrace();
            return HttpHelper.buildResponse(500, "{\"erro\": \"Internal Server Error\"}");
        }
    }

    /**
     * Formata o objeto retornado pelo Invoker para JSON e delega o empacotamento HTTP
     */
    private String marshalResult(Object result) {
        String jsonResponse;
        
        if (result instanceof String && (((String) result).startsWith("{") || ((String) result).startsWith("["))) {
            jsonResponse = (String) result;
        } else {
            jsonResponse = gson.toJson(result);
        }
        
        return HttpHelper.buildResponse(200, jsonResponse);
    }
}