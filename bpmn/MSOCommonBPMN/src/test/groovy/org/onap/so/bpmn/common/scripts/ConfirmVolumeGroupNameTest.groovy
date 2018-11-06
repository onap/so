/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.so.bpmn.common.scripts.ConfirmVolumeGroupName
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults

import javax.ws.rs.core.UriBuilder

import static org.assertj.core.api.Java6Assertions.assertThat
import static org.assertj.core.api.ThrowableAssert.catchThrowable
import static org.junit.Assert.assertEquals

import static org.mockito.Mockito.when

class ConfirmVolumeGroupNameTest extends MsoGroovyTest {

    private static final AAIResourceUri RESOURCE_URI = AAIUriFactory.createResourceFromExistingURI(
            AAIObjectType.VOLUME_GROUP, UriBuilder.fromPath('/aai/test/volume-groups/volume-group/testVolumeGroup').build())
    @Spy
    private ConfirmVolumeGroupName confirmVolumeGroupName
    @Mock
    private VolumeGroup volumeGroup
    private ExceptionUtilFake exceptionUtilFake
    private DelegateExecution delegateExecution

    @Before
    public void init() throws IOException {
        super.init("ConfirmVolumeGroupName")
        MockitoAnnotations.initMocks(this)
        exceptionUtilFake = new ExceptionUtilFake()
        delegateExecution = new DelegateExecutionFake()
        volumeGroup = createVolumeGroup()
        confirmVolumeGroupName.exceptionUtil = exceptionUtilFake
        when(confirmVolumeGroupName.getAAIClient()).thenReturn(client)
    }

    @Test
    public void preProcessRequest_shouldSetUpVariables() {
        String volumeGroupId = "volume-group-id-1"
        String volumeGroupName = "volume-group-name-1"
        String aicCloudRegion = "aic-cloud-region-1"
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER, aicCloudRegion, volumeGroupId)

        delegateExecution.setVariable("ConfirmVolumeGroupName_volumeGroupId", volumeGroupId)
        delegateExecution.setVariable("ConfirmVolumeGroupName_volumeGroupName", volumeGroupName)
        delegateExecution.setVariable("ConfirmVolumeGroupName_aicCloudRegion", aicCloudRegion)
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", uri)

        confirmVolumeGroupName.preProcessRequest(delegateExecution)

        assertEquals(ConfirmVolumeGroupName.Prefix, delegateExecution.getVariable("prefix"))

        assertEquals(volumeGroupId, delegateExecution.getVariable("CVGN_volumeGroupId"))
        assertEquals(volumeGroupName, delegateExecution.getVariable("CVGN_volumeGroupName"))
        assertEquals(aicCloudRegion, delegateExecution.getVariable("CVGN_aicCloudRegion"))
    }

    @Test
    public void queryAAIForVolumeGroupId_shouldSucceed_whenVolumeGroupExists() {
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponseCode", 200)
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponse", volumeGroup)
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", RESOURCE_URI)
        when(client.get(VolumeGroup.class, RESOURCE_URI)).thenReturn(Optional.of(volumeGroup))

        confirmVolumeGroupName.queryAAIForVolumeGroupId(delegateExecution)

        assertEquals(200, delegateExecution.getVariable("CVGN_queryVolumeGroupResponseCode"))
        assertEquals(volumeGroup, delegateExecution.getVariable("CVGN_queryVolumeGroupResponse"))
    }

    @Test
    public void queryAAIForVolumeGroupId_shouldFailWith404_whenVolumeGroupDoesNotExist() {
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", RESOURCE_URI)
        when(client.get(VolumeGroup.class, RESOURCE_URI)).thenReturn(Optional.empty())

        confirmVolumeGroupName.queryAAIForVolumeGroupId(delegateExecution)

        assertEquals(404, delegateExecution.getVariable("CVGN_queryVolumeGroupResponseCode"))
        assertEquals("Volume Group not Found!", delegateExecution.getVariable("CVGN_queryVolumeGroupResponse"))
    }

    @Test
    public void queryAAIForVolumeGroupId_shouldThrowWorkflowException_whenRuntimeExceptionIsThrown() throws BpmnError {
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", RESOURCE_URI)
        delegateExecution.setVariable("testProcessKey", "process-key1")

        def errorMsg = "my runtime exception"
        when(client.get(VolumeGroup.class, RESOURCE_URI)).thenThrow(new RuntimeException(errorMsg))

        Throwable thrown = catchThrowable(
                { -> confirmVolumeGroupName.queryAAIForVolumeGroupId(delegateExecution) })
        assertThat(thrown).isExactlyInstanceOf(BpmnError.class)
        assertEquals("MSOWorkflowException", thrown.getErrorCode())

        assertEquals(500, delegateExecution.getVariable("CVGN_queryVolumeGroupResponseCode"))
        assertEquals(String.format("AAI GET Failed:%s", errorMsg), delegateExecution.getVariable("CVGN_queryVolumeGroupResponse"))
    }

    @Test
    public void checkAAIQueryResult_shouldSetVolumeGroupNameMatchesToFalse_whenResponseCodeIs404() {
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponseCode", 404)
        delegateExecution.setVariable("CVGN_volumeGroupName", "")

        confirmVolumeGroupName.checkAAIQueryResult(delegateExecution)

        assertEquals(false, delegateExecution.getVariable("CVGN_volumeGroupNameMatches"))
    }

    @Test
    public void checkAAIQueryResult_shouldSetVolumeGroupNameMatchesToTrue_whenResponseCodeIs200AndVolumeGroupNameExists() {
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponseCode", 200)
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponse", volumeGroup)
        delegateExecution.setVariable("CVGN_volumeGroupName", volumeGroup.getVolumeGroupName())

        confirmVolumeGroupName.checkAAIQueryResult(delegateExecution)

        assertEquals(true, delegateExecution.getVariable("CVGN_volumeGroupNameMatches"))
    }

    @Test
    public void handleVolumeGroupNameNoMatch_shouldThrowBpmnErrorException() {
        def volumeGroupId = "volume-group-id"
        def volumeGroupName = "volume-group-name"

        delegateExecution.setVariable("CVGN_volumeGroupId", volumeGroupId)
        delegateExecution.setVariable("CVGN_volumeGroupName", volumeGroupName)

        Throwable thrown = catchThrowable(
                { -> confirmVolumeGroupName.handleVolumeGroupNameNoMatch(delegateExecution) })
        assertThat(thrown).isExactlyInstanceOf(BpmnError.class)
        assertEquals("MSOWorkflowException", thrown.getErrorCode())
    }

    @Test
    public void reportSuccess_shouldSetWorkflowResponseToEmptyString() {
        confirmVolumeGroupName.reportSuccess(delegateExecution)
        assertEquals("", delegateExecution.getVariable("WorkflowResponse"))
    }

    private VolumeGroup createVolumeGroup() {
        VolumeGroup volumeGroup = new VolumeGroup()

        volumeGroup.setVolumeGroupId("volume-group-id")
        volumeGroup.setVolumeGroupName("volume-group-name")
        volumeGroup.setHeatStackId("heat-stack-id")
        volumeGroup.setVnfType("vnf-type")
        volumeGroup.setOrchestrationStatus("orchestration-status")
        volumeGroup.setModelCustomizationId("model-customization-id")
        volumeGroup.setVfModuleModelCustomizationId("vf-module-model-customization-id")
        volumeGroup.setResourceVersion("resource-version")
        volumeGroup.setRelationshipList(new RelationshipList())

        return volumeGroup
    }

    private class ExceptionUtilFake extends ExceptionUtil {
        @Override
        public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
            throw new BpmnError("MSOWorkflowException")
        }
    }

}
