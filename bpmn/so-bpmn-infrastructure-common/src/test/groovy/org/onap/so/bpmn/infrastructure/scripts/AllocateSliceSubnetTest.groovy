/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, Wipro Limited.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import static org.junit.Assert.*
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.bpmn.common.scripts.MsoGroovyTest


class AllocateSliceSubnetTest extends MsoGroovyTest {
	
	@Before
	void init() throws IOException {
		super.init("AllocateSliceSubnet")
	}

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	@Test
	public void testPreProcessRequest() {
		when(mockExecution.getVariable("serviceInstanceID")).thenReturn("12345")
		when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
				"name": "eMBB-001",
				"modelInvariantUuid": "NSST-C-001-HDBNJ-NSSMF-01-A-ZX",
				"modelUuid": "NSST-C-001-HDBNJ-NSSMF-01-A-ZX-UUID",
				"globalSubscriberId": "5GCustomer",
				"subscriptionServiceType": "5G",
				"networkType": "an/cn/tn",
				"additionalProperties": {
					"sliceProfile": {
					"snssaiList": [
						"001-100001"
					],
					"sliceProfileId": "ab9af40f13f721b5f13539d87484098",
					"plmnIdList": [
						"460-00",
						"460-01"
					],
					"perfReq": {
					"perfReqEmbbList ": [
						{
							"activityFactor": 50
						}
					]
					},
				"maxNumberofUEs": 200,
				"coverageAreaTAList": [
						"1",
						"2",
						"3",
						"4"
					],
				"latency": 2,
				"resourceSharingLevel": "non-shared"
					},
				"endPoints": [
					{
						"nodeId": "",
						"additionalInfo": {
							"xxx": "xxx"
						}
					}
					],
				"nsiInfo": {
					"nsiId": "NSI-M-001-HDBNJ-NSMF-01-A-ZX",
					"nsiName": "eMBB-001"
				},
				"scriptName": "AN1"
				}
}
""".replaceAll("\\s+", ""))
		when(mockExecution.getVariable("mso-request-id")).thenReturn("edb08d97-e0f9-4c71-840a-72080d7be42e")
		AllocateSliceSubnet sliceSubnet = new AllocateSliceSubnet()
		sliceSubnet.preProcessRequest(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(captor.capture() as String, captor.capture())
		List<ExecutionEntity> values = captor.getAllValues()
		assertNotNull(values)
	}
	
	@Test
	void testPrepareInitOperationStatus() {

		when(mockExecution.getVariable("dummyServiceId")).thenReturn("12345")
		when(mockExecution.getVariable("jobId")).thenReturn("54321")

		when(mockExecution.getVariable("nsiId")).thenReturn("11111")

		AllocateSliceSubnet sliceSubnet = new AllocateSliceSubnet()

		sliceSubnet.prepareInitOperationStatus(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(eq("initResourceOperationStatus"), captor.capture())
		String res = captor.getValue()
		assertNotNull(res)
	}


	@Test
	void testSendSyncResponse() {
		when(mockExecution.getVariable("jobId")).thenReturn("123456")
		AllocateSliceSubnet sliceSubnet = new AllocateSliceSubnet()
		sliceSubnet.sendSyncResponse(mockExecution)
		Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
		def updateVolumeGroupRequest = captor.getValue()
		assertEquals(updateVolumeGroupRequest, true)
	}

}
