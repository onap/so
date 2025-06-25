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
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.v19.AllottedResource
import org.onap.aai.domain.yang.v19.AllottedResources
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aai.domain.yang.v19.SliceProfiles
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isAllLowerCase
import static org.apache.commons.lang3.StringUtils.isBlank

class DoModifyCoreNSSI extends DoCommonCoreNSSI {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoModifyCoreNSSI.class)
    private static final ObjectMapper mapper = new ObjectMapper()
    private final String PREFIX ="DoModifyCoreNSSI"
    private final String ACTION = "Modify"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private MsoUtils utils = new MsoUtils()
    private JsonUtils jsonUtil = new JsonUtils()

    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start preProcessRequest")

        super.preProcessRequest(execution)

        String modifyAction = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "modifyAction")
        if (isBlank(modifyAction)) {
            String msg = "modifyAction is mandatory parameter"
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        else {
            String createSliceProfileInstance = ""
            if(modifyAction.equals("allocate")) { // In case Slice Profile should be created
                createSliceProfileInstance = "true"
            }
            else if(modifyAction.equals("deallocate")) { // In case Slice Profile should be created
                createSliceProfileInstance = "false"
            }
            else {
                String msg = "Value of modifyAction parameter should be either allocate or deallocate"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            execution.setVariable("isCreateSliceProfileInstance", createSliceProfileInstance)
        }

        execution.setVariable("operationType", "MODIFY")

        LOGGER.debug("${getPrefix()} Exit preProcessRequest")
    }


    /**
     * Prepares Slice Profile
     * @param execution
     * @return SLice Profile
     */
    SliceProfile prepareSliceProfile(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start prepareSliceProfile")

        def currentNSSI = execution.getVariable("currentNSSI")

        String givenSliceProfileId = currentNSSI['sliceProfileId'] //UUID.randomUUID().toString()
        Map<String,Object> sliceProfileMap = mapper.readValue(currentNSSI['sliceProfile'], Map.class)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setServiceAreaDimension("")
        sliceProfile.setPayloadSize(0)
        sliceProfile.setJitter(0)
        sliceProfile.setSurvivalTime(0)
        sliceProfile.setExpDataRate(0)
        sliceProfile.setTrafficDensity(0)
        sliceProfile.setConnDensity(0)
        sliceProfile.setSNssai(currentNSSI['S-NSSAI'])

        if(sliceProfileMap.get("expDataRateUL") != null) {
            sliceProfile.setExpDataRateUL(Integer.parseInt(sliceProfileMap.get("expDataRateUL").toString()))
        }

        if(sliceProfileMap.get("expDataRateDL") != null) {
            sliceProfile.setExpDataRateDL(Integer.parseInt(sliceProfileMap.get("expDataRateDL").toString()))
        }

        if(sliceProfileMap.get("activityFactor") != null) {
            sliceProfile.setActivityFactor(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        }

        if(sliceProfileMap.get("resourceSharingLevel") != null) {
            sliceProfile.setResourceSharingLevel(sliceProfileMap.get("resourceSharingLevel").toString())
        }

        if(sliceProfileMap.get("uEMobilityLevel") != null) {
            sliceProfile.setUeMobilityLevel(sliceProfileMap.get("uEMobilityLevel").toString())
        }

        if(sliceProfileMap.get("coverageAreaTAList") != null) {
            sliceProfile.setCoverageAreaTAList(sliceProfileMap.get("coverageAreaTAList").toString())
        }

        if(sliceProfileMap.get("maxNumberOfUEs") != null) {
            sliceProfile.setMaxNumberOfUEs(Integer.parseInt(sliceProfileMap.get("maxNumberOfUEs").toString()))
        }

        if(sliceProfileMap.get("latency") != null) {
            sliceProfile.setLatency(Integer.parseInt(sliceProfileMap.get("latency").toString()))
        }

        sliceProfile.setProfileId(givenSliceProfileId)
        sliceProfile.setE2ELatency(0)

        LOGGER.debug("${PREFIX} Exit prepareSliceProfile")

        return sliceProfile
    }


    /**
     * Prepares Slice Profile Instance
     * @param execution
     * @return Slice Profile Instance
     */
    ServiceInstance prepareSliceProfileInstance(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start prepareSliceProfileInstance")

        def currentNSSI = execution.getVariable("currentNSSI")

        ServiceInstance sliceProfileInstance = new ServiceInstance()
        String sliceProfileInstanceId = UUID.randomUUID().toString()
        sliceProfileInstance.setServiceInstanceId(sliceProfileInstanceId)


        String sliceInstanceName = "sliceprofile_" + sliceProfileInstanceId
        sliceProfileInstance.setServiceInstanceName(sliceInstanceName)

        String serviceType = jsonUtil.getJsonValue(currentNSSI['sliceProfile'], "sST")
        sliceProfileInstance.setServiceType(serviceType)

        String serviceStatus = "deactivated"
        sliceProfileInstance.setOrchestrationStatus(serviceStatus)

        String serviceInstanceLocationid = jsonUtil.getJsonValue(currentNSSI['sliceProfile'], "pLMNIdList")
        sliceProfileInstance.setServiceInstanceLocationId(serviceInstanceLocationid)

        String serviceRole = "slice-profile-instance"
        sliceProfileInstance.setServiceRole(serviceRole)
        List<String> snssaiList = (List<String>)currentNSSI['S-NSSAIs']
        String snssai = snssaiList.get(0)

        sliceProfileInstance.setEnvironmentContext(snssai)
        sliceProfileInstance.setWorkloadContext("CN-NF")

        // TO DO: Model info

        LOGGER.debug("${PREFIX} Exit prepareSliceProfileInstance")

        return sliceProfileInstance
    }



    /**
     * Creates Slice Profile Instance
     * @param execution
     */
    void createSliceProfileInstance(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start createSliceProfileInstance")

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        SliceProfile sliceProfile = prepareSliceProfile(execution)

        ServiceInstance sliceProfileInstance = prepareSliceProfileInstance(execution)

        SliceProfiles sliceProfiles = new SliceProfiles()
        sliceProfiles.getSliceProfile().add(sliceProfile)
        sliceProfileInstance.setSliceProfiles(sliceProfiles)

        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).
                                                                serviceInstance(sliceProfileInstance.getServiceInstanceId()))
            client.create(uri, sliceProfileInstance)

            currentNSSI['createdSliceProfileInstanceId'] = sliceProfileInstance.getServiceInstanceId()
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occurred while Slice Profile create call:" + ex.getMessage())
        }

        LOGGER.debug("${PREFIX} Exit createSliceProfileInstance")
    }


    /**
     * Creates Allotted Resource
     * @param execution
     * @return AllottedResource
     */
    AllottedResource createAllottedResource(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start createAllottedResource")

        def currentNSSI = execution.getVariable("currentNSSI")

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        String sliceProfileInstanceId = currentNSSI['createdSliceProfileInstanceId']

        AllottedResource allottedResource = new AllottedResource()

        String allottedResourceId = UUID.randomUUID().toString()

        allottedResource.setId(allottedResourceId)

        // TO DO: No other info

        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(sliceProfileInstanceId).allottedResource(allottedResourceId))

            client.create(allottedResourceUri, allottedResource)

            currentNSSI['allottedResourceUri'] = allottedResourceUri
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occurred while Allotted Resource create call:" + ex.getMessage())
        }

        LOGGER.debug("${PREFIX} Exit createAllottedResource")

        return allottedResource
    }



    /**
     * Creates Slice Profile association with NSSI
     * @param execution
     */
    void associateSliceProfileInstanceWithNSSI(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start associateSliceProfileInstanceWithNSSI")

        def currentNSSI = execution.getVariable("currentNSSI")

        String nssiId = currentNSSI['nssiId']

        String sliceProfileInstanceId = currentNSSI['createdSliceProfileInstanceId']

        AAIResourcesClient client = getAAIClient()

        // Creates Allotted Resource
        AllottedResource allottedResource = createAllottedResource(execution)
        AAIResourceUri allottedResourceUri = (AAIResourceUri)currentNSSI['allottedResourceUri']

        // Associates Allotted Resource with Slice Profile Instance
        try {
            AAIResourceUri sliceProfileInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(sliceProfileInstanceId))
            Optional<ServiceInstance> sliceProfileInstanceOpt = client.get(ServiceInstance.class, sliceProfileInstanceUri)
            if (sliceProfileInstanceOpt.isPresent()) {
             /*   ServiceInstance sliceProfileInstance = sliceProfileInstanceOpt.get()

                AllottedResources allottedResources = sliceProfileInstance.getAllottedResources()
                if(allottedResources == null) {
                    allottedResources = new AllottedResources()
                }

                allottedResources.getAllottedResource().add(allottedResource)
                sliceProfileInstance.setAllottedResources(allottedResources)

                client.update(sliceProfileInstanceUri, sliceProfileInstance) */

                client.connect(sliceProfileInstanceUri, allottedResourceUri)
            }
            else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, "No slice profile instance found with id = " + sliceProfileInstanceId)
            }

        } catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while Slice Profile Instance update call: " + e.getMessage())
        }


        // Associates NSSI with Allotted Resource
        try {
            AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))
            client.connect(allottedResourceUri, nssiUri)
        } catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 25000, "Exception occured while NSSI with Allotted Resource connect call: " + e.getMessage())
        }

        LOGGER.debug("${PREFIX} Exit associateSliceProfileInstanceWithNSSI")
    }


    @Override
    void checkAssociatedProfiles(DelegateExecution execution, List<SliceProfile> associatedProfiles, ServiceInstance nssi) {
        LOGGER.debug("${PREFIX} Start checkAssociatedProfiles")

        LOGGER.debug("associatedProfiles == null = " + (associatedProfiles == null))
        if(associatedProfiles == null || associatedProfiles.isEmpty()) {
            String isCreateSliceProfileInstanceVar = execution.getVariable("isCreateSliceProfileInstance" )
            boolean isCreateSliceProfileInstance = Boolean.parseBoolean(isCreateSliceProfileInstanceVar)

            if(!isCreateSliceProfileInstance) { // New Slice Profile Instance should not be created
                String msg = String.format("No associated profiles found for NSSI %s in AAI", nssi.getServiceInstanceId())
                LOGGER.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        }

        LOGGER.debug("${PREFIX} Exit checkAssociatedProfiles")
    }


    /**
     * Calculates a final list of S-NSSAI
     * @param execution
     */
    void calculateSNSSAI(DelegateExecution execution) {
        LOGGER.debug("${getPrefix()} Start calculateSNSSAI")

        def currentNSSI = execution.getVariable("currentNSSI")

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI['associatedProfiles']

        String currentSNSSAI = currentNSSI['S-NSSAI']

        String givenSliceProfileId = currentNSSI['sliceProfileId']

        List<String> snssais = new ArrayList<>()

        String isCreateSliceProfileInstanceVar = execution.getVariable("isCreateSliceProfileInstance" )

        boolean isCreateSliceProfileInstance = Boolean.parseBoolean(isCreateSliceProfileInstanceVar)

        if(isCreateSliceProfileInstance) { // Slice Profile Instance has to be created
            for (SliceProfile associatedProfile : associatedProfiles) {
                snssais.add(associatedProfile.getSNssai())
            }

            snssais.add(currentSNSSAI)
        }
        else { // Slice profile instance has to be deleted
            if(associatedProfiles != null) {
                for (SliceProfile associatedProfile : associatedProfiles) {
                    if (!associatedProfile.getProfileId().equals(givenSliceProfileId)) { // not given profile id
                        LOGGER.debug("calculateSNSSAI: associatedProfile.getSNssai()" + associatedProfile.getSNssai())
                        snssais.add(associatedProfile.getSNssai())
                    } else {
                        currentNSSI['sliceProfileS-NSSAI'] = associatedProfile
                    }
                }
            }
        }

        currentNSSI['S-NSSAIs'] = snssais

        LOGGER.debug("${getPrefix()} Exit calculateSNSSAI")
    }


    private String getPrefix() {
        return PREFIX
    }


    @Override
    String getAction() {
        return ACTION
    }
}
