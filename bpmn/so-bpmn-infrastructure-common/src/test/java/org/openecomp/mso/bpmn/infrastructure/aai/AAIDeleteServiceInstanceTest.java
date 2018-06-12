package org.openecomp.mso.bpmn.infrastructure.aai;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;

public class AAIDeleteServiceInstanceTest extends BaseTaskTest {
	private AAIDeleteServiceInstance aaiDeleteServiceInstance;
	@Mock
	private DelegateExecution execution;
	
	@Mock
	private AAIResourcesClient aaiResourcesClient;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		
		aaiDeleteServiceInstance = new AAIDeleteServiceInstance();
		aaiDeleteServiceInstance.setAaiClient(aaiResourcesClient);
	}
	
	@Test
	public void executeTest() throws Exception {
		doReturn("serviceInstanceId").when(execution).getVariable("serviceInstanceId");
		doNothing().when(aaiResourcesClient).delete(isA(AAIResourceUri.class));
		doNothing().when(execution).setVariable(isA(String.class), isA(Boolean.class));
		
		aaiDeleteServiceInstance.execute(execution);
		
		verify(execution, times(1)).getVariable("serviceInstanceId");
		verify(aaiResourcesClient, times(1)).delete(isA(AAIResourceUri.class));
		verify(execution, times(1)).setVariable("GENDS_SuccessIndicator", true);
	}
	
	@Test
	public void executeExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doReturn("testProcessKey").when(execution).getVariable("testProcessKey");
		doReturn("serviceInstanceId").when(execution).getVariable("serviceInstanceId");
		doThrow(Exception.class).when(aaiResourcesClient).delete(isA(AAIResourceUri.class));
		
		aaiDeleteServiceInstance.execute(execution);
	}
}
