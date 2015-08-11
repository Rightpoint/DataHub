package com.raizlabs.datahub.webservicemanager;

import com.raizlabs.coreutils.util.Converter;
import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.webservicemanager.requests.WebServiceRequest;
import com.raizlabs.webservicemanager.webservicemanager.ResultInfo;
import com.raizlabs.webservicemanager.webservicemanager.WebServiceManager;

/**
 * {@link AsyncDataAccess} implementation which fetches data using a {@link WebServiceRequest} and a
 * {@link WebServiceManager}. See {@link Factory} for quick instantiation.
 * <p></p>
 * When this class returns errors, it may use error types out of
 * {@link BaseWebServiceManagerDataAccess.ErrorTypes} and will attempt to set the
 * error tag to any returned {@link ResultInfo}.
 *
 * @param <Data> {@inheritDoc}
 */
public interface WebServiceManagerDataAccess<Data> extends AsyncDataAccess<Data> {

    /**
     * Class which contains helper methods for quickly instantiating basic {@link WebServiceManagerDataAccess}es.
     */
    class Factory {
        /**
         * Creates a {@link WebServiceManagerDataAccess} that simply returns the data returned by the given request.
         *
         * @param request The request to use to request data.
         * @param manager The manager to request data through.
         * @param <Data>  The type of data being accessed.
         * @return The created {@link WebServiceManagerDataAccess}.
         */
        public static <Data> WebServiceManagerDataAccess<Data> create(WebServiceRequest<Data> request,
                                                                      WebServiceManager manager) {
            return new SimpleWebServiceManagerDataAccess<>(request, manager);
        }

        /**
         * Creates a {@link WebServiceManagerDataAccess} which returns data which is converted from the return of the
         * given request.
         *
         * @param request   The request to use to request data.
         * @param converter The {@link Converter} to use to convert from the request result type to the data type.
         * @param manager   The manager to request data through.
         * @param <Result>  The type of data returned by the request.
         * @param <Data>    The type of data being accessed.
         * @return The created {@link WebServiceManagerDataAccess}.
         */
        public static <Result, Data> WebServiceManagerDataAccess<Data> create(WebServiceRequest<Result> request,
                                                                              Converter<Result, Data> converter,
                                                                              WebServiceManager manager) {
            return new ConvertedWebServiceManagerDataAccess<>(request, converter, manager);
        }
    }

    /**
     * Error type constants for {@link BaseWebServiceManagerDataAccess} errors.
     */
    class ErrorTypes {
        /**
         * Error type that indicates we could not make a successful connection.
         */
        public static final int CONNECTION = 800;
        /**
         * Error type that indicates the server returned a failed status code.
         */
        public static final int STATUS_CODE = 840;
    }

    /**
     * Sets the type id to return for this access.
     *
     * @param typeId The type id to return.
     * @see #getTypeId()
     */
    void setTypeId(int typeId);

    /**
     * Sets the {@link WebServiceManager} to request data through. This will impact future requests but not any that
     * are already running.
     *
     * @param manager The manager to request data through.
     */
    void setWebServiceManager(WebServiceManager manager);
}
