package br.ufrn.middleware.extension;

import java.util.HashMap;
import java.util.Map;

public class InvocationContext {
    private final Map<String, Object> metadata = new HashMap<>();

    public void set(String key, Object value) {
        metadata.put(key, value);
    }

    public Object get(String key) {
        return metadata.get(key);
    }

    public boolean has(String key) {
        return metadata.containsKey(key);
    }
}