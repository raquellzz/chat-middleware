package br.ufrn.middleware.utils;

public class HttpHelper {
    
    public static class HttpRequestData {
        public String method;
        public String route;
        public String body;
        public boolean isValid;
    }

    public static HttpRequestData parse(String rawRequest) {
        HttpRequestData data = new HttpRequestData();
        data.isValid = false;

        try {
            String requestClean = rawRequest.replace("\r", "").replace("\0", "");
            if (requestClean.trim().isEmpty() || requestClean.contains("/favicon")) return data;

            String[] headerEBody = requestClean.split("\n\n", 2);
            String cabecalho = headerEBody[0];
            data.body = headerEBody.length > 1 ? headerEBody[1].trim() : "";

            String primeiraLinha = cabecalho.split("\n")[0];
            String[] partes = primeiraLinha.split(" ");
            
            if (partes.length >= 2) {
                data.method = partes[0];
                data.route = partes[1];
                data.isValid = true;
            }
        } catch (Exception ignored) {}

        return data;
    }

    public static String buildRequest(String method, String host, String route, String body) {
        StringBuilder req = new StringBuilder();
        req.append(method).append(" ").append(route).append(" HTTP/1.1\r\n");
        req.append("Host: ").append(host).append("\r\n");
        
        if (body != null && !body.isEmpty()) {
            req.append("Content-Length: ").append(body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).append("\r\n");
            req.append("Content-Type: application/json; charset=UTF-8\r\n\r\n");
            req.append(body);
        } else {
            req.append("\r\n");
        }
        return req.toString();
    }

    public static String buildResponse(int statusCode, String body) {
        String statusText = statusCode == 200 ? "OK" : (statusCode == 400 ? "Bad Request" : (statusCode == 404 ? "Not Found" : "Error"));
        return "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
               "Content-Type: application/json; charset=UTF-8\r\n" +
               "Connection: close\r\n" +
               "Content-Length: " + body.getBytes().length + "\r\n" +
               "\r\n" + body;
    }

    public static String extractBody(String rawMessage) {
        String clean = rawMessage.replace("\r", "").replace("\0", "");
        String[] parts = clean.split("\n\n", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }
}