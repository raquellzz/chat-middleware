package br.ufrn.middleware.remoting;

import br.ufrn.middleware.invocation.Marshaller;

public class ServerRequestHandler implements MessageProcessor {

    private final ProtocolPlugIn protocol;
    private final Marshaller marshaller;

    public ServerRequestHandler(ProtocolPlugIn protocol, Marshaller marshaller) {
        this.protocol = protocol;
        this.marshaller = marshaller;
    }

    public void start(int port) {
        protocol.startListen(port, this);
    }

    @Override
    public String processMessage(String rawRequest) {
        return marshaller.unmarshalAndInvoke(rawRequest);
    }
}
