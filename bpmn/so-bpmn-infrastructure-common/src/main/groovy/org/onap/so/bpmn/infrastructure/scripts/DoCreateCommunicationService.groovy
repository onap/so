/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.CommunicationServiceProfile
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoCreateCommunicationService.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Create
 *
 */
class DoCreateCommunicationService extends AbstractServiceTaskProcessor{
    String Prefix="DCCS_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    AAIResourcesClient client = new AAIResourcesClient()

    private static final Logger logger = LoggerFactory.getLogger( DoCreateCommunicationService.class)

    @Override
     void preProcessRequest(DelegateExecution execution) {
        logger.trace("start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""
        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                msg = "Input serviceInstanceId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
            if (isBlank(subscriptionServiceType)) {
                msg = "Input subscriptionServiceType' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }


        } catch(BpmnError e) {
            throw e
        } catch(Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit preProcessRequest")
    }

    /**
     * create communication service, generate S-NSSAI Id and communication service profile
     * 1.create communication service profile
     *
     */
    def createCommunicationServiceProfile = { DelegateExecution execution ->
        logger.trace("createCSandServiceProfile")
        String msg = ""
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        try {
            // String sNSSAI_id = execution.getVariable("sNSSAI_id")
            // create communication service profile
            String profileId = UUID.randomUUID().toString()
            execution.setVariable("communicationProfileId", profileId)

            def csInputMap = execution.getVariable("csInputMap") as Map<String, ?>
            Integer latency = csInputMap.get("latency") as Integer
            Integer maxNumberOfUEs = csInputMap.get("maxNumberofUEs") as Integer
            Integer expDataRateDL = csInputMap.get("expDataRateDL") as Integer
            Integer expDataRateUL = csInputMap.get("expDataRateUL") as Integer
            String coverageArea = csInputMap.get("coverageAreaTAList")
            String uEMobilityLevel = csInputMap.get("uEMobilityLevel")
            String resourceSharingLevel = csInputMap.get("resourceSharingLevel")

            CommunicationServiceProfile csp = new CommunicationServiceProfile()
            csp.setProfileId(profileId)

            csp.setLatency(latency)
            csp.setMaxNumberOfUEs(maxNumberOfUEs)
            csp.setUeMobilityLevel(uEMobilityLevel)
            csp.setResourceSharingLevel(resourceSharingLevel)
            csp.setExpDataRateDL(expDataRateDL)
            csp.setExpDataRateUL(expDataRateUL)
            csp.setCoverageAreaList(coverageArea)

            execution.setVariable("communicationServiceInstanceProfile", csp)

            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.COMMUNICATION_SERVICE_PROFILE,
                    globalSubscriberId,
                    subscriptionServiceType,
                    serviceInstanceId,
                    profileId
            )
            client.create(uri, csp)


        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in createCSandServiceProfile " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("exit createCSandServiceProfile")
    }


    /**
     * create communication service, generate S-NSSAI Id
     * 1.generate S-NSSAI Id
     * 2.create communication service
     *
     */
    def createCommunicationService = { DelegateExecution execution ->
        logger.trace("create communication service")
        String msg
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        try {
            //generate S-NSSAI Id and communication service profile
            String sNSSAI_id = generateNSSAI(serviceInstanceId)

            execution.setVariable("sNSSAI_id", sNSSAI_id)
            // 创建service
            String serviceInstanceName = execution.getVariable("serviceInstanceName")
            String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
            String csServiceType = execution.getVariable("csServiceType")
            String aaiServiceRole = "communication-service" //待确定

            String oStatus = "processing"
            String uuiRequest = execution.getVariable("uuiRequest")
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            String useInterval = execution.getVariable("useInterval")
            String globalSubscriberId = execution.getVariable("globalSubscriberId")

            // create service
            ServiceInstance csi = new ServiceInstance()
            csi.setServiceInstanceName(serviceInstanceName)
            csi.setServiceType(csServiceType)
            csi.setServiceRole(aaiServiceRole)
            csi.setOrchestrationStatus(oStatus)
            csi.setModelInvariantId(modelInvariantUuid)
            csi.setModelVersionId(modelUuid)
            csi.setInputParameters(uuiRequest)
            csi.setWorkloadContext(useInterval)
            csi.setEnvironmentContext(sNSSAI_id)

            //timestamp format YYYY-MM-DD hh:mm:ss
            csi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

            execution.setVariable("communicationServiceInstance", csi)

            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, subscriptionServiceType, serviceInstanceId)
            client.create(uri, csi)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in communication service " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("exit communication service")
    }

    private static generateNSSAI = { final String instanceId ->
        int h, res
        res = (instanceId == null) ? 0 : (h = instanceId.hashCode()) ^ (h >>> 16)
        res = res >>> 1
        return "01-" + Integer.toHexString(res).toUpperCase()
    }
}
