package com.raizlabs.datacontroller.access;


import com.raizlabs.datacontroller.controller.DataController;

public class KeyedDataAccess<Data> implements SynchronousDataAccess<Data> {

    private final Object key;
    private final int sourceId;
    private final KeyedDataManager<?, ? super Data> dataManager;
    private final ManagerHelper<?, Data> managerHelper;

    public KeyedDataAccess(Object key) {
        this(key, DataController.DataSourceIds.MEMORY_DATA);
    }

    public KeyedDataAccess(Object key, int sourceId) {
        this(key, sourceId, MemoryDataManager.getGlobalInstance());
    }

    public <K, M extends KeyedDataManager<K, ? super Data>> KeyedDataAccess(K key, M manager) {
        this(key, DataController.DataSourceIds.MEMORY_DATA, manager);
    }

    public <K, M extends KeyedDataManager<K, ? super Data>> KeyedDataAccess(K key, int sourceId, M manager) {
        this.key = key;
        this.sourceId = sourceId;
        this.dataManager = manager;
        this.managerHelper = new ManagerHelper<>(key, manager);
    }

    public Object getKey() {
        return key;
    }

    public KeyedDataManager<?, ? super Data> getDataManager() {
        return dataManager;
    }

    @Override
    public Data get() {
        return managerHelper.get();
    }

    @Override
    public void importData(Data data) {
        managerHelper.set(data);
    }

    @Override
    public void close() {

    }

    @Override
    public int getSourceId() {
        return sourceId;
    }

    public void clear() {
        managerHelper.clear();
    }

    private static class ManagerHelper<K, V> {
        private K key;
        private KeyedDataManager<K, ? super V> manager;

        public ManagerHelper(K key, KeyedDataManager<K, ? super V> manager) {
            this.key = key;
            this.manager = manager;
        }

        public V get() {
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
