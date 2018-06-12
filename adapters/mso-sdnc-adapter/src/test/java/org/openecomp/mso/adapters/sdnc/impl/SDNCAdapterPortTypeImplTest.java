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

package org.openecomp.mso.adapters.sdnc.impl;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.adapters.sdnc.RequestHeader;
import org.openecomp.mso.adapters.sdnc.SDNCAdapterApplication;
import org.openecomp.mso.adapters.sdnc.SDNCAdapterPortType;
import org.openecomp.mso.adapters.sdnc.SDNCAdapterRequest;
import org.openecomp.mso.adapters.sdnc.SDNCAdapterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SDNCAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","non-async"})
public class SDNCAdapterPortTypeImplTest {

	@Autowired
	private SDNCAdapterPortType sdncAdapter;
	
	
	SDNCAdapterRequest sdncAdapterRequest;
	
	public void setupTestEntities() throws ParserConfigurationException, SAXException, IOException   {
		buildTestRequest();
	}	

	private void buildTestRequest() throws ParserConfigurationException, SAXException, IOException {	
		sdncAdapterRequest= new SDNCAdapterRequest();
		File fXmlFile = new File("src/test/resources/sdncTestPayload.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);	
		System.out.println(doc.toString());
		sdncAdapterRequest.setRequestData(doc);
		RequestHeader requestHeader = new RequestHeader();
		requestHeader.setCallbackUrl("http://localhost:9090/callback");
		requestHeader.setMsoAction("gammainternet");
		requestHeader.setRequestId("testReqId");
		requestHeader.setSvcAction("assign");
		requestHeader.setSvcInstanceId("servInstanceId");
		requestHeader.setSvcOperation("svc-topology-operation");
		sdncAdapterRequest.setRequestHeader(requestHeader );
	}
	
	
	
	@Test
	public void sendRequest() throws ParserConfigurationException, SAXException, IOException  {
		// Given
		setupTestEntities();
		
		// When
		SDNCAdapterResponse response = sdncAdapter.sdncAdapter(sdncAdapterRequest);
		if(response ==null)
			 fail("Null infraRequest");
		
		// Then
		//assertThat(infraRequest, sameBeanAs(testRequest).ignoring("requestBody").ignoring("endTime").ignoring("startTime").ignoring("modifyTime"));		
	}
}
