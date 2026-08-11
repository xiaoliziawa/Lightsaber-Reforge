package fiskfille.utils.registry;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public class FiskRegistryEntry<T> {
    @SuppressWarnings("unchecked")
    public final FiskDelegate<T> delegate = new FiskDelegate<>((T) this, (Class<T>) getClass());

    private Identifier registryName;

    @SuppressWarnings("unchecked")
    public final T setRegistryName(String name) {
        if (registryName != null) {
            throw new IllegalStateException(
                    "Attempted to replace registry name " + registryName + " with " + name
            );
        }

        registryName = Objects.requireNonNull(Identifier.tryParse(name));
        delegate.setName(registryName.toString());
        return (T) this;
    }

    public final T setRegistryName(Identifier name) {
        return setRegistryName(name.toString());
    }

    public final T setRegistryName(String domain, String name) {
        return setRegistryName(Identifier.fromNamespaceAndPath(domain, name));
    }

    public final Identifier getRegistryName() {
        return registryName;
    }

    public final String getDomain() {
        return registryName.getNamespace();
    }

    public final Class<T> getRegistryType() {
        return delegate.type();
    }

    @Override
    public String toString() {
        return delegate.name();
    }
}
