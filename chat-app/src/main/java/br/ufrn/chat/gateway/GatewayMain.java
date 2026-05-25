package br.ufrn.chat.gateway;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.identification.ObjectId;
import br.ufrn.middleware.remoting.ClientRequestHandler;
public class GatewayMain {

    private final List<AOR> storagesAtivos = new CopyOnWriteArrayList<>();
    private final List<AOR> validatorsAtivos = new CopyOnWriteArrayList<>();
    private final ClientRequestHandler clienteMiddleware = new ClientRequestHandler();

    public GatewayMain(String protocolo) {
        List<AOR> catStorages = Arrays.asList(
            new AOR("127.0.0.1", 8081, protocolo, new ObjectId("STORAGE_1")),
            new AOR("127.0.0.1", 8082, protocolo, new ObjectId("STORAGE_2"))
        );
        List<AOR> catValidators = Arrays.asList(
            new AOR("127.0.0.1", 8083, protocolo, new ObjectId("VALIDATOR_1")),
            new AOR("127.0.0.1", 8084, protocolo, new ObjectId("VALIDATOR_2"))
        );

        new HeartbeatMonitor(catStorages, storagesAtivos, "/storage/ping").start();
        new HeartbeatMonitor(catValidators, validatorsAtivos, "/validator/ping").start(); 
    }

    /**
     * Função auxiliar para tentar executar um pedido numa lista de nós tolerando falhas
     */
    private String executarComTolerancia(List<AOR> nosAtivos, String method, String route, String payload, String nomeEtapa) throws Exception {
        if (nosAtivos.isEmpty()) throw new Exception("Nenhum nó disponível para a camada: " + nomeEtapa);

        for (AOR aor : nosAtivos) {
            try {
                System.out.println("   [" + nomeEtapa + "] Enviando para " + aor.getObjectId().getId() + "...");
                return clienteMiddleware.sendRequest(aor, method, route, payload);
            } catch (Exception e) {
                System.err.println("   [!] Falha no " + aor.getObjectId().getId() + " -> Tentando próximo nó de " + nomeEtapa + "...");
            }
        }
        throw new Exception("Todos os nós da camada " + nomeEtapa + " falharam.");
    }

    /**
     * Orquestrador de Rotas do Gateway
     */
    public String processarRotaOrquestrada(String method, String route, String payload) {
        try {
            if (route.equals("/chat")) {
                System.out.println("\n[GATEWAY] Iniciando fluxo de nova mensagem (/chat)");
                
                // Validação
                System.out.println("[GATEWAY] Passo 1: Validando regras de negócio...");
                String resValidacao = executarComTolerancia(validatorsAtivos, "POST", "/validator/validar", payload, "VALIDAÇÃO");
                if (resValidacao.contains("REJEITADA") || resValidacao.contains("erro")) {
                    System.out.println("[GATEWAY] X Validação falhou. Abortando.");
                    return formatarResposta(400, resValidacao);
                }
                System.out.println("[GATEWAY] v Validação APROVADA.");

                // Salvamento
                System.out.println("[GATEWAY] Passo 2: Gravando no banco distribuído...");
                String resSalvar = executarComTolerancia(storagesAtivos, "POST", "/storage/salvar", payload, "SALVAMENTO");
                System.out.println("[GATEWAY] v Mensagem salva com sucesso!");
                
                return formatarResposta(200, resSalvar);

            } else if (route.equals("/ler")) {
                System.out.println("\n[GATEWAY] Buscando histórico (/ler)");
                String resListar = executarComTolerancia(storagesAtivos, "POST", "/storage/listar", payload, "LISTAGEM");
                System.out.println("[GATEWAY] v Histórico retornado com sucesso.");
                return formatarResposta(200, resListar);
            }
            
            return formatarResposta(404, "{\"erro\": \"Rota não encontrada no Gateway\"}");

        } catch (Exception e) {
            System.err.println("[GATEWAY] ERRO FATAL: " + e.getMessage());
            return formatarResposta(503, "{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }

    private String formatarResposta(int status, String corpo) {
        String msgStatus = status == 200 ? "OK" : status == 400 ? "Bad Request" : status == 404 ? "Not Found" : "Service Unavailable";
        return "HTTP/1.1 " + status + " " + msgStatus + "\r\n" +
               "Content-Type: application/json; charset=UTF-8\r\n" +
               "Connection: close\r\n" +
               "Content-Length: " + corpo.getBytes().length + "\r\n\r\n" + corpo;
    }

    public void iniciarServidorTCP(int porta) {
        System.out.println("=== Iniciando MAESTRO API GATEWAY na porta " + porta + " (Modo TCP) ===");
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                    ) {
                        String primeiraLinha = in.readLine();
                        if (primeiraLinha == null || primeiraLinha.isEmpty() || primeiraLinha.contains("/favicon")) return;
                        
                        String[] partes = primeiraLinha.split(" ");
                        String metodo = partes[0];
                        String rota = partes[1];

                        int contentLength = 0;
                        String linha;
                        while ((linha = in.readLine()) != null && !linha.isEmpty()) {
                            if (linha.toLowerCase().startsWith("content-length:")) contentLength = Integer.parseInt(linha.substring(15).trim());
                        }

                        String corpo = "";
                        if (contentLength > 0) {
                            char[] bodyChars = new char[contentLength];
                            in.read(bodyChars, 0, contentLength);
                            corpo = new String(bodyChars);
                        }

                        String resposta = processarRotaOrquestrada(metodo, rota, corpo);
                        out.print(resposta);
                    } catch (Exception ignored) {}
                }).start();
            }
        } catch (Exception e) {
            System.err.println("Erro fatal no Gateway TCP: " + e.getMessage());
        }
    }

    public void iniciarServidorUDP(int porta) {
        System.out.println("=== Iniciando MAESTRO API GATEWAY na porta " + porta + " (Modo UDP Pts/Seg) ===");
        try (DatagramSocket socket = new DatagramSocket(porta)) {
            while (true) {
                // Buffer isolado para cada requisição para evitar o bug de concorrência
                byte[] buffer = new byte[65535];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                new Thread(() -> {
                    try {
                        String rawRequest = new String(requestPacket.getData(), 0, requestPacket.getLength(), "UTF-8");
                        if (rawRequest.trim().isEmpty() || rawRequest.contains("/favicon")) return;

                        // Parse manual da string pseudo-HTTP que o JMeter manda
                        String[] headerEBody = rawRequest.split("\r\n\r\n", 2);
                        String cabecalho = headerEBody[0];
                        String corpo = headerEBody.length > 1 ? headerEBody[1].trim() : "";

                        String primeiraLinha = cabecalho.split("\r\n")[0];
                        String[] partes = primeiraLinha.split(" ");
                        if (partes.length < 2) return;
                        String metodo = partes[0];
                        String rota = partes[1];

                        // Envia para o orquestrador
                        String resposta = processarRotaOrquestrada(metodo, rota, corpo);

                        // Devolve o datagrama para o IP e Porta do JMeter
                        byte[] responseBytes = resposta.getBytes("UTF-8");
                        DatagramPacket responsePacket = new DatagramPacket(
                            responseBytes, responseBytes.length,
                            requestPacket.getAddress(), requestPacket.getPort()
                        );
                        socket.send(responsePacket);

                    } catch (Exception e) {
                        System.err.println("Erro processando pacote UDP no Gateway: " + e.getMessage());
                    }
                }).start();
            }
        } catch (Exception e) {
            System.err.println("Erro fatal no Gateway UDP: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String protocolo = (args.length > 0) ? args[0].toUpperCase() : "TCP";
        
        GatewayMain gateway = new GatewayMain(protocolo);

        // De acordo com o argumento passado, ele levanta o servidor na estratégia correta
        if (protocolo.equals("UDP")) {
            gateway.iniciarServidorUDP(8080);
        } else {
            gateway.iniciarServidorTCP(8080);
        }
    }
}
