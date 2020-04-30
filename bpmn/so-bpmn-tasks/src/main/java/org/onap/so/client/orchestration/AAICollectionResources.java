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

package org.onap.so.client.orchestration;

import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAICollectionResources {
    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public void createCollection(Collection collection) {
        AAIResourceUri networkCollectionURI =
                AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, collection.getId());
        collection.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        org.onap.aai.domain.yang.Collection aaiCollection = aaiObjectMapper.mapCollection(collection);
        injectionHelper.getAaiClient().create(networkCollectionURI, aaiCollection);
    }

    public void updateCollection(Collection collection) {
        AAIResourceUri networkCollectionURI =
                AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, collection.getId());
        org.onap.aai.domain.yang.Collection aaiCollection = aaiObjectMapper.mapCollection(collection);
        injectionHelper.getAaiClient().update(networkCollectionURI, aaiCollection);
    }

    public void deleteCollection(Collection collection) {
        AAIResourceUri instanceGroupUri = AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, collection.getId());
        injectionHelper.getAaiClient().delete(instanceGroupUri);
    }
}
