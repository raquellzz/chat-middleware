package br.ufrn.middleware.extension;

public interface InvocationInterceptor {
    
    void beforeInvocation(String route, InvocationContext context);
    
    void afterInvocation(String route, InvocationContext context, Object result);
}