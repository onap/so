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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.License;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.stereotype.Component;

import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestinformationRequestInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfinformationVnfInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfrequestinputVnfRequestInput;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfrequestinputVnfrequestinputVnfNetworkInstanceGroupIds;

@Component
public class VnfTopologyOperationRequestMapper {

	static GeneralTopologyObjectMapper generalTopologyObjectMapper = new GeneralTopologyObjectMapper();

	public GenericResourceApiVnfOperationInformation reqMapper(SDNCSvcOperation svcOperation, SDNCSvcAction svcAction,
			GenericResourceApiRequestActionEnumeration requestAction, GenericVnf vnf, ServiceInstance serviceInstance,
			Customer customer, CloudRegion cloudRegion, RequestContext requestContext, boolean homing) {
		String sdncReqId = UUID.randomUUID().toString();
		GenericResourceApiVnfOperationInformation req = new GenericResourceApiVnfOperationInformation();
		GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = generalTopologyObjectMapper
				.buildSdncRequestHeader(svcAction, sdncReqId);
		GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper
				.buildGenericResourceApiRequestinformationRequestInformation(sdncReqId, requestAction);
		GenericResourceApiServiceinformationServiceInformation serviceInformation = generalTopologyObjectMapper
				.buildServiceInformation(serviceInstance, requestContext, customer, true);
		GenericResourceApiVnfinformationVnfInformation vnfInformation = generalTopologyObjectMapper
				.buildVnfInformation(vnf, serviceInstance, true);
		GenericResourceApiVnfrequestinputVnfRequestInput vnfRequestInput = new GenericResourceApiVnfrequestinputVnfRequestInput();

		vnfRequestInput.setTenant(cloudRegion.getTenantId());
		vnfRequestInput.setAicCloudRegion(cloudRegion.getLcpCloudRegionId());
		
		if (StringUtils.isNotBlank(vnf.getVnfName())) {
			vnfRequestInput.setVnfName(vnf.getVnfName());
		}

		req.setRequestInformation(requestInformation);
		req.setSdncRequestHeader(sdncRequestHeader);
		req.setServiceInformation(serviceInformation);
		req.setVnfInformation(vnfInformation);

		GenericResourceApiParam vnfInputParameters = new GenericResourceApiParam();
		if (requestContext.getUserParams() != null) {
			for (Map.Entry<String, String> entry : requestContext.getUserParams().entrySet()) {
				GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
				paramItem.setName(entry.getKey());
				paramItem.setValue(entry.getValue());
				vnfInputParameters.addParamItem(paramItem);
				vnfRequestInput.setVnfInputParameters(vnfInputParameters);
			}
		}
		if (vnf.getCloudParams() != null) {
			for (Map.Entry<String, String> entry : vnf.getCloudParams().entrySet()) {
				GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
				paramItem.setName(entry.getKey());
				paramItem.setValue(entry.getValue());
				vnfInputParameters.addParamItem(paramItem);
			}
		}
		if (homing) {
			License license = vnf.getLicense();
			if (license != null) {
				if(license.getEntitlementPoolUuids() != null && !license.getEntitlementPoolUuids().isEmpty()) {
					String entitlementPoolUuid = license.getEntitlementPoolUuids().get(0);
					GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
					paramItem.setName("entitlement_assignment_group_uuid");
					paramItem.setValue(entitlementPoolUuid);
					vnfInputParameters.addParamItem(paramItem);
				}
				if (license.getLicenseKeyGroupUuids() != null && !license.getLicenseKeyGroupUuids().isEmpty()) {
					String licenseKeyGroupUuid = license.getLicenseKeyGroupUuids().get(0);
					GenericResourceApiParamParam paramItem2 = new GenericResourceApiParamParam();
					paramItem2.setName("license_assignment_group_uuid");
					paramItem2.setValue(licenseKeyGroupUuid);
					vnfInputParameters.addParamItem(paramItem2);
				}
			}
		}
		List<InstanceGroup> instanceGroups = vnf.getInstanceGroups();
		List<GenericResourceApiVnfrequestinputVnfrequestinputVnfNetworkInstanceGroupIds> networkInstanceGroupIdList = new ArrayList<GenericResourceApiVnfrequestinputVnfrequestinputVnfNetworkInstanceGroupIds>();

		for (InstanceGroup instanceGroup : instanceGroups) {
			if (ModelInfoInstanceGroup.TYPE_NETWORK_INSTANCE_GROUP
					.equalsIgnoreCase(instanceGroup.getModelInfoInstanceGroup().getType())) {
				GenericResourceApiVnfrequestinputVnfrequestinputVnfNetworkInstanceGroupIds instanceGroupId = new GenericResourceApiVnfrequestinputVnfrequestinputVnfNetworkInstanceGroupIds();
				instanceGroupId.setVnfNetworkInstanceGroupId(instanceGroup.getId());
				networkInstanceGroupIdList.add(instanceGroupId);
			}
		}

		vnfRequestInput.setVnfNetworkInstanceGroupIds(networkInstanceGroupIdList);
		vnfRequestInput.setVnfInputParameters(vnfInputParameters);
		req.setVnfRequestInput(vnfRequestInput);
		return req;
	}
}
