/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Intel Corp. All rights reserved.
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

package org.onap.so.bpmn.buildingblock;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.AllottedResource;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBondingLink;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.License;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.homingobjects.Candidate;
import org.onap.so.bpmn.servicedecomposition.homingobjects.CandidateType;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionCandidates;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionInfo;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoMetadata;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.oof.OofClient;
import org.onap.so.client.oof.OofValidator;
import org.onap.so.client.oof.beans.LicenseDemand;
import org.onap.so.client.oof.beans.LicenseInfo;
import org.onap.so.client.oof.beans.ModelInfo;
import org.onap.so.client.oof.beans.OofRequest;
import org.onap.so.client.oof.beans.OofRequestParameters;
import org.onap.so.client.oof.beans.PlacementDemand;
import org.onap.so.client.oof.beans.PlacementInfo;
import org.onap.so.client.oof.beans.RequestInfo;
import org.onap.so.client.oof.beans.ServiceInfo;
import org.onap.so.client.oof.beans.SubscriberInfo;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


/**
 * The oof homing building block obtains licensing and homing solutions for a given resource or set of resources.
 *
 */
@Component("OofHoming")
public class OofHomingV2 {

    public static final String ERROR_WHILE_PREPARING_OOF_REQUEST = " Error - while preparing oof request: ";
    private static final Logger logger = LoggerFactory.getLogger(OofHomingV2.class);
    private JsonUtils jsonUtils = new JsonUtils();
    @Autowired
    private Environment env;
    @Autowired
    private OofClient oofClient;
    @Autowired
    private OofValidator oofValidator;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    private static final String MODEL_INVARIANT_ID = "modelInvariantId";
    private static final String MODEL_VERSION_ID = "modelVersionId";
    private static final String SERVICE_RESOURCE_ID = "serviceResourceId";
    private static final String IDENTIFIER_TYPE = "identifierType";
    private static final String SOLUTIONS = "solutions";
    private static final String RESOURCE_MISSING_DATA = "Resource does not contain: ";
    private static final String SERVICE_MISSING_DATA = "Service Instance does not contain: ";
    private static final String UNPROCESSABLE = "422";
    private static final int INTERNAL = 500;

    /**
     * Generates the request payload then sends to Oof to perform homing and licensing for the provided demands
     *
     * @param execution
     */
    public void callOof(BuildingBlockExecution execution) {
        logger.trace("Started Oof Homing Call Oof");
        try {
            GeneralBuildingBlock bb = execution.getGeneralBuildingBlock();

            RequestContext requestContext = bb.getRequestContext();
            String requestId = requestContext.getMsoRequestId();

            ServiceInstance serviceInstance = bb.getCustomer().getServiceSubscription().getServiceInstances().get(0);
            Customer customer = bb.getCustomer();

            String timeout = execution.getVariable("timeout");
            if (isBlank(timeout)) {
                timeout = env.getProperty("oof.timeout", "PT30M");
            }

            OofRequest oofRequest = new OofRequest();

            RequestInfo requestInfo = buildRequestInfo(requestId, timeout);
            oofRequest.setRequestInformation(requestInfo);

            ServiceInfo serviceInfo = buildServiceInfo(serviceInstance);
            oofRequest.setServiceInformation(serviceInfo);

            PlacementInfo placementInfo = buildPlacementInfo(customer);

            placementInfo = buildPlacementDemands(serviceInstance, placementInfo);
            oofRequest.setPlacementInformation(placementInfo);

            LicenseInfo licenseInfo = buildLicenseInfo(serviceInstance);
            oofRequest.setLicenseInformation(licenseInfo);

            if (!placementInfo.getPlacementDemands().isEmpty() || !licenseInfo.getLicenseDemands().isEmpty()) {
                oofClient.postDemands(oofRequest);
            } else {
                logger.debug("{} resources eligible for homing or licensing", SERVICE_MISSING_DATA);
                throw new BpmnError(UNPROCESSABLE,
                        SERVICE_MISSING_DATA + " resources eligible for homing or licensing");
            }

            // Variables for ReceiveWorkflowMessage subflow
            execution.setVariable("asyncCorrelator", requestId);
            execution.setVariable("asyncMessageType", "OofResponse");
            execution.setVariable("asyncTimeout", timeout);

            logger.trace("Completed Oof Homing Call Oof");
        } catch (BpmnError e) {
            logger.debug("{}{}", ERROR_WHILE_PREPARING_OOF_REQUEST, e.getStackTrace());
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(e.getErrorCode()), e.getMessage());
        } catch (BadResponseException e) {
            logger.debug("{}{}", ERROR_WHILE_PREPARING_OOF_REQUEST, e.getStackTrace());
            exceptionUtil.buildAndThrowWorkflowException(execution, 400, e.getMessage());
        } catch (Exception e) {
            logger.debug("{}{}", ERROR_WHILE_PREPARING_OOF_REQUEST, e.getStackTrace());
            exceptionUtil.buildAndThrowWorkflowException(execution, INTERNAL, "Internal Error - occurred while "
                    + "preparing oof request: " + e + "   Stack:" + ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * Validates, processes, and sets the homing and licensing solutions that are returned by Oof
     *
     * @param execution
     * @param asyncResponse
     */
    public void processSolution(BuildingBlockExecution execution, String asyncResponse) {
        logger.trace("Started Oof Homing Process Solution");
        try {
            oofValidator.validateSolution(asyncResponse);
            ServiceInstance serviceInstance = execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription()
                    .getServiceInstances().get(0);

            logger.debug("Processing Oof asyncronous response");
            JSONObject response = new JSONObject(asyncResponse);
            if (response.has(SOLUTIONS)) {
                JSONObject allSolutions = response.getJSONObject(SOLUTIONS);
                if (allSolutions.has("placementSolutions")) {
                    JSONArray placementSolutions = allSolutions.getJSONArray("placementSolutions");
                    for (int i = 0; i < placementSolutions.length(); i++) {
                        JSONArray placements = placementSolutions.getJSONArray(i);
                        processPlacementSolution(serviceInstance, placements, i);
                    }
                }
                if (allSolutions.has("licenseSolutions")) {
                    JSONArray licenseSolutions = allSolutions.getJSONArray("licenseSolutions");
                    if (licenseSolutions.length() > 0) {
                        processLicenseSolution(serviceInstance, licenseSolutions);
                    }
                }
            } else {
                throw new BpmnError(UNPROCESSABLE, "Oof response does not contain: " + SOLUTIONS);
            }

            execution.setVariable("generalBuildingBlock", execution.getGeneralBuildingBlock());

            logger.trace("Completed Oof Homing Process Solution");
        } catch (BpmnError e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(e.getErrorCode()), e.getMessage());
        } catch (BadResponseException e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 400, e.getMessage());
        } catch (Exception e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, INTERNAL,
                    "Internal Error - occurred while processing Oof asynchronous response: " + e.getMessage());
        }
    }

    /**
     * Builds the request information section for the homing/licensing request
     *
     * @throws Exception
     */
    private RequestInfo buildRequestInfo(String requestId, String timeout) throws Exception {
        logger.trace("Building request information");
        RequestInfo requestInfo = new RequestInfo();
        if (requestId != null) {
            String host = env.getProperty("mso.workflow.message.endpoint");
            String callbackUrl = host + "/" + UriUtils.encodePathSegment("OofResponse", "UTF-8") + "/"
                    + UriUtils.encodePathSegment(requestId, "UTF-8");

            Duration d = Duration.parse(timeout);
            long timeoutSeconds = d.getSeconds();

            requestInfo.setTransactionId(requestId);
            requestInfo.setRequestId(requestId);
            requestInfo.setCallbackUrl(callbackUrl);
            requestInfo.setSourceId("mso");
            requestInfo.setRequestType("create");
            requestInfo.setTimeout(timeoutSeconds);
            requestInfo.setNumSolutions(1);
            ArrayList optimizers = new ArrayList();
            optimizers.add("placement");
            requestInfo.setOptimizers(optimizers);
        } else {
            throw new BpmnError(UNPROCESSABLE, "Request Context does not contain: requestId");
        }
        return requestInfo;
    }

    /**
     * Builds the request information section for the homing/licensing request
     *
     */
    private ServiceInfo buildServiceInfo(ServiceInstance serviceInstance) {
        logger.trace("Building service information");
        ServiceInfo info = new ServiceInfo();
        ModelInfoServiceInstance modelInfo = serviceInstance.getModelInfoServiceInstance();
        if (isNotBlank(modelInfo.getModelInvariantUuid()) && isNotBlank(modelInfo.getModelUuid())) {
            info.setServiceInstanceId(serviceInstance.getServiceInstanceId());
            if (modelInfo.getServiceType() != null && modelInfo.getServiceType().length() > 0) { // temp solution
                info.setServiceName(modelInfo.getServiceType());
            }
            info.setModelInfo(buildModelInfo(serviceInstance.getModelInfoServiceInstance()));
        } else {
            throw new BpmnError(UNPROCESSABLE, SERVICE_MISSING_DATA + MODEL_VERSION_ID + ", " + MODEL_INVARIANT_ID);
        }
        return info;
    }

    /**
     * Builds initial section of placement info for the homing/licensing request
     *
     */
    private PlacementInfo buildPlacementInfo(Customer customer) {
        PlacementInfo placementInfo = new PlacementInfo();
        if (customer != null) {
            logger.debug("Adding subscriber to placement information");
            SubscriberInfo subscriberInfo = new SubscriberInfo();
            subscriberInfo.setGlobalSubscriberId(customer.getGlobalCustomerId());
            subscriberInfo.setSubscriberName(customer.getSubscriberName());
            subscriberInfo.setSubscriberCommonSiteId(customer.getSubscriberCommonSiteId());
            placementInfo.setSubscriberInfo(subscriberInfo);
            OofRequestParameters oofRequestParams = new OofRequestParameters();
            oofRequestParams.setCustomerLatitude(customer.getCustomerLatitude());
            oofRequestParams.setCustomerLongitude(customer.getCustomerLongitude());
            oofRequestParams.setCustomerName(customer.getSubscriberName());
            placementInfo.setRequestParameters(oofRequestParams);
        } else {
            throw new BpmnError(UNPROCESSABLE, SERVICE_MISSING_DATA + "customer");
        }
        return placementInfo;

    }

    /**
     * Builds the placement demand list for the homing/licensing request
     *
     */
    private PlacementInfo buildPlacementDemands(ServiceInstance serviceInstance, PlacementInfo placementInfo) {
        logger.trace("Building placement information demands");

        List<AllottedResource> allottedResourceList = serviceInstance.getAllottedResources();
        if (!allottedResourceList.isEmpty()) {
            logger.debug("Adding allotted resources to placement demands list");
            for (AllottedResource ar : allottedResourceList) {
                if (isBlank(ar.getId())) {
                    ar.setId(UUID.randomUUID().toString());
                }
                PlacementDemand demand = buildDemand(ar.getId(), ar.getModelInfoAllottedResource());
                // addCandidates(ar, demand);
                placementInfo.getPlacementDemands().add(demand);
            }
        }
        List<VpnBondingLink> vpnBondingLinkList = serviceInstance.getVpnBondingLinks();
        if (!vpnBondingLinkList.isEmpty()) {
            logger.debug("Adding vpn bonding links to placement demands list");
            for (VpnBondingLink vbl : vpnBondingLinkList) {
                List<ServiceProxy> serviceProxyList = vbl.getServiceProxies();
                for (ServiceProxy sp : serviceProxyList) {
                    if (isBlank(sp.getId())) {
                        sp.setId(UUID.randomUUID().toString());
                    }
                    PlacementDemand demand = buildDemand(sp.getId(), sp.getModelInfoServiceProxy());
                    // addCandidates(sp, demand);
                    placementInfo.getPlacementDemands().add(demand);
                }
            }
        }
        return placementInfo;
    }

    /**
     * Builds the license demand list for the homing/licensing request
     *
     */
    private LicenseInfo buildLicenseInfo(ServiceInstance serviceInstance) {
        logger.trace("Building license information");
        LicenseInfo licenseInfo = new LicenseInfo();
        List<GenericVnf> vnfList = serviceInstance.getVnfs();
        if (!vnfList.isEmpty()) {
            logger.debug("Adding vnfs to license demands list");
            for (GenericVnf vnf : vnfList) {
                LicenseDemand demand = buildLicenseDemand(vnf.getVnfId(), vnf.getModelInfoGenericVnf());
                licenseInfo.getLicenseDemands().add(demand);
            }
        }
        return licenseInfo;
    }

    /**
     * Builds a single license demand object
     *
     */
    private LicenseDemand buildLicenseDemand(String id, ModelInfoMetadata metadata) {
        logger.debug("Building demand for service or resource: {}", id);
        LicenseDemand demand = new LicenseDemand();
        if (isNotBlank(id) && isNotBlank(metadata.getModelInstanceName())) {

            demand.setServiceResourceId(id);
            demand.setResourceModuleName(metadata.getModelInstanceName());
            demand.setResourceModelInfo(buildModelInfo(metadata));
        } else {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + "modelInstanceName");
        }
        return demand;
    }

    /**
     * Builds a single demand object
     *
     */
    private PlacementDemand buildDemand(String id, ModelInfoMetadata metadata) {
        logger.debug("Building demand for service or resource: {}", id);
        PlacementDemand placementDemand = new PlacementDemand();
        if (isNotBlank(id) && isNotBlank(metadata.getModelInstanceName())) {
            placementDemand.setServiceResourceId(id);
            placementDemand.setResourceModuleName(metadata.getModelInstanceName());
            placementDemand.setResourceModelInfo(buildModelInfo(metadata));
        } else {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + "modelInstanceName");
        }
        return placementDemand;
    }

    /**
     * Builds the resource model info section
     *
     */
    private ModelInfo buildModelInfo(ModelInfoMetadata metadata) {
        ModelInfo modelInfo = new ModelInfo();
        String invariantUuid = metadata.getModelInvariantUuid();
        String modelUuid = metadata.getModelUuid();
        if (isNotBlank(invariantUuid) && isNotBlank(modelUuid)) {
            modelInfo.setModelInvariantId(invariantUuid);
            modelInfo.setModelVersionId(modelUuid);
            modelInfo.setModelName(metadata.getModelName());
            modelInfo.setModelVersion(metadata.getModelVersion());
        } else if (isNotBlank(invariantUuid)) {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + MODEL_VERSION_ID);
        } else {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + MODEL_INVARIANT_ID);
        }
        return modelInfo;
    }

    /**
     * Adds required, excluded, and existing candidates to a demand
     *
     */
    private void addCandidates(SolutionCandidates candidates, JSONObject demand) {
        List<Candidate> required = candidates.getRequiredCandidates();
        List<Candidate> excluded = candidates.getExcludedCandidates();
        if (!required.isEmpty()) {
            demand.put("requiredCandidates", required);
        }
        if (!excluded.isEmpty()) {
            demand.put("excludedCandidates", excluded);
        }
        // TODO support existing candidates
    }

    /**
     * Processes the license solutions and sets to the corresponding generic vnf
     *
     */
    private void processLicenseSolution(ServiceInstance serviceInstance, JSONArray licenseSolutions) {
        List<GenericVnf> vnfs = serviceInstance.getVnfs();

        logger.debug("Processing the license solution");
        for (int i = 0; i < licenseSolutions.length(); i++) {
            JSONObject licenseSolution = licenseSolutions.getJSONObject(i);
            for (GenericVnf vnf : vnfs) {
                if (licenseSolution.getString(SERVICE_RESOURCE_ID).equals(vnf.getVnfId())) {
                    License license = new License();
                    JSONArray entitlementPools = licenseSolution.getJSONArray("entitlementPoolUUID");
                    List<String> entitlementPoolsList = jsonUtils.StringArrayToList(entitlementPools);
                    license.setEntitlementPoolUuids(entitlementPoolsList);
                    JSONArray licenseKeys = licenseSolution.getJSONArray("licenseKeyGroupUUID");
                    List<String> licenseKeysList = jsonUtils.StringArrayToList(licenseKeys);
                    license.setLicenseKeyGroupUuids(licenseKeysList);

                    vnf.setLicense(license);
                }
            }
        }
    }

    /**
     * Processes a placement solution list then correlates and sets each placement solution to its corresponding
     * resource
     *
     */
    private void processPlacementSolution(ServiceInstance serviceInstance, JSONArray placements, int i) {
        List<VpnBondingLink> links = serviceInstance.getVpnBondingLinks();
        List<AllottedResource> allottes = serviceInstance.getAllottedResources();
        List<GenericVnf> vnfs = serviceInstance.getVnfs();

        logger.debug("Processing placement solution {}1", i);
        for (int p = 0; p < placements.length(); p++) {
            JSONObject placement = placements.getJSONObject(p);
            SolutionInfo solutionInfo = new SolutionInfo();
            solutionInfo.setSolutionId(i + 1);
            search: {
                for (VpnBondingLink vbl : links) {
                    List<ServiceProxy> proxies = vbl.getServiceProxies();
                    for (ServiceProxy sp : proxies) {
                        if (placement.getString(SERVICE_RESOURCE_ID).equals(sp.getId())) {
                            if (i > 0) {
                                if (p % 2 == 0) {
                                    VpnBondingLink vblNew = (VpnBondingLink) SerializationUtils.clone(vbl);
                                    vblNew.setVpnBondingLinkId(UUID.randomUUID().toString());
                                    links.add(vblNew);
                                }
                                links.get(links.size() - 1).getServiceProxy(sp.getId())
                                        .setServiceInstance(setSolution(solutionInfo, placement));
                            } else {
                                sp.setServiceInstance(setSolution(solutionInfo, placement));
                            }
                            break search;
                        }
                    }
                }
                for (AllottedResource ar : allottes) {
                    if (placement.getString(SERVICE_RESOURCE_ID).equals(ar.getId())) {
                        ar.setParentServiceInstance(setSolution(solutionInfo, placement));
                        break search;
                    }
                }
                for (GenericVnf vnf : vnfs) {
                    if (placement.getString(SERVICE_RESOURCE_ID).equals(vnf.getVnfId())) {
                        ServiceInstance si = setSolution(solutionInfo, placement);
                        serviceInstance.setSolutionInfo(si.getSolutionInfo());
                        serviceInstance.getVnfs().add(si.getVnfs().get(0));
                        break search;
                    }
                }
            }
        }
    }


    /**
     * Creates and sets necessary pojos with placement solution data for a given demand
     *
     */
    private ServiceInstance setSolution(SolutionInfo solutionInfo, JSONObject placement) {
        logger.debug("Mapping placement solution");
        String invalidMessage = "Oof Response contains invalid: ";

        JSONObject solution = placement.getJSONObject("solution");
        String identifierType = solution.getString(IDENTIFIER_TYPE);
        List<String> identifiersList = jsonUtils.StringArrayToList(solution.getJSONArray("identifiers").toString());
        String identifierValue = identifiersList.get(0);

        JSONArray assignments = placement.getJSONArray("assignmentInfo");
        Map<String, String> assignmentsMap = jsonUtils.entryArrayToMap(assignments.toString(), "key", "value");
        solutionInfo.setRehome(Boolean.parseBoolean(assignmentsMap.get("isRehome")));
        String type = identifierType;

        ServiceInstance si = new ServiceInstance();
        CloudRegion cloud = setCloud(assignmentsMap);
        if (type.equals("serviceInstanceId")) {
            if (identifierType.equals(CandidateType.SERVICE_INSTANCE_ID.toString())) {
                solutionInfo.setHomed(true);
                si.setServiceInstanceId(identifierValue);
                si.setOrchestrationStatus(OrchestrationStatus.CREATED);
                cloud.setLcpCloudRegionId(assignmentsMap.get("cloudRegionId"));
                if (assignmentsMap.containsKey("vnfHostName")) {
                    logger.debug("Resources has been homed to a vnf");
                    GenericVnf vnf = setVnf(assignmentsMap);
                    vnf.setCloudRegion(cloud);
                    si.getVnfs().add(vnf);

                } else if (assignmentsMap.containsKey("primaryPnfName")) {
                    logger.debug("Resources has been homed to a pnf");
                    Pnf priPnf = setPnf(assignmentsMap, "primary");
                    priPnf.setCloudRegion(cloud);
                    si.getPnfs().add(priPnf);
                    if (assignmentsMap.containsKey("secondaryPnfName")) {
                        Pnf secPnf = setPnf(assignmentsMap, "secondary");
                        secPnf.setCloudRegion(cloud);
                        si.getPnfs().add(secPnf);
                    }
                }
            } else {
                logger.debug("{}{}", invalidMessage, IDENTIFIER_TYPE);
                throw new BpmnError(UNPROCESSABLE, invalidMessage + IDENTIFIER_TYPE);
            }
        } else if (type.equals("cloudRegionId")) {
            if (identifierType.equals(CandidateType.CLOUD_REGION_ID.toString())) {
                logger.debug("Resources has been homed to a cloud region");
                cloud.setLcpCloudRegionId(identifierValue);
                solutionInfo.setHomed(false);
                solutionInfo.setTargetedCloudRegion(cloud);
                si.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
            } else {
                logger.debug("{}{}", invalidMessage, IDENTIFIER_TYPE);
                throw new BpmnError(UNPROCESSABLE, invalidMessage + IDENTIFIER_TYPE);
            }
        }
        si.setSolutionInfo(solutionInfo);
        return si;
    }

    /**
     * Sets the cloud data to a cloud region object
     *
     */
    private CloudRegion setCloud(Map<String, String> assignmentsMap) {
        CloudRegion cloud = new CloudRegion();
        cloud.setCloudOwner(assignmentsMap.get("cloudOwner"));
        cloud.setCloudRegionVersion(assignmentsMap.get("aicVersion"));
        cloud.setComplex(assignmentsMap.get("aicClli"));
        return cloud;
    }

    /**
     * Sets the vnf data to a generic vnf object
     *
     */
    private GenericVnf setVnf(Map<String, String> assignmentsMap) {
        GenericVnf vnf = new GenericVnf();
        vnf.setOrchestrationStatus(OrchestrationStatus.CREATED);
        vnf.setVnfName(assignmentsMap.get("vnfHostName"));
        vnf.setVnfId(assignmentsMap.get("vnfId"));
        return vnf;
    }

    /**
     * Sets the pnf data to a pnf object
     *
     */
    private Pnf setPnf(Map<String, String> assignmentsMap, String role) {
        Pnf pnf = new Pnf();
        pnf.setRole(role);
        pnf.setOrchestrationStatus(OrchestrationStatus.CREATED);
        pnf.setPnfName(assignmentsMap.get(role + "PnfName"));
        return pnf;
    }



}
