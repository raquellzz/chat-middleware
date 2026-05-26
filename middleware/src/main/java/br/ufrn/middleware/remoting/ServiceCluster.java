package br.ufrn.middleware.remoting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import br.ufrn.middleware.identification.AOR;

public class ServiceCluster {
    private final List<AOR> catalogo;
    private final List<AOR> ativos = new CopyOnWriteArrayList<>();
    private final ClientRequestHandler client = new ClientRequestHandler();
    private final String pingRoute;
    private final String layerName;

    public ServiceCluster(String layerName, List<AOR> catalogo, String pingRoute) {
        this.layerName = layerName;
        this.catalogo = catalogo;
        this.pingRoute = pingRoute;
        
        // Inicia o Heartbeat interno do cluster
        new Thread(this::monitorHeartbeat).start();
    }

    private void monitorHeartbeat() {
        while (true) {
            try {
                Thread.sleep(3000);
                for (AOR aor : catalogo) {
                    try {
                        client.sendRequest(aor, "GET", pingRoute, null);
                        // client.sendRequest(aor, "POST", pingRoute, "{}");
                        if (!ativos.contains(aor)) {
                            ativos.add(aor);
                            System.out.println("[CLUSTER " + layerName + "] " + aor.getObjectId().getId() + " ONLINE.");
                        }
                    } catch (Exception e) {
                        if (ativos.contains(aor)) {
                            ativos.remove(aor);
                            System.out.println("[CLUSTER " + layerName + "] ALERTA! " + aor.getObjectId().getId() + " OFFLINE.");
                        }
                    }
                }
            } catch (InterruptedException e) { break; }
        }
    }

    public String sendWithFaultTolerance(String method, String route, String payload) throws Exception {
        if (ativos.isEmpty()) throw new Exception("Nenhum nó disponível na camada: " + layerName);

        for (AOR aor : ativos) {
            try {
                return client.sendRequest(aor, method, route, payload);
            } catch (Exception e) {
            }
        }
        throw new Exception("Falha total de comunicação na camada: " + layerName);
    }
}