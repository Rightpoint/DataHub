package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.DataHubErrorInfo;
import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.hub.DataHubResult;

import java.util.LinkedHashMap;

/**
 * Base class for implementing a {@link DataObserverListener} which provides some convenience methods and helper
 * functionality - mainly around keeping track of received results and providing access to the history.
 * <p></p>
 * This class provides different methods to implement in place of the standard {@link DataObserverListener} methods,
 * making sure they are called appropriately around the base logic. The standard methods are left available for
 * overriding if necessary, but generally should be avoided.
 *
 * @param <Data> {@inheritDoc}
 */
public abstract class BaseDataObserverListener<Data> implements DataObserverListener<Data> {

    private LinkedHashMap<Integer, DataHubResult<Data>> resultList;

    private int lastTypeId;
    private int lastDataTypeId;

    /**
     * Constructs a new {@link BaseDataObserverListener}.
     */
    public BaseDataObserverListener() {
        resultList = new LinkedHashMap<>();
        lastTypeId = DataAccess.AccessTypeIds.NONE;
        lastDataTypeId = DataAccess.AccessTypeIds.NONE;
    }

    //region Inherited Methods
    @Override
    public void onDataFetchStarted() {
        resultList.clear();
        lastTypeId = DataAccess.AccessTypeIds.NONE;
        lastDataTypeId = DataAccess.AccessTypeIds.NONE;
        onFetchStarted();
    }

    @Override
    public void onDataFetchFinished() {
        onFetchFinished();
        onFinalDataReceived(getResult(lastDataTypeId));
    }

    @Override
    public void onResultReceived(DataHubResult<Data> result) {
        if (result.hasError()) {
            onErrorReceived(result);
        } else {
            lastDataTypeId = result.getAccessTypeId();
            onDataReceived(result);
        }
    }
    //endregion Inherited Methods

    //region Abstract Methods

    /**
     * Called when a fetch has been started.
     *
     * @see #onDataFetchStarted()
     */
    public abstract void onFetchStarted();

    /**
     * Called when a fetch has finished.
     *
     * @see #onDataFetchFinished()
     */
    public abstract void onFetchFinished();

    /**
     * Called when a result is received which doesn't contain an error.
     *
     * @param data The received {@link DataResult}.
     * @see #onResultReceived(DataHubResult)
     */
    public abstract void onDataReceived(DataResult<Data> data);

    /**
     * Called when a result is received which contains an error.
     *
     * @param error The received {@link DataHubErrorInfo}.
     * @see #onResultReceived(DataHubResult)
     */
    public abstract void onErrorReceived(DataHubErrorInfo error);
    //endregion Abstract Methods

    /**
     * Called with the last {@link DataResult} which was stored and dispatched. This is called when the request for
     * data has been finished.
     *
     * @param dataResult The last stored and dispatched {@link DataResult}, potentially null.
     */
    protected void onFinalDataReceived(DataResult<Data> dataResult) {

    }

    /**
     * @return True if this result has received a result.
     */
    public boolean hasResult() {
        return !resultList.isEmpty();
    }

    /**
     * Gets the last result we have from the given type ID since a fetch was started. These values are cleared when
     * {@link #onDataFetchStarted()} is called.
     *
     * @param accessTypeId The access type ID to get a result from.
     * @return The last result for the given type id, or null if none has been received.
     * @see #getMostRecentResult()
     * @see #getMostRecentDataResult()
     */
    public DataHubResult<Data> getResult(int accessTypeId) {
        return resultList.get(accessTypeId);
    }

    /**
     * Gets the most recent result sent through {@link #onResultReceived(DataHubResult)} since a fetch was started.
     * This is the newest result we have from any data access. This value is cleared when
     * {@link #onDataFetchStarted()} is called.
     *
     * @return The most recent result, or null if none has been received.
     * @see #getResult(int)
     * @see #getMostRecentDataResult()
     */
    public DataHubResult<Data> getMostRecentResult() {
        return getResult(lastTypeId);
    }

    /**
     * Gets the most recent result sent through {@link #onResultReceived(DataHubResult)} which wasn't an error, since
     * a fetch was started. This is essentially the value of the last call to {@link #onDataReceived(DataResult)}.
     * This value is cleared when {@link #onDataFetchStarted()} is called.
     *
     * @return The most recent data result, or null if none has been received.
     */
    public DataHubResult<Data> getMostRecentDataResult() {
        return getResult(lastDataTypeId);
    }
}
