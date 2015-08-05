package com.raizlabs.datahub.access;

/**
 * A {@link KeyedDataManager} defines a key/value means of storing and accessing data.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public interface KeyedDataManager<K, V> {

    /**
     * Returns whether this manager contains the specified key.
     *
     * @param key The key to search for.
     * @return True if this manager contains the specified key.
     */
    boolean containsKey(K key);

    /**
     * Returns the value at the specified key, attempting to cast it to the expected type.
     *
     * @param key The key to get the value of.
     * @param <T> The type to cast the value to.
     * @return The value at the specified key, or null if there was no value or the value couldn't be casted.
     */
    <T> T get(K key);

    /**
     * Sets the specified key to the specified value.
     *
     * @param key   The key to set the value of.
     * @param value The value to set.
     */
    void set(K key, V value);

    /**
     * Removes the specified key and its associated value from this manager.
     *
     * @param key The key to remove.
     * @return The existing value associated with the key, or null if none existed.
     */
    V remove(K key);

    /**
     * Removes all keys and values from this manager.
     */
    void clear();

    /**
     * Creates a {@link KeyedMemoryDataAccess} which accesses the value of the given key from this manager. This access
     * will use a default type id.
     *
     * @param key The key to access.
     * @param <T> The expected type of the value. See {@link #get(Object)}.
     * @return The {@link KeyedMemoryDataAccess} which accesses the value of the given key.
     */
    <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key);

    /**
     * Creates a {@link KeyedMemoryDataAccess} which accesses the value of the given key from this manager.
     *
     * @param key    The key to access.
     * @param typeId The type id that the access should provide.
     * @param <T>    The expected type of the value. See {@link #get(Object)}.
     * @return The {@link KeyedMemoryDataAccess} which accesses the value of the given key.
     */
    <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key, int typeId);
}
