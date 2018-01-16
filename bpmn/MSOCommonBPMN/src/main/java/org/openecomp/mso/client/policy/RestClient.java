/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.client.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;
import org.apache.log4j.Logger;
import org.openecomp.mso.client.RestProperties;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.stereotype.Service;

@Service
public abstract class RestClient {

    private static final Logger LOG = Logger.getLogger(RestClient.class);

    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;
    private WebTarget webTarget;

    protected final Map<String, String> headerMap;
    protected final MsoLogger msoLogger;
    protected URL host;
    protected Optional<URI> path;
    protected String accept;
    protected String contentType;

    protected RestClient(RestProperties props, Optional<URI> path) {
        msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

        headerMap = new HashMap<>();
        try {
            host = props.getEndpoint();
        } catch (MalformedURLException e) {
            LOG.error("url not valid", e);
            throw new RuntimeException(e);
        }

        this.path = path;
        initializeClient(getClient());
    }

    protected RestClient(RestProperties props, Optional<URI> path, String accept, String contentType) {
        this(props, path);
        this.accept = accept;
        this.contentType = contentType;

    }

    protected RestClient(URL host, String contentType) {
        headerMap = new HashMap<>();
        msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
        path = Optional.empty();
        this.host = host;
        this.contentType = contentType;
        initializeClient(getClient());
    }

    /**
     * Override method to return false to disable logging.
     *
     * @return true - to enable logging, false otherwise
     */
    protected boolean enableLogging() {
        return true;
    }

    /**
     * Override method to return custom value for max payload size.
     *
     * @return Default value for MAX_PAYLOAD_SIZE = 1024 * 1024
     */
    protected int getMaxPayloadSize() {
        return MAX_PAYLOAD_SIZE;
    }

    protected Builder createInvocationBuilder() {

        Builder builder = webTarget.request();
        initializeHeaderMap(headerMap);

        for (Entry<String, String> entry : headerMap.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    protected abstract void initializeHeaderMap(Map<String, String> headerMap);

    protected abstract Optional<ClientResponseFilter> addResponseFilter();

    public abstract void addRequestId(String requestId);

    protected ContextResolver<ObjectMapper> getMapper() {
        return new CommonObjectMapperProvider();
    }

    protected String getAccept() {
        return accept;
    }

    protected String getContentType() {
        return contentType;
    }

    protected String getMergeContentType() {
        return "application/merge-patch+json";
    }

    protected Client getClient() {
        return ClientBuilder.newBuilder().build();
    }

    protected void initializeClient(Client client) {
        if (enableLogging()) {
            client.register(LOG).register(new LoggingFilter(getMaxPayloadSize()));
        }
        client.register(getMapper());
        addResponseFilter().ifPresent(client::register);
        webTarget = path.map(path -> client.target(UriBuilder.fromUri(host + path.toString())))
                .orElseGet(() -> client.target(host.toString()));
        accept = MediaType.APPLICATION_JSON;
        contentType = MediaType.APPLICATION_JSON;
    }

    public Response get() {
        return createInvocationBuilder().accept(getAccept()).get();
    }

    public Response post(Object obj) {
        return createInvocationBuilder().accept(getAccept()).post(Entity.entity(obj, getContentType()));
    }

    public Response patch(Object obj) {
        return createInvocationBuilder().header("X-HTTP-Method-Override", "PATCH").accept(getAccept())
                .post(Entity.entity(obj, getMergeContentType()));
    }

    public Response put(Object obj) {
        return createInvocationBuilder().accept(getAccept()).put(Entity.entity(obj, getContentType()));
    }

    public Response delete() {
        return createInvocationBuilder().accept(getAccept()).delete();
    }

    public Response delete(Object obj) {
        return createInvocationBuilder().header("X-HTTP-Method-Override", "DELETE").accept(getAccept())
                .put(Entity.entity(obj, getContentType()));
    }

    public <T> T get(Class<T> resultClass) {
        return get().readEntity(resultClass);
    }

    public <T> T get(GenericType<T> resultClass) {
        return get().readEntity(resultClass);
    }

    public <T> T post(Object obj, Class<T> resultClass) {
        return post(obj).readEntity(resultClass);
    }

    public <T> T patch(Object obj, Class<T> resultClass) {
        return patch(obj).readEntity(resultClass);
    }

    public <T> T put(Object obj, Class<T> resultClass) {
        return put(obj).readEntity(resultClass);
    }

    public <T> T delete(Class<T> resultClass) {
        return delete().readEntity(resultClass);
    }

    public <T> T delete(Object obj, Class<T> resultClass) {
        return delete(obj).readEntity(resultClass);
    }
}
