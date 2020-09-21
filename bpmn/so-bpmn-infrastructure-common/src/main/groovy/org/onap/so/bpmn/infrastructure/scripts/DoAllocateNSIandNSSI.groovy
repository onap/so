/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
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

import static org.apache.commons.lang3.StringUtils.isBlank
import javax.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.beans.nsmf.AllocateAnNssi
import org.onap.so.beans.nsmf.AllocateCnNssi
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.AnSliceProfile
import org.onap.so.beans.nsmf.CnSliceProfile
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest
import org.onap.so.beans.nsmf.ResponseDescriptor
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.beans.nsmf.SliceTaskInfo
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter
import org.onap.so.beans.nsmf.TnSliceProfile
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoAllocateNSIandNSSI extends org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger(DoAllocateNSIandNSSI.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    AAIResourcesClient client = getAAIClient()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */

    void preProcessRequest (DelegateExecution execution) {
        String msg = ""
        logger.trace("Enter preProcessRequest()")
        Map<String, Object> nssiMap = new HashMap<>()
        execution.setVariable("nssiMap", nssiMap)
        boolean isMoreNSSTtoProcess = true
        execution.setVariable("isMoreNSSTtoProcess", isMoreNSSTtoProcess)
        List<String> nsstSequence = new ArrayList<>(Arrays.asList("cn"))
        execution.setVariable("nsstSequence", nsstSequence)
        logger.trace("Exit preProcessRequest")
    }

    /**
     * Process NSI options
     * @param execution
     */
    void retriveSliceOption(DelegateExecution execution) {
        logger.trace("Enter retriveSliceOption() of DoAllocateNSIandNSSI")

        boolean isNSIOptionAvailable

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        if(isBlank(sliceParams.getSuggestNsiId())) {
            isNSIOptionAvailable = false
        }
        else {
            isNSIOptionAvailable = true
            execution.setVariable('nsiServiceInstanceId', sliceParams.getSuggestNsiId())
            execution.setVariable('nsiServiceInstanceName', sliceParams.getSuggestNsiName())
        }
        execution.setVariable("isNSIOptionAvailable", isNSIOptionAvailable)
        logger.trace("Exit retriveSliceOption() of DoAllocateNSIandNSSI")
    }


    /**
     * create nsi instance in aai
     * @param execution
     */
    void createNSIinAAI(DelegateExecution execution) {
        logger.debug("Enter CreateNSIinAAI in DoAllocateNSIandNSSI()")

        String sliceInstanceId = UUID.randomUUID().toString()
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        //set new nsiId to sliceParams suggestNsiId
        sliceParams.setSuggestNsiId(sliceInstanceId)

        ServiceInstance nsi = new ServiceInstance()


        String sliceInstanceName = "nsi_"+execution.getVariable("sliceServiceInstanceName")
        String serviceType = execution.getVariable("serviceType")
        String serviceStatus = "deactivated"
        String modelInvariantUuid = sliceParams.getNSTInfo().invariantUUID
        String modelUuid = sliceParams.getNSTInfo().UUID

        String uuiRequest = execution.getVariable("uuiRequest")
        String serviceInstanceLocationid = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.plmnIdList")
        String serviceRole = "nsi"

        execution.setVariable("sliceInstanceId", sliceInstanceId)
        nsi.setServiceInstanceId(sliceInstanceId)
        nsi.setServiceInstanceName(sliceInstanceName)
        nsi.setServiceType(serviceType)
        nsi.setOrchestrationStatus(serviceStatus)
        nsi.setModelInvariantId(modelInvariantUuid)
        nsi.setModelVersionId(modelUuid)
        nsi.setServiceInstanceLocationId(serviceInstanceLocationid)
        nsi.setServiceRole(serviceRole)
        String msg
        try {

            AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sliceInstanceId))
            client.create(nsiServiceUri, nsi)

            execution.setVariable("nsiServiceUri", nsiServiceUri)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug("Exit CreateNSIinAAI in DoAllocateNSIandNSSI()")
    }


    /**
     * create relationship between nsi and service profile instance
     * @param execution
     */
    void createRelationship(DelegateExecution execution) {
        //relation ship
        String allottedResourceId = execution.getVariable("allottedResourceId")
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        AAIResourceUri nsiServiceUri = execution.getVariable("nsiServiceUri") as AAIResourceUri
        logger.info("Creating Allotted resource relationship, nsiServiceUri: " + nsiServiceUri)

        AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sliceParams.suggestNsiId).allottedResource(allottedResourceId))

        client.connect(allottedResourceUri, nsiServiceUri)
    }

    /**
     *
     * @param execution
     */
    void updateRelationship(DelegateExecution execution) {
        logger.debug("Enter update relationship in DoAllocateNSIandNSSI()")
        //todo: allottedResourceId
        String allottedResourceId = execution.getVariable("allottedResourceId")
        //Need to check whether nsi exist : Begin

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        String nsiServiceInstanceID = sliceParams.getSuggestNsiId()
        //sliceParams.setServiceId(nsiServiceInstanceID)

        AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(nsiServiceInstanceID))

        try {
            AAIResultWrapper wrapper = client.get(nsiServiceUri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            //todo: if exists
            if (!si.ifPresent()) {
                String msg = "NSI suggested in the option doesn't exist. " + nsiServiceInstanceID
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }

            AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sliceParams.suggestNsiId).allottedResource(allottedResourceId))

            client.connect(allottedResourceUri, nsiServiceUri)

            execution.setVariable("sliceTaskParams", sliceParams)
        }catch(BpmnError e) {
            throw e
        }catch (Exception ex){
            String msg = "NSI suggested in the option doesn't exist. " + nsiServiceInstanceID
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Exit update relationship in DoAllocateNSIandNSSI()")
    }

    /**
     * create RAN Slice Profile Instance
     * @param execution
     */
    void createAnSliceProfileInstance(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        String serviceInstanceId = UUID.randomUUID().toString()
        execution.setVariable("ranSliceProfileInstanceId", serviceInstanceId) //todo:

        String serviceType = ""
        String serviceRole = "slice-profile"
        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<AnSliceProfile> sliceTaskInfo = sliceParams.anSliceTaskInfo
        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)

        // create slice profile
        ServiceInstance rspi = new ServiceInstance()
        rspi.setServiceInstanceName(sliceTaskInfo.NSSTInfo.name)
        rspi.setServiceType(serviceType)
        rspi.setServiceRole(serviceRole)
        rspi.setOrchestrationStatus(oStatus)
        rspi.setModelInvariantId(sliceTaskInfo.NSSTInfo.invariantUUID)
        rspi.setModelVersionId(sliceTaskInfo.NSSTInfo.UUID)
        rspi.setInputParameters(uuiRequest)
        rspi.setWorkloadContext(useInterval)
        rspi.setEnvironmentContext(sNSSAI_id)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))
        client.create(uri, rspi)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create An Slice Profile
     * @param execution
     */
    void createAnSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        //String serviceInstanceId = execution.getVariable("ranSliceProfileInstanceId")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<AnSliceProfile> sliceTaskInfo = sliceParams.anSliceTaskInfo
        AnSliceProfile anSliceProfile = sliceTaskInfo.sliceProfile

        String profileId = UUID.randomUUID().toString()
        anSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setCoverageAreaTAList(anSliceProfile.coverageAreaTAList as String)
        //todo:...
        AAIResourceUri uri = AAIUriFactory.createResourceUri(
            AAIFluentTypeBuilder.business().customer(globalSubscriberId)
            .serviceSubscription(subscriptionServiceType)
            .serviceInstance(sliceTaskInfo.sliceInstanceId)
            .sliceProfile(profileId)
        )
        client.create(uri, sliceProfile)
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * prepare AllocateAnNssi
     * @param execution
     */
    void prepareAllocateAnNssi(DelegateExecution execution) {

        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<AnSliceProfile> sliceTaskInfo = sliceParams.anSliceTaskInfo

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateAnNssi allocateAnNssi = new AllocateAnNssi()
        allocateAnNssi.nsstId = sliceTaskInfo.NSSTInfo.UUID
        allocateAnNssi.nssiId = sliceTaskInfo.NSSTInfo.UUID
        allocateAnNssi.nssiName = sliceTaskInfo.NSSTInfo.name
        allocateAnNssi.sliceProfile = sliceTaskInfo.sliceProfile
        allocateAnNssi.nsiInfo.nsiId = sliceParams.suggestNsiId

        EsrInfo esrInfo = new EsrInfo()
        //todo: vendor and network
        esrInfo.setVendor(sliceTaskInfo.getVendor())
        esrInfo.setNetworkType(sliceTaskInfo.getNetworkType())

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        //todo: service info
        ServiceInfo serviceInfo = new ServiceInfo()
        serviceInfo.globalSubscriberId = globalSubscriberId
        serviceInfo.subscriptionServiceType = subscriptionServiceType
        serviceInfo.nsiId = sliceParams.suggestNsiId
        serviceInfo.serviceInvariantUuid = sliceTaskInfo.NSSTInfo.invariantUUID
        serviceInfo.serviceUuid = sliceTaskInfo.NSSTInfo.UUID

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateAnNssi(allocateAnNssi)

        execution.setVariable("AnAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("anBHSliceTaskInfo", sliceTaskInfo)
    }


    /**
     * create RAN Slice Profile Instance
     * @param execution
     */
    void createCnSliceProfileInstance(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        String serviceInstanceId = UUID.randomUUID().toString()
        execution.setVariable("cnSliceProfileInstanceId", serviceInstanceId) //todo:

        String serviceType = ""
        String serviceRole = "slice-profile"
        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<CnSliceProfile> sliceTaskInfo = sliceParams.cnSliceTaskInfo
        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)

        // create slice profile
        ServiceInstance rspi = new ServiceInstance()
        rspi.setServiceInstanceName(sliceTaskInfo.NSSTInfo.name)
        rspi.setServiceType(serviceType)
        rspi.setServiceRole(serviceRole)
        rspi.setOrchestrationStatus(oStatus)
        rspi.setModelInvariantId(sliceTaskInfo.NSSTInfo.invariantUUID)
        rspi.setModelVersionId(sliceTaskInfo.NSSTInfo.UUID)
        rspi.setInputParameters(uuiRequest)
        rspi.setWorkloadContext(useInterval)
        rspi.setEnvironmentContext(sNSSAI_id)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))
        client.create(uri, rspi)
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create An Slice Profile
     * @param execution
     */
    void createCnSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        //String serviceInstanceId = execution.getVariable("ranSliceProfileInstanceId")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<CnSliceProfile> sliceTaskInfo = sliceParams.cnSliceTaskInfo
        CnSliceProfile cnSliceProfile = sliceTaskInfo.sliceProfile

        String profileId = UUID.randomUUID().toString()
        cnSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setCoverageAreaTAList(cnSliceProfile.coverageAreaTAList as String)
        //todo:...
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(sliceTaskInfo.sliceInstanceId).sliceProfile(profileId))
        client.create(uri, sliceProfile)
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * prepare AllocateCnNssi
     * @param execution
     */
    void prepareAllocateCnNssi(DelegateExecution execution) {

        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<CnSliceProfile> sliceTaskInfo = sliceParams.cnSliceTaskInfo

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateCnNssi allocateCnNssi = new AllocateCnNssi()
        allocateCnNssi.nsstId = sliceTaskInfo.NSSTInfo.UUID
        allocateCnNssi.nssiId = sliceTaskInfo.NSSTInfo.UUID
        allocateCnNssi.nssiName = sliceTaskInfo.NSSTInfo.name
        allocateCnNssi.sliceProfile = sliceTaskInfo.sliceProfile
        allocateCnNssi.nsiInfo.nsiId = sliceParams.suggestNsiId

        EsrInfo esrInfo = new EsrInfo()
        //todo: vendor and network
        esrInfo.setVendor(sliceTaskInfo.getVendor())
        esrInfo.setNetworkType(sliceTaskInfo.getNetworkType())

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        //todo: service info
        ServiceInfo serviceInfo = new ServiceInfo()
        serviceInfo.globalSubscriberId = globalSubscriberId
        serviceInfo.subscriptionServiceType = subscriptionServiceType
        serviceInfo.nsiId = sliceParams.suggestNsiId
        serviceInfo.serviceInvariantUuid = sliceTaskInfo.NSSTInfo.invariantUUID
        serviceInfo.serviceUuid = sliceTaskInfo.NSSTInfo.UUID

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateCnNssi(allocateCnNssi)

        execution.setVariable("CnAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("cnSliceTaskInfo", sliceTaskInfo)
    }


    /**
     * create TN Slice Profile Instance
     * @param execution
     */
    void createTnBHSliceProfileInstance(DelegateExecution execution) {
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        String serviceType = ""
        String serviceRole = "slice-profile"
        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<TnSliceProfile> sliceTaskInfo = sliceParams.tnBHSliceTaskInfo
        String serviceInstanceId = UUID.randomUUID().toString()

        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)
        //execution.setVariable("cnSliceProfileInstanceId", serviceInstanceId) //todo:

        // create slice profile
        ServiceInstance rspi = new ServiceInstance()
        rspi.setServiceInstanceName(sliceTaskInfo.NSSTInfo.name)
        rspi.setServiceType(serviceType)
        rspi.setServiceRole(serviceRole)
        rspi.setOrchestrationStatus(oStatus)
        rspi.setModelInvariantId(sliceTaskInfo.NSSTInfo.invariantUUID)
        rspi.setModelVersionId(sliceTaskInfo.NSSTInfo.UUID)
        rspi.setInputParameters(uuiRequest)
        rspi.setWorkloadContext(useInterval)
        rspi.setEnvironmentContext(sNSSAI_id)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))
        client.create(uri, rspi)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create An Slice Profile
     * @param execution
     */
    void createTnBHSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<TnSliceProfile> sliceTaskInfo = sliceParams.tnBHSliceTaskInfo

        TnSliceProfile tnSliceProfile = sliceTaskInfo.sliceProfile
        String profileId = UUID.randomUUID().toString()
        tnSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        //todo:...
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(sliceTaskInfo.sliceInstanceId).sliceProfile(profileId))
        client.create(uri, sliceProfile)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * prepare AllocateCnNssi
     * @param execution
     */
    void prepareAllocateTnBHNssi(DelegateExecution execution) {

        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<TnSliceProfile> sliceTaskInfo = sliceParams.tnBHSliceTaskInfo

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateTnNssi allocateTnNssi = new AllocateTnNssi()
        //todo: AllocateTnNssi
        allocateTnNssi.setTransportSliceNetworks()
        allocateTnNssi.setNetworkSliceInfos()


        //allocateTnNssi.networkSliceInfos

        EsrInfo esrInfo = new EsrInfo()
        esrInfo.setVendor(sliceTaskInfo.getVendor())
        esrInfo.setNetworkType(sliceTaskInfo.getNetworkType())

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        ServiceInfo serviceInfo = new ServiceInfo()
        serviceInfo.globalSubscriberId = globalSubscriberId
        serviceInfo.subscriptionServiceType = subscriptionServiceType
        serviceInfo.nsiId = sliceParams.suggestNsiId
        serviceInfo.serviceInvariantUuid = sliceTaskInfo.NSSTInfo.invariantUUID
        serviceInfo.serviceUuid = sliceTaskInfo.NSSTInfo.UUID

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateTnNssi(allocateTnNssi)

        execution.setVariable("TnBHAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("tnBHSliceTaskInfo", sliceTaskInfo)
    }

    /**
     * Update relationship between
     * 1. NSI and NSSI
     * 2. Slice Profile and Service Profile
     * 3. SliceProfile and NSSI
     * 4. sliceProfile and endpoint
     *
     * @param execution
     */
    public void updateAnRelationship(DelegateExecution execution) {
        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        ResponseDescriptor result = execution.getVariable("anNssiAllocateResult") as ResponseDescriptor
        String nssiId = result.getNssiId()
        String endPointId = result.getEndPointId()
        String nsiId = sliceParams.getSuggestNsiId()
        String sliceProfileInstanceId = sliceParams.anSliceTaskInfo.sliceInstanceId
        String serviceProfileInstanceId = sliceParams.serviceId
        //nsi id

        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution, sliceProfileInstanceId, nssiId)

        updateRelationship(execution, sliceProfileInstanceId, endPointId)

    }


    /**
     * Update relationship between
     * 1. NSI and NSSI
     * 2. Slice Profile and Service Profile
     * 3. SliceProfile and NSSI
     *
     * @param execution
     */
    public void updateCnRelationship(DelegateExecution execution) {
        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        NssiResponse result = execution.getVariable("cnNssiAllocateResult") as NssiResponse
        String nssiId = result.getNssiId()
        String nsiId = sliceParams.getSuggestNsiId()
        String sliceProfileInstanceId = sliceParams.cnSliceTaskInfo.sliceInstanceId
        String serviceProfileInstanceId = sliceParams.serviceId
        //nsi id

        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution,sliceProfileInstanceId, nssiId)


    }

    /**
     * Update relationship between
     * 1. NSI and NSSI
     * 2. Slice Profile and Service Profile
     * 3. SliceProfile and NSSI
     *
     * @param execution
     */
    public void updateTnBHRelationship(DelegateExecution execution) {
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        NssiResponse result = execution.getVariable("tnBHNssiAllocateResult") as NssiResponse
        String nssiId = result.getNssiId()
        String nsiId = sliceParams.getSuggestNsiId()
        String sliceProfileInstanceId = sliceParams.tnBHSliceTaskInfo.sliceInstanceId
        String serviceProfileInstanceId = sliceParams.serviceId
        //nsi id

        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution,sliceProfileInstanceId, nssiId)
    }

    /**
     * sourceId -> targetId
     * @param execution
     * @param sourceId
     * @param targetId
     */
    void updateRelationship(DelegateExecution execution, String sourceId, String targetId) {
        //relation ship
        Relationship relationship = new Relationship()

        AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(targetId))

        logger.info("Creating Allotted resource relationship, nsiServiceUri: " + nsiServiceUri)

        relationship.setRelatedLink(nsiServiceUri.build().toString())

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sourceId).relationshipAPI())
        client.create(uri, relationship)
    }

}
