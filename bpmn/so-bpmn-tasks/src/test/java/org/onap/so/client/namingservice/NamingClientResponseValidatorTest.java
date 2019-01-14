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

package org.onap.so.client.namingservice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onap.namingservice.model.NameGenDeleteResponse;
import org.onap.namingservice.model.NameGenResponse;
import org.onap.namingservice.model.Respelement;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.client.exception.BadResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class NamingClientResponseValidatorTest extends TestDataSetup {
	
	private NamingClientResponseValidator responseValidator = new NamingClientResponseValidator();	
	private String instanceGroupName = "generatedInstanceGroupName";
	
	@Test
	public void validateNameGenResponseSuccessTest() throws BadResponseException {
		NameGenResponse name = new NameGenResponse();
		Respelement respElement = new Respelement();
		respElement.setResourceName("instance-group-name");
		respElement.setResourceValue(instanceGroupName);
		List<Respelement> respList = new ArrayList<Respelement>();
		respList.add(respElement);
		name.setElements(respList);		
		ResponseEntity<NameGenResponse> resp = new ResponseEntity<>(name, null, HttpStatus.OK);		
		
		String actual = responseValidator.validateNameGenResponse(resp);
		
		assertEquals(actual, "generatedInstanceGroupName");
	}
	
	@Test
	public void validateNameGenResponseNoNameGeneratedTest() throws BadResponseException {
		NameGenResponse name = new NameGenResponse();
		Respelement respElement = new Respelement();
		respElement.setResourceName("instance-group");
		respElement.setResourceValue(instanceGroupName);
		List<Respelement> respList = new ArrayList<Respelement>();
		respList.add(respElement);
		name.setElements(respList);		
		ResponseEntity<NameGenResponse> resp = new ResponseEntity<>(name, null, HttpStatus.OK);		
		
		String actual = responseValidator.validateNameGenResponse(resp);
		
		assertEquals(actual, "");
	}
	
	@Test
	public void validateNameGenResponseBadStatusTest() throws BadResponseException {
		NameGenResponse name = new NameGenResponse();
			
		ResponseEntity<NameGenResponse> resp = new ResponseEntity<>(name, null, HttpStatus.NOT_FOUND);		
		
		expectedException.expect(BadResponseException.class);
		responseValidator.validateNameGenResponse(resp);		
	}
	
	@Test
	public void validateNameGenDeleteResponseSuccessTest() throws BadResponseException {
		NameGenDeleteResponse name = new NameGenDeleteResponse();		
		ResponseEntity<NameGenDeleteResponse> resp = new ResponseEntity<>(name, null, HttpStatus.OK);		
		
		String actual = responseValidator.validateNameGenDeleteResponse(resp);
		
		assertEquals(actual, "");
	}	
	
	@Test
	public void validateNameGenDeleteResponseBadStatusTest() throws BadResponseException {
		NameGenDeleteResponse name = new NameGenDeleteResponse();
			
		ResponseEntity<NameGenDeleteResponse> resp = new ResponseEntity<>(name, null, HttpStatus.NOT_FOUND);		
		
		expectedException.expect(BadResponseException.class);
		responseValidator.validateNameGenDeleteResponse(resp);		
	}
	
}
