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
import java.io.Serializable;
import javax.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;

public abstract class SimplePluralUri<T extends GraphInventoryPluralResourceUri<?, ?>, Parent extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, PT extends GraphInventoryObjectPlurals, OT extends GraphInventoryObjectType>
        extends SimpleBaseUri<T, Parent, PT> implements GraphInventoryPluralResourceUri<T, PT>, Serializable {

    private static final long serialVersionUID = -337701171277616439L;

    protected final PT pluralType;

    protected SimplePluralUri(PT type, UriBuilder builder, Object... values) {
        super(type, builder, values);
        this.pluralType = type;
    }

    protected SimplePluralUri(PT type) {
        super(type, new Object[0]);
        this.pluralType = type;
    }

    protected SimplePluralUri(PT type, Object... values) {
        super(type, values);
        this.pluralType = type;
    }

    protected SimplePluralUri(Parent parentUri, PT childType) {
        super(parentUri, childType, new Object[0]);
        this.pluralType = childType;
    }

    public SimplePluralUri(SimplePluralUri<T, Parent, PT, OT> copy) {
        super(copy);
        this.pluralType = copy.pluralType;
    }

    protected void setInternalURI(UriBuilder builder) {
        this.internalURI = builder;
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
