package br.ufrn.middleware.remoting;

public interface MessageProcessor {
    String processMessage(String rawRequest);
}