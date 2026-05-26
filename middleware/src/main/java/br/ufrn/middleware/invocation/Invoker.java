package br.ufrn.middleware.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.ufrn.middleware.annotations.Param;
import br.ufrn.middleware.exceptions.RemotingException;
import br.ufrn.middleware.extension.InvocationContext;
import br.ufrn.middleware.extension.InvocationInterceptor;
import br.ufrn.middleware.identification.Lookup;
import br.ufrn.middleware.lifecycle.LifecycleManager;

public class Invoker {
    
    private final Lookup lookup;
    private final LifecycleManager lifecycleManager;
    private final List<InvocationInterceptor> interceptors = new ArrayList<>();

    public Invoker(Lookup lookup, LifecycleManager lifecycleManager) {
        this.lookup = lookup;
        this.lifecycleManager = lifecycleManager;
    }

    public void addInterceptor(InvocationInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    /**
     * O Invoker recebe a requisição limpa.
     * @param httpMethod Ex: "GET", "POST"
     * @param route Ex: "/storage/salvar"
     * @param requestParams Um mapa com os parâmetros que vieram da rede (Ex: "geracao" -> 2)
     * @return A resposta do método executado
     */
    public Object invoke(String httpMethod, String route, Map<String, Object> requestParams) throws Exception {
        
        Lookup.RouteInfo info = lookup.getMethodInfo(httpMethod, route);
        if (info == null) throw new RemotingException(404, "Rota nao encontrada");
        
        Method method = info.method;
        Parameter[] methodParameters = method.getParameters();
        Object[] argsToInject = new Object[methodParameters.length];

        for (int i = 0; i < methodParameters.length; i++) {
            Parameter param = methodParameters[i];
            
            if (param.isAnnotationPresent(Param.class)) {
                String expectedName = param.getAnnotation(Param.class).name();
                
                if (!requestParams.containsKey(expectedName)) {
                    throw new RemotingException(400, "Parametro obrigatorio ausente: " + expectedName);
                }
                argsToInject[i] = requestParams.get(expectedName);
            }
        }

        InvocationContext context = new InvocationContext();
        context.set("httpMethod", httpMethod);

        for (InvocationInterceptor interceptor : interceptors) {
            interceptor.beforeInvocation(route, context);
        }
        
        Object targetInstance = null;
        Object result = null;
        try {
            // Solicita a instância correta conforme a estratégia de ciclo de vida
            targetInstance = lifecycleManager.getInstance(info.targetClass);
            
            result = method.invoke(targetInstance, argsToInject);
            return result;
            
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            throw new RemotingException(500, "Erro na aplicacao: " + e.getTargetException().getMessage());
        } catch (Exception e) {
            throw new RemotingException(500, "Erro de ciclo de vida: " + e.getMessage());
        } finally {
            
            for (InvocationInterceptor interceptor : interceptors) {
                interceptor.afterInvocation(route, context, result);
            }

            if (targetInstance != null) {
                lifecycleManager.releaseInstance(info.targetClass, targetInstance);
            }
        }
    }
}