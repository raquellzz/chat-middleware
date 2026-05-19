package br.ufrn.middleware.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import br.ufrn.middleware.exceptions.RemotingException;
import br.ufrn.middleware.identification.Lookup;
import br.ufrn.middleware.lifecycle.LifecycleManager;

public class Invoker {
    
    private final Lookup lookup;
    private final LifecycleManager lifecycleManager;

    public Invoker(Lookup lookup, LifecycleManager lifecycleManager) {
        this.lookup = lookup;
        this.lifecycleManager = lifecycleManager;
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

        // ... (A lógica de validação de parâmetros @Param continua exatamente igual) ...
        
        Object targetInstance = null;
        try {
            // Solicita a instância correta conforme a estratégia de ciclo de vida
            targetInstance = lifecycleManager.getInstance(info.targetClass);
            
            return info.method.invoke(targetInstance, argsToInject);
            
        } catch (InvocationTargetException e) {
            throw new RemotingException(500, "Erro na aplicacao: " + e.getTargetException().getMessage());
        } catch (Exception e) {
            throw new RemotingException(500, "Erro de ciclo de vida: " + e.getMessage());
        } finally {
            // Liberta ou devolve ao Pool se necessário
            if (targetInstance != null) {
                lifecycleManager.releaseInstance(info.targetClass, targetInstance);
            }
        }
    }
}