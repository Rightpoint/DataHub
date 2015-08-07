package com.raizlabs.datahub.webservicemanager;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.webservicemanager.requests.WebServiceRequest;
import com.raizlabs.webservicemanager.webservicemanager.ResultInfo;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceManager;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceRequestListener;

/**
 * {@link AsyncDataAccess} implementation which fetches data using a {@link WebServiceRequest} and a
 * {@link WebServiceManager}.
 * <p></p>
 * When this class returns errors, it may use error types out of
 * {@link com.raizlabs.datahub.webservicemanager.WebServiceManagerDataAccess.ErrorTypes} and will attempt to set the
 * error tag to any returned {@link ResultInfo}.
 * @param <Data> {@inheritDoc}
 */
public class WebServiceManagerDataAccess<Data> implements AsyncDataAccess<Data> {

    /**
     * Error type constants for {@link WebServiceManagerDataAccess} errors.
     */
    public static class ErrorTypes {
        /**
         * Error type that indicates we could not make a successful connection.
         */
        public static final int CONNECTION = 800;
        /**
         * Error type that indicates the server returned a failed status code.
         */
        public static final int STATUS_CODE = 840;
    }

    private WebServiceManager webManager;
    private WebServiceRequest<Data> request;
    private int typeId;

    /**
     * Constructs a new {@link WebServiceManagerDataAccess} which fetches data using the given
     * {@link WebServiceRequest} and {@link WebServiceManager}.
     *
     * @param request The request to use to request data.
     * @param manager The manager to request data through.
     */
    public WebServiceManagerDataAccess(WebServiceRequest<Data> request, WebServiceManager manager) {
        setWebServiceManager(manager);
        this.request = request;
        setTypeId(AccessTypeIds.WEB_DATA);
    }

    /**
     * Sets the type id to return for this access.
     *
     * @param typeId The type id to return.
     * @see #getTypeId()
     */
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    /**
     * Sets the {@link WebServiceManager} to request data through. This will impact future requests but not any that
     * are already running.
     * @param manager The manager to request data through.
     */
    public void setWebServiceManager(WebServiceManager manager) {
        this.webManager = manager;
    }

    @Override
    public void get(final AsyncDataCallback<Data> asyncDataCallback) {
        webManager.doRequestInBackground(request, new WebServiceRequestListener<Data>() {
            @Override
            public void onRequestComplete(WebServiceManager manager, ResultInfo<Data> result) {
                processResult(result, asyncDataCallback);
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    @Override
    public void importData(Data data) {
        // Can't do anything with this data
    }

    private void processResult(ResultInfo<Data> result, AsyncDataCallback<Data> callback) {
        callback.onResult(getResult(result), this);
    }

    /**
     * Called to create a {@link DataAccessResult} from the given {@link ResultInfo}.
     * @param resultInfo The {@link ResultInfo} to get a result from.
     * @return The created result.
     */
    protected DataAccessResult<Data> getResult(ResultInfo<Data> resultInfo) {
        if ((resultInfo == null) || (resultInfo.getResponseCode() == -1)) {
            final String className = request.getClass().getSimpleName();
            final String message = "Error establishing a connection in " + className;
            return DataAccessResult.fromError(new DataHubError(message, ErrorTypes.CONNECTION, resultInfo));
        } else if (!resultInfo.isStatusOK()) {
            final String connectionErrorFormat = "Connection error in %s (status code: %d)";
            final String className = request.getClass().getSimpleName();
            final int statusCode = resultInfo.getResponseCode();
            final String message = String.format(connectionErrorFormat, className, statusCode);

            DataHubError error = new DataHubError(message, DataHubError.Types.DATA_ACCESS, resultInfo);
            return DataAccessResult.fromError(error);
        } else {
            return DataAccessResult.fromResult(resultInfo.getResult());
        }
    }
}
