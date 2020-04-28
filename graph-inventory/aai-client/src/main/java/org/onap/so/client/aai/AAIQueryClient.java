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

package org.onap.so.client.aai;

import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.CustomQuery;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.GraphInventoryQueryClient;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;

public class AAIQueryClient
        extends GraphInventoryQueryClient<AAIQueryClient, CustomQuery, AAIResultWrapper, AAIObjectType> {

    public AAIQueryClient() {
        super(new AAIClient());
    }

    public AAIQueryClient(AAIVersion version) {
        super(new AAIClient(version));
    }

    @Override
    protected GraphInventoryUri getQueryUri() {
        return AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY);
    }

    @Override
    protected GraphInventoryUri setupQueryParams(GraphInventoryUri uri) {
        return super.setupQueryParams(uri);
    }

    @Override
    public AAIResultWrapper createWrapper(String json) {
        return new AAIResultWrapper(json);
    }

    @Override
    public AAIObjectType createType(String name) {
        return AAIObjectType.fromTypeName(name);
    }

}
