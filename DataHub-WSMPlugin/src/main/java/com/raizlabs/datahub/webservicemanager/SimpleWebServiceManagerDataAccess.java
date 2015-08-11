package com.raizlabs.datahub.webservicemanager;

import com.raizlabs.webservicemanager.requests.WebServiceRequest;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceManager;

/**
 * Simple implementation of {@link WebServiceManagerDataAccess} which directly returns the data returned by a
 * {@link WebServiceRequest}.
 *
 * @param <Data> The data being returned by the request and provided by the access.
 */
public class SimpleWebServiceManagerDataAccess<Data> extends BaseWebServiceManagerDataAccess<Data, Data> {

    /**
     * Constructs a {@link SimpleWebServiceManagerDataAccess} which directly returns the data returned by the given
     * {@link WebServiceRequest}.
     *
     * @param request The request to use to request data.
     * @param manager The manager to request data through.
     */
    public SimpleWebServiceManagerDataAccess(WebServiceRequest<Data> request, WebServiceManager manager) {
        super(request, manager);
    }

    @Override
    protected Data getDataFromResult(Data data) {
        return data;
    }
}
