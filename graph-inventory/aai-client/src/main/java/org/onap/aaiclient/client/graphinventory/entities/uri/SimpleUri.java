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

package org.onap.aaiclient.client.graphinventory.entities.uri;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import jakarta.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPluralFragment;
import org.onap.aaiclient.client.graphinventory.GraphInventorySingleFragment;

public abstract class SimpleUri<T extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, PT extends GraphInventoryPluralResourceUri<?, ?>, S extends GraphInventoryObjectType, P extends GraphInventoryObjectPlurals, SF extends GraphInventorySingleFragment<?>, PF extends GraphInventoryPluralFragment<?>>
        extends SimpleBaseUri<T, T, S> implements GraphInventorySingleResourceUri<T, PT, S, P, SF, PF> {

    private static final long serialVersionUID = -337701171277616439L;
    protected static final String relationshipAPI = "/relationship-list/relationship";
    protected static final String relatedTo = "/related-to";

    protected SimpleUri(S type, Object... values) {
        super(type, values);
    }

    protected SimpleUri(S type, URI uri) {
        super(type, uri);

    }

    protected SimpleUri(S type, UriBuilder builder, Object... values) {
        super(type, builder, values);

    }

    protected SimpleUri(T parentUri, S childType, Object... childValues) {
        super(parentUri, childType, childValues);
    }

    protected SimpleUri(SimpleBaseUri<T, T, S> copy) {
        super(copy);
    }

    @Override
    public T resourceVersion(String version) {
        this.internalURI = internalURI.replaceQueryParam("resource-version", version);
        return (T) this;
    }

    @Override
    public T relationshipAPI() {
        this.internalURI = internalURI.path(relationshipAPI);
        return (T) this;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(this.internalURI.toTemplate());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        String uri = ois.readUTF();
        this.setInternalURI(UriBuilder.fromUri(uri));
    }

}
