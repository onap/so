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

class DoAllocateNSIandNSSI extends AbstractServiceTaskProcessor{

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
        ServiceInstance nsi = new ServiceInstance()

        String sliceInstanceName = "nsi_"+execution.getVariable("sliceServiceInstanceName")
        String serviceType = sliceParams.serviceProfile.get("sST")
        String serviceStatus = "deactivated"
        String modelInvariantUuid = sliceParams.getNSTInfo().invariantUUID
        String modelUuid = sliceParams.getNSTInfo().UUID

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
        String sliceProfileName = "an_" + sliceParams.serviceName

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
        sliceProfile.setCoverageAreaTAList(anSliceProfile.coverageAreaTAList)
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
        SliceTaskInfo<SliceProfileAdapter> sliceTaskInfo = sliceParams.anSliceTaskInfo

        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

        AllocateAnNssi allocateAnNssi = new AllocateAnNssi()
        allocateAnNssi.sliceProfile = sliceTaskInfo.sliceProfile.trans2AnProfile()
        allocateAnNssi.sliceProfile.sliceProfileId = sliceTaskInfo.sliceInstanceId
        allocateAnNssi.nsstId = sliceTaskInfo.NSSTInfo.UUID
        allocateAnNssi.nssiId = sliceTaskInfo.suggestNssiId
        allocateAnNssi.nssiName = "nssi_an" + execution.getVariable("sliceServiceInstanceName")
        NsiInfo nsiInfo = new NsiInfo()
        nsiInfo.nsiId = sliceParams.suggestNsiId
        nsiInfo.nsiName = sliceParams.suggestNsiName
        allocateAnNssi.nsiInfo = nsiInfo
        //endPoint
        EndPoint endPoint = new EndPoint()
        endPoint.setIpAddress(sliceTaskInfo.sliceProfile.ipAddress)
        endPoint.setLogicInterfaceId(sliceTaskInfo.sliceProfile.logicInterfaceId)
        endPoint.setNextHopInfo(sliceTaskInfo.sliceProfile.nextHopInfo)
        allocateAnNssi.setEndPoint(endPoint)

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
        serviceInfo.sST = sliceTaskInfo.sliceProfile.sST ?: sliceParams.serviceProfile.get("sST")
        serviceInfo.nssiName = sliceTaskInfo.suggestNssiId ? sliceTaskInfo.NSSTInfo.name : allocateAnNssi.nssiName
        serviceInfo.nssiId = sliceTaskInfo.suggestNssiId

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
        String sliceProfileName = "cn_"+sliceParams.serviceName

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
        String sliceProfileName = "tn_" + sliceParams.serviceName
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

        //timestamp format YYYY-MM-DD hh:mm:ss
        rspi.setCreatedAt(new Date(System.currentTimeMillis()).format("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault()))
        return rspi
    }

}
