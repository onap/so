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

package org.onap.so.client.sdnc.mapper;

import java.util.Map;
import java.util.UUID;

import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.stereotype.Component;

import org.onap.sdnc.apps.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiNetworkinformationNetworkInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiNetworkrequestinputNetworkRequestInput;
import org.onap.sdnc.apps.client.model.GenericResourceApiParam;
import org.onap.sdnc.apps.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestinformationRequestInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.apps.client.model.GenericResourceApiServiceinformationServiceInformation;

/**
 * Mapper creating SDNC request
 *
 */
@Component
public class NetworkTopologyOperationRequestMapper {
	
	static GeneralTopologyObjectMapper generalTopologyObjectMapper = new GeneralTopologyObjectMapper();

	public GenericResourceApiNetworkOperationInformation reqMapper(SDNCSvcOperation svcOperation,
			SDNCSvcAction svcAction, GenericResourceApiRequestActionEnumeration reqAction, L3Network network, ServiceInstance serviceInstance,
			Customer customer, RequestContext requestContext, CloudRegion cloudRegion) {
		GenericResourceApiNetworkOperationInformation req = new GenericResourceApiNetworkOperationInformation();
		String sdncReqId = UUID.randomUUID().toString();
		GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId);
		GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper.buildGenericResourceApiRequestinformationRequestInformation(sdncReqId, reqAction);
		GenericResourceApiServiceinformationServiceInformation serviceInformation = generalTopologyObjectMapper.buildServiceInformation(serviceInstance, requestContext, customer, true);
		GenericResourceApiNetworkinformationNetworkInformation networkInformation = generalTopologyObjectMapper.buildNetworkInformation(network);
		GenericResourceApiNetworkrequestinputNetworkRequestInput networkRequestInput = buildNetworkRequestInput(network, serviceInstance, cloudRegion);

		req.setRequestInformation(requestInformation);
		req.setSdncRequestHeader(sdncRequestHeader);
		req.setServiceInformation(serviceInformation);
		req.setNetworkInformation(networkInformation);

		if (requestContext.getUserParams() != null) {
			for (Map.Entry<String, String> entry : requestContext.getUserParams().entrySet()) {
				GenericResourceApiParam networkInputParameters = new GenericResourceApiParam();
				GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
				paramItem.setName(entry.getKey()); 
				paramItem.setValue(entry.getValue()); 
				networkInputParameters.addParamItem(paramItem);
				networkRequestInput.setNetworkInputParameters(networkInputParameters);
			}
		}

		req.setNetworkRequestInput(networkRequestInput);
		return req;
	}
	/*
	 * Private helper to build GenericResourceApiNetworkrequestinputNetworkRequestInput
	 */
	private GenericResourceApiNetworkrequestinputNetworkRequestInput buildNetworkRequestInput(L3Network network, ServiceInstance serviceInstance, CloudRegion cloudRegion){
		GenericResourceApiNetworkrequestinputNetworkRequestInput networkRequestInput = new GenericResourceApiNetworkrequestinputNetworkRequestInput();
		networkRequestInput.setTenant(cloudRegion.getTenantId());
		networkRequestInput.setAicCloudRegion(cloudRegion.getLcpCloudRegionId());
		if (network.getNetworkName() != null && !network.getNetworkName().equals("")) {
			networkRequestInput.setNetworkName(network.getNetworkName());
		}
		if (serviceInstance.getCollection() != null && serviceInstance.getCollection().getInstanceGroup() != null){
			//set only for network created as part of the collection/instance since 1806
			networkRequestInput.setNetworkInstanceGroupId(serviceInstance.getCollection().getInstanceGroup().getId());
		}
		return networkRequestInput;
	}
}
