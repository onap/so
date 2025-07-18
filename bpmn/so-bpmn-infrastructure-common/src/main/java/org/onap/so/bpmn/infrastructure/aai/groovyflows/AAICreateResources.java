/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.aai.groovyflows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.onap.aai.domain.yang.OwningEntities;
import org.onap.aai.domain.yang.OwningEntity;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAICreateResources {

    private static final Logger logger = LoggerFactory.getLogger(AAICreateResources.class);

    public void createAAIProject(String projectName, String serviceInstance) {
        AAIResourceUri projectURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().project(projectName));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance));
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        aaiRC.createIfNotExists(projectURI, Optional.empty()).connect(projectURI, serviceInstanceURI);

    }

    public void createAAIOwningEntity(String owningEntityId, String owningEntityName, String serviceInstance) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntity(owningEntityId));
        AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createNodesUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance));
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("owning-entity-name", owningEntityName);
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        aaiRC.createIfNotExists(owningEntityURI, Optional.of(hashMap)).connect(owningEntityURI, serviceInstanceURI);
    }

    public boolean existsOwningEntity(String owningEntityId) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntity(owningEntityId));
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        return aaiRC.exists(owningEntityURI);
    }

    protected OwningEntities getOwningEntityName(String owningEntityName) {

        AAIResourcesClient aaiRC = new AAIResourcesClient();
        return aaiRC.get(OwningEntities.class,
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntities())
                        .queryParam("owning-entity-name", owningEntityName))
                .orElseGet(() -> {
                    logger.debug("No Owning Entity matched by name");
                    return null;
                });

    }

    public Optional<OwningEntity> getOwningEntityNames(String owningEntityName) throws Exception {
        OwningEntity owningEntity = null;
        OwningEntities owningEntities = null;
        owningEntities = getOwningEntityName(owningEntityName);

        if (owningEntities == null) {
            return Optional.empty();
        } else if (owningEntities.getOwningEntity().size() > 1) {
            throw new Exception("Multiple OwningEntities Returned");
        } else {
            owningEntity = owningEntities.getOwningEntity().get(0);
        }
        return Optional.of(owningEntity);
    }

    public void connectOwningEntityandServiceInstance(String owningEntityId, String serviceInstance) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntity(owningEntityId));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance));
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        aaiRC.connect(owningEntityURI, serviceInstanceURI);
    }

    public void createAAIPlatform(String platformName, String vnfId) {
        AAIResourceUri platformURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().platform(platformName));
        AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        aaiRC.createIfNotExists(platformURI, Optional.empty()).connect(platformURI, genericVnfURI);
    }

    public void createAAILineOfBusiness(String lineOfBusiness, String vnfId) {
        AAIResourceUri lineOfBusinessURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().lineOfBusiness(lineOfBusiness));
        AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        aaiRC.createIfNotExists(lineOfBusinessURI, Optional.empty()).connect(lineOfBusinessURI, genericVnfURI);
    }

    public void createAAIServiceInstance(String globalCustomerId, String serviceType, String serviceInstanceId) {
        AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalCustomerId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId));
        AAIResourcesClient aaiRC = new AAIResourcesClient();
        aaiRC.createIfNotExists(serviceInstanceURI, Optional.empty());
    }

}
