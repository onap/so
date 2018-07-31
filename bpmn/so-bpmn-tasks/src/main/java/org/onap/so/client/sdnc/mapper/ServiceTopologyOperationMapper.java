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

import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.stereotype.Component;

import org.onap.sdnc.apps.client.model.GenericResourceApiParam;
import org.onap.sdnc.apps.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestinformationRequestInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.apps.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiServicerequestinputServiceRequestInput;

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
