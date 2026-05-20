package br.ufrn.middleware.extension;

public class LoggerInterceptor implements InvocationInterceptor {

    @Override
    public void beforeInvocation(String route, InvocationContext context) {
        context.set("startTime", System.currentTimeMillis());
        System.out.println("[INTERCEPTOR] -> Chegou requisição para: " + route);
    }

    @Override
    public void afterInvocation(String route, InvocationContext context, Object result) {
        long startTime = (long) context.get("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("[INTERCEPTOR] <- Resposta de " + route + " gerada em " + duration + "ms.");
    }
}