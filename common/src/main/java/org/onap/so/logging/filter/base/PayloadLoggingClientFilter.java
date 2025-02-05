/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.logging.filter.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PayloadLoggingClientFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PayloadLoggingClientFilter.class);
    private static final String ENTITY_STREAM_PROPERTY = "LoggingFilter.entityStream";
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final int maxEntitySize;

    public PayloadLoggingClientFilter() {
        maxEntitySize = 1024 * 1024;
    }

    public PayloadLoggingClientFilter(int maxPayloadSize) {
        this.maxEntitySize = Integer.min(maxPayloadSize, 1024 * 1024);
    }

    protected InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset)
            throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = stream.read(entity);
        if (entitySize != -1) {
            b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize), charset));
        }
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (requestContext.hasEntity()) {
            final OutputStream stream = new LoggingStream(requestContext.getEntityStream());
            requestContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_STREAM_PROPERTY, stream);
        }
        String method = formatMethod(requestContext);
        logger.debug("Sending HTTP {} to:{} with request headers:{}", method, requestContext.getUri(),
                getHeaders(requestContext.getHeaders()));
    }

    protected String getHeaders(MultivaluedMap<String, Object> headers) {
        MultivaluedMap<String, Object> printHeaders = new MultivaluedHashMap<>();
        for (String header : headers.keySet()) {
            if (!header.equals(HttpHeaders.AUTHORIZATION)) {
                printHeaders.add(header, headers.getFirst(header));
            } else {
                printHeaders.add(header, Constants.REDACTED);;
            }
        }
        return printHeaders.toString();
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        String method = formatMethod(requestContext);
        logger.debug("Response from method:{} performed on uri:{} has http status code:{} and response headers:{}",
                method, requestContext.getUri(), responseContext.getStatus(), responseContext.getHeaders().toString());
        if (responseContext.hasEntity()) {
            final StringBuilder sb = new StringBuilder();
            responseContext.setEntityStream(logInboundEntity(sb, responseContext.getEntityStream(), DEFAULT_CHARSET));
            logger.debug(sb.toString());
        } else {
            logger.debug("Response was returned with an empty entity.");
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) context.getProperty(ENTITY_STREAM_PROPERTY);
        context.proceed();
        if (stream != null) {
            logger.debug(stream.getStringBuilder(DEFAULT_CHARSET).toString());
        }
    }

    private class LoggingStream extends FilterOutputStream {

        private final StringBuilder sb = new StringBuilder();
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LoggingStream(OutputStream out) {
            super(out);
        }

        StringBuilder getStringBuilder(Charset charset) {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            sb.append(new String(entity, 0, entity.length, charset));
            if (entity.length > maxEntitySize) {
                sb.append("...more...");
            }
            sb.append('\n');

            return sb;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= maxEntitySize) {
                baos.write(i);
            }
            out.write(i);
        }
    }

    protected String formatMethod(ClientRequestContext requestContext) {
        String httpMethodOverride = requestContext.getHeaderString("X-HTTP-Method-Override");
        if (httpMethodOverride == null) {
            return requestContext.getMethod();
        } else {
            return requestContext.getMethod() + " (overridden to " + httpMethodOverride + ")";
        }
    }
}
