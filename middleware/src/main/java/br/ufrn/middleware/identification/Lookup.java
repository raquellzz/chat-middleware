package br.ufrn.middleware.identification;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import br.ufrn.middleware.annotations.Get;
import br.ufrn.middleware.annotations.Post;
import br.ufrn.middleware.annotations.RequestMap;

public class Lookup {
    
    private final Map<String, RouteInfo> registry = new HashMap<>();

    public static class RouteInfo {
        public final Class<?> targetClass; // Mudou de Object para Class<?>
        public final Method method;

        public RouteInfo(Class<?> targetClass, Method method) {
            this.targetClass = targetClass;
            this.method = method;
        }
    }

    public void registerService(Class<?> clazz) {
        
        if (!clazz.isAnnotationPresent(RequestMap.class)) return;

        String baseRoute = clazz.getAnnotation(RequestMap.class).router();

        for (Method method : clazz.getDeclaredMethods()) {
            String methodRoute = "";
            String httpMethod = "";

            if (method.isAnnotationPresent(Get.class)) {
                methodRoute = method.getAnnotation(Get.class).router();
                httpMethod = "GET";
            } else if (method.isAnnotationPresent(Post.class)) {
                methodRoute = method.getAnnotation(Post.class).router();
                httpMethod = "POST";
            }

            if (!methodRoute.isEmpty()) {
                String fullRoute = httpMethod + ":" + baseRoute + methodRoute;
                registry.put(fullRoute, new RouteInfo(clazz, method));
                System.out.println("[LOOKUP] Registrado: [" + fullRoute + "]");
            }
        }
    }

    public RouteInfo getMethodInfo(String httpMethod, String route) {
        return registry.get(httpMethod.toUpperCase() + ":" + route);
    }
}