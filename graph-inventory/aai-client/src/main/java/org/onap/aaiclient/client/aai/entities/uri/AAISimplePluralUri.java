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

import jakarta.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.graphinventory.entities.uri.SimplePluralUri;

public class AAISimplePluralUri
        extends SimplePluralUri<AAIPluralResourceUri, AAIResourceUri, AAIObjectPlurals, AAIObjectType>
        implements AAIPluralResourceUri {

    private static final long serialVersionUID = -6397024057188453229L;

    protected AAISimplePluralUri(AAIObjectPlurals type, UriBuilder builder, Object... values) {
        super(type, builder, values);
    }

    protected AAISimplePluralUri(AAIObjectPlurals type) {
        super(type);
    }

    protected AAISimplePluralUri(AAIObjectPlurals type, Object... values) {
        super(type, values);
    }

    protected AAISimplePluralUri(AAIObjectPlurals type, AAIResourceUri uri) {
        super(type, uri);
    }

    protected AAISimplePluralUri(AAIResourceUri parentUri, AAIObjectPlurals childType) {
        super(parentUri, childType);
    }

    protected AAISimplePluralUri(AAISimplePluralUri copy) {
        super(copy);
    }

    @Override
    public AAISimplePluralUri clone() {
        return new AAISimplePluralUri(this);
    }

    @Override
    public AAIObjectPlurals getObjectType() {
        return this.pluralType;
    }

}
