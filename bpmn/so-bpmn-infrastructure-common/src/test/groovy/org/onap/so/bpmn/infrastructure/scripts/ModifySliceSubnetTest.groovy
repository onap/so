package org.onap.so.bpmn.infrastructure.scripts

import static org.junit.Assert.*

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import static org.mockito.Mockito.*

class ModifySliceSubnetTest extends MsoGroovyTest {
	@Before
	void init() throws IOException {
                mockExecution = setupMock("ModifySliceSubnet")
                client = mock(org.onap.aaiclient.client.aai.AAIResourcesClient.class)
	}
	@Captor
	static ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class)

	@Test
	public void testPreProcessRequest() {
		when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
				"serviceInstanceID": "NSSI-C-001-HDBNJ-NSSMF-01-A-ZX ",
				"networkType": "an/cn/tn",
				"globalSubscriberId": "5GCustomer",
				"subscriptionServiceType": "5G",
				"additionalProperties": {
				"nsiInfo": {
					"nsiId": "NSI-M-001-HDBNJ-NSMF-01-A-ZX",
					"nsiName": "eMBB-001"
				},
				}
}
""".replaceAll("\\s+", ""))
		when(mockExecution.getVariable("mso-request-id")).thenReturn("edb08d97-e0f9-4c71-840a-72080d7be42e")
		ModifySliceSubnet sliceSubnet = new ModifySliceSubnet()
		sliceSubnet.preProcessRequest(mockExecution)
		Mockito.verify(mockExecution, atLeast(1)).setVariable(captor.capture() as String, captor.capture())
		List<ExecutionEntity> values = captor.getAllValues()
		assertNotNull(values)
	}

	@Test
	@Ignore("Requires mocking of AnNssmfUtils.getModelUuid")
	void testPrepareInitOperationStatus() {
		when(mockExecution.getVariable("serviceInstanceId")).thenReturn("54321")
		when(mockExecution.getVariable("jobId")).thenReturn("54321")
		when(mockExecution.getVariable("nsiId")).thenReturn("11111")
		ModifySliceSubnet sliceSubnet = new ModifySliceSubnet()
		sliceSubnet.prepareInitOperationStatus(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(eq("initResourceOperationStatus"), captor.capture())
		String res = captor.getValue()
		assertNotNull(res)
	}


	@Test
	void testSendSyncResponse() {
		when(mockExecution.getVariable("jobId")).thenReturn("123456")
		ModifySliceSubnet sliceSubnet = new ModifySliceSubnet()
		sliceSubnet.sendSyncResponse(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
		def updateVolumeGroupRequest = captor.getValue()
		assertEquals(updateVolumeGroupRequest, true)
	}

	/**
	 * Exercises the so-far-untested validation branch of preProcessRequest: when the incoming bpmnRequest is missing a
	 * globalSubscriberId, preProcessRequest must build and throw a WorkflowException (surfaced as a BpmnError) rather
	 * than continue. This uses only the mockExecution/JsonUtils path and does not require static-mocking
	 * AnNssmfUtils.getModelUuid (the documented blocker on testPrepareInitOperationStatus).
	 */
	@Test
	void testPreProcessRequestMissingGlobalSubscriberIdThrows() {
		when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
					"serviceInstanceID": "NSSI-C-001-HDBNJ-NSSMF-01-A-ZX",
					"networkType": "an/cn/tn",
					"subscriptionServiceType": "5G",
					"additionalProperties": {
					"nsiInfo": {
						"nsiId": "NSI-M-001-HDBNJ-NSMF-01-A-ZX",
						"nsiName": "eMBB-001"
					}
					}
}
""".replaceAll("\\s+", ""))
		when(mockExecution.getVariable("mso-request-id")).thenReturn("edb08d97-e0f9-4c71-840a-72080d7be42e")
		ModifySliceSubnet sliceSubnet = new ModifySliceSubnet()

		try {
			sliceSubnet.preProcessRequest(mockExecution)
			fail("Expected a BpmnError to be thrown for missing globalSubscriberId")
		} catch (BpmnError e) {
			assertEquals("500", e.getErrorCode())
		}

		// The failure must be recorded as a WorkflowException on the execution and must NOT set globalSubscriberId.
		Mockito.verify(mockExecution).setVariable(eq("WorkflowException"), any())
		Mockito.verify(mockExecution, never()).setVariable(eq("globalSubscriberId"), any())
	}

}
