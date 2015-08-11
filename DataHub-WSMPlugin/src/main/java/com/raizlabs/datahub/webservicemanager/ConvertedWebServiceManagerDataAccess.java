package com.raizlabs.datahub.webservicemanager;

import com.raizlabs.coreutils.util.Converter;
import com.raizlabs.webservicemanager.requests.WebServiceRequest;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceManager;

/**
 * Implementation of {@link WebServiceManagerDataAccess} which returns data which is converted from the return type of
 * a {@link WebServiceRequest}.
 *
 * @param <Result> The type of data returned by the request.
 * @param <Data>   The type of data being accessed.
 */
public class ConvertedWebServiceManagerDataAccess<Result, Data> extends BaseWebServiceManagerDataAccess<Result, Data> {

    private Converter<Result, Data> resultConverter;

    /**
     * Constructs a {@link ConvertedWebServiceManagerDataAccess} which returns data converted from the given request
     * using the given converter.
     *
     * @param request   The request to use to request data.
     * @param converter The converter to use to convert data into the accessed data type.
     * @param manager   The manager to request data through.
     */
    public ConvertedWebServiceManagerDataAccess(WebServiceRequest<Result> request,
                                                Converter<Result, Data> converter,
                                                WebServiceManager manager) {
        super(request, manager);
        this.resultConverter = converter;
    }

    @Override
    protected Data getDataFromResult(Result result) {
        return resultConverter.convert(result);
    }
}
