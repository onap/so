/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.db.request.beans.InfraActiveRequests
import org.onap.so.db.request.client.RequestsDbClient
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.bpmn.core.UrnPropertiesReader;

public class CompleteMsoProcess extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CompleteMsoProcess.class);

    String Prefix="CMSO_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    public void preProcessRequest (DelegateExecution execution) {
        try {
            def xml = execution.getVariable("CompleteMsoProcessRequest")

            logger.debug("CompleteMsoProcess Request: " + xml)
            logger.debug("Incoming Request is: "+ xml)

            //mso-bpel-name from the incoming request
            def msoBpelName = utils.getNodeText(xml,"mso-bpel-name")
            execution.setVariable("CMSO_mso-bpel-name",msoBpelName)

            if (utils.nodeExists(xml, "request-information")) {
                throw new BpmnError("500", "CompleteMsoProcess subflow does not support this request type.")
            }
            if (utils.nodeExists(xml, "request-id")) {
                execution.setVariable("CMSO_request_id",utils.getNodeText(xml,"request-id"))
            }

        } catch (BpmnError e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, e.getMessage())
        } catch (Exception e) {
            logger.debug("Exception Occured During PreProcessRequest: " + e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in preprocess")
        }
    }

    public void updateInfraRequestDB (DelegateExecution execution){
        try {

            def xml = execution.getVariable("CompleteMsoProcessRequest")

            //Get statusMessage if exists
            def statusMessage
            if(utils.nodeExists(xml, "status-message")){
                statusMessage = utils.getNodeText(xml, "status-message")
            }else{
                statusMessage = "Resource Completed Successfully"
            }

            RequestsDbClient dbClient = getDbClient()

            InfraActiveRequests infraRequest = dbClient.getInfraActiveRequests(execution.getVariable("CMSO_request_id"), UrnPropertiesReader.getVariable("mso.adapters.requestDb.auth"), UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint"))
            if(infraRequest == null){
                infraRequest = new InfraActiveRequests();
                infraRequest.setRequestId(execution.getVariable("CMSO_request_id"))
            }
            infraRequest.setLastModifiedBy("BPMN")
            infraRequest.setStatusMessage(statusMessage)
            infraRequest.setRequestStatus("COMPLETE")
            infraRequest.setProgress(100)

            if(utils.nodeExists(xml, "vnfId")){
                infraRequest.setVnfId(utils.getNodeText(xml, "vnfId"))
            }else if(utils.nodeExists(xml, "networkId")){
                infraRequest.setNetworkId(utils.getNodeText(xml, "networkId"))
            }else if(utils.nodeExists(xml, "configurationId")){
                infraRequest.setConfigurationId(utils.getNodeText(xml, "configurationId"))
            }else if(utils.nodeExists(xml, "serviceInstanceId")){
                infraRequest.setServiceInstanceId(utils.getNodeText(xml, "serviceInstanceId"))
            }else if(utils.nodeExists(xml, "vfModuleId")){
                infraRequest.setVfModuleId(utils.getNodeText(xml, "vfModuleId"))
            }else if(utils.nodeExists(xml, "volumeGroupId")){
                infraRequest.setVolumeGroupId(utils.getNodeText(xml, "volumeGroupId"))
            }else if(utils.nodeExists(xml, "pnfName")){
                infraRequest.setPnfName(utils.getNodeText(xml, "pnfName"))
            }

            dbClient.updateInfraActiveRequests(infraRequest, UrnPropertiesReader.getVariable("mso.adapters.requestDb.auth"), UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint"))

        } catch (Exception e) {
            logger.error("Internal error while updating request db", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in update infra request db")
        }
    }

    protected RequestsDbClient getDbClient(){
        return new RequestsDbClient()
    }

}
