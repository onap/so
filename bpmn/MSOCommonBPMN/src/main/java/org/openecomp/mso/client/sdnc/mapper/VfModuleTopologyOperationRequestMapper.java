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

package org.openecomp.mso.client.sdnc.mapper;

import java.util.Map;
import java.util.UUID;

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.stereotype.Component;


import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VfModuleTopologyOperationRequestMapper {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, VfModuleTopologyOperationRequestMapper.class);
	static GeneralTopologyObjectMapper generalTopologyObjectMapper = new GeneralTopologyObjectMapper();

	public GenericResourceApiVfModuleOperationInformation reqMapper(SDNCSvcOperation svcOperation,
			SDNCSvcAction svcAction,  VfModule vfModule, VolumeGroup volumeGroup, GenericVnf vnf, ServiceInstance serviceInstance,
			Customer customer, CloudRegion cloudRegion, RequestContext requestContext, String sdncAssignResponse) {
		GenericResourceApiVfModuleOperationInformation req = new GenericResourceApiVfModuleOperationInformation();
		
		boolean includeModelInformation = false;	
		
		GenericResourceApiRequestActionEnumeration requestAction = GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
		GenericResourceApiSvcActionEnumeration genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.ASSIGN;

		if (svcAction.equals(SDNCSvcAction.ACTIVATE)) {
			genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.ACTIVATE;
			requestAction = GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
			includeModelInformation = true;
		} else if (svcAction.equals(SDNCSvcAction.ASSIGN)) {
			genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.ASSIGN;
			requestAction = GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
			includeModelInformation = true;
		} else if (svcAction.equals(SDNCSvcAction.DEACTIVATE)) {
			genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.DEACTIVATE;
			requestAction = GenericResourceApiRequestActionEnumeration.DELETEVFMODULEINSTANCE;
			includeModelInformation = false;
		} else if (svcAction.equals(SDNCSvcAction.DELETE)) {
			genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.DELETE;
			requestAction = GenericResourceApiRequestActionEnumeration.DELETEVFMODULEINSTANCE;
			includeModelInformation = false;			
		} else if (svcAction.equals(SDNCSvcAction.UNASSIGN)) {
			genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.UNASSIGN;
			requestAction = GenericResourceApiRequestActionEnumeration.DELETEVFMODULEINSTANCE;
			includeModelInformation = false;
		}		
		
		String sdncReqId = UUID.randomUUID().toString();
		
		GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper.buildGenericResourceApiRequestinformationRequestInformation(sdncReqId, 
				requestAction);
		GenericResourceApiServiceinformationServiceInformation serviceInformation = generalTopologyObjectMapper.buildServiceInformation(serviceInstance, requestContext, customer, includeModelInformation);
				GenericResourceApiVnfinformationVnfInformation vnfInformation = generalTopologyObjectMapper.buildVnfInformation(vnf, serviceInstance, includeModelInformation);
		GenericResourceApiVfmoduleinformationVfModuleInformation vfModuleInformation = generalTopologyObjectMapper.buildVfModuleInformation(vfModule, vnf, serviceInstance, includeModelInformation);
		GenericResourceApiVfmodulerequestinputVfModuleRequestInput vfModuleRequestInput = buildVfModuleRequestInput(vfModule, volumeGroup, cloudRegion, requestContext);
		GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = buildVfModuleSdncRequestHeader(sdncReqId, genericResourceApiSvcAction);		
		
		req.setRequestInformation(requestInformation);
		req.setSdncRequestHeader(sdncRequestHeader);
		req.setServiceInformation(serviceInformation);
		req.setVnfInformation(vnfInformation);
		req.setVfModuleInformation(vfModuleInformation);		
		req.setVfModuleRequestInput(vfModuleRequestInput);
		
		return req;
	}
	
	private GenericResourceApiVfmodulerequestinputVfModuleRequestInput buildVfModuleRequestInput(VfModule vfModule, VolumeGroup volumeGroup, CloudRegion cloudRegion, RequestContext requestContext) {	
		GenericResourceApiVfmodulerequestinputVfModuleRequestInput vfModuleRequestInput = new GenericResourceApiVfmodulerequestinputVfModuleRequestInput();
		if (cloudRegion != null) {
			vfModuleRequestInput.setTenant(cloudRegion.getTenantId());
			vfModuleRequestInput.setAicCloudRegion(cloudRegion.getLcpCloudRegionId());
		}
		if (vfModule.getVfModuleName() != null || !vfModule.getVfModuleName().equals("")) {
			vfModuleRequestInput.setVfModuleName(vfModule.getVfModuleName());
		}
		GenericResourceApiParam vfModuleInputParameters = new GenericResourceApiParam();
		
		if (requestContext != null && requestContext.getUserParams() != null) {
			for (Map.Entry<String, String> entry : requestContext.getUserParams().entrySet()) {
				GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
				paramItem.setName(entry.getKey()); 
				paramItem.setValue(entry.getValue()); 
				vfModuleInputParameters.addParamItem(paramItem);
			}
		}
		
		if (volumeGroup != null) {
			GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
			paramItem.setName("volume-group-id");
			paramItem.setValue(volumeGroup.getVolumeGroupId());
			vfModuleInputParameters.addParamItem(paramItem);
		}
		vfModuleRequestInput.setVfModuleInputParameters(vfModuleInputParameters);
		
		return vfModuleRequestInput;
	}
	
	private GenericResourceApiSdncrequestheaderSdncRequestHeader buildVfModuleSdncRequestHeader(String sdncReqId, GenericResourceApiSvcActionEnumeration svcAction) {
		GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = new GenericResourceApiSdncrequestheaderSdncRequestHeader();	
		
		sdncRequestHeader.setSvcRequestId(sdncReqId);
		sdncRequestHeader.setSvcAction(svcAction);
		
		return sdncRequestHeader;
	}
	
	public String buildObjectPath(String sdncAssignResponse) {
		String objectPath = null;
		if (sdncAssignResponse != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				GenericResourceApiVfModuleResponseInformation assignResponseInfo = mapper.readValue(sdncAssignResponse, GenericResourceApiVfModuleResponseInformation.class);				
				objectPath = assignResponseInfo.getVfModuleResponseInformation().getObjectPath();
			} catch (Exception e) {				
			    msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, e.getMessage(), "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e.getMessage());
			}
		}
		return objectPath;
	}
}
