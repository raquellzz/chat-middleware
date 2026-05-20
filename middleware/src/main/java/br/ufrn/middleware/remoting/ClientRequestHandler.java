package br.ufrn.middleware.remoting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import br.ufrn.middleware.identification.AOR;

public class ClientRequestHandler {

    /**
     * Envia uma requisição pela rede para o nó especificado na AOR.
     */
    public String sendRequest(AOR aor, String httpMethod, String route, String jsonPayload) {
        String host = aor.getHost();
        int port = aor.getPort();
        
        // TODO: validação de aor.getProtocol() para lidar com udp e tcp
        // Por enquanto, forçamos TCP
        
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // 1. Monta a Requisição HTTP
            StringBuilder request = new StringBuilder();
            request.append(httpMethod).append(" ").append(route).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(host).append("\r\n");
            
            if (jsonPayload != null && !jsonPayload.isEmpty()) {
                request.append("Content-Length: ").append(jsonPayload.getBytes("UTF-8").length).append("\r\n");
                request.append("Content-Type: application/json\r\n");
                request.append("\r\n");
                request.append(jsonPayload);
            } else {
                request.append("\r\n");
            }
            
            out.print(request.toString());
            out.flush();
            
            String line;
            int contentLength = 0;
            
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }
            
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                return new String(body);
            }
            
            return "";
            
        } catch (Exception e) {
            throw new RuntimeException("Falha ao contactar o nó [" + aor.getObjectId().getId() + "] em " + host + ":" + port + " -> " + e.getMessage());
        }
    }
}