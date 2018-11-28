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