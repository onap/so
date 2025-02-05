/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.aai.entities.uri;

import java.net.URI;
import jakarta.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.graphinventory.entities.uri.SimpleUri;

public class AAISimpleUri extends
        SimpleUri<AAIResourceUri, AAIPluralResourceUri, AAIObjectType, AAIObjectPlurals, AAISingleFragment, AAIPluralFragment>
        implements AAIResourceUri {

    private static final long serialVersionUID = -6397024057188453229L;

    protected AAISimpleUri(AAIObjectType type, Object... values) {
        super(type, values);

    }

    protected AAISimpleUri(AAIObjectType type, URI uri) {
        super(type, uri);
    }

    protected AAISimpleUri(AAIObjectType type, UriBuilder builder, Object... values) {
        super(type, builder, values);
    }

    protected AAISimpleUri(AAIResourceUri parentUri, AAIObjectType childType, Object... childValues) {
        super(parentUri, childType, childValues);
    }

    // copy constructor
    protected AAISimpleUri(AAISimpleUri copy) {
        super(copy);
    }

    @Override
    public AAISimpleUri clone() {
        return new AAISimpleUri(this);
    }

    @Override
    public AAISimpleUri relatedTo(AAIObjectType type, String... values) {
        this.internalURI = internalURI.path(relatedTo);
        return new AAISimpleUri(this, type, values);
    }

    @Override
    public AAISimplePluralUri relatedTo(AAIObjectPlurals plural) {
        this.internalURI.path(relatedTo);
        return new AAISimplePluralUri(this, plural);
    }

    @Override
    public AAISimpleUri relatedTo(AAISingleFragment fragment) {
        this.internalURI.path(relatedTo);
        return new AAISimpleUri(this, fragment.get().build(), fragment.get().values());
    }

    @Override
    public AAISimplePluralUri relatedTo(AAIPluralFragment fragment) {
        this.internalURI.path(relatedTo);
        return new AAISimplePluralUri(this, fragment.get().build());
    }

}
