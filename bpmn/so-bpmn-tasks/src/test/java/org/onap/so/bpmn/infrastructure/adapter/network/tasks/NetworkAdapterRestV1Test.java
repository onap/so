/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkError;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.exception.MapperException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

public class NetworkAdapterRestV1Test extends BaseTaskTest {
	
	@InjectMocks
	NetworkAdapterRestV1 networkAdapterRestV1Tasks = new NetworkAdapterRestV1();
	@Mock
	ExceptionBuilder exceptionBuilder = new ExceptionBuilder();

	@Before
	public void setup(){    	
    	delegateExecution = new DelegateExecutionFake();  	
	}
	
	@Test
	public void testUnmarshalXml() throws IOException, JAXBException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><createNetworkResponse><messageId>ec37c121-e3ec-4697-8adf-2d7dca7044fc</messageId><networkCreated>true</networkCreated><networkFqdn>someNetworkFqdn</networkFqdn><networkId>991ec7bf-c9c4-4ac1-bb9c-4b61645bddb3</networkId><networkStackId>someStackId</networkStackId><neutronNetworkId>9c47521a-2916-4018-b2bc-71ab767497e3</neutronNetworkId><rollback><cloudId>someCloudId</cloudId><modelCustomizationUuid>b7171cdd-8b05-459b-80ef-2093150e8983</modelCustomizationUuid><msoRequest><requestId>90b32315-176e-4dab-bcf1-80eb97a1c4f4</requestId><serviceInstanceId>71e7db22-7907-4d78-8fcc-8d89d28e90be</serviceInstanceId></msoRequest><networkCreated>true</networkCreated><networkStackId>someStackId</networkStackId><networkType>SomeNetworkType</networkType><neutronNetworkId>9c47521a-2916-4018-b2bc-71ab767497e3</neutronNetworkId><tenantId>b60da4f71c1d4b35b8113d4eca6deaa1</tenantId></rollback><subnetMap><entry><key>6b381fa9-48ce-4e16-9978-d75309565bb6</key><value>bc1d5537-860b-4894-8eba-6faff41e648c</value></entry></subnetMap></createNetworkResponse>";
		CreateNetworkResponse response = (CreateNetworkResponse) new NetworkAdapterRestV1().unmarshalXml(xml, CreateNetworkResponse.class);
		String returnedXml = response.toXmlString();
		System.out.println(returnedXml);
	}
	
	@Test
	public void testUnmarshalXmlUpdate() throws IOException, JAXBException {
		UpdateNetworkResponse expectedResponse = new UpdateNetworkResponse();
		expectedResponse.setMessageId("ec100bcc-2659-4aa4-b4d8-3255715c2a51");
		expectedResponse.setNetworkId("80de31e3-cc78-4111-a9d3-5b92bf0a39eb");
		Map<String,String>subnetMap = new HashMap<String,String>();
		subnetMap.put("95cd8437-25f1-4238-8720-cbfe7fa81476", "d8d16606-5d01-4822-b160-9a0d257303e0");
		expectedResponse.setSubnetMap(subnetMap);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><updateNetworkResponse><messageId>ec100bcc-2659-4aa4-b4d8-3255715c2a51</messageId><networkId>80de31e3-cc78-4111-a9d3-5b92bf0a39eb</networkId><subnetMap><entry><key>95cd8437-25f1-4238-8720-cbfe7fa81476</key><value>d8d16606-5d01-4822-b160-9a0d257303e0</value></entry></subnetMap></updateNetworkResponse>";
		UpdateNetworkResponse response = (UpdateNetworkResponse) new NetworkAdapterRestV1().unmarshalXml(xml, UpdateNetworkResponse.class);
		assertThat(expectedResponse, sameBeanAs(response));
	}
	
	@Test
	public void processCallbackTest() throws MapperException, BadResponseException, IOException{
		UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
		UpdateNetworkResponse updateNetworkResponse = new UpdateNetworkResponse();
		updateNetworkResponse.setMessageId("messageId");
		updateNetworkResponse.setNetworkId("networkId");
		delegateExecution.setVariable("networkAdapterRequest", updateNetworkRequest);
		delegateExecution.setVariable("NetworkAResponse_MESSAGE", updateNetworkResponse.toXmlString());
		
		networkAdapterRestV1Tasks.processCallback(delegateExecution);
		
		assertThat(updateNetworkResponse,sameBeanAs(delegateExecution.getVariable("updateNetworkResponse")));
	}
	
	@Test
	public void processCallbackErrorTest() throws MapperException, BadResponseException, IOException{
		UpdateNetworkRequest updateNetworkRequest = new UpdateNetworkRequest();
		UpdateNetworkError updateNetworkResponse = new UpdateNetworkError();
		updateNetworkResponse.setMessageId("messageId");
		updateNetworkResponse.setMessage("test error message");
		delegateExecution.setVariable("networkAdapterRequest", updateNetworkRequest);
		delegateExecution.setVariable("NetworkAResponse_MESSAGE", updateNetworkResponse.toXmlString());
		
		doThrow(new BpmnError("MSOWorkflowException")).when(exceptionBuilder).buildAndThrowWorkflowException(any(DelegateExecution.class), anyInt(), any(String.class));
		try {
			networkAdapterRestV1Tasks.processCallback(delegateExecution);
		} 
		catch (BpmnError be){
			assertEquals("MSOWorkflowException",be.getErrorCode());
		}
		assertNull(delegateExecution.getVariable("updateNetworkResponse"));
		verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(any(DelegateExecution.class), eq(7000), eq("test error message"));
	}
}
