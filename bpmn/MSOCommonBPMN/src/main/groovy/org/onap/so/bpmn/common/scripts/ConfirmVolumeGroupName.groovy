/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.constants.Defaults
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import joptsimple.internal.Strings

public class ConfirmVolumeGroupName extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( ConfirmVolumeGroupName.class);

    def static final Prefix = "CVGN_"
    private final ExceptionUtil exceptionUtil

    ConfirmVolumeGroupName(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil
    }

    public void initProcessVariables(DelegateExecution execution) {
        execution.setVariable("prefix",Prefix)
        execution.setVariable("CVGN_volumeGroupId",null)
        execution.setVariable("CVGN_volumeGroupName",null)
        execution.setVariable("CVGN_aicCloudRegion", null)
        execution.setVariable("CVGN_volumeGroupGetEndpoint",null)

        // ConfirmVolumeGroupName workflow response variable placeholders
        execution.setVariable("CVGN_volumeGroupNameMatches", false)
        execution.setVariable("CVGN_queryVolumeGroupResponseCode",null)
        execution.setVariable("CVGN_queryVolumeGroupResponse","")
        execution.setVariable("CVGN_ResponseCode",null)
        execution.setVariable("RollbackData", null)
    }

    // store the incoming data in the flow DelegateExecution
    public void preProcessRequest(DelegateExecution execution) {
        def volumeGroupId = execution.getVariable("ConfirmVolumeGroupName_volumeGroupId")
        def volumeGroupName= execution.getVariable("ConfirmVolumeGroupName_volumeGroupName")
        def aicCloudRegion = execution.getVariable("ConfirmVolumeGroupName_aicCloudRegion")

        initProcessVariables(execution)
        execution.setVariable("CVGN_volumeGroupId", volumeGroupId)
        execution.setVariable("CVGN_volumeGroupName", volumeGroupName)
        execution.setVariable("CVGN_aicCloudRegion", aicCloudRegion)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), aicCloudRegion).volumeGroup(volumeGroupId))
        execution.setVariable("CVGN_volumeGroupGetEndpoint", uri)
    }

    // send a GET request to AA&I to retrieve the Volume information based on volume-group-id
    // expect a 200 response with the information in the response body or a 404 if the volume group id does not exist
    public void queryAAIForVolumeGroupId(DelegateExecution execution) {
        AAIResourceUri resourceUri = execution.getVariable("CVGN_volumeGroupGetEndpoint")

        try {
            Optional<VolumeGroup> volumeGroupOp = getAAIClient().get(VolumeGroup.class,  resourceUri)
            if(volumeGroupOp.isPresent()){
                execution.setVariable("CVGN_queryVolumeGroupResponseCode", HttpStatus.OK.value())
                execution.setVariable("CVGN_queryVolumeGroupResponse", volumeGroupOp.get())
            }else{
                execution.setVariable("CVGN_queryVolumeGroupResponseCode", HttpStatus.NOT_FOUND.value())
                execution.setVariable("CVGN_queryVolumeGroupResponse", "Volume Group not Found!")
            }
        } catch (Exception ex) {
            logger.debug("Exception occurred while executing AAI GET:" + ex.getMessage())
            execution.setVariable("CVGN_queryVolumeGroupResponseCode", HttpStatus.INTERNAL_SERVER_ERROR.value())
            execution.setVariable("CVGN_queryVolumeGroupResponse", "AAI GET Failed:" + ex.getMessage())
            exceptionUtil.buildAndThrowWorkflowException(execution, HttpStatus.INTERNAL_SERVER_ERROR.value(), "AAI GET Failed")
        }
    }

    // process the result from queryAAIVolumeGroupId()

    public void checkAAIQueryResult(DelegateExecution execution) {
        def actualVolumeGroupName = ""
        if (execution.getVariable("CVGN_queryVolumeGroupResponseCode") == HttpStatus.NOT_FOUND.value()) {
            logger.debug('volumeGroupId does not exist in AAI')
        }
        else if (execution.getVariable("CVGN_queryVolumeGroupResponseCode") == HttpStatus.OK.value()) {
            VolumeGroup volumeGroup = execution.getVariable("CVGN_queryVolumeGroupResponse")

            if (!Strings.isNullOrEmpty(volumeGroup.getVolumeGroupName())) {
                actualVolumeGroupName =  volumeGroup.getVolumeGroupName()
                logger.debug("volumeGroupId exists in AAI")
            }
        }
        execution.setVariable("CVGN_volumeGroupNameMatches", false)
        def volumeGroupName = execution.getVariable("CVGN_volumeGroupName")

        if (!actualVolumeGroupName.isEmpty() && volumeGroupName.equals(actualVolumeGroupName)) {
            logger.debug('Volume Group Name Matches AAI records')
            execution.setVariable("CVGN_volumeGroupNameMatches", true)
        }
    }


    // generates a WorkflowException if the A&AI query returns a response code other than 200/404
    public void handleAAIQueryFailure(DelegateExecution execution) {
        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                "Error occurred attempting to query AAI, Response Code " + execution.getVariable("CVGN_queryVolumeGroupResponseCode"),
                "BPMN", ErrorCode.UnknownError.getValue(),
                "ErrorResponse is:\n" + execution.getVariable("CVGN_queryVolumeGroupResponse"));
    }

    // generates a WorkflowException if the volume group name does not match AAI record for this volume group
    public void handleVolumeGroupNameNoMatch(DelegateExecution execution) {
        def errorNotAssociated = "Error occurred - volume group id ${execution.getVariable('CVGN_volumeGroupId')} " +
                "is not associated with ${execution.getVariable('CVGN_volumeGroupName')}"
        logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), errorNotAssociated, "BPMN",
                ErrorCode.UnknownError.getValue());
        exceptionUtil.buildAndThrowWorkflowException(execution, 1002, errorNotAssociated)
    }

    // sends a successful WorkflowResponse
    public void reportSuccess(DelegateExecution execution) {
        logger.debug("Sending 200 back to the caller")
        def responseXML = ""
        execution.setVariable("WorkflowResponse", responseXML)
    }
}
