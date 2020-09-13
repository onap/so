/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.scripts

import static org.junit.Assert.*

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.mockito.Mockito.when
import static org.mockito.Mockito.times
import static org.mockito.ArgumentMatchers.eq

class DoAllocateCoreNSSITest extends MsoGroovyTest {

	@Before
	void init() throws IOException {
		super.init("DoAllocateCoreNSSI")
	}

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	@Test
	public void testPreProcessRequest() {

		String sliceParams="""{
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
			"perfReqEmbbList ": [{
				"activityFactor": 50
			}]
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
	"endPoints": [{
			"nodeId": "",
			"additionalInfo": {
				"xxx": "xxx"
			}
		},
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
}"""
		
		String expected = """{"plmnIdList":["460-00","460-01"],"sliceProfileId":"ab9af40f13f721b5f13539d87484098","maxNumberofUEs":200,"latency":2,"snssaiList":["001-100001"],"perfReq":{"perfReqEmbbList ":[{"activityFactor":50}]},"coverageAreaTAList":["1","2","3","4"],"resourceSharingLevel":"non-shared"}"""

		when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)

		DoAllocateCoreNSSI allocateNssi = new DoAllocateCoreNSSI()
		allocateNssi.preProcessRequest(mockExecution)
		
		Mockito.verify(mockExecution, times(1)).setVariable(eq("sliceProfile"), captor.capture())
		def sliceProfile = captor.getValue()
		assertEquals(expected, sliceProfile)
		
		Mockito.verify(mockExecution, times(3)).setVariable(captor.capture() as String, captor.capture())
	}
	
	
}
