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

import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.RollbackNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.adapter.network.NetworkAdapterClientException;
import org.onap.so.client.adapter.network.NetworkAdapterClientImpl;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterResources {

    @Autowired
    private NetworkAdapterClientImpl networkAdapterClient;

    @Autowired
    private NetworkAdapterObjectMapper networkAdapterObjectMapper;

    public Optional<CreateNetworkResponse> createNetwork(RequestContext requestContext, CloudRegion cloudRegion,
            OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network,
            Map<String, String> userInput, String cloudRegionPo, Customer customer)
            throws NetworkAdapterClientException {

        CreateNetworkRequest createNetworkRequest =
                networkAdapterObjectMapper.createNetworkRequestMapper(requestContext, cloudRegion, orchestrationContext,
                        serviceInstance, l3Network, userInput, cloudRegionPo, customer);
        return Optional.of(networkAdapterClient.createNetwork(createNetworkRequest));
    }

    public Optional<RollbackNetworkResponse> rollbackCreateNetwork(RequestContext requestContext,
            CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance,
            L3Network l3Network, Map<String, String> userInput, String cloudRegionPo,
            CreateNetworkResponse createNetworkResponse) throws NetworkAdapterClientException {

        RollbackNetworkRequest rollbackNetworkRequest = null;
        rollbackNetworkRequest = networkAdapterObjectMapper.createNetworkRollbackRequestMapper(requestContext,
                cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo,
                createNetworkResponse);

        return Optional.of(networkAdapterClient.rollbackNetwork(l3Network.getNetworkId(), rollbackNetworkRequest));
    }

    public Optional<UpdateNetworkResponse> updateNetwork(RequestContext requestContext, CloudRegion cloudRegion,
            OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network,
            Map<String, String> userInput, Customer customer) throws NetworkAdapterClientException {
        UpdateNetworkRequest updateNetworkRequest = networkAdapterObjectMapper.createNetworkUpdateRequestMapper(
                requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, customer);

        return Optional.of(networkAdapterClient.updateNetwork(l3Network.getNetworkId(), updateNetworkRequest));
    }

    public Optional<DeleteNetworkResponse> deleteNetwork(RequestContext requestContext, CloudRegion cloudRegion,
            ServiceInstance serviceInstance, L3Network l3Network) throws NetworkAdapterClientException {

        DeleteNetworkRequest deleteNetworkRequest = networkAdapterObjectMapper
                .deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network);
        return Optional.of(networkAdapterClient.deleteNetwork(l3Network.getNetworkId(), deleteNetworkRequest));
    }

    public Optional<Response> createNetworkAsync(CreateNetworkRequest createNetworkRequest)
            throws NetworkAdapterClientException {

        return Optional.of(networkAdapterClient.createNetworkAsync(createNetworkRequest));
    }

    public Optional<Response> deleteNetworkAsync(DeleteNetworkRequest deleteNetworkRequest)
            throws NetworkAdapterClientException {

        return Optional
                .of(networkAdapterClient.deleteNetworkAsync(deleteNetworkRequest.getNetworkId(), deleteNetworkRequest));
    }

    public Optional<Response> updateNetworkAsync(UpdateNetworkRequest updateNetworkRequest)
            throws NetworkAdapterClientException {

        return Optional
                .of(networkAdapterClient.updateNetworkAsync(updateNetworkRequest.getNetworkId(), updateNetworkRequest));
    }

    public Optional<RollbackNetworkResponse> rollbackCreateNetwork(String networkId,
            RollbackNetworkRequest rollbackNetworkRequest) throws NetworkAdapterClientException {

        return Optional.of(networkAdapterClient.rollbackNetwork(networkId, rollbackNetworkRequest));
    }

    public Optional<UpdateNetworkResponse> updateNetwork(UpdateNetworkRequest updateNetworkRequest)
            throws NetworkAdapterClientException {

        return Optional
                .of(networkAdapterClient.updateNetwork(updateNetworkRequest.getNetworkId(), updateNetworkRequest));
    }

    public Optional<DeleteNetworkResponse> deleteNetwork(DeleteNetworkRequest deleteNetworkRequest)
            throws NetworkAdapterClientException {

        return Optional
                .of(networkAdapterClient.deleteNetwork(deleteNetworkRequest.getNetworkId(), deleteNetworkRequest));
    }
}
