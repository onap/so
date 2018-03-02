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

import java.io.StringWriter;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.sdnc.beans.SDNCRequest;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;

import openecomp.org.mso.workflow.schema.v1.EcompModelInformation;
import openecomp.org.mso.workflow.schema.v1.RequestInformation;
import openecomp.org.mso.workflow.schema.v1.SDNCServiceInstanceRequestData;
import openecomp.org.mso.workflow.schema.v1.ServiceInformation;
import openecomp.org.mso.workflow.schema.v1.ServiceRequestInput;

public class ServiceTopologyOperationRequestMapper extends SDNCRequestMapper{
	
	public ServiceTopologyOperationRequestMapper(Optional<String> msoAction, SDNCSvcOperation svcOperation,
			SDNCSvcAction svcAction, String requestAction) {
		super(msoAction, svcOperation, svcAction, requestAction);
	}

	@Override
	public SDNCRequest reqMapper (ServiceDecomposition serviceDecomp) {
		SDNCRequest req = new SDNCRequest();
		req.setCallbackUrl(serviceDecomp.getCallbackURN());
		if(msoAction.isPresent()){
			req.setMsoAction(msoAction.get()); 
		}
		req.setRequestId(serviceDecomp.getRequest().getSdncRequestId());
		req.setSvcInstanceId(serviceDecomp.getServiceInstance().getInstanceId());
		req.setSvcAction(svcAction); 
		req.setSvcOperation(svcOperation); 
		String reqData ="";
		
		RequestInformation reqInfo = new RequestInformation();
		reqInfo.setRequestAction(requestAction);
		reqInfo.setSource("MSO");
		reqInfo.setRequestId(serviceDecomp.getRequest().getRequestId());
		ServiceInformation servInfo = new ServiceInformation();
		EcompModelInformation emi = new EcompModelInformation();
		emi.setModelInvariantUuid(serviceDecomp.getRequest().getModelInfo().getModelInvariantUuid());
		emi.setModelName(serviceDecomp.getRequest().getModelInfo().getModelName());
		emi.setModelVersion(serviceDecomp.getRequest().getModelInfo().getModelVersion() );
		servInfo.setEcompModelInformation(emi);
		servInfo.setServiceId(serviceDecomp.getServiceInstance().getServiceId());
		servInfo.setSubscriptionServiceType(serviceDecomp.getCustomer().getSubscriptionServiceType());
		servInfo.setServiceInstanceId(serviceDecomp.getServiceInstance().getInstanceName());
		servInfo.setGlobalCustomerId(serviceDecomp.getCustomer().getGlobalSubscriberId());
		ServiceRequestInput servReqInput = new ServiceRequestInput();
		servReqInput.setServiceInstanceName(serviceDecomp.getServiceInstance().getInstanceName());
		SDNCServiceInstanceRequestData sdncSIRD = new SDNCServiceInstanceRequestData();
		sdncSIRD.setRequestInformation(reqInfo);
		sdncSIRD.setServiceInformation(servInfo);
		sdncSIRD.setServiceRequestInput(servReqInput);
		
		try {
            JAXBContext context = JAXBContext.newInstance(SDNCServiceInstanceRequestData.class);

            Marshaller jaxbMarshaller = context.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(sdncSIRD, sw);
            reqData = sw.toString();
            req.setRequestData(reqData);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
				
		return req;
	}
}
