package com.raizlabs.datahub.access;

/**
 * A class which provides access to the value stored under a particular key in a {@link KeyedDataManager}. This will
 * always fetch the current value of the key in the given manager.
 *
 * @param <Data> {@inheritDoc}
 */
public class KeyedMemoryDataAccess<Data> implements SyncDataAccess<Data> {

    private final Object key;
    private final int typeId;
    private final KeyedDataManager<?, ? super Data> dataManager;
    private final ValueAccessHelper<?, Data> valueAccessHelper;

    /**
     * Creates a {@link KeyedMemoryDataAccess} which fetches the given key from the global shared
     * {@link MemoryKeyedDataManager}.
     *
     * @param key The key to access the value of.
     */
    public KeyedMemoryDataAccess(Object key) {
        this(key, AccessTypeIds.MEMORY_DATA);
    }

    /**
     * Creates a {@link KeyedMemoryDataAccess} which fetches the given key from the global shared
     * {@link MemoryKeyedDataManager}.
     *
     * @param key    The key to access the value of.
     * @param typeId The type ID to return for this access.
     */
    public KeyedMemoryDataAccess(Object key, int typeId) {
        this(key, MemoryKeyedDataManager.getGlobalInstance(), typeId);
    }

    /**
     * Creates a {@link KeyedMemoryDataAccess} which fetches the given key from the given manager.
     *
     * @param key     The key to access the value of.
     * @param manager The manager to access the data from.
     */
    public <K, M extends KeyedDataManager<K, ? super Data>> KeyedMemoryDataAccess(K key, M manager) {
        this(key, manager, AccessTypeIds.MEMORY_DATA);
    }

    /**
     * Creates a {@link KeyedMemoryDataAccess} which fetches the given key from the given manager.
     *
     * @param key     The key to access the value of.
     * @param manager The manager to access the data from.
     * @param typeId  The type ID to return for this access.
     */
    public <K, M extends KeyedDataManager<K, ? super Data>> KeyedMemoryDataAccess(K key, M manager, int typeId) {
        this.key = key;
        this.typeId = typeId;
        this.dataManager = manager;
        this.valueAccessHelper = new ValueAccessHelper<>(key, manager);
    }

    /**
     * @return The key being used to access the data.
     */
    public Object getKey() {
        return key;
    }

    /**
     * @return The {@link KeyedDataManager} that data is being accessed from.
     */
    public KeyedDataManager<?, ? super Data> getDataManager() {
        return dataManager;
    }

    @Override
    public DataAccessResult<Data> get() {
        return valueAccessHelper.getResult();
    }

    @Override
    public void importData(Data data) {
        valueAccessHelper.set(data);
    }

    @Override
    public void close() {

    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    /**
     * Clears the value stored for the key.
     */
    public void clear() {
        valueAccessHelper.clear();
    }

    /**
     * Helper class which helps with accessing the manager and consolidates some of the necessary generic constraints.
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value being accessed.
     */
    private static class ValueAccessHelper<K, V> {
        private K key;
        private KeyedDataManager<K, ? super V> manager;

        public ValueAccessHelper(K key, KeyedDataManager<K, ? super V> manager) {
            this.key = key;
            this.manager = manager;
        }

        public DataAccessResult<V> getResult() {
            if (manager.containsKey(key)) {
                return DataAccessResult.fromResult(getValue());
            } else {
                return DataAccessResult.fromUnavailable();
            }
        }

        public V getValue() {
            return manager.get(key);
        }

        public void set(V value) {
            manager.set(key, value);
        }

        public void clear() {
            manager.remove(key);
        }
    }
}
