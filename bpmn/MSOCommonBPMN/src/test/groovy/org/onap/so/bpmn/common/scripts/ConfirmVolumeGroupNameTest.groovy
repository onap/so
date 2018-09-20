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

package org.onap.so.bpmn.common.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule

import static org.mockito.Mockito.*
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*

@RunWith(MockitoJUnitRunner.class)
class ConfirmVolumeGroupNameTest extends MsoGroovyTest {

	private static final def AAA_URI = "uri_test"
	private static final def AIC_CLOUD_REGION = "AicClReg_test"
	private static final def VOLUME_GROUP_NAME = "volumeTestGName"
	private static final def VOLUME_GROUP_ID = "vol_gr_id_test"

	@Captor
	ArgumentCaptor<ExecutionEntity> captor=  ArgumentCaptor.forClass(ExecutionEntity.class);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

	@Test
	void preProcessRequestSuccessful() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("ConfirmVolumeGroupName_volumeGroupId")).thenReturn(VOLUME_GROUP_ID)
		when(mockExecution.getVariable("ConfirmVolumeGroupName_volumeGroupName")).thenReturn(VOLUME_GROUP_NAME)
		when(mockExecution.getVariable("ConfirmVolumeGroupName_aicCloudRegion")).thenReturn(AIC_CLOUD_REGION)

		when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('namespace_test')
		when(mockExecution.getVariable("mso.workflow.ConfirmVolumeGroupName.aai.cloud-region.uri")).thenReturn(AAA_URI)
		new ConfirmVolumeGroupName().preProcessRequest(mockExecution)

		verifyInitProcessVariables(mockExecution)
		verify(mockExecution).setVariable("CVGN_volumeGroupId", VOLUME_GROUP_ID)
		verify(mockExecution).setVariable("CVGN_volumeGroupName", "volumeTestGName")
		verify(mockExecution).setVariable("CVGN_aicCloudRegion", AIC_CLOUD_REGION)
		verify(mockExecution).setVariable("CVGN_volumeGroupGetEndpoint",
				"${AAA_URI}/${AIC_CLOUD_REGION}/volume-groups/volume-group/" + VOLUME_GROUP_ID)
	}

	private void verifyInitProcessVariables(ExecutionEntity mockExecution) {
		verify(mockExecution).setVariable("prefix", "CVGN_")
		verify(mockExecution).setVariable("CVGN_volumeGroupId", null)
		verify(mockExecution).setVariable("CVGN_volumeGroupName", null)
		verify(mockExecution).setVariable("CVGN_aicCloudRegion", null)
		verify(mockExecution).setVariable("CVGN_volumeGroupGetEndpoint", null)
		verify(mockExecution).setVariable("CVGN_volumeGroupNameMatches", false)
		verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponseCode", null)
		verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponse", "")
		verify(mockExecution).setVariable("CVGN_ResponseCode", null)
		verify(mockExecution).setVariable("RollbackData", null)
	}

	@Test
	void checkAAIQueryResult_volumeGroupNamesMatch() {
		ExecutionEntity mockExecution = setupMock()
		commonPartOfCheckAAIQueryTest(mockExecution, VOLUME_GROUP_NAME)
		verify(mockExecution).setVariable("CVGN_volumeGroupNameMatches", true)
	}

	@Test
	void checkAAIQueryResult_volumeGroupNamesDoNotMatch() {
		ExecutionEntity mockExecution = setupMock()
		commonPartOfCheckAAIQueryTest(mockExecution, "grName2")
		verify(mockExecution, Mockito.times(0)).setVariable("CVGN_volumeGroupNameMatches", true)
	}

	private void commonPartOfCheckAAIQueryTest(ExecutionEntity mockExecution, def volumeGroupName) {
		when(mockExecution.getVariable("CVGN_volumeGroupName")).thenReturn(VOLUME_GROUP_NAME)
		def xml = "<volume-group-name>" + volumeGroupName + "</volume-group-name>"
		when(mockExecution.getVariable("CVGN_queryVolumeGroupResponse")).thenReturn(xml)
		new ConfirmVolumeGroupName().checkAAIQueryResult(mockExecution)
		verify(mockExecution).setVariable("CVGN_volumeGroupNameMatches", false)
	}

	@Test
	public void testQueryAAIForVolumeGroupId() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
		when(mockExecution.getVariable("CVGN_volumeGroupGetEndpoint")).thenReturn('/aai/test/volume-groups/volume-group/testVolumeGroup')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')

		mockData()

		ConfirmVolumeGroupName confirmVolumeGroupName = new ConfirmVolumeGroupName()
		confirmVolumeGroupName.queryAAIForVolumeGroupId(mockExecution)
		verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponseCode",200)
		verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponse","")
	}

	@Test
	public void testQueryAAIForVolumeGroupId_404() {

		ExecutionEntity mockExecution = setupMock()
		try {
			when(mockExecution.getVariable("CVGN_volumeGroupGetEndpoint")).thenReturn('/aai/test/volume-groups/volume-group/testVolumeGroup')
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')

			mockData()

			ConfirmVolumeGroupName confirmVolumeGroupName = new ConfirmVolumeGroupName()
			confirmVolumeGroupName.queryAAIForVolumeGroupId(mockExecution)
		}
		catch(Exception ex){

		}
		Mockito.verify(mockExecution,times(4)).setVariable(captor.capture(),captor.capture())
		WorkflowException workflowException = captor.getValue()
		Assert.assertEquals("AAI GET Failed",workflowException.getErrorMessage())
		Assert.assertEquals(500,workflowException.getErrorCode())
	}
	private void  mockData() {
		stubFor(get(urlMatching("/aai/test/volume-groups/volume-group/testVolumeGroup"))
				.willReturn(aResponse()
				.withStatus(200)))
	}

	private ExecutionEntity setupMock() {

		ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
		when(mockProcessDefinition.getKey()).thenReturn("ConfirmVolumeGroupName")
		RepositoryService mockRepositoryService = mock(RepositoryService.class)
		when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
		when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("ConfirmVolumeGroupName")
		when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
		ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
		when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getId()).thenReturn("100")
		when(mockExecution.getProcessDefinitionId()).thenReturn("ConfirmVolumeGroupName")
		when(mockExecution.getProcessInstanceId()).thenReturn("ConfirmVolumeGroupName")
		when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
		when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
		return mockExecution
	}
}
