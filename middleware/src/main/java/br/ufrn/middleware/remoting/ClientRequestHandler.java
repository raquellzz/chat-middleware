package br.ufrn.middleware.remoting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.utils.HttpHelper;

public class ClientRequestHandler {

    public String sendRequest(AOR aor, String httpMethod, String route, String jsonPayload) {
        try {
            // Delega a formatação HTTP para o utilitário do Middleware
            String rawRequestString = HttpHelper.buildRequest(httpMethod, aor.getHost(), route, jsonPayload);

            // Roteia para o protocolo correto
            if (aor.getProtocol().equalsIgnoreCase("UDP")) {
                return sendUdp(aor.getHost(), aor.getPort(), rawRequestString);
            } else {
                return sendTcp(aor.getHost(), aor.getPort(), rawRequestString);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha na comunicação de rede: " + e.getMessage());
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
            
            return HttpHelper.extractBody(rawResponse);
        }
    }
}