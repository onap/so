/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.graphinventory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.graphinventory.entities.GraphInventoryEdgeLabel;
import org.onap.aaiclient.client.graphinventory.entities.GraphInventoryResultWrapper;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryPluralResourceUri;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryResourceUri;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventorySingleResourceUri;
import org.onap.aaiclient.client.graphinventory.entities.uri.HttpAwareUri;
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryMultipleItemsException;
import org.onap.so.client.RestClient;
import org.onap.so.client.RestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphInventoryResourcesClient<Self, Uri extends GraphInventoryResourceUri<?, ?>, SingleUri extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, PluralUri extends GraphInventoryPluralResourceUri<?, ?>, EdgeLabel extends GraphInventoryEdgeLabel, Wrapper extends GraphInventoryResultWrapper, TransactionalClient, SingleTransactionClient> {

    private static final Logger logger = LoggerFactory.getLogger(GraphInventoryResourcesClient.class);

    protected GraphInventoryClient client;

    protected GraphInventoryResourcesClient(GraphInventoryClient client) {
        this.client = client;
    }

    /**
     * creates a new object in GraphInventory
     * 
     * @param obj - can be any object which will marshal into a valid GraphInventory payload
     * @param uri
     * @return
     */
    public void create(SingleUri uri, Object obj) {
        RestClient giRC = client.createClient(uri);
        giRC.put(obj);
    }

    /**
     * creates a new object in GraphInventory with no payload body
     * 
     * @param uri
     * @return
     */
    public void createEmpty(SingleUri uri) {
        RestClient giRC = client.createClient(uri);
        giRC.put("");
    }

    /**
     * returns false if the object does not exist in GraphInventory
     * 
     * @param uri
     * @return
     */
    public boolean exists(Uri uri) {
        GraphInventoryResourceUri<?, ?> forceMinimal = (Uri) uri.clone();
        forceMinimal.format(Format.COUNT);
        forceMinimal.limit(1);
        try {
            RestClient giRC = client.createClient(forceMinimal);

            return giRC.get().getStatus() == Response.Status.OK.getStatusCode();
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Adds a relationship between two objects in GraphInventory
     * 
     * @param uriA
     * @param uriB
     * @return
     */
    public void connect(SingleUri uriA, SingleUri uriB) {
        GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?> uriAClone = (SingleUri) uriA.clone();
        RestClient giRC = client.createClient(uriAClone.relationshipAPI());
        giRC.put(this.buildRelationship(uriB));
    }

    /**
     * Adds a relationship between two objects in GraphInventory with a given edge label
     * 
     * @param uriA
     * @param uriB
     * @param edge label
     * @return
     */
    public void connect(SingleUri uriA, SingleUri uriB, EdgeLabel label) {
        GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?> uriAClone = (SingleUri) uriA.clone();
        RestClient giRC = client.createClient(uriAClone.relationshipAPI());
        giRC.put(this.buildRelationship(uriB, label));
    }

    /**
     * Removes relationship from two objects in GraphInventory
     * 
     * @param uriA
     * @param uriB
     * @return
     */
    public void disconnect(SingleUri uriA, SingleUri uriB) {
        GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?> uriAClone = (SingleUri) uriA.clone();
        RestClient giRC = client.createClient(uriAClone.relationshipAPI());
        giRC.delete(this.buildRelationship(uriB));
    }

    /**
     * Deletes object from GraphInventory. Automatically handles resource-version.
     * 
     * @param uri
     * @return
     */
    public void delete(SingleUri uri) {
        GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?> clone = (SingleUri) uri.clone();
        RestClient giRC = client.createClient(clone);
        Map<String, Object> result = giRC.get(new GenericType<Map<String, Object>>() {}).orElseThrow(
                () -> new NotFoundException(clone.build() + " does not exist in " + client.getGraphDBName()));
        String resourceVersion = (String) result.get("resource-version");
        giRC = client.createClient(clone.resourceVersion(resourceVersion));
        giRC.delete();

    }

    /**
     * Deletes object from GraphInventory only if exists. Automatically handles resource-version.
     * 
     * @param uri
     * @return
     */
    public void deleteIfExists(SingleUri uri) {
        GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?> clone = (SingleUri) uri.clone();
        RestClient giRC = client.createClient(clone);
        Optional<Map<String, Object>> result = giRC.get(new GenericType<Map<String, Object>>() {});
        if (result.isPresent()) {
            String resourceVersion = (String) result.get().get("resource-version");
            giRC = client.createClient(clone.resourceVersion(resourceVersion));
            giRC.delete();
        } else {
            logger.warn(clone.build() + " already does not exist in " + client.getGraphDBName()
                    + " therefore delete call not executed");
        }
    }

    /**
     * @param obj - can be any object which will marshal into a valid GraphInventory payload
     * @param uri
     * @return
     */
    public void update(SingleUri uri, Object obj) {
        RestClient giRC = client.createClient(uri);
        giRC.patch(obj);
    }

    /**
     * Retrieves an object from GraphInventory and unmarshalls it into the Class specified
     * 
     * @param clazz
     * @param uri
     * @return
     */
    public <T> Optional<T> get(Class<T> clazz, Uri uri) {
        try {
            return client.createClient(uri).get(clazz);
        } catch (NotFoundException e) {
            if (this.getRestProperties().mapNotFoundToEmpty()) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    /**
     * Retrieves an object from GraphInventory and returns complete response
     * 
     * @param uri
     * @return
     */
    public Response getFullResponse(Uri uri) {
        try {
            return client.createClient(uri).get();
        } catch (NotFoundException e) {
            if (this.getRestProperties().mapNotFoundToEmpty()) {
                return e.getResponse();
            } else {
                throw e;
            }
        }
    }

    /**
     * Retrieves an object from GraphInventory and automatically unmarshalls it into a Map or List
     * 
     * @param resultClass
     * @param uri
     * @return
     */
    public <T> Optional<T> get(GenericType<T> resultClass, Uri uri) {
        try {
            return client.createClient(uri).get(resultClass);
        } catch (NotFoundException e) {
            if (this.getRestProperties().mapNotFoundToEmpty()) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    public <T, R> Optional<R> getOne(Class<T> pluralClass, Class<R> resultClass, PluralUri uri) {
        Optional<List<R>> result = unwrapPlural(pluralClass, resultClass, uri);

        if (result.isPresent()) {
            if (result.get().size() == 1) {
                return Optional.of(result.get().get(0));
            } else {
                throw new GraphInventoryMultipleItemsException(result.get().size(), uri);
            }
        }

        return Optional.empty();
    }

    public <T, R> Optional<R> getFirst(Class<T> pluralClass, Class<R> resultClass, PluralUri uri) {
        Optional<List<R>> result = unwrapPlural(pluralClass, resultClass, uri);

        if (result.isPresent() && !result.get().isEmpty()) {
            return Optional.of(result.get().get(0));
        }

        return Optional.empty();
    }

    public <T, R> Optional<Wrapper> getFirstWrapper(Class<T> pluralClass, Class<R> resultClass, PluralUri uri) {

        Optional<R> result = getFirst(pluralClass, resultClass, uri);
        if (result.isPresent()) {
            return Optional.of(this.createWrapper(result.get()));
        } else {
            return Optional.empty();
        }
    }

    public <T, R> Optional<Wrapper> getOneWrapper(Class<T> pluralClass, Class<R> resultClass, PluralUri uri) {

        Optional<R> result = getOne(pluralClass, resultClass, uri);
        if (result.isPresent()) {
            return Optional.of(this.createWrapper(result.get()));
        } else {
            return Optional.empty();
        }
    }

    protected <T, R> Optional<List<R>> unwrapPlural(Class<T> pluralClass, Class<R> resultClass, PluralUri uri) {
        try {
            PluralUri clone = (PluralUri) uri.clone().limit(1);
            Optional<T> obj = client.createClient(clone).get(pluralClass);
            if (obj.isPresent()) {
                Optional<Method> listMethod = Arrays.stream(obj.get().getClass().getMethods()).filter(method -> {

                    Type returnType = method.getGenericReturnType();
                    if (returnType instanceof ParameterizedType) {
                        Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
                        if (types != null && types[0] instanceof Class) {
                            Class<?> listClass = (Class<?>) types[0];
                            return resultClass.equals(listClass);
                        }
                    }

                    return false;
                }).findFirst();
                if (listMethod.isPresent()) {
                    try {
                        return Optional.of((List<R>) listMethod.get().invoke(obj.get()));

                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return Optional.empty();

        } catch (NotFoundException e) {
            if (this.getRestProperties().mapNotFoundToEmpty()) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }

    /**
     * Retrieves an object from GraphInventory wrapped in a helper class which offer additional features
     * 
     * @param uri
     * @return
     */
    public Wrapper get(Uri uri) {
        String json;
        try {
            json = client.createClient(uri).get(String.class).orElse(null);
        } catch (NotFoundException e) {
            if (this.getRestProperties().mapNotFoundToEmpty()) {
                json = null;
            } else {
                throw e;
            }
        }
        return this.createWrapper(json);
    }

    /**
     * Retrieves an object from GraphInventory wrapped in a helper class which offer additional features If the object
     * cannot be found in GraphInventory the method will throw the runtime exception included as an argument
     * 
     * @param uri
     * @return
     */
    public Wrapper get(Uri uri, Class<? extends RuntimeException> c) {
        String json;
        try {
            json = client.createClient(uri).get(String.class).orElseThrow(() -> createException(c,
                    uri.build() + " not found in " + client.getGraphDBName(), Optional.empty()));
        } catch (NotFoundException e) {
            throw createException(c, "could not construct uri for use with " + client.getGraphDBName(), Optional.of(e));
        }

        return this.createWrapper(json);
    }

    private RuntimeException createException(Class<? extends RuntimeException> c, String message,
            Optional<Throwable> t) {
        RuntimeException e;
        try {
            if (t.isPresent()) {
                e = c.getConstructor(String.class, Throwable.class).newInstance(message, t.get());
            } else {
                e = c.getConstructor(String.class).newInstance(message);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e1) {
            throw new IllegalArgumentException("could not create instance for " + c.getName());
        }

        return e;
    }

    /**
     * Will automatically create the object if it does not exist
     * 
     * @param obj - Optional object which serializes to a valid GraphInventory payload
     * @param uri
     * @return
     */
    public Self createIfNotExists(SingleUri uri, Optional<Object> obj) {
        if (!this.exists((Uri) uri)) {
            if (obj.isPresent()) {
                this.create(uri, obj.get());
            } else {
                this.createEmpty(uri);
            }

        }
        return (Self) this;
    }

    protected Relationship buildRelationship(SingleUri uri) {
        return buildRelationship(uri, Optional.empty());
    }

    protected Relationship buildRelationship(SingleUri uri, GraphInventoryEdgeLabel label) {
        return buildRelationship(uri, Optional.of(label));
    }

    protected Relationship buildRelationship(SingleUri uri, Optional<GraphInventoryEdgeLabel> label) {
        final Relationship result = new Relationship();
        if (uri instanceof HttpAwareUri) {
            result.setRelatedLink(((HttpAwareUri) uri).locateAndBuild().toString());
        } else {
            result.setRelatedLink(uri.build().toString());
        }
        if (label.isPresent()) {
            result.setRelationshipLabel(label.get().toString());
        }
        return result;
    }

    public abstract Wrapper createWrapper(String json);

    public abstract Wrapper createWrapper(Object json);

    /**
     * Starts a transaction which encloses multiple GraphInventory mutations
     * 
     * @return
     */
    public abstract TransactionalClient beginTransaction();

    /**
     * Starts a transaction groups multiple GraphInventory mutations
     * 
     * @return
     */
    public abstract SingleTransactionClient beginSingleTransaction();

    public <T extends RestProperties> T getRestProperties() {
        return client.getRestProperties();
    }

}
