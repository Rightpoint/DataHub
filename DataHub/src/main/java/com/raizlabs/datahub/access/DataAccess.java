package com.raizlabs.datahub.access;

/**
 * A {@link DataAccess} defines a means of obtaining data from an individual source. This interface defines the methods
 * that all types of access must define.
 *
 * @param <Data> The type of data being accessed.
 */
public interface DataAccess<Data> {

    /**
     * Closes this {@link DataAccess}, indicating that it will no longer be used and may free associated resources.
     */
    void close();

    /**
     * Get the ID representing the type of access. This should be shared by accesses using the same mechanism. See
     * {@link com.raizlabs.datahub.access.DataAccess.AccessTypeIds} for predefined constants which may be used, though
     * custom values are permitted.
     *
     * @return The ID representing the type of this access.
     */
    int getTypeId();

    /**
     * Attempts to import the given data into the source of this {@link DataAccess}, replacing the current data.
     * Whether this is possible is up to the implementation of the particular {@link DataAccess}, therefore this
     * method is not guaranteed to change the data returned from this {@link DataAccess}.
     *
     * @param data The data to import.
     */
    void importData(Data data);

    /**
     * Class of constants for {@link DataAccess#getTypeId()}.
     */
    class AccessTypeIds {
        /**
         * Type ID constant which represents no {@link DataAccess} at all.
         */
        public static final int NONE = 0;

        /**
         * Type ID constant which represents accessing data from memory.
         */
        public static final int MEMORY_DATA = 1000;

        /**
         * Type ID constant which represents accessing data from local persistent storage.
         */
        public static final int PERSISTENT_DATA = 2000;

        /**
         * Type ID constant which represents accessing data from the web.
         */
        public static final int WEB_DATA = 4000;
    }
}
