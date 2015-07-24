package com.raizlabs.datahub.access;


import com.raizlabs.datahub.hub.DataHub;

public class KeyedMemoryDataAccess<Data> implements SynchronousDataAccess<Data> {

    private final Object key;
    private final int typeId;
    private final KeyedDataManager<?, ? super Data> dataManager;
    private final ValueAccessHelper<?, Data> valueAccessHelper;

    public KeyedMemoryDataAccess(Object key) {
        this(key, DataHub.AccessTypeIds.MEMORY_DATA);
    }

    public KeyedMemoryDataAccess(Object key, int typeId) {
        this(key, typeId, MemoryDataManager.getGlobalInstance());
    }

    public <K, M extends KeyedDataManager<K, ? super Data>> KeyedMemoryDataAccess(K key, M manager) {
        this(key, DataHub.AccessTypeIds.MEMORY_DATA, manager);
    }

    public <K, M extends KeyedDataManager<K, ? super Data>> KeyedMemoryDataAccess(K key, int typeId, M manager) {
        this.key = key;
        this.typeId = typeId;
        this.dataManager = manager;
        this.valueAccessHelper = new ValueAccessHelper<>(key, manager);
    }

    public Object getKey() {
        return key;
    }

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

    public void clear() {
        valueAccessHelper.clear();
    }

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
