package br.ufrn.middleware.identification;

import java.util.Objects;

public class ObjectId {
    private final String id;

    public ObjectId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("O ID do objeto não pode ser nulo ou vazio.");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectId objectId = (ObjectId) o;
        return Objects.equals(id, objectId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ObjectId[" + id + "]";
    }
}