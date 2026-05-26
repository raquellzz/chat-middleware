package br.ufrn.chat.gateway;

import java.util.Arrays;

import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.identification.ObjectId;
import br.ufrn.middleware.remoting.ProtocolPlugIn;
import br.ufrn.middleware.remoting.ServerRequestHandler;
import br.ufrn.middleware.remoting.ServiceCluster;
import br.ufrn.middleware.remoting.TCPProtocolPlugIn;
import br.ufrn.middleware.remoting.UDPProtocolPlugIn;
public class GatewayMain {

    public static void main(String[] args) {
        String protocolo = (args.length > 0) ? args[0].toUpperCase() : "TCP";
        System.out.println("=== Iniciando API GATEWAY na porta 8080 (Modo " + protocolo + ") ===");
        
        ServiceCluster clusterStorages = new ServiceCluster("STORAGE", Arrays.asList(
            new AOR("127.0.0.1", 8081, protocolo, new ObjectId("STORAGE_1")),
            new AOR("127.0.0.1", 8082, protocolo, new ObjectId("STORAGE_2"))
        ), "/storage/ping");

        ServiceCluster clusterValidators = new ServiceCluster("VALIDATOR", Arrays.asList(
            new AOR("127.0.0.1", 8083, protocolo, new ObjectId("VALIDATOR_1")),
            new AOR("127.0.0.1", 8084, protocolo, new ObjectId("VALIDATOR_2"))
        ), "/validator/ping");

        ChatOrchestrator orquestrador = new ChatOrchestrator(clusterValidators, clusterStorages);
        
        ProtocolPlugIn pluginDeRede = protocolo.equals("UDP") ? new UDPProtocolPlugIn() : new TCPProtocolPlugIn();

        ServerRequestHandler server = new ServerRequestHandler(pluginDeRede, orquestrador);
        server.start(8080);
    }
}
