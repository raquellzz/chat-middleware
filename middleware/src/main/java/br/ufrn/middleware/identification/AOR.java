package br.ufrn.middleware.identification;

public class AOR {
    private final String host;
    private final int port;
    private final String protocol;
    private final ObjectId objectId;

    public AOR(String host, int port, String protocol, ObjectId objectId) {
        this.host = host;
        this.port = port;
        this.protocol = protocol.toUpperCase();
        this.objectId = objectId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    /**
     * Método utilitário que o Requestor (Cliente) do Middleware usará 
     * para saber exatamente qual a URL base a chamar.
     */
    public String formatBaseUrl() {
        return "http://" + host + ":" + port;
    }

    @Override
    public String toString() {
        return String.format("AOR [ID: %s | Protocolo: %s | Endereço: %s:%d]", 
                objectId.getId(), protocol, host, port);
    }
}