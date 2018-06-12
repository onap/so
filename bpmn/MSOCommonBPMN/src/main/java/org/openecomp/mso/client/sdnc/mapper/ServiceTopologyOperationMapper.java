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

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.stereotype.Component;


@Component
public class ServiceTopologyOperationMapper{

	static GeneralTopologyObjectMapper generalTopologyObjectMapper = new GeneralTopologyObjectMapper();
	
	public GenericResourceApiServiceOperationInformation reqMapper (SDNCSvcOperation svcOperation,
			SDNCSvcAction svcAction, GenericResourceApiRequestActionEnumeration resourceAction,ServiceInstance serviceInstance, Customer customer, RequestContext requestContext) {

		String sdncReqId = UUID.randomUUID().toString();
		GenericResourceApiServiceOperationInformation servOpInput = new GenericResourceApiServiceOperationInformation();
		GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId);
		GenericResourceApiRequestinformationRequestInformation reqInfo = generalTopologyObjectMapper.buildGenericResourceApiRequestinformationRequestInformation(sdncReqId, resourceAction);
		GenericResourceApiServiceinformationServiceInformation servInfo = generalTopologyObjectMapper.buildServiceInformation(serviceInstance, requestContext, customer, true);
		GenericResourceApiServicerequestinputServiceRequestInput servReqInfo = new GenericResourceApiServicerequestinputServiceRequestInput();
	
		servReqInfo.setServiceInstanceName(serviceInstance.getServiceInstanceId());
		
		servOpInput.setSdncRequestHeader(sdncRequestHeader);
		servOpInput.setRequestInformation(reqInfo);
		servOpInput.setServiceInformation(servInfo);
		servOpInput.setServiceRequestInput(servReqInfo);
		
		if(requestContext.getUserParams()!=null){
			for (Map.Entry<String, String> entry : requestContext.getUserParams().entrySet()) {
				GenericResourceApiServicerequestinputServiceRequestInput serviceRequestInput = new GenericResourceApiServicerequestinputServiceRequestInput(); 
				serviceRequestInput.setServiceInstanceName(serviceInstance.getServiceInstanceId()); 
				GenericResourceApiParam serviceInputParameters = new GenericResourceApiParam(); 
				GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam(); 
				paramItem.setName(entry.getKey()); 
				paramItem.setValue(entry.getValue()); 
				serviceInputParameters.addParamItem(paramItem ); 
				serviceRequestInput.serviceInputParameters(serviceInputParameters); 
				servOpInput.setServiceRequestInput(serviceRequestInput ); 
			}
		}
		return servOpInput;		
	}
}
