package br.ufrn.chat.gateway;

import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.identification.ObjectId;
import br.ufrn.middleware.remoting.ClientRequestHandler;

public class GatewayMain {
    public static void main(String[] args) {
        System.out.println("=== Iniciando GATEWAY (Broker) ===");
        
        ObjectId storageId = new ObjectId("STORAGE_NODE_1");
        AOR storageAor = new AOR("127.0.0.1", 8080, "TCP", storageId);
        
        System.out.println("[GATEWAY] Conectando-se ao " + storageId.getId() + " em " + storageAor.getHost() + ":" + storageAor.getPort());
        
        ClientRequestHandler cliente = new ClientRequestHandler();
        
        try {
            System.out.println("\n-> [1] Enviando teste de PING...");
            String respostaPing = cliente.sendRequest(storageAor, "GET", "/storage/ping", null);
            System.out.println("<- Resposta recebida: " + respostaPing);
            
            Thread.sleep(2000); 
            
            System.out.println("\n-> [2] Enviando Mensagem para SALVAR no banco distribuído...");
            String jsonPayload = "{\"geracao\": 1, \"payload\": \"{ 'remetente': 'Gateway', 'sala': 'Geral', 'texto': 'Testando a rede distribuída!' }\"}";
            String respostaSalvar = cliente.sendRequest(storageAor, "POST", "/storage/salvar", jsonPayload);
            System.out.println("<- Resposta recebida: " + respostaSalvar);
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        System.out.println("\n=== Gateway finalizou os testes ===");
    }
}