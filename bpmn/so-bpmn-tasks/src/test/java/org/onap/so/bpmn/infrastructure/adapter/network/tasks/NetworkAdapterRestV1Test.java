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

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;

public class NetworkAdapterRestV1Test {

	@Test
	public void testUnmarshalXml() throws IOException, JAXBException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><createNetworkResponse><messageId>ec37c121-e3ec-4697-8adf-2d7dca7044fc</messageId><networkCreated>true</networkCreated><networkFqdn>someNetworkFqdn</networkFqdn><networkId>991ec7bf-c9c4-4ac1-bb9c-4b61645bddb3</networkId><networkStackId>someStackId</networkStackId><neutronNetworkId>9c47521a-2916-4018-b2bc-71ab767497e3</neutronNetworkId><rollback><cloudId>someCloudId</cloudId><modelCustomizationUuid>b7171cdd-8b05-459b-80ef-2093150e8983</modelCustomizationUuid><msoRequest><requestId>90b32315-176e-4dab-bcf1-80eb97a1c4f4</requestId><serviceInstanceId>71e7db22-7907-4d78-8fcc-8d89d28e90be</serviceInstanceId></msoRequest><networkCreated>true</networkCreated><networkStackId>someStackId</networkStackId><networkType>SomeNetworkType</networkType><neutronNetworkId>9c47521a-2916-4018-b2bc-71ab767497e3</neutronNetworkId><tenantId>b60da4f71c1d4b35b8113d4eca6deaa1</tenantId></rollback><subnetMap><entry><key>6b381fa9-48ce-4e16-9978-d75309565bb6</key><value>bc1d5537-860b-4894-8eba-6faff41e648c</value></entry></subnetMap></createNetworkResponse>";
		CreateNetworkResponse response = (CreateNetworkResponse) new NetworkAdapterRestV1().unmarshalXml(xml, CreateNetworkResponse.class);
		String returnedXml = response.toXmlString();
		System.out.println(returnedXml);
	}
}
