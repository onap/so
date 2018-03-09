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

package org.openecomp.mso.camunda.tests;



import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;

import org.openecomp.mso.apihandler.camundabeans.CamundaInput;
import org.openecomp.mso.apihandler.camundabeans.CamundaRequest;
import org.openecomp.mso.apihandler.common.CommonConstants;

/**
 * This class implements test methods of Camunda Beans.
 * 
 *
 */
public class CamundaRequestTest {

	@Test
	public final void testSerialization() throws JsonGenerationException,
			JsonMappingException, IOException {
		CamundaRequest camundaRequest = new CamundaRequest();
		CamundaInput camundaInput = new CamundaInput();
		CamundaInput host = new CamundaInput();
		CamundaInput schema = new CamundaInput();
		CamundaInput reqid = new CamundaInput();
		CamundaInput svcid = new CamundaInput();
		CamundaInput timeout = new CamundaInput();
		camundaInput
				.setValue("<aetgt:CreateTenantRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns5=\"http://org.openecomp/mso/request/types/v1\"> <msoservtypes:request-information xmlns:msoservtypes=\"http://org.openecomp/mso/request/types/v1\"><msoservtypes:request-id>155415ab-b4a7-4382-b4c6-d17d950604</msoservtypes:request-id><msoservtypes:request-action>Layer3ServiceActivateRequest</msoservtypes:request-action><msoservtypes:source>OMX</msoservtypes:source><msoservtypes:notification-url>https://localhost:22443/Services/com/cingular/csi/sdn/SendManagedNetworkStatusNotification.jws</msoservtypes:notification-url><msoservtypes:order-number>5051563</msoservtypes:order-number><msoservtypes:order-version>1</msoservtypes:order-version> </msoservtypes:request-information> <msoservtypes:service-information xmlns:msoservtypes=\"http://org.openecomp/mso/request/types/v1\"><msoservtypes:service-type>SDN-ETHERNET-INTERNET</msoservtypes:service-type><msoservtypes:service-instance-id>HI/VLXM/950604//SW_INTERNET</msoservtypes:service-instance-id><msoservtypes:subscriber-name>SubName01</msoservtypes:subscriber-name> </msoservtypes:service-information> <sdncadapterworkflow:cloudId>MTSNJA4LCP1</sdncadapterworkflow:cloudId> </aetgt:CreateTenantRequest>");
		camundaRequest.setServiceInput(camundaInput);
		host.setValue("localhost");
		camundaRequest.setHost(host);
		schema.setValue("v1");
		camundaRequest.setSchema(schema);
		reqid.setValue("reqid123");
		camundaRequest.setReqid(reqid);
		svcid.setValue("svcid123");
		camundaRequest.setSvcid(svcid);
		timeout.setValue("");
		camundaRequest.setTimeout(timeout);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);

		String json = mapper.writeValueAsString(camundaRequest);
		System.out.println(json);
		assertEquals(
				"{\"variables\":{\""+CommonConstants.CAMUNDA_SERVICE_INPUT+"\":{\"value\":\"<aetgt:CreateTenantRequest xmlns:aetgt=\\\"http://org.openecomp/mso/workflow/schema/v1\\\" xmlns:sdncadapterworkflow=\\\"http://org.openecomp/mso/workflow/schema/v1\\\" xmlns:ns5=\\\"http://org.openecomp/mso/request/types/v1\\\"> <msoservtypes:request-information xmlns:msoservtypes=\\\"http://org.openecomp/mso/request/types/v1\\\"><msoservtypes:request-id>155415ab-b4a7-4382-b4c6-d17d950604</msoservtypes:request-id><msoservtypes:request-action>Layer3ServiceActivateRequest</msoservtypes:request-action><msoservtypes:source>OMX</msoservtypes:source><msoservtypes:notification-url>https://localhost:22443/Services/com/cingular/csi/sdn/SendManagedNetworkStatusNotification.jws</msoservtypes:notification-url><msoservtypes:order-number>5051563</msoservtypes:order-number><msoservtypes:order-version>1</msoservtypes:order-version> </msoservtypes:request-information> <msoservtypes:service-information xmlns:msoservtypes=\\\"http://org.openecomp/mso/request/types/v1\\\"><msoservtypes:service-type>SDN-ETHERNET-INTERNET</msoservtypes:service-type><msoservtypes:service-instance-id>HI/VLXM/950604//SW_INTERNET</msoservtypes:service-instance-id><msoservtypes:subscriber-name>SubName01</msoservtypes:subscriber-name> </msoservtypes:service-information> <sdncadapterworkflow:cloudId>MTSNJA4LCP1</sdncadapterworkflow:cloudId> </aetgt:CreateTenantRequest>\",\"type\":\"String\"}" +
				",\"host\":{\"value\":\"localhost\",\"type\":\"String\"},\"mso-schema-version\":{\"value\":\"v1\",\"type\":\"String\"},\"mso-request-id\":{\"value\":\"reqid123\",\"type\":\"String\"},\"mso-service-instance-id\":{\"value\":\"svcid123\",\"type\":\"String\"},\"mso-service-request-timeout\":{\"value\":\"\",\"type\":\"String\"}}}",
				json);

	}

}
