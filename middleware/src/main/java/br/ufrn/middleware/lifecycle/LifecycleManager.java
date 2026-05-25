package br.ufrn.middleware.lifecycle;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import br.ufrn.middleware.annotations.Lifecycle;
import br.ufrn.middleware.enums.CreationStrategy;
import br.ufrn.middleware.enums.ResourceStrategy;

public class LifecycleManager {

    private final Map<Class<?>, Object> staticInstances = new ConcurrentHashMap<>();
    private final Map<Class<?>, Queue<Object>> objectPools = new ConcurrentHashMap<>();
    private final int POOL_SIZE = 5;

    /**
     * Regista uma classe na gestão de ciclo de vida
     */
    public void registerClass(Class<?> clazz) {
        Lifecycle lifecycle = clazz.getAnnotation(Lifecycle.class);
        CreationStrategy creation = (lifecycle != null) ? lifecycle.creation() : CreationStrategy.STATIC;
        ResourceStrategy resource = (lifecycle != null) ? lifecycle.resource() : ResourceStrategy.LAZY;

        // Se for estratégia static e não for lazy, cria a instância imediatamente
        if (creation == CreationStrategy.STATIC && resource != ResourceStrategy.LAZY) {
            getOrCreateStatic(clazz);
        }

        // Se for estratégia pooling, pré-popula o pool para evitar lentidão futura
        if (resource == ResourceStrategy.POOLING || resource == ResourceStrategy.POOLING) {
            Queue<Object> pool = objectPools.computeIfAbsent(clazz, k -> new ConcurrentLinkedQueue<>());
            while (pool.size() < POOL_SIZE) {
                pool.add(createNewInstance(clazz));
            }
        }
    }

    /**
     * Obtém uma instância pronta para uso com base nas configurações da classe
     */
    public Object getInstance(Class<?> clazz) throws Exception {
        Lifecycle lifecycle = clazz.getAnnotation(Lifecycle.class);
        CreationStrategy creation = (lifecycle != null) ? lifecycle.creation() : CreationStrategy.STATIC;
        ResourceStrategy resource = (lifecycle != null) ? lifecycle.resource() : ResourceStrategy.LAZY;

        if (creation == CreationStrategy.STATIC) {
            return getOrCreateStatic(clazz);
        } else {
            if (resource == ResourceStrategy.POOLING) {
                Queue<Object> pool = objectPools.get(clazz);
                Object instance = (pool != null) ? pool.poll() : null;
                if (instance == null) {
                    instance = createNewInstance(clazz);
                }
                return instance;
            } else {
                // Instanciação puramente por requisição (Lazy/Default)
                return createNewInstance(clazz);
            }
        }
    }

    /**
     * Devolve a instância ao gestor (crucial para reutilização em POOLING)
     */
    public void releaseInstance(Class<?> clazz, Object instance) {
        Lifecycle lifecycle = clazz.getAnnotation(Lifecycle.class);
        ResourceStrategy resource = (lifecycle != null) ? lifecycle.resource() : ResourceStrategy.LAZY;

        if (resource == ResourceStrategy.POOLING) {
            Queue<Object> pool = objectPools.get(clazz);
            if (pool != null && pool.size() < POOL_SIZE) {
                pool.add(instance);
            }
        }
    }

    private Object getOrCreateStatic(Class<?> clazz) {
        return staticInstances.computeIfAbsent(clazz, k -> createNewInstance(k));
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            System.out.println("[LIFECYCLE] Instanciando nova cópia via Reflection de: " + clazz.getSimpleName());
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar instância de " + clazz.getName(), e);
        }
    }
}