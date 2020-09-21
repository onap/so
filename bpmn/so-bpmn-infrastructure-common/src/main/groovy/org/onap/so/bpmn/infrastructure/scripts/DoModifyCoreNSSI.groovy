/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Telecom Italia
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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.CloudRegion
import org.onap.aai.domain.yang.Customer
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.ModelVer
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.ServiceSubscription
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.Tenant
import org.onap.aai.domain.yang.VfModule
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.requestsdb.RequestsDbConstant
import org.onap.so.serviceinstancebeans.CloudConfiguration
import org.onap.so.serviceinstancebeans.ModelInfo
import org.onap.so.serviceinstancebeans.ModelType
import org.onap.so.serviceinstancebeans.OwningEntity
import org.onap.so.serviceinstancebeans.Project
import org.onap.so.serviceinstancebeans.RequestDetails
import org.onap.so.serviceinstancebeans.RequestInfo
import org.onap.so.serviceinstancebeans.RequestParameters
import org.onap.so.serviceinstancebeans.Resources
import org.onap.so.serviceinstancebeans.Service
import org.onap.so.serviceinstancebeans.SubscriberInfo
import org.onap.so.serviceinstancebeans.VfModules
import org.onap.so.serviceinstancebeans.Vnfs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.Response

class DoModifyCoreNSSI extends DoCommonCoreNSSI {

    private final String PREFIX ="DoModifyCoreNSSI"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()

    private static final Logger LOGGER = LoggerFactory.getLogger( DoModifyCoreNSSI.class)

    /**
     * Creates Slice Profile Instance
     * @param execution
     */
    void createSliceProfileInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start createSliceProfileInstance")

        def currentNSSI = execution.getVariable("currentNSSI")

        String sliceProfileID = execution.getVariable("sliceProfileID")
        Map<String, Object> sliceProfileMap = execution.getVariable("sliceProfileCn")
        Map<String, Object> serviceProfileMap = execution.getVariable("serviceProfile")

        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String serviceType = currentNSSI['serviceType']
        String nssiId = currentNSSI['nssiId']

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setServiceAreaDimension("")
        sliceProfile.setPayloadSize(0)
        sliceProfile.setJitter(0)
        sliceProfile.setSurvivalTime(0)
        sliceProfile.setExpDataRate(0)
        sliceProfile.setTrafficDensity(0)
        sliceProfile.setConnDensity(0)
        sliceProfile.setSNssai(sliceProfileMap.get("sNSSAI").toString())
        sliceProfile.setExpDataRateUL(Integer.parseInt(sliceProfileMap.get("expDataRateUL").toString()))
        sliceProfile.setExpDataRateDL(Integer.parseInt(sliceProfileMap.get("expDataRateDL").toString()))
        sliceProfile.setActivityFactor(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        sliceProfile.setResourceSharingLevel(sliceProfileMap.get("activityFactor").toString())
        sliceProfile.setUeMobilityLevel(serviceProfileMap.get("uEMobilityLevel").toString())
        sliceProfile.setCoverageAreaTAList(serviceProfileMap.get("coverageAreaTAList").toString())
        sliceProfile.setMaxNumberOfUEs(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        sliceProfile.setLatency(Integer.parseInt(sliceProfileMap.get("latency").toString()))
        sliceProfile.setProfileId(sliceProfileID)
        sliceProfile.setE2ELatency(0)

        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(nssiId).sliceProfile(sliceProfileID))
            client.create(uri, sliceProfile)

            currentNSSI['createdSliceProfile'] = sliceProfile
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile create call:" + ex.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit createSliceProfileInstance")
    }


    /**
     * Creates Slice Profile association with NSSI
     * @param execution
     */
    void associateSliceProfileInstanceWithNSSI(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start associateSliceProfileInstanceWithNSSI")

        String sliceProfileID = execution.getVariable("sliceProfileID")

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String serviceType = currentNSSI['serviceType']
        String nssiId = currentNSSI['nssiId']

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))
        AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(nssiId).sliceProfile(sliceProfileID))
        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        try {
            SliceProfile createdSliceProfile = (SliceProfile)currentNSSI['createdSliceProfile']
            ServiceInstance nssi = (ServiceInstance)currentNSSI['nssi']
            List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()
            associatedProfiles.add(createdSliceProfile)

            getAAIClient().update(nssiUri, nssi)

            getAAIClient().connect(sliceProfileUri, nssiUri, AAIEdgeLabel.BELONGS_TO)
        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile association with NSSI disconnect call: " + e.getMessage())
        }

        LOGGER.trace("${PREFIX} Exit associateSliceProfileInstanceWithNSSI")
    }


    @Override
    String getPrefix() {
        return PREFIX
    }

}
