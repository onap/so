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

import org.onap.aai.domain.yang.NetworkRoute
import org.onap.so.beans.nsmf.ConnectionLink
import org.onap.so.beans.nsmf.EndPoint
import org.onap.so.beans.nsmf.NsiInfo
import org.onap.so.beans.nsmf.SliceProfileAdapter
import org.onap.so.beans.nsmf.TransportSliceNetwork
import org.onap.so.beans.nsmf.oof.SubnetType
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import javax.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aaiclient.client.aai.AAINamespaceConstants
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.beans.nsmf.AllocateAnNssi
import org.onap.so.beans.nsmf.AllocateCnNssi
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.beans.nsmf.SliceTaskInfo
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.apache.commons.lang3.StringUtils.isBlank
import org.onap.aaiclient.client.aai.AAIObjectType

class DoAllocateNSIandNSSI extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger(DoAllocateNSIandNSSI.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    AnNssmfUtils anNssmfUtils = new AnNssmfUtils()

    AAIResourcesClient client = getAAIClient()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */

    void preProcessRequest (DelegateExecution execution) {
        logger.trace("Enter preProcessRequest()")
        Map<String, Object> nssiMap = new HashMap<>()
        int nsstCount=execution.getVariable("nsstCount") as int
        if(nsstCount==6){
            execution.setVariable("processFHandMH", true)
        }
        else{
            execution.setVariable("processFHandMH", false)
        }
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
        ServiceInstance nsi = new ServiceInstance()

        String sliceInstanceName = "nsi_"+execution.getVariable("sliceServiceInstanceName")
        String serviceType = sliceParams.serviceProfile.get("sST")
        String serviceStatus = "deactivated"
        String modelInvariantUuid = sliceParams.getNSTInfo().invariantUUID
        String modelUuid = sliceParams.getNSTInfo().UUID
        String serviceFunction = sliceParams.serviceProfile.get("resourceSharingLevel")

        sliceParams.setSuggestNsiId(sliceInstanceId)
        sliceParams.setSuggestNsiName(sliceInstanceName)

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
        nsi.setServiceFunction(serviceFunction)
        String msg
        try {

            AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(execution.getVariable("globalSubscriberId"))
                    .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                    .serviceInstance(sliceInstanceId))
            client.create(nsiServiceUri, nsi)

            execution.setVariable("nsiServiceUri", nsiServiceUri)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in DoAllocateNSIandNSSI.createNSIinAAI: " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        execution.setVariable("sliceTaskParams", sliceParams)

        logger.debug("Exit CreateNSIinAAI in DoAllocateNSIandNSSI()")
    }


    /**
     * create relationship between nsi and service profile instance
     * @param execution
     */
    void createRelationship(DelegateExecution execution) {
        //relation ship
        logger.debug("Enter createRelationship in DoAllocateNSIandNSSI")
        //String allottedResourceId = execution.getVariable("allottedResourceId")
        //SliceTaskParamsAdapter sliceParams =
        //        execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        String msg
        try {

            AAIResourceUri nsiServiceUri = execution.getVariable("nsiServiceUri") as AAIResourceUri
            logger.debug("Creating Allotted resource relationship, nsiServiceUri: " + nsiServiceUri.toString())

            //AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sliceParams.suggestNsiId).allottedResource(allottedResourceId))

            AAIResourceUri allottedResourceUri = execution.getVariable("allottedResourceUri") as AAIResourceUri
            logger.debug("Creating Allotted resource relationship, allottedResourceUri: " + allottedResourceUri.toString())

            client.connect(allottedResourceUri, nsiServiceUri)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in DoAllocateNSIandNSSI.createRelationship. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Exit createRelationship in DoAllocateNSIandNSSI")
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

        AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(execution.getVariable("globalSubscriberId"))
                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                .serviceInstance(nsiServiceInstanceID))

        try {
            AAIResultWrapper wrapper = client.get(nsiServiceUri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            if (!si.isPresent()) {
                String msg = "NSI suggested in the option doesn't exist. " + nsiServiceInstanceID
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }

            //AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sliceParams.suggestNsiId).allottedResource(allottedResourceId))
            AAIResourceUri allottedResourceUri = execution.getVariable("allottedResourceUri") as AAIResourceUri
            logger.debug("updateRelationship Allotted resource relationship, allottedResourceUri: " + allottedResourceUri.toString())
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

        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.anSliceTaskInfo
        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)
        String sliceProfileName = "sliceprofile_an_" + sliceParams.serviceName

        // create slice profile
        ServiceInstance rspi = createSliceProfileInstance(sliceTaskInfo, sliceProfileName, oStatus)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(serviceInstanceId))
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
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.anSliceTaskInfo
        SliceProfileAdapter anSliceProfile = sliceTaskInfo.sliceProfile

        String profileId = UUID.randomUUID().toString()
        anSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setCoverageAreaTAList(anSliceProfile.coverageAreaTAList as String)
        sliceProfile.setMaxNumberOfUEs(anSliceProfile.maxNumberOfUEs)
        sliceProfile.setLatency(anSliceProfile.latency)
        sliceProfile.setMaxNumberOfPDUSession(anSliceProfile.maxNumberOfPDUSession)
        sliceProfile.setExpDataRateDL(anSliceProfile.expDataRateDL)
        sliceProfile.setExpDataRateUL(anSliceProfile.expDataRateUL)
        sliceProfile.setAreaTrafficCapDL(anSliceProfile.areaTrafficCapDL)
        sliceProfile.setAreaTrafficCapUL(anSliceProfile.areaTrafficCapUL)
        sliceProfile.setOverallUserDensity(anSliceProfile.overallUserDensity)
        sliceProfile.setActivityFactor(anSliceProfile.activityFactor)
        sliceProfile.setUeMobilityLevel(anSliceProfile.ueMobilityLevel)
        sliceProfile.setResourceSharingLevel(anSliceProfile.resourceSharingLevel)
        sliceProfile.setCsAvailabilityTarget(anSliceProfile.csAvailabilityTarget)
        sliceProfile.setCsReliabilityMeanTime(anSliceProfile.csReliabilityMeanTime)
        sliceProfile.setExpDataRate(anSliceProfile.expDataRate)
        sliceProfile.setMsgSizeByte(anSliceProfile.msgSizeByte)
        sliceProfile.setTransferIntervalTarget(anSliceProfile.transferIntervalTarget)
        sliceProfile.setSurvivalTime(anSliceProfile.survivalTime)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(
            AAIFluentTypeBuilder.business().customer(globalSubscriberId)
            .serviceSubscription(subscriptionServiceType)
            .serviceInstance(sliceTaskInfo.sliceInstanceId)
            .sliceProfile(profileId)
        )
        client.create(uri, sliceProfile)
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    void createANEndpoint(DelegateExecution execution){
        logger.debug("Enter createANEndpoint in DoAllocateNSIandNSSI()")
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.anSliceTaskInfo

        NetworkRoute route = new NetworkRoute()
        String routeId = UUID.randomUUID().toString()
        route.setRouteId(routeId)
        route.setType("endpoint")
        route.setRole("AN")
        route.setFunction("3gppTransportEP")
        route.setIpAddress( sliceTaskInfo.sliceProfile.ipAddress)
        route.setNextHop(sliceTaskInfo.sliceProfile.nextHopInfo)
        route.setLogicalInterfaceId(sliceTaskInfo.sliceProfile.logicInterfaceId)
        route.setAddressFamily("ipv4")
        route.setPrefixLength(24)
        sliceTaskInfo.setEndPointId(routeId)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(routeId))
        client.create(uri, route)
        execution.setVariable("sliceTaskParams", sliceParams)
        logger.info("an endpointId:" + sliceParams.anSliceTaskInfo.endPointId)
    }


    void createCNEndpoint(DelegateExecution execution){
        logger.debug("Enter createCNNetworkRoute in DoAllocateNSIandNSSI()")
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.cnSliceTaskInfo

        NetworkRoute route = new NetworkRoute()
        String routeId = UUID.randomUUID().toString()
        route.setRouteId(routeId)
        route.setType("endpoint")
        route.setRole("CN")
        route.setFunction("3gppTransportEP")
        route.setIpAddress( sliceTaskInfo.sliceProfile.ipAddress)
        route.setNextHop(sliceTaskInfo.sliceProfile.nextHopInfo)
        route.setLogicalInterfaceId(sliceTaskInfo.sliceProfile.logicInterfaceId)
        route.setAddressFamily("ipv4")
        route.setPrefixLength(24)

        sliceTaskInfo.setEndPointId(routeId)
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(routeId))
        client.create(uri, route)

        execution.setVariable("cnEndpointId", routeId)
        execution.setVariable("sliceTaskParams", sliceParams)
        logger.info("cn endpointId:" + sliceParams.cnSliceTaskInfo.endPointId)
    }

    /**
     * prepare AllocateAnNssi
     * @param execution
     */
    void prepareAllocateAnNssi(DelegateExecution execution) {

        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.getAnSliceTaskInfo()

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateAnNssi allocateAnNssi = new AllocateAnNssi()
        allocateAnNssi.setSliceProfile(sliceTaskInfo.getSliceProfile().trans2AnProfile())
        allocateAnNssi.getSliceProfile().setSliceProfileId(sliceTaskInfo.getSliceInstanceId())
        allocateAnNssi.setNsstId(sliceTaskInfo.getNSSTInfo().getUUID())
        allocateAnNssi.setNssiId(sliceTaskInfo.getSuggestNssiId())
        allocateAnNssi.setNssiName("nssi_an" + execution.getVariable("sliceServiceInstanceName") as String)
        allocateAnNssi.setScriptName(sliceTaskInfo.getScriptName())
        NsiInfo nsiInfo = new NsiInfo()
        nsiInfo.setNsiId(sliceParams.getSuggestNsiId())
        nsiInfo.setNsiName(sliceParams.getSuggestNsiName())
        allocateAnNssi.setNsiInfo(nsiInfo)
        //endPoint
        EndPoint endPoint = new EndPoint()
        endPoint.setIpAddress(sliceTaskInfo.getSliceProfile().getIpAddress())
        endPoint.setLogicInterfaceId(sliceTaskInfo.getSliceProfile().getLogicInterfaceId())
        endPoint.setNextHopInfo(sliceTaskInfo.getSliceProfile().getNextHopInfo())
        allocateAnNssi.setEndPoint(endPoint)

        EsrInfo esrInfo = new EsrInfo()
        //todo: vendor and network
        esrInfo.setVendor(sliceTaskInfo.getVendor())
        esrInfo.setNetworkType(sliceTaskInfo.getNetworkType())

        String globalSubscriberId = execution.getVariable("globalSubscriberId") as String
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType") as String

        //todo: service info
        ServiceInfo serviceInfo = ServiceInfo.builder()
                .globalSubscriberId(globalSubscriberId)
                .subscriptionServiceType(subscriptionServiceType)
                .nsiId(sliceParams.getSuggestNsiId())
                .serviceInvariantUuid(sliceTaskInfo.getNSSTInfo().getInvariantUUID())
                .serviceUuid(sliceTaskInfo.getNSSTInfo().getUUID())
                .sST(sliceTaskInfo.getSliceProfile().getSST() ?: sliceParams.getServiceProfile().get("sST") as String)
                .nssiName(sliceTaskInfo.getSuggestNssiId() ? sliceTaskInfo.getNSSTInfo().getName() : allocateAnNssi.getNssiName())
                .nssiId(sliceTaskInfo.getSuggestNssiId())
                .resourceSharingLevel(sliceParams.serviceProfile.get("resourceSharingLevel") as String)
                .build()

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateAnNssi(allocateAnNssi)

        execution.setVariable("AnAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("anSliceTaskInfo", sliceTaskInfo)
        execution.setVariable("anSubnetType", SubnetType.AN)
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

        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.cnSliceTaskInfo
        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)
        String sliceProfileName = "sliceprofile_cn_"+sliceParams.serviceName

        // create slice profile
        ServiceInstance rspi = createSliceProfileInstance(sliceTaskInfo, sliceProfileName, oStatus)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(serviceInstanceId))
        client.create(uri, rspi)
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create Cn Slice Profile
     * @param execution
     */
    void createCnSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        //String serviceInstanceId = execution.getVariable("ranSliceProfileInstanceId")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.cnSliceTaskInfo
        SliceProfileAdapter cnSliceProfile = sliceTaskInfo.sliceProfile

        String profileId = UUID.randomUUID().toString()
        cnSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setCoverageAreaTAList(cnSliceProfile.coverageAreaTAList as String)
        sliceProfile.setMaxNumberOfUEs(cnSliceProfile.maxNumberOfUEs)
        sliceProfile.setLatency(cnSliceProfile.latency)
        sliceProfile.setMaxNumberOfPDUSession(cnSliceProfile.maxNumberOfPDUSession)
        sliceProfile.setExpDataRateDL(cnSliceProfile.expDataRateDL)
        sliceProfile.setExpDataRateUL(cnSliceProfile.expDataRateUL)
        sliceProfile.setAreaTrafficCapDL(cnSliceProfile.areaTrafficCapDL)
        sliceProfile.setAreaTrafficCapUL(cnSliceProfile.areaTrafficCapUL)
        sliceProfile.setOverallUserDensity(cnSliceProfile.overallUserDensity)
        sliceProfile.setActivityFactor(cnSliceProfile.activityFactor)
        sliceProfile.setUeMobilityLevel(cnSliceProfile.ueMobilityLevel)
        sliceProfile.setResourceSharingLevel(cnSliceProfile.resourceSharingLevel)
        sliceProfile.setCsAvailabilityTarget(cnSliceProfile.csAvailabilityTarget)
        sliceProfile.setCsReliabilityMeanTime(cnSliceProfile.csReliabilityMeanTime)
        sliceProfile.setExpDataRate(cnSliceProfile.expDataRate)
        sliceProfile.setMsgSizeByte(cnSliceProfile.msgSizeByte)
        sliceProfile.setTransferIntervalTarget(cnSliceProfile.transferIntervalTarget)
        sliceProfile.setSurvivalTime(cnSliceProfile.survivalTime)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(sliceTaskInfo.sliceInstanceId)
                .sliceProfile(profileId))
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
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.cnSliceTaskInfo

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateCnNssi allocateCnNssi = new AllocateCnNssi()
        allocateCnNssi.nsstId = sliceTaskInfo.NSSTInfo.UUID
        allocateCnNssi.nssiId = sliceTaskInfo.suggestNssiId
        allocateCnNssi.nssiName = "nssi_cn" + execution.getVariable("sliceServiceInstanceName")
        allocateCnNssi.sliceProfile = sliceTaskInfo.sliceProfile.trans2CnProfile()
        allocateCnNssi.sliceProfile.sliceProfileId = sliceTaskInfo.sliceInstanceId
        allocateCnNssi.scriptName = sliceTaskInfo.getScriptName()

        NsiInfo nsiInfo = new NsiInfo()
        nsiInfo.nsiId = sliceParams.suggestNsiId
        nsiInfo.nsiName = sliceParams.suggestNsiName
        allocateCnNssi.nsiInfo = nsiInfo
        // endPoint
        EndPoint endPoint = new EndPoint()
        endPoint.setIpAddress(sliceTaskInfo.sliceProfile.ipAddress)
        endPoint.setLogicInterfaceId(sliceTaskInfo.sliceProfile.logicInterfaceId)
        endPoint.setNextHopInfo(sliceTaskInfo.sliceProfile.nextHopInfo)
        allocateCnNssi.setEndPoint(endPoint)

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
        serviceInfo.nssiId = sliceTaskInfo.suggestNssiId //if shared
        serviceInfo.sST = sliceTaskInfo.sliceProfile.sST ?: sliceParams.serviceProfile.get("sST")
        serviceInfo.nssiName = allocateCnNssi.nssiName
        serviceInfo.resourceSharingLevel = sliceParams.serviceProfile.get("resourceSharingLevel")

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateCnNssi(allocateCnNssi)

        execution.setVariable("CnAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("cnSliceTaskInfo", sliceTaskInfo)
        execution.setVariable("cnSubnetType", SubnetType.CN)
    }


    /**
     * create TN Slice Profile Instance
     * @param execution
     */
    void createTnBHSliceProfileInstance(DelegateExecution execution) {
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnBHSliceTaskInfo
        String serviceInstanceId = UUID.randomUUID().toString()

        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)
        String sliceProfileName = "sliceprofile_tn_" + sliceParams.serviceName
        //execution.setVariable("cnSliceProfileInstanceId", serviceInstanceId) //todo:

        // create slice profile
        ServiceInstance rspi = createSliceProfileInstance(sliceTaskInfo, sliceProfileName, oStatus)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(serviceInstanceId))
        client.create(uri, rspi)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create Tn Slice Profile
     * @param execution
     */
    void createTnBHSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnBHSliceTaskInfo

        SliceProfileAdapter tnSliceProfile = sliceTaskInfo.sliceProfile
        String profileId = UUID.randomUUID().toString()
        tnSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setLatency(tnSliceProfile.latency)
        sliceProfile.setMaxBandwidth(tnSliceProfile.maxBandwidth)
        sliceProfile.setJitter(tnSliceProfile.jitter)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(sliceTaskInfo.sliceInstanceId)
                .sliceProfile(profileId))
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
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnBHSliceTaskInfo

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateTnNssi allocateTnNssi = new AllocateTnNssi()
        allocateTnNssi.setNssiId(sliceTaskInfo.suggestNssiId)
        allocateTnNssi.scriptName = sliceTaskInfo.getScriptName()
        //todo: AllocateTnNssi
        //todo: endPointId -> set into tn
        List<TransportSliceNetwork> transportSliceNetworks = new ArrayList<>()
        TransportSliceNetwork transportSliceNetwork = new TransportSliceNetwork()
        List<ConnectionLink> connectionLinks = new ArrayList<>()
        ConnectionLink connectionLink = new ConnectionLink()
        connectionLink.setTransportEndpointA(sliceParams.anSliceTaskInfo.endPointId)
        connectionLink.setTransportEndpointB(sliceParams.cnSliceTaskInfo.endPointId)
        connectionLinks.add(connectionLink)
        transportSliceNetwork.setConnectionLinks(connectionLinks)
        transportSliceNetworks.add(transportSliceNetwork)
        allocateTnNssi.setTransportSliceNetworks(transportSliceNetworks)

        allocateTnNssi.setNetworkSliceInfos()
        allocateTnNssi.setSliceProfile(sliceTaskInfo.sliceProfile.trans2TnProfile())
        allocateTnNssi.getSliceProfile().setDomainType(sliceTaskInfo.subnetType.subnetType)
        NsiInfo nsiInfo = new NsiInfo()
        nsiInfo.setNsiId(sliceParams.suggestNsiId)
        nsiInfo.setNsiName(sliceParams.suggestNsiName)
        allocateTnNssi.setNsiInfo(nsiInfo)

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
        serviceInfo.nssiId = sliceTaskInfo.suggestNssiId
        serviceInfo.sST = sliceTaskInfo.sliceProfile.sST ?: sliceParams.serviceProfile.get("sST")
        serviceInfo.nssiName = "nssi_tn" + execution.getVariable("sliceServiceInstanceName")
        serviceInfo.resourceSharingLevel = sliceParams.serviceProfile.get("resourceSharingLevel")

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateTnNssi(allocateTnNssi)

        execution.setVariable("TnBHAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("tnBHSliceTaskInfo", sliceTaskInfo)
        execution.setVariable("tnBHSubnetType", SubnetType.TN_BH)
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

        NssiResponse result = execution.getVariable("anNssiAllocateResult") as NssiResponse
        String nssiId = result.getNssiId()
        String nsiId = sliceParams.getSuggestNsiId()
        String sliceProfileInstanceId = sliceParams.anSliceTaskInfo.sliceInstanceId
        String serviceProfileInstanceId = sliceParams.serviceId
        String epId = sliceParams.anSliceTaskInfo.endPointId
        //nsi id
        //todo: aai -> nssi -> relationship -> endPointId -> set into tn
        //String endPointId = getEndpointIdFromAAI(execution, nssiId)
        //execution.setVariable("endPointIdAn", endPointId)
        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution, sliceProfileInstanceId, nssiId)

        updateEPRelationship(execution, nssiId, epId)

        updateEPRelationship(execution, sliceProfileInstanceId, epId)

        sliceParams.anSliceTaskInfo.suggestNssiId = nssiId
        execution.setVariable("sliceTaskParams", sliceParams)
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
        String epId = sliceParams.cnSliceTaskInfo.endPointId
        //nsi id
        //todo: aai -> nssi -> relationship -> endPointId -> set into tn
//        String endPointId = getEndpointIdFromAAI(execution, nssiId)
//        execution.setVariable("endPointIdCn", endPointId)

        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution, sliceProfileInstanceId, nssiId)

        updateEPRelationship(execution, nssiId, epId)

        updateEPRelationship(execution, sliceProfileInstanceId, epId)

        sliceParams.cnSliceTaskInfo.suggestNssiId = nssiId
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * get endpoint Id from AAI by nssi id
     * @param execution
     * @param nssiId
     * @return
     */
    private String getEndpointIdFromAAI(DelegateExecution execution, String nssiId) {
        logger.debug("Enter update relationship in DoAllocateNSIandNSSI()")
        //todo: allottedResourceId

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        //sliceParams.setServiceId(nsiServiceInstanceID)
        AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(execution.getVariable("globalSubscriberId"))
                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                .serviceInstance(nssiId))

        String endpointId = null

        try {
            AAIResultWrapper wrapper = client.get(nsiServiceUri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            if (!si.isPresent()) {
                String msg = "NSSI in the option doesn't exist. " + nssiId
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            } else {
                ServiceInstance nssiInstance = si.get()
                //todo: handle relationship and return endPointId
                if (nssiInstance.relationshipList == null) {
                    String msg = "relationshipList of " + nssiId + " is null"
                    logger.debug(msg)
                    return null
                }
                for (Relationship relationship : nssiInstance.relationshipList.getRelationship()) {
                    if (relationship.relationshipLabel){
                        endpointId = relationship //todo
                    }
                }

                return endpointId
            }

        }catch(BpmnError e) {
            throw e
        }catch (Exception ex){
            String msg = "Exception: " + ex
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Exit update relationship in DoAllocateNSIandNSSI()")
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

        sliceParams.tnBHSliceTaskInfo.suggestNssiId = nssiId
        execution.setVariable("sliceTaskParams", sliceParams)
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

        AAIResourceUri targetInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(execution.getVariable("globalSubscriberId"))
                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                .serviceInstance(targetId))

        logger.debug("Creating relationship, targetInstanceUri: " + targetInstanceUri)

        relationship.setRelatedLink(targetInstanceUri.build().toString())

        AAIResourceUri sourceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(execution.getVariable("globalSubscriberId"))
                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                .serviceInstance(sourceId))
                .relationshipAPI()
        client.create(sourceInstanceUri, relationship)
    }

    /**
     * update endpoint relationship
     * @param execution
     * @param sourceId
     * @param targetId
     */
    void updateEPRelationship(DelegateExecution execution, String sourceId, String endpointId) {
        //relation ship
        Relationship relationship = new Relationship()

        AAIResourceUri endpointUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(endpointId))

        logger.debug("Creating relationship, endpoint Uri: " + endpointUri + ",endpointId: " + endpointId)

        relationship.setRelatedLink(endpointUri.build().toString())

        AAIResourceUri sourceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(execution.getVariable("globalSubscriberId"))
                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                .serviceInstance(sourceId))
                .relationshipAPI()
        client.create(sourceInstanceUri, relationship)
    }

    static def createSliceProfileInstance(SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo, String sliceProfileName, String oStatus) {
        // create slice profile
        ServiceInstance rspi = new ServiceInstance()
        rspi.setServiceInstanceName(sliceProfileName)
        rspi.setServiceType(sliceTaskInfo.sliceProfile.getSST())
        rspi.setServiceRole("slice-profile")
        rspi.setOrchestrationStatus(oStatus)
        rspi.setServiceInstanceLocationId(sliceTaskInfo.sliceProfile.getPLMNIdList())
        rspi.setModelInvariantId(sliceTaskInfo.NSSTInfo.invariantUUID)
        rspi.setModelVersionId(sliceTaskInfo.NSSTInfo.UUID)
        rspi.setWorkloadContext(sliceTaskInfo.subnetType.subnetType)
        rspi.setEnvironmentContext(sliceTaskInfo.sliceProfile.getSNSSAIList())
        rspi.setServiceFunction(sliceTaskInfo.sliceProfile.getResourceSharingLevel())

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))
        return rspi
    }

    public void createTNEndPoints(DelegateExecution execution) {
        String type = "endpoint"
        String function = "transport_EP"
        int prefixLength = 24
        String addressFamily = "ipv4"
        //BH RAN end point update
        //set BH end point
        String sliceParams = execution.getVariable("sliceParams")
        List<String> BH_endPoints = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceParams, "endPoints"))
        logger.debug("BH end points list : "+BH_endPoints)
        if(BH_endPoints.empty) {
            String msg = "End point info is empty"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        String bh_routeId = UUID.randomUUID().toString()
        execution.setVariable("tranportEp_ID_bh", bh_routeId)
        String role = "CU"
        String CU_IpAddress = jsonUtil.getJsonValue(bh_endpoint, "IpAddress")
        String LogicalLinkId = jsonUtil.getJsonValue(bh_endpoint, "LogicalLinkId")
        String nextHopInfo = jsonUtil.getJsonValue(bh_endpoint, "nextHopInfo")
        NetworkRoute bh_ep = new NetworkRoute()
        bh_ep.setRouteId(bh_routeId)
        bh_ep.setFunction(function)
        bh_ep.setRole(role)
        bh_ep.setType(type)
        bh_ep.setIpAddress(CU_IpAddress)
        bh_ep.setLogicalInterfaceId(LogicalLinkId)
        bh_ep.setNextHop(nextHopInfo)
        bh_ep.setPrefixLength(prefixLength)
        bh_ep.setAddressFamily(addressFamily)
        //FH RAN end points update
        //RU
        String RU_routeId = UUID.randomUUID().toString()
        execution.setVariable("tranportEp_ID_RU", RU_routeId)
        role = "RU"
        NetworkRoute RU_ep = new NetworkRoute()
        RU_ep.setRouteId(RU_routeId)
        RU_ep.setFunction(function)
        RU_ep.setRole(role)
        RU_ep.setType(type)
        RU_ep.setIpAddress("192.168.100.4")
        RU_ep.setLogicalInterfaceId("1234")
        RU_ep.setNextHop("Host1")
        RU_ep.setPrefixLength(prefixLength)
        RU_ep.setAddressFamily(addressFamily)
        //DU Ingress
        String DUIN_routeId = UUID.randomUUID().toString()
        execution.setVariable("tranportEp_ID_DUIN", DUIN_routeId)
        role = "DU"
        NetworkRoute DU_ep = new NetworkRoute()
        DU_ep.setRouteId(DUIN_routeId)
        DU_ep.setFunction(function)
        DU_ep.setRole(role)
        DU_ep.setType(type)
        DU_ep.setIpAddress("192.168.100.5")
        DU_ep.setLogicalInterfaceId("1234")
        DU_ep.setNextHop("Host2")
        DU_ep.setPrefixLength(prefixLength)
        DU_ep.setAddressFamily(addressFamily)
        //MH RAN end point update
        //DUEG
        String DUEG_routeId = UUID.randomUUID().toString()
        execution.setVariable("tranportEp_ID_DUEG", DUEG_routeId)
        NetworkRoute DUEG_ep = new NetworkRoute()
        DUEG_ep.setRouteId(DUEG_routeId)
        DUEG_ep.setFunction(function)
        DUEG_ep.setRole(role)
        DUEG_ep.setType(type)
        DUEG_ep.setIpAddress("192.168.100.5")
        DUEG_ep.setLogicalInterfaceId("1234")
        DUEG_ep.setPrefixLength(prefixLength)
        DUEG_ep.setAddressFamily(addressFamily)
        DUEG_ep.setNextHop("Host3")
        //CUIN
        String CUIN_routeId = UUID.randomUUID().toString()
        execution.setVariable("tranportEp_ID_CUIN", CUIN_routeId)
        NetworkRoute CUIN_ep = new NetworkRoute()
        CUIN_ep.setRouteId(CUIN_routeId)
        CUIN_ep.setFunction(function)
        CUIN_ep.setRole(role)
        CUIN_ep.setType(type)
        CUIN_ep.setIpAddress("192.168.100.6")
        CUIN_ep.setLogicalInterfaceId("1234")
        CUIN_ep.setNextHop("Host4")
        CUIN_ep.setPrefixLength(prefixLength)
        CUIN_ep.setAddressFamily(addressFamily)
        try {
            AAIResourcesClient client = new AAIResourcesClient()
            logger.debug("creating bh endpoint . ID : "+bh_routeId+" node details : "+bh_ep.toString())
            AAIResourceUri networkRouteUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(bh_routeId))
            client.create(networkRouteUri, bh_ep)
            logger.debug("creating RU endpoint . ID : "+RU_routeId+" node details : "+RU_ep.toString())
            networkRouteUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(RU_routeId))
            client.create(networkRouteUri, RU_ep)
            logger.debug("creating DUIN endpoint . ID : "+DUIN_routeId+" node details : "+DU_ep.toString())
            networkRouteUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(DUIN_routeId))
            client.create(networkRouteUri, DU_ep)
            logger.debug("creating DUEG endpoint . ID : "+DUEG_routeId+" node details : "+DUEG_ep.toString())
            networkRouteUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(DUEG_routeId))
            client.create(networkRouteUri, DUEG_ep)
            logger.debug("creating CUIN endpoint . ID : "+CUIN_routeId+" node details : "+CUIN_ep.toString())
            networkRouteUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkRoute(CUIN_routeId))
            client.create(networkRouteUri, CUIN_ep)
            //relationship b/w bh_ep and RAN NSSI
            def AN_NSSI = execution.getVariable("RANServiceInstanceId")
            Relationship relationship = new Relationship()
            String relatedLink = "aai/v21/network/network-routes/network-route/${bh_routeId}"
            relationship.setRelatedLink(relatedLink)
            relationship.setRelatedTo("network-route")
            relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
            anNssmfUtils.createRelationShipInAAI(execution, relationship, AN_NSSI)
            def ANNF_serviceInstanceId = execution.getVariable("RANNFServiceInstanceId")
            relatedLink = "aai/v21/network/network-routes/network-route/${RU_routeId}"
            relationship.setRelatedLink(relatedLink)
            anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
            relatedLink = "aai/v21/network/network-routes/network-route/${DUIN_routeId}"
            relationship.setRelatedLink(relatedLink)
            anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
            relatedLink = "aai/v21/network/network-routes/network-route/${DUEG_routeId}"
            relationship.setRelatedLink(relatedLink)
            anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
            relatedLink = "aai/v21/network/network-routes/network-route/${CUIN_routeId}"
            relationship.setRelatedLink(relatedLink)
            anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in createEndPointsInAai " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    /**
     * create TN Slice Profile Instance
     * @param execution
     */
    void createTnFHSliceProfileInstance(DelegateExecution execution) {
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnFHSliceTaskInfo
        String serviceInstanceId = UUID.randomUUID().toString()

        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)
        String sliceProfileName = "tn_fh" + sliceParams.serviceName
        //execution.setVariable("cnSliceProfileInstanceId", serviceInstanceId) //todo:

        // create slice profile
        ServiceInstance rspi = createSliceProfileInstance(sliceTaskInfo, sliceProfileName, oStatus)

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(serviceInstanceId))
        client.create(uri, rspi)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create Tn Slice Profile
     * @param execution
     */
    void createTnFHSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnFHSliceTaskInfo

        SliceProfileAdapter tnSliceProfile = sliceTaskInfo.sliceProfile
        String profileId = UUID.randomUUID().toString()
        tnSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setLatency(tnSliceProfile.latency)
        sliceProfile.setMaxBandwidth(tnSliceProfile.maxBandwidth)
        sliceProfile.setJitter(tnSliceProfile.jitter)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(sliceTaskInfo.sliceInstanceId)
                .sliceProfile(profileId))
        client.create(uri, sliceProfile)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * prepare AllocateCnNssi
     * @param execution
     */
    void prepareAllocateTnFHNssi(DelegateExecution execution) {

        //todo:
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.getTnFHSliceTaskInfo()

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateTnNssi allocateTnNssi = new AllocateTnNssi()
        allocateTnNssi.setNssiId(sliceTaskInfo.getSuggestNssiId())
        //todo: AllocateTnNssi
        //todo: endPointId -> set into tn
        List<TransportSliceNetwork> transportSliceNetworks = new ArrayList<>()
        TransportSliceNetwork transportSliceNetwork = new TransportSliceNetwork()
        List<ConnectionLink> connectionLinks = new ArrayList<>()
        ConnectionLink connectionLink = new ConnectionLink()
        connectionLink.setTransportEndpointA(UUID.randomUUID().toString())
        connectionLink.setTransportEndpointB(UUID.randomUUID().toString())
        connectionLinks.add(connectionLink)
        transportSliceNetwork.setConnectionLinks(connectionLinks)
        transportSliceNetworks.add(transportSliceNetwork)
        allocateTnNssi.setTransportSliceNetworks(transportSliceNetworks)

        allocateTnNssi.setSliceProfile(sliceTaskInfo.getSliceProfile().trans2TnProfile())
        NsiInfo nsiInfo = new NsiInfo()
        nsiInfo.setNsiId(sliceParams.getSuggestNsiId())
        nsiInfo.setNsiName(sliceParams.getSuggestNsiName())
        allocateTnNssi.setNsiInfo(nsiInfo)

        EsrInfo esrInfo = new EsrInfo()
        esrInfo.setVendor(sliceTaskInfo.getVendor())
        esrInfo.setNetworkType(sliceTaskInfo.getNetworkType())

        String globalSubscriberId = execution.getVariable("globalSubscriberId") as String
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType") as String

        ServiceInfo serviceInfo = ServiceInfo.builder()
                .globalSubscriberId(globalSubscriberId)
                .subscriptionServiceType(subscriptionServiceType)
                .nsiId(sliceParams.getSuggestNsiId())
                .serviceInvariantUuid(sliceTaskInfo.getNSSTInfo().getInvariantUUID())
                .serviceUuid(sliceTaskInfo.getNSSTInfo().getUUID())
                .nssiId(sliceTaskInfo.getSuggestNssiId())
                .sST(sliceTaskInfo.getSliceProfile().getSST() ?: sliceParams.getServiceProfile().get("sST"))
                .nssiName("nssi_tn_fh_" + execution.getVariable("sliceServiceInstanceName") as String)
                .build()

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateTnNssi(allocateTnNssi)

        execution.setVariable("TnFHAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("tnFHSliceTaskInfo", sliceTaskInfo)
        execution.setVariable("tnFHSubnetType", SubnetType.TN_FH)
    }

    /**
     * Update relationship between
     * 1. NSI and NSSI
     * 2. Slice Profile and Service Profile
     * 3. SliceProfile and NSSI
     *
     * @param execution
     */
    public void updateTnFHRelationship(DelegateExecution execution) {
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        NssiResponse result = execution.getVariable("tnFHNssiAllocateResult") as NssiResponse
        String nssiId = result.getNssiId()
        String nsiId = sliceParams.getSuggestNsiId()
        String sliceProfileInstanceId = sliceParams.tnFHSliceTaskInfo.sliceInstanceId
        String serviceProfileInstanceId = sliceParams.serviceId

        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution,sliceProfileInstanceId, nssiId)

        sliceParams.tnFHSliceTaskInfo.suggestNssiId = nssiId
        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create TN Slice Profile Instance
     * @param execution
     */
    void createTnMHSliceProfileInstance(DelegateExecution execution) {
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        String oStatus = "deactivated"

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnMHSliceTaskInfo
        String serviceInstanceId = UUID.randomUUID().toString()

        sliceTaskInfo.setSliceInstanceId(serviceInstanceId)
        String sliceProfileName = "tn_mh_" + sliceParams.serviceName
        ServiceInstance rspi = createSliceProfileInstance(sliceTaskInfo, sliceProfileName, oStatus)

        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))

        execution.setVariable("communicationServiceInstance", rspi)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(serviceInstanceId))
        client.create(uri, rspi)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * create Tn Slice Profile
     * @param execution
     */
    void createTnMHSliceProfile(DelegateExecution execution) {

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.tnMHSliceTaskInfo

        SliceProfileAdapter tnSliceProfile = sliceTaskInfo.sliceProfile
        String profileId = UUID.randomUUID().toString()
        tnSliceProfile.setSliceProfileId(profileId)

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(profileId)
        sliceProfile.setLatency(tnSliceProfile.latency)
        sliceProfile.setMaxBandwidth(tnSliceProfile.maxBandwidth)
        sliceProfile.setJitter(tnSliceProfile.jitter)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer(globalSubscriberId)
                .serviceSubscription(subscriptionServiceType)
                .serviceInstance(sliceTaskInfo.sliceInstanceId)
                .sliceProfile(profileId))
        client.create(uri, sliceProfile)

        execution.setVariable("sliceTaskParams", sliceParams)
    }

    /**
     * prepare AllocateCnNssi
     * @param execution
     */
    void prepareAllocateTnMHNssi(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.getTnMHSliceTaskInfo()

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateTnNssi allocateTnNssi = new AllocateTnNssi()
        allocateTnNssi.setNssiId(sliceTaskInfo.getSuggestNssiId())
        List<TransportSliceNetwork> transportSliceNetworks = new ArrayList<>()
        TransportSliceNetwork transportSliceNetwork = new TransportSliceNetwork()
        List<ConnectionLink> connectionLinks = new ArrayList<>()
        ConnectionLink connectionLink = new ConnectionLink()
        connectionLink.setTransportEndpointA(UUID.randomUUID().toString())
        connectionLink.setTransportEndpointB(UUID.randomUUID().toString())
        connectionLinks.add(connectionLink)
        transportSliceNetwork.setConnectionLinks(connectionLinks)
        transportSliceNetworks.add(transportSliceNetwork)
        allocateTnNssi.setTransportSliceNetworks(transportSliceNetworks)

        allocateTnNssi.setSliceProfile(sliceTaskInfo.getSliceProfile().trans2TnProfile())
        NsiInfo nsiInfo = new NsiInfo()
        nsiInfo.setNsiId(sliceParams.getSuggestNsiId())
        nsiInfo.setNsiName(sliceParams.getSuggestNsiName())
        allocateTnNssi.setNsiInfo(nsiInfo)

        EsrInfo esrInfo = new EsrInfo()
        esrInfo.setVendor(sliceTaskInfo.getVendor())
        esrInfo.setNetworkType(sliceTaskInfo.getNetworkType())

        String globalSubscriberId = execution.getVariable("globalSubscriberId") as String
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType") as String

        ServiceInfo serviceInfo = ServiceInfo.builder()
                .globalSubscriberId(globalSubscriberId)
                .subscriptionServiceType(subscriptionServiceType)
                .nsiId(sliceParams.getSuggestNsiId())
                .serviceInvariantUuid(sliceTaskInfo.getNSSTInfo().getInvariantUUID())
                .serviceUuid(sliceTaskInfo.getNSSTInfo().getUUID())
                .nssiId(sliceTaskInfo.getSuggestNssiId())
                .sST(sliceTaskInfo.getSliceProfile().getSST() ?: sliceParams.getServiceProfile().get("sST"))
                .nssiName("nssi_tn_mh_" + execution.getVariable("sliceServiceInstanceName") as String)
                .build()

        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setAllocateTnNssi(allocateTnNssi)

        execution.setVariable("TnMHAllocateNssiNbiRequest", nbiRequest)
        execution.setVariable("tnMHSliceTaskInfo", sliceTaskInfo)
        execution.setVariable("tnMHSubnetType", SubnetType.TN_MH)
    }

    /**
     * Update relationship between
     * 1. NSI and NSSI
     * 2. Slice Profile and Service Profile
     * 3. SliceProfile and NSSI
     *
     * @param execution
     */
    public void updateTnMHRelationship(DelegateExecution execution) {
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        NssiResponse result = execution.getVariable("tnMHNssiAllocateResult") as NssiResponse
        String nssiId = result.getNssiId()
        String nsiId = sliceParams.getSuggestNsiId()
        String sliceProfileInstanceId = sliceParams.tnMHSliceTaskInfo.sliceInstanceId
        String serviceProfileInstanceId = sliceParams.serviceId

        updateRelationship(execution, nsiId, nssiId)

        updateRelationship(execution, serviceProfileInstanceId, sliceProfileInstanceId)

        updateRelationship(execution,sliceProfileInstanceId, nssiId)

        sliceParams.tnMHSliceTaskInfo.suggestNssiId = nssiId
        execution.setVariable("sliceTaskParams", sliceParams)
    }
}
