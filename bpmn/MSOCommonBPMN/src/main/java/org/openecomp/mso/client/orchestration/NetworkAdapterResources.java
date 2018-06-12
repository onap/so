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

package org.openecomp.mso.client.orchestration;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;

import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.adapter.network.NetworkAdapterClientException;
import org.openecomp.mso.client.adapter.network.NetworkAdapterClientImpl;
import org.openecomp.mso.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterResources {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NetworkAdapterResources.class);
	
	@Autowired
	private NetworkAdapterClientImpl networkAdapterClient;
	
	@Autowired
	private NetworkAdapterObjectMapper networkAdapterObjectMapper;
	
	public Optional<CreateNetworkResponse> createNetwork(RequestContext requestContext, CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network, Map<String, String> userInput, String cloudRegionPo, Customer customer) throws UnsupportedEncodingException, NetworkAdapterClientException {

		CreateNetworkRequest createNetworkRequest = networkAdapterObjectMapper.createNetworkRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, customer);
		return Optional.of(networkAdapterClient.createNetwork(createNetworkRequest));
	}

	public Optional<RollbackNetworkResponse> rollbackCreateNetwork(RequestContext requestContext, CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network, Map<String, String> userInput, String cloudRegionPo, CreateNetworkResponse createNetworkResponse) throws UnsupportedEncodingException, NetworkAdapterClientException {

		RollbackNetworkRequest rollbackNetworkRequest = null;
		rollbackNetworkRequest = networkAdapterObjectMapper.createNetworkRollbackRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, cloudRegionPo, createNetworkResponse);

		return Optional.of(networkAdapterClient.rollbackNetwork(l3Network.getNetworkId(), rollbackNetworkRequest));
	}
	
	public Optional<UpdateNetworkResponse> updateNetwork(RequestContext requestContext, CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, L3Network l3Network, Map<String, String> userInput, Customer customer) throws UnsupportedEncodingException, NetworkAdapterClientException {
		UpdateNetworkRequest updateNetworkRequest = networkAdapterObjectMapper.createNetworkUpdateRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, l3Network, userInput, customer);
		
		return Optional.of(networkAdapterClient.updateNetwork(l3Network.getNetworkId(), updateNetworkRequest));
	}
	
	public Optional<DeleteNetworkResponse> deleteNetwork(RequestContext requestContext, CloudRegion cloudRegion, ServiceInstance serviceInstance, L3Network l3Network, String cloudRegionPo) throws UnsupportedEncodingException, NetworkAdapterClientException {
		
		DeleteNetworkRequest deleteNetworkRequest = networkAdapterObjectMapper.deleteNetworkRequestMapper(requestContext, cloudRegion, serviceInstance, l3Network, cloudRegionPo);
		return Optional.of(networkAdapterClient.deleteNetwork(l3Network.getNetworkId(), deleteNetworkRequest));
	}
}
