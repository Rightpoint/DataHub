package com.raizlabs.datahub.webservicemanager;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.webservicemanager.requests.WebServiceRequest;
import com.raizlabs.webservicemanager.webservicemanager.ResultInfo;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceManager;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceRequestListener;

/**
 * Base class which assists in the implementation of a {@link WebServiceManagerDataAccess}. Allows the return type of
 * the web request to differ from the data type being accessed via {@link com.raizlabs.datahub.access.AsyncDataAccess}.
 *
 * @param <Result> The type of data returned by the request.
 * @param <Data>   The type of data being accessed.
 */
public abstract class BaseWebServiceManagerDataAccess<Result, Data> implements WebServiceManagerDataAccess<Data> {

    private WebServiceManager webManager;
    private WebServiceRequest<Result> request;
    private int typeId;

    /**
     * Constructs a new {@link BaseWebServiceManagerDataAccess} which fetches data using the given
     * {@link WebServiceRequest} and {@link WebServiceManager}.
     *
     * @param request The request to use to request data.
     * @param manager The manager to request data through.
     */
    public BaseWebServiceManagerDataAccess(WebServiceRequest<Result> request,
                                           WebServiceManager manager) {
        setWebServiceManager(manager);
        this.request = request;
        setTypeId(AccessTypeIds.WEB_DATA);
    }

    @Override
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public void setWebServiceManager(WebServiceManager manager) {
        this.webManager = manager;
    }

    @Override
    public void get(final AsyncDataCallback<Data> asyncDataCallback) {
        webManager.doRequestInBackground(request, new WebServiceRequestListener<Result>() {
            @Override
            public void onRequestComplete(WebServiceManager manager, ResultInfo<Result> result) {
                asyncDataCallback.onResult(getResult(result), BaseWebServiceManagerDataAccess.this);
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

    /**
     * Called to create a {@link DataAccessResult} from the given {@link ResultInfo}.
     *
     * @param resultInfo The {@link ResultInfo} to get a result from.
     * @return The created result.
     */
    protected DataAccessResult<Data> getResult(ResultInfo<Result> resultInfo) {
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
            return DataAccessResult.fromResult(getDataFromResult(resultInfo.getResult()));
        }
    }

    /**
     * Called to obtain the proper data from the given result from the web request if no errors are detected.
     *
     * @param result The result provided by the web request.
     * @return The data from the result.
     */
    protected abstract Data getDataFromResult(Result result);
}
