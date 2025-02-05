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

import static org.assertj.core.api.Assertions.catchThrowableOfType
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when
import jakarta.ws.rs.core.UriBuilder
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.constants.Defaults
import org.springframework.http.HttpStatus

class ConfirmVolumeGroupNameTest {

    private static final AAIResourceUri RESOURCE_URI = AAIUriFactory.createResourceFromExistingURI(
            Types.VOLUME_GROUP, UriBuilder.fromPath('/aai/test/volume-groups/volume-group/testVolumeGroup').build())

    private ConfirmVolumeGroupName confirmVolumeGroupName
    @Mock
    private VolumeGroup volumeGroup
    @Mock
    private AAIResourcesClient client
    private ExceptionUtilFake exceptionUtilFake

    private DelegateExecution delegateExecution

    @Before
    public void init() throws IOException {
        exceptionUtilFake = new ExceptionUtilFake()
        confirmVolumeGroupName = spy(new ConfirmVolumeGroupName(exceptionUtilFake))
        MockitoAnnotations.initMocks(this)
        delegateExecution = new DelegateExecutionFake()
        volumeGroup = createVolumeGroup()
        when(confirmVolumeGroupName.getAAIClient()).thenReturn(client)
    }

    @Test
    public void preProcessRequest_shouldSetUpVariables() {
        String volumeGroupId = "volume-group-id-1"
        String volumeGroupName = "volume-group-name-1"
        String aicCloudRegion = "aic-cloud-region-1"
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), aicCloudRegion).volumeGroup(volumeGroupId))

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
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponseCode", HttpStatus.OK)
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponse", volumeGroup)
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", RESOURCE_URI)
        when(client.get(VolumeGroup.class, RESOURCE_URI)).thenReturn(Optional.of(volumeGroup))

        confirmVolumeGroupName.queryAAIForVolumeGroupId(delegateExecution)

        assertEquals(HttpStatus.OK.value(), delegateExecution.getVariable("CVGN_queryVolumeGroupResponseCode"))
        assertEquals(volumeGroup, delegateExecution.getVariable("CVGN_queryVolumeGroupResponse"))
    }

    @Test
    public void queryAAIForVolumeGroupId_shouldFailWith404_whenVolumeGroupDoesNotExist() {
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", RESOURCE_URI)
        when(client.get(VolumeGroup.class, RESOURCE_URI)).thenReturn(Optional.empty())

        confirmVolumeGroupName.queryAAIForVolumeGroupId(delegateExecution)

        assertEquals(HttpStatus.NOT_FOUND.value(), delegateExecution.getVariable("CVGN_queryVolumeGroupResponseCode"))
        assertEquals("Volume Group not Found!", delegateExecution.getVariable("CVGN_queryVolumeGroupResponse"))
    }

    @Test
    public void queryAAIForVolumeGroupId_shouldThrowWorkflowException_whenRuntimeExceptionIsThrown() throws BpmnError {
        delegateExecution.setVariable("CVGN_volumeGroupGetEndpoint", RESOURCE_URI)
        delegateExecution.setVariable("testProcessKey", "process-key1")

        def errorMsg = "my runtime exception"
        when(client.get(VolumeGroup.class, RESOURCE_URI)).thenThrow(new RuntimeException(errorMsg))

        def exceptionMsg = "AAI GET Failed"

        BpmnError error = catchThrowableOfType(
                { -> confirmVolumeGroupName.queryAAIForVolumeGroupId(delegateExecution) }, BpmnError.class)

        assertEquals(String.format("MSOWorkflowException: %s", exceptionMsg), error.getMessage())
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value().toString(), error.getErrorCode())

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), delegateExecution.getVariable("CVGN_queryVolumeGroupResponseCode"))
        assertEquals(String.format("AAI GET Failed:%s", errorMsg), delegateExecution.getVariable("CVGN_queryVolumeGroupResponse"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exceptionUtilFake.getErrorCode())
        assertEquals(exceptionMsg, exceptionUtilFake.getErrorMessage())
        assertEquals(delegateExecution, exceptionUtilFake.getDelegateExecution())
    }

    @Test
    public void checkAAIQueryResult_shouldSetVolumeGroupNameMatchesToFalse_whenResponseCodeIs404() {
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponseCode", HttpStatus.NOT_FOUND)
        delegateExecution.setVariable("CVGN_volumeGroupName", "")

        confirmVolumeGroupName.checkAAIQueryResult(delegateExecution)

        assertFalse(delegateExecution.getVariable("CVGN_volumeGroupNameMatches"))
    }

    @Test
    public void checkAAIQueryResult_shouldSetVolumeGroupNameMatchesToTrue_whenResponseCodeIs200AndVolumeGroupNameExists() {
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponseCode", HttpStatus.OK.value())
        delegateExecution.setVariable("CVGN_queryVolumeGroupResponse", volumeGroup)
        delegateExecution.setVariable("CVGN_volumeGroupName", volumeGroup.getVolumeGroupName())

        confirmVolumeGroupName.checkAAIQueryResult(delegateExecution)

        assertTrue(delegateExecution.getVariable("CVGN_volumeGroupNameMatches"))
    }

    @Test
    public void handleVolumeGroupNameNoMatch_shouldThrowBpmnErrorException() {
        def volumeGroupId = "volume-group-id"
        def volumeGroupName = "volume-group-name"

        delegateExecution.setVariable("CVGN_volumeGroupId", volumeGroupId)
        delegateExecution.setVariable("CVGN_volumeGroupName", volumeGroupName)

        def errorMessage = String.format("Error occurred - volume group id %s is not associated with %s",
                delegateExecution.getVariable('CVGN_volumeGroupId'), delegateExecution.getVariable('CVGN_volumeGroupName'))

        BpmnError error = catchThrowableOfType(
                { -> confirmVolumeGroupName.handleVolumeGroupNameNoMatch(delegateExecution) }, BpmnError.class)

        assertEquals(String.format("MSOWorkflowException: %s", errorMessage), error.getMessage())
        assertEquals("1002", error.getErrorCode())

        assertEquals(1002, exceptionUtilFake.getErrorCode())
        assertEquals(errorMessage, exceptionUtilFake.getErrorMessage())
        assertEquals(delegateExecution, exceptionUtilFake.getDelegateExecution())
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

    private static class ExceptionUtilFake extends ExceptionUtil {

        private int errorCode
        private String errorMessage
        private DelegateExecution execution

        @Override
        public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
            this.errorCode = errorCode
            this.errorMessage = errorMessage
            this.execution = execution
            throw new BpmnError(errorCode.toString(), "MSOWorkflowException: ${errorMessage}")
        }

        public int getErrorCode() {
            return errorCode
        }

        public String getErrorMessage() {
            return errorMessage
        }

        public DelegateExecution getDelegateExecution() {
            return execution
        }
    }

}
