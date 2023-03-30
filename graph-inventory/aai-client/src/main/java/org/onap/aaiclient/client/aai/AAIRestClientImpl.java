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

package org.onap.aaiclient.client.aai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.entities.CustomQuery;
import org.onap.aaiclient.client.aai.entities.Results;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.Format;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIRestClientImpl implements AAIRestClientI {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String PSERVER_VNF_QUERY = "pservers-fromVnf";

    @Override
    public List<Pserver> getPhysicalServerByVnfId(String vnfId) throws IOException {
        List<AAIResourceUri> startNodes = new ArrayList<>();
        startNodes.add(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)));
        String jsonInput = new AAIQueryClient().query(Format.RESOURCE, new CustomQuery(startNodes, PSERVER_VNF_QUERY));

        return this.getListOfPservers(jsonInput);

    }

    protected List<Pserver> getListOfPservers(String jsonInput) throws IOException {
        ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
        Results<Map<String, Pserver>> resultsFromJson =
                mapper.readValue(jsonInput, new TypeReference<Results<Map<String, Pserver>>>() {});
        List<Pserver> results = new ArrayList<>();
        for (Map<String, Pserver> m : resultsFromJson.getResult()) {
            results.add(m.get("pserver"));
        }
        return results;
    }

    @Override
    public void updateMaintenceFlagVnfId(String vnfId, boolean inMaint) {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setInMaint(inMaint);
        new AAIResourcesClient()
                .update(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)), genericVnf);

    }

    @Override
    public GenericVnf getVnfByName(String vnfId) {
        return new AAIResourcesClient().get(GenericVnf.class,
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))).orElse(null);
    }

    @Override
    public Optional<Pnf> getPnfByName(String pnfId) {
        Response response = new AAIResourcesClient()
                .getFullResponse(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnfId)));
        if (response.getStatus() != 200) {
            return Optional.empty();
        } else {
            return Optional.of(response.readEntity(Pnf.class));
        }
    }

    @Override
    public void createPnf(String pnfId, Pnf pnf) {
        new AAIResourcesClient().createIfNotExists(
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnfId)), Optional.of(pnf));
    }

    @Override
    public void updatePnf(String pnfId, Pnf pnf) {
        new AAIResourcesClient().update(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnfId)),
                pnf);
    }

    @Override
    public Optional<ServiceInstance> getServiceInstanceById(String serviceInstanceId, String serviceType,
            String globalSubscriberId) {
        Response response = new AAIResourcesClient().getFullResponse(
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId)
                        .serviceSubscription(serviceType).serviceInstance(serviceInstanceId)));
        return Optional.ofNullable(response.readEntity(ServiceInstance.class));
    }

    public Optional<ServiceInstance> getServiceInstanceByIdWithDepth(String serviceInstanceId, String serviceType,
            String globalSubscriberId) {
        Response response =
                new AAIResourcesClient()
                        .getFullResponse(AAIUriFactory
                                .createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId)
                                        .serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
                                .depth(Depth.ALL));
        return Optional.ofNullable(response.readEntity(ServiceInstance.class));
    }

    @Override
    public void updateServiceInstance(String serviceInstanceId, String serviceType, String globalSubscriberId,
            ServiceInstance serviceInstance) {
        try {
            new AAIResourcesClient().update(AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId)),
                    serviceInstance);
        } catch (Throwable ex) {
            log.error("Exception happened while updating ServiceInstance, Exception: {}", ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
    }
}
