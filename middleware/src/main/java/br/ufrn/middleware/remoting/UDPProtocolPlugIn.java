package br.ufrn.middleware.remoting;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPProtocolPlugIn implements ProtocolPlugIn {
    private final ExecutorService virtualPool = Executors.newVirtualThreadPerTaskExecutor();
    @Override
    public void startListen(int port, MessageProcessor processor) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            
            System.out.println("[UDP PLUGIN] Servidor escutando na porta " + port);

            // Buffer grande o suficiente para as mensagens (64KB)
            while (true) {
                byte[] buffer = new byte[65535]; 
                
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                // new Thread(() -> handlePacket(socket, requestPacket, processor)).start();
                virtualPool.submit(() -> handlePacket(socket, requestPacket, processor));
            }
        } catch (Exception e) {
            System.err.println("[UDP PLUGIN] Erro fatal no servidor: " + e.getMessage());
        }
    }

    private void handlePacket(DatagramSocket socket, DatagramPacket requestPacket, MessageProcessor processor) {
        try {
            String rawRequest = new String(requestPacket.getData(), 0, requestPacket.getLength(), "UTF-8");

            String response = processor.processMessage(rawRequest);

            byte[] responseBytes = response.getBytes("UTF-8");
            DatagramPacket responsePacket = new DatagramPacket(
                    responseBytes,
                    responseBytes.length,
                    requestPacket.getAddress(),
                    requestPacket.getPort()
            );

            socket.send(responsePacket);

        } catch (Exception e) {
            System.err.println("[UDP PLUGIN] Erro ao processar pacote: " + e.getMessage());
        }
    }
}