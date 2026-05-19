package br.ufrn.middleware.remoting;

public interface ProtocolPlugIn {
    void startListen(int port, MessageProcessor processor);
}