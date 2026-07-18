package fiskfille.utils.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public class FiskRegistryNamespaced<T extends FiskRegistryEntry<T>> extends FiskSimpleRegistry<T> {
    private final Map<Integer, T> valuesById = new HashMap<>();
    private final Map<T, Integer> idsByValue = new IdentityHashMap<>();
    private int maxId;
    private int nextId;

    public FiskRegistryNamespaced(String domain, String key) {
        super(domain, key);
    }

    public FiskRegistryNamespaced<T> setMaxId(int max) {
        maxId = max;
        return this;
    }

    @Override
    public void putObject(String key, T value) {
        while (valuesById.containsKey(nextId)) {
            nextId++;
        }
        addObject(nextId++, key, value);
    }

    public void addObject(int id, String key, T value) {
        if (id < 0 || maxId > 0 && id > maxId) {
            throw new IndexOutOfBoundsException("Index: " + id + ", Max: " + maxId);
        }
        if (valuesById.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id '" + id + "'");
        }

        super.putObject(key, value);
        valuesById.put(id, value);
        idsByValue.put(value, id);
    }

    public int getIDForObject(T value) {
        return idsByValue.getOrDefault(value, -1);
    }

    public T getObjectById(int id) {
        return castDefault(valuesById.get(id));
    }

    public boolean containsId(int id) {
        return valuesById.containsKey(id);
    }

    public T lookup(String key) {
        if (containsKey(key)) {
            return getObject(key);
        }

        try {
            int id = Integer.parseInt(key);
            return containsId(id) ? getObjectById(id) : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return valuesById.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .iterator();
    }
}
