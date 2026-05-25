package br.ufrn.chat.gateway;

import java.util.List;

import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.remoting.ClientRequestHandler;

class HeartbeatMonitor extends Thread {
    private final List<AOR> catalogo;
    private final List<AOR> ativos;
    private final String rotaPing;
    private final ClientRequestHandler cli = new ClientRequestHandler();

    public HeartbeatMonitor(List<AOR> catalogo, List<AOR> ativos, String rotaPing) {
        this.catalogo = catalogo;
        this.ativos = ativos;
        this.rotaPing = rotaPing;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(3000);
                for (AOR aor : catalogo) {
                    try {
                        cli.sendRequest(aor, "GET", rotaPing, null);
                        if (!ativos.contains(aor)) {
                            ativos.add(aor);
                            System.out.println("[HEARTBEAT] " + aor.getObjectId().getId() + " ONLINE.");
                        }
                    } catch (Exception e) {
                        if (ativos.contains(aor)) {
                            ativos.remove(aor);
                            System.out.println("[HEARTBEAT] " + aor.getObjectId().getId() + " OFFLINE.");
                        }
                    }
                }
            } catch (InterruptedException e) { break; }
        }
    }
}