package fiskfille.utils.registry;

import java.util.Objects;

public final class FiskDelegate<T> {
    private T referent;
    private String name;
    private final Class<T> type;

    public FiskDelegate(T referent, Class<T> type) {
        this.referent = referent;
        this.type = type;
    }

    public T get() {
        return referent;
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    void changeReference(T newTarget) {
        referent = newTarget;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FiskDelegate<?> other && Objects.equals(other.name, name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
