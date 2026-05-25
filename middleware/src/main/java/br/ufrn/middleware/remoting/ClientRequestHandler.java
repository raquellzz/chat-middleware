package br.ufrn.middleware.remoting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import br.ufrn.middleware.identification.AOR;

public class ClientRequestHandler {

    public String sendRequest(AOR aor, String httpMethod, String route, String jsonPayload) {
        String host = aor.getHost();
        int port = aor.getPort();
        
        StringBuilder request = new StringBuilder();
        request.append(httpMethod).append(" ").append(route).append(" HTTP/1.1\r\n");
        request.append("Host: ").append(host).append("\r\n");
        
        try {
            if (jsonPayload != null && !jsonPayload.isEmpty()) {
                request.append("Content-Length: ").append(jsonPayload.getBytes("UTF-8").length).append("\r\n");
                request.append("Content-Type: application/json\r\n");
                request.append("\r\n"); 
                request.append(jsonPayload);
            } else {
                request.append("\r\n");
            }
            
            String rawRequestString = request.toString();

            if (aor.getProtocol().equalsIgnoreCase("UDP")) {
                return sendUdp(host, port, rawRequestString);
            } else {
                return sendTcp(host, port, rawRequestString);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Falha ao preparar requisição: " + e.getMessage());
        }
    }

    private String sendTcp(String host, int port, String rawRequestString) throws Exception {
        try (Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.print(rawRequestString);
            out.flush();
            
            String line;
            int contentLength = 0;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) break;
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
        }
    }

    private String sendUdp(String host, int port, String rawRequestString) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(500); 
            
            InetAddress address = InetAddress.getByName(host);
            byte[] sendData = rawRequestString.getBytes("UTF-8");
            
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);
            
            byte[] receiveData = new byte[65535];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            
            String rawResponse = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
            
            String[] parts = rawResponse.split("\r\n\r\n", 2);
            return parts.length > 1 ? parts[1] : parts[0];
        }
    }
}