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

package org.onap.so.bpmn.infrastructure.aai;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAICreateResources extends AAIResource {

    private static final Logger logger = LoggerFactory.getLogger(AAICreateResources.class);

    public void createAAIProject(String projectName, String serviceInstance) {
        AAIResourceUri projectURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().project(projectName));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance));
        getAaiClient().createIfNotExists(projectURI, Optional.empty()).connect(projectURI, serviceInstanceURI);

    }

    public void createAAIOwningEntity(String owningEntityId, String owningEntityName, String serviceInstance) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntity(owningEntityId));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance));
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("owning-entity-name", owningEntityName);
        getAaiClient().createIfNotExists(owningEntityURI, Optional.of(hashMap)).connect(owningEntityURI,
                serviceInstanceURI);
    }

    public boolean existsOwningEntity(String owningEntityId) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntity(owningEntityId));
        return getAaiClient().exists(owningEntityURI);
    }

    public void connectOwningEntityandServiceInstance(String owningEntityId, String serviceInstance) {
        AAIResourceUri owningEntityURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().owningEntity(owningEntityId));
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance));
        getAaiClient().connect(owningEntityURI, serviceInstanceURI);
    }

    public void createAAIPlatform(String platformName, String vnfId) {
        AAIResourceUri platformURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().platform(platformName));
        AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        getAaiClient().createIfNotExists(platformURI, Optional.empty()).connect(platformURI, genericVnfURI);
    }

    public void createAAILineOfBusiness(String lineOfBusiness, String vnfId) {
        AAIResourceUri lineOfBusinessURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().lineOfBusiness(lineOfBusiness));
        AAIResourceUri genericVnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        getAaiClient().createIfNotExists(lineOfBusinessURI, Optional.empty()).connect(lineOfBusinessURI, genericVnfURI);
    }

    public void createAAIServiceInstance(String globalCustomerId, String serviceType, String serviceInstanceId) {
        AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalCustomerId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId));
        getAaiClient().createIfNotExists(serviceInstanceURI, Optional.empty());
    }

    public Optional<GenericVnf> getVnfInstance(String vnfId) {
        try {
            AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
            AAIResultWrapper aaiResponse = getAaiClient().get(vnfURI);
            Optional<GenericVnf> vnf = aaiResponse.asBean(GenericVnf.class);
            return vnf;
        } catch (Exception ex) {
            logger.error("Exception in getVnfInstance", ex);
            return Optional.empty();
        }
    }

}
