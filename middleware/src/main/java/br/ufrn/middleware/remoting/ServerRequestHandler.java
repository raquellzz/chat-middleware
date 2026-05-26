package br.ufrn.middleware.remoting;

public class ServerRequestHandler {

    private final ProtocolPlugIn protocol;
    private final MessageProcessor processor;

    public ServerRequestHandler(ProtocolPlugIn protocol, MessageProcessor processor) {
        this.protocol = protocol;
        this.processor = processor;
    }

    public void start(int port) {
        protocol.startListen(port, processor);
    }
}