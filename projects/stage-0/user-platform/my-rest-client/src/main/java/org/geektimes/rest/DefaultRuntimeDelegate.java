package org.geektimes.rest;

import org.geektimes.rest.client.DefaultVariantListBuilder;
import org.geektimes.rest.core.DefaultResponseBuilder;
import org.geektimes.rest.core.DefaultUriBuilder;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.RuntimeDelegate;

public class DefaultRuntimeDelegate extends RuntimeDelegate {

    @Override
    public UriBuilder createUriBuilder() {
        return new DefaultUriBuilder();
    }

    @Override
    public Response.ResponseBuilder createResponseBuilder() {
        return new DefaultResponseBuilder();
    }

    @Override
    public Variant.VariantListBuilder createVariantListBuilder() {
        return new DefaultVariantListBuilder();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType) throws IllegalArgumentException, UnsupportedOperationException {
        return null;
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
        return new HeaderDelegateImpl();
    }

    @Override
    public Link.Builder createLinkBuilder() {
        return null;
    }

    class HeaderDelegateImpl implements HeaderDelegate {
        /**
         * Parse the supplied value and create an instance of {@code T}.
         *
         * @param value the string value.
         * @return the newly created instance of {@code T}.
         * @throws IllegalArgumentException if the supplied string cannot be
         *                                  parsed or is {@code null}.
         */
        @Override
        public Object fromString(String value) {
            return new MediaType(value, null);
        }

        /**
         * Convert the supplied value to a String.
         *
         * @param value the value of type {@code T}.
         * @return a String representation of the value.
         * @throws IllegalArgumentException if the supplied object cannot be
         *                                  serialized or is {@code null}.
         */
        @Override
        public String toString(Object value) {
            return MediaType.APPLICATION_JSON;
        }
    }
}
