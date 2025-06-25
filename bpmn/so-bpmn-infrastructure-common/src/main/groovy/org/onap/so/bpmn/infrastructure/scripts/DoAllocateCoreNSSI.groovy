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
import org.apache.commons.collections.map.HashedMap
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.apache.commons.lang3.StringUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import javax.ws.rs.core.Response
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil

class DoAllocateCoreNSSI extends AbstractServiceTaskProcessor {
    String Prefix="DACNSSI_"
    private static final Logger logger = LoggerFactory.getLogger( DoAllocateCoreNSSI.class);
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    private static final ObjectMapper mapper = new ObjectMapper()
    OofUtils oofUtils = new OofUtils()
    void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix+" **** Enter DoAllocateCoreNSSI ::: preProcessRequest ****")
        execution.setVariable("prefix", Prefix)
        String msg = ""
        //Get SliceProfile from sliceParams JSON
        String sliceProfile = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "sliceProfile")
        if (isBlank(sliceProfile)) {
            msg = "Slice Profile is null"
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        } else {
            execution.setVariable("sliceProfile", sliceProfile)
        }
        String coreServiceInstanceId = UUID.randomUUID().toString()
        execution.setVariable("coreServiceInstanceId", coreServiceInstanceId)
        logger.debug(Prefix+" **** Exit DoAllocateCoreNSSI ::: preProcessRequest ****")
    }

    void getNSSTName(DelegateExecution execution){
        logger.debug(Prefix+" **** Enter DoAllocateCoreNSSI ::: getNSSTName ****")
        String nsstModelInvariantUuid = execution.getVariable("modelInvariantUuid")
        try{
            String json = catalogDbUtils.getServiceResourcesByServiceModelInvariantUuidString(execution, nsstModelInvariantUuid)
            logger.debug("***** JSON Response is: "+json)
            String nsstName = jsonUtil.getJsonValue(json, "serviceResources.modelInfo.modelName") ?: ""
            List serviceProxyList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(json, "serviceResources.serviceProxy"))
            String networkServiceModelInfo = serviceProxyList.get(0)
            execution.setVariable("networkServiceModelInfo", networkServiceModelInfo)
            logger.debug("***** nsstName is: "+ nsstName)
            execution.setVariable("nsstName",nsstName)
        }catch(BpmnError e){
            throw e
        } catch (Exception ex){
            String msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix+" **** Exit DoAllocateCoreNSSI ::: getNSSTName ****")
    }

    void prepareOOFRequest(DelegateExecution execution){
        logger.debug(Prefix+" **** Enter DoAllocateCoreNSSI ::: prepareOOFRequest ****")
        //API Path
        String apiPath =  "/api/oof/selection/nssi/v1"
        logger.debug("API path for DoAllocateCoreNSSI: "+apiPath)
        execution.setVariable("NSSI_apiPath", apiPath)
        //Setting correlator as requestId
        String requestId = execution.getVariable("msoRequestId")
        execution.setVariable("NSSI_correlator", requestId)
        //Setting messageType for all Core slice as cn
        String messageType = "cn"
        execution.setVariable("NSSI_messageType", messageType)
        String timeout = "PT30M"
        execution.setVariable("NSSI_timeout", timeout)
        Map<String, Object> profileInfo = mapper.readValue(execution.getVariable("sliceProfile"), Map.class)
        String nsstModelUuid = execution.getVariable("modelUuid")
        String nsstModelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String nsstName = execution.getVariable("nsstName")
        String oofRequest = oofUtils.buildSelectNSSIRequest(requestId, messageType, nsstModelUuid, nsstModelInvariantUuid, nsstName, profileInfo)
        logger.debug("**** OOfRequest for Core Slice: "+oofRequest)
        execution.setVariable("NSSI_oofRequest", oofRequest)
        logger.debug(Prefix+" **** Exit DoAllocateCoreNSSI ::: prepareOOFRequest ****")
    }

    void processOOFAsyncResponse(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreNSSI ::: processOOFAsyncResponse ****")
        String OOFResponse = execution.getVariable("NSSI_asyncCallbackResponse")
        String requestStatus = jsonUtil.getJsonValue(OOFResponse, "requestStatus")
        logger.debug("NSSI OOFResponse is: " + OOFResponse)
        execution.setVariable("OOFResponse", OOFResponse)
        String solutions =""
        if(requestStatus.equals("completed")) {
            List solutionsList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(OOFResponse, "solutions"))
            if(solutionsList!=null && !solutionsList.isEmpty() ) {
                solutions = solutionsList.get(0)
            }
        } else {
            String statusMessage = jsonUtil.getJsonValue(OOFResponse, "statusMessage")
            logger.error("received failed status from oof "+ statusMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a failed Async Response from OOF : "+statusMessage)
        }
        execution.setVariable("solutions", solutions)
        logger.debug(Prefix+" **** Exit DoAllocateCoreNSSI ::: processOOFAsyncResponse ****")
    }

    void prepareFailedOperationStatusUpdate(DelegateExecution execution){
        logger.debug(Prefix + " **** Enter DoAllocateCoreNSSI ::: prepareFailedOperationStatusUpdate ****")
        String serviceId = execution.getVariable("nsiId")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")
        String nssiId = execution.getVariable("nssiId")
        String operationType = "ALLOCATE"
        logger.debug("serviceId: "+serviceId+" jobId: "+jobId+" nsiId: "+nsiId+" operationType: "+operationType)
        String modelUuid= execution.getVariable("modelUuid")
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setJobId(jobId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setProgress("0")
        resourceOperationStatus.setStatus("failed")
        resourceOperationStatus.setStatusDescription("Core NSSI Allocate Failed")
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix + " **** Exit DoAllocateCoreNSSI ::: prepareFailedOperationStatusUpdate ****")
    }
}
