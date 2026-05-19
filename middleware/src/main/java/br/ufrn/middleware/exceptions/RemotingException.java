package br.ufrn.middleware.exceptions;

public class RemotingException extends RuntimeException {
    
    private final int httpStatusCode;

    public RemotingException(int httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}