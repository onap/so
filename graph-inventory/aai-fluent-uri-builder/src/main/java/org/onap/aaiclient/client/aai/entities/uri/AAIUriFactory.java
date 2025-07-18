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

package org.onap.aaiclient.client.aai.entities.uri;

import java.net.URI;
import org.onap.aaiclient.client.aai.AAIObjectName;
import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.aai.AAIObjectType;

public class AAIUriFactory {


    public static final AAIFluentTypeReverseLookup reverseLookup = new AAIFluentTypeReverseLookup();

    /**
     * values are filled into the URI template specified in {@link AAIObjectType} in order <br>
     *
     * @param type
     * @param values
     * @return
     */
    public static AAIResourceUri createResourceUri(AAIObjectType type, Object... values) {
        return new AAISimpleUri(type, values);
    }

    public static AAIResourceUri createResourceUri(AAIFluentSingleType uri) {
        return new AAISimpleUri(uri.build(), uri.values());
    }

    protected static NodesSingleUri createNodesUri(AAIObjectType type, Object... values) {
        return new NodesSingleUri(type, values);
    }

    public static NodesSingleUri createNodesUri(AAISingleFragment fragment) {
        return new NodesSingleUri(fragment.get().build(), fragment.get().values());
    }

    public static NodesPluralUri createNodesUri(AAIPluralFragment fragment) {
        return new NodesPluralUri(fragment.get().build());

    }

    /**
     * This method should only be used to wrap a URI retrieved from A&AI contained within an object response
     *
     * @param type
     * @param uri
     * @return
     */
    public static AAISimpleUri createResourceFromExistingURI(AAIObjectName name, URI uri) {
        AAIObjectType type = reverseLookup.fromName(name.typeName(), uri.toString());
        return new AAISimpleUri(type, uri);
    }


    /**
     * creates an AAIResourceUri from a parentUri
     *
     * @param parentUri
     * @param childType
     * @param childValues
     * @return
     */
    public static AAISimpleUri createResourceFromParentURI(AAIResourceUri parentUri, AAISingleFragment fragment) {

        return new AAISimpleUri(parentUri, fragment.get().build(), fragment.get().values());
    }

    public static AAISimplePluralUri createResourceFromParentURI(AAIResourceUri parentUri, AAIPluralFragment fragment) {

        return new AAISimplePluralUri(parentUri, fragment.get().build());
    }

    public static AAISimplePluralUri createResourceUri(AAIFluentPluralType uri) {

        return new AAISimplePluralUri(uri.build(), uri.values());

    }

    /**
     * Creates a uri for a plural type e.g. /cloud-infrastructure/pservers
     *
     * @param type
     * @return
     */
    public static AAISimplePluralUri createResourceUri(AAIObjectPlurals type) {

        return new AAISimplePluralUri(type);

    }

    /**
     * Creates a uri for a plural type with values e.g. /cloud-infrastructure/pservers
     *
     * @param type
     * @return
     */
    public static AAISimplePluralUri createResourceUri(AAIObjectPlurals type, Object... values) {

        return new AAISimplePluralUri(type, values);

    }
}
