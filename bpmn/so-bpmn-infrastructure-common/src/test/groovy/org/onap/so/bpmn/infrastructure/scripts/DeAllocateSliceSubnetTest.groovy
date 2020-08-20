package org.onap.so.bpmn.infrastructure.scripts

import static org.junit.Assert.*

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor

class DeAllocateSliceSubnetTest {
	
	@Before
	void init() throws IOException {
		super.init("DeAllocateSliceSubnet")
	}

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	@Test
	public void testPreProcessRequest() {
		when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
				"serviceInstanceID": "NSSI-C-001-HDBNJ-NSSMF-01-A-ZX ",
				"networkType": "an/cn/tn",
				"globalSubscriberId": "5GCustomer",
				"subscriptionServiceType": "5G",
				"additionalProperties": {
				"snssaiList": [
				"001-100001"
				],
				"scriptName": "AN1",
				"nsiInfo": {
					"nsiId": "NSI-M-001-HDBNJ-NSMF-01-A-ZX",
					"nsiName": "eMBB-001"
				},
				}
}
""".replaceAll("\\s+", ""))
		when(mockExecution.getVariable("mso-request-id")).thenReturn("edb08d97-e0f9-4c71-840a-72080d7be42e")
		DeAllocateSliceSubnet sliceSubnet = new DeAllocateSliceSubnet()
		sliceSubnet.preProcessRequest(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(captor.capture() as String, captor.capture())
		List<ExecutionEntity> values = captor.getAllValues()
		assertNotNull(values)
	}
	
	@Test
	void testPrepareInitOperationStatus() {
		when(mockExecution.getVariable("serviceInstanceId")).thenReturn("54321")
		when(mockExecution.getVariable("jobId")).thenReturn("54321")
		when(mockExecution.getVariable("nsiId")).thenReturn("11111")
		DeAllocateSliceSubnet sliceSubnet = new DeAllocateSliceSubnet()
		sliceSubnet.prepareInitOperationStatus(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(eq("initResourceOperationStatus"), captor.capture())
		String res = captor.getValue()
		assertNotNull(res)
	}


	@Test
	void testSendSyncResponse() {
		when(mockExecution.getVariable("jobId")).thenReturn("123456")
		DeAllocateSliceSubnet sliceSubnet = new DeAllocateSliceSubnet()
		sliceSubnet.sendSyncResponse(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
		def updateVolumeGroupRequest = captor.getValue()
		assertEquals(updateVolumeGroupRequest, true)
	}

}
