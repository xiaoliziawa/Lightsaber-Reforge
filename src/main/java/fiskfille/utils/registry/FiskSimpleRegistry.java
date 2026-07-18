package fiskfille.utils.registry;

import com.google.common.base.Predicate;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FiskSimpleRegistry<T extends FiskRegistryEntry<T>> implements Iterable<T> {
    protected final Map<String, T> registryObjects = new LinkedHashMap<>();
    protected final Map<T, String> nameLookup = new IdentityHashMap<>();

    private final String defaultDomain;
    private final String defaultKey;
    private T defaultValue;

    public FiskSimpleRegistry(String domain, String key) {
        defaultDomain = domain;
        defaultKey = namespace(key);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void putObject(String key, T value) {
        String namespacedKey = namespace(key);
        if (registryObjects.containsKey(namespacedKey)) {
            throw new IllegalArgumentException("Duplicate key '" + namespacedKey + "'");
        }

        value.setRegistryName(namespacedKey);
        registryObjects.put(namespacedKey, value);
        nameLookup.put(value, namespacedKey);
        if (namespacedKey.equals(defaultKey)) {
            defaultValue = value;
        }
    }

    public T getObject(String key) {
        return castDefault(registryObjects.get(namespace(key)));
    }

    public String getNameForObject(T value) {
        return nameLookup.get(value);
    }

    public boolean containsKey(String key) {
        return registryObjects.containsKey(namespace(key));
    }

    public boolean containsValue(T value) {
        return nameLookup.containsKey(value);
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(registryObjects.keySet());
    }

    public Set<String> getKeys(Predicate<T> predicate) {
        Set<String> keys = new LinkedHashSet<>();
        registryObjects.forEach((key, value) -> {
            if (predicate.apply(value)) {
                keys.add(key);
            }
        });
        return keys;
    }

    public T castDefault(T value) {
        return value == null ? defaultValue : value;
    }

    public T getRandom(Random random) {
        if (registryObjects.isEmpty()) {
            return defaultValue;
        }

        int index = random.nextInt(registryObjects.size());
        Iterator<T> iterator = iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public T getRandom() {
        return getRandom(new Random());
    }

    protected String namespace(String key) {
        if (key == null) {
            return null;
        }
        return key.indexOf(':') == -1 ? defaultDomain + ":" + key : key;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableCollection(registryObjects.values()).iterator();
    }
}
