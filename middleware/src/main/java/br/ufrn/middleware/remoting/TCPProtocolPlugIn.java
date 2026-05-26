package br.ufrn.middleware.remoting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPProtocolPlugIn implements ProtocolPlugIn {
    // private final ExecutorService virtualPool = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void startListen(int port, MessageProcessor processor) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[TCP PLUGIN] Servidor escutando na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                // Dispara uma Thread para cada requisição para não travar o servidor
                new Thread(() -> handleConnection(clientSocket, processor)).start();
                // virtualPool.submit(() -> handleConnection(clientSocket, processor));
            }
        } catch (IOException e) {
            System.err.println("[TCP PLUGIN] Erro fatal no servidor: " + e.getMessage());
        }
    }

    private void handleConnection(Socket socket, MessageProcessor processor) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            StringBuilder rawRequest = new StringBuilder();
            String line;
            int contentLength = 0;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                rawRequest.append(line).append("\r\n");
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }
            rawRequest.append("\r\n");

            if (contentLength > 0) {
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                rawRequest.append(body);
            }

            if (rawRequest.toString().trim().isEmpty()) return;

            // Manda para o Middleware (Marshaller -> Invoker) processar
            String response = processor.processMessage(rawRequest.toString());

            // Devolve a resposta pronta para o cliente
            out.print(response);
            out.flush();

        } catch (Exception e) {
            System.err.println("[TCP PLUGIN] Erro ao processar requisição: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}