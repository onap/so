/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.buildingblock;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.SerializationUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
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
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.homingobjects.Candidate;
import org.onap.so.bpmn.servicedecomposition.homingobjects.CandidateType;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionCandidates;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionInfo;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoMetadata;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.sniro.SniroClient;
import static org.onap.so.client.sniro.SniroValidator.validateSolution;
import org.onap.so.client.sniro.beans.Demand;
import org.onap.so.client.sniro.beans.LicenseInfo;
import org.onap.so.client.sniro.beans.ModelInfo;
import org.onap.so.client.sniro.beans.PlacementInfo;
import org.onap.so.client.sniro.beans.RequestInfo;
import org.onap.so.client.sniro.beans.ServiceInfo;
import org.onap.so.client.sniro.beans.SniroManagerRequest;
import org.onap.so.client.sniro.beans.SubscriberInfo;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.logging.filter.base.ONAPComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;


/**
 * The sniro homing building block obtains licensing and homing solutions for a given resource or set of resources.
 *
 * @author cb645j
 *
 */
@Component("SniroHoming")
public class SniroHomingV2 {

    private static final Logger logger = LoggerFactory.getLogger(SniroHomingV2.class);
    private JsonUtils jsonUtils = new JsonUtils();
    @Autowired
    private Environment env;
    @Autowired
    private SniroClient client;
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
    private static final String EXCEPTION_OCCURRED = "Exception occurred";
    private static final String VNF_HOST_NAME = "vnfHostName";

    /**
     * Generates the request payload then sends to sniro manager to perform homing and licensing for the provided
     * demands
     *
     * @param execution
     */
    public void callSniro(BuildingBlockExecution execution) {
        logger.debug("Started Sniro Homing Call Sniro");
        try {
            GeneralBuildingBlock bb = execution.getGeneralBuildingBlock();

            RequestContext requestContext = bb.getRequestContext();
            RequestParameters requestParams = requestContext.getRequestParameters();
            String requestId = requestContext.getMsoRequestId();

            ServiceInstance serviceInstance = bb.getCustomer().getServiceSubscription().getServiceInstances().get(0);
            Customer customer = bb.getCustomer();

            String timeout = execution.getVariable("timeout");
            if (isBlank(timeout)) {
                timeout = env.getProperty("sniro.manager.timeout", "PT30M");
            }

            SniroManagerRequest request = new SniroManagerRequest();

            RequestInfo requestInfo = buildRequestInfo(requestId, timeout);
            request.setRequestInformation(requestInfo);

            ServiceInfo serviceInfo = buildServiceInfo(serviceInstance);
            request.setServiceInformation(serviceInfo);

            PlacementInfo placementInfo = buildPlacementInfo(customer, requestParams);

            List<Demand> placementDemands = buildPlacementDemands(serviceInstance);
            placementInfo.setDemands(placementDemands);
            request.setPlacementInformation(placementInfo);

            LicenseInfo licenseInfo = new LicenseInfo();

            List<Demand> licenseDemands = buildLicenseDemands(serviceInstance);
            licenseInfo.setDemands(licenseDemands);
            request.setLicenseInformation(licenseInfo);

            if (!placementDemands.isEmpty() || !licenseDemands.isEmpty()) {
                client.postDemands(request);
            } else {
                logger.debug(SERVICE_MISSING_DATA + "resources eligible for homing or licensing");
                throw new BpmnError(UNPROCESSABLE, SERVICE_MISSING_DATA + "resources eligible for homing or licensing");
            }

            // Variables for ReceiveWorkflowMessage subflow
            execution.setVariable("asyncCorrelator", requestId);
            execution.setVariable("asyncMessageType", "SNIROResponse");
            execution.setVariable("asyncTimeout", timeout);

            logger.trace("Completed Sniro Homing Call Sniro");
        } catch (BpmnError e) {
            logger.error(EXCEPTION_OCCURRED, e);
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(e.getErrorCode()), e.getMessage(),
                    ONAPComponents.SNIRO);
        } catch (BadResponseException e) {
            logger.error(EXCEPTION_OCCURRED, e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 400, e.getMessage(), ONAPComponents.SNIRO);
        } catch (Exception e) {
            logger.error(EXCEPTION_OCCURRED, e);
            exceptionUtil.buildAndThrowWorkflowException(execution, INTERNAL,
                    "Internal Error - occurred while preparing sniro request: " + e.getMessage(), ONAPComponents.SO);
        }
    }

    /**
     * Validates, processes, and sets the homing and licensing solutions that are returned by sniro manager
     *
     * @param execution
     * @param asyncResponse
     */
    public void processSolution(BuildingBlockExecution execution, String asyncResponse) {
        logger.trace("Started Sniro Homing Process Solution");
        try {
            // TODO improve handling multiple solutions but is dependent on sniro enhancing api + work with sniro
            validateSolution(asyncResponse);
            ServiceInstance serviceInstance = execution.getGeneralBuildingBlock().getCustomer().getServiceSubscription()
                    .getServiceInstances().get(0);

            logger.debug("Processing sniro manager asyncronous response");
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
                throw new BpmnError(UNPROCESSABLE, "Sniro Managers response does not contain: " + SOLUTIONS);
            }

            execution.setVariable("generalBuildingBlock", execution.getGeneralBuildingBlock());

            logger.trace("Completed Sniro Homing Process Solution");
        } catch (BpmnError e) {
            logger.error(EXCEPTION_OCCURRED, e);
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(e.getErrorCode()), e.getMessage(),
                    ONAPComponents.SNIRO);
        } catch (BadResponseException e) {
            logger.error(EXCEPTION_OCCURRED, e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 400, e.getMessage(), ONAPComponents.SNIRO);
        } catch (Exception e) {
            logger.error(EXCEPTION_OCCURRED, e);
            exceptionUtil.buildAndThrowWorkflowException(execution, INTERNAL,
                    "Internal Error - occurred while processing sniro asynchronous response: " + e.getMessage(),
                    ONAPComponents.SO);
        }
    }

    /**
     * Builds the request information section for the homing/licensing request
     *
     * @throws Exception
     */
    private RequestInfo buildRequestInfo(String requestId, String timeout) {
        logger.trace("Building request information");
        RequestInfo requestInfo = new RequestInfo();
        if (requestId != null) {
            String host = env.getProperty("mso.workflow.message.endpoint");
            String callbackUrl = host + "/" + UriUtils.encodePathSegment("SNIROResponse", "UTF-8") + "/"
                    + UriUtils.encodePathSegment(requestId, "UTF-8");

            Duration d = Duration.parse(timeout);

            requestInfo.setTransactionId(requestId);
            requestInfo.setRequestId(requestId);
            requestInfo.setCallbackUrl(callbackUrl);
            requestInfo.setRequestType("create");
            requestInfo.setTimeout(d.getSeconds());

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
            if (modelInfo.getServiceRole() != null) {
                info.setServiceRole(modelInfo.getServiceRole());
            }
            info.setModelInfo(buildModelInfo(modelInfo));
        } else {
            throw new BpmnError(UNPROCESSABLE, SERVICE_MISSING_DATA + MODEL_VERSION_ID + ", " + MODEL_INVARIANT_ID);
        }
        return info;
    }

    /**
     * Builds initial section of placement info for the homing/licensing request
     *
     */
    private PlacementInfo buildPlacementInfo(Customer customer, RequestParameters requestParams) {
        PlacementInfo placementInfo = new PlacementInfo();
        if (customer != null) {
            logger.debug("Adding subscriber to placement information");
            SubscriberInfo subscriber = new SubscriberInfo();
            subscriber.setGlobalSubscriberId(customer.getGlobalCustomerId());
            subscriber.setSubscriberName(customer.getSubscriberName());
            subscriber.setSubscriberCommonSiteId(customer.getSubscriberCommonSiteId());
            placementInfo.setSubscriberInfo(subscriber);
            if (requestParams != null) {
                logger.debug("Adding request parameters to placement information");
                placementInfo.setRequestParameters(requestParams.toJsonString());
            }
        } else {
            throw new BpmnError(UNPROCESSABLE, SERVICE_MISSING_DATA + "customer");
        }
        return placementInfo;

    }

    /**
     * Builds the placement demand list for the homing/licensing request
     *
     */
    private List<Demand> buildPlacementDemands(ServiceInstance serviceInstance) {
        logger.trace("Building placement information demands");
        List<Demand> placementDemands = new ArrayList<>();

        List<AllottedResource> allottedResourceList = serviceInstance.getAllottedResources();
        if (!allottedResourceList.isEmpty()) {
            logger.debug("Adding allotted resources to placement demands list");
            for (AllottedResource ar : allottedResourceList) {
                if (isBlank(ar.getId())) {
                    ar.setId(UUID.randomUUID().toString());
                }
                Demand demand = buildDemand(ar.getId(), ar.getModelInfoAllottedResource());
                addCandidates(ar, demand);
                placementDemands.add(demand);
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
                    Demand demand = buildDemand(sp.getId(), sp.getModelInfoServiceProxy());
                    addCandidates(sp, demand);
                    placementDemands.add(demand);
                }
            }
        }
        List<ServiceProxy> serviceProxies = serviceInstance.getServiceProxies();
        if (!serviceProxies.isEmpty()) {
            logger.debug("Adding service proxies to placement demands list");
            for (ServiceProxy sp : serviceProxies) {
                if (isBlank(sp.getId())) {
                    sp.setId(UUID.randomUUID().toString());
                }
                Demand demand = buildDemand(sp.getId(), sp.getModelInfoServiceProxy());
                addCandidates(sp, demand);
                placementDemands.add(demand);
            }
        }
        return placementDemands;
    }

    /**
     * Builds the license demand list for the homing/licensing request
     *
     */
    private List<Demand> buildLicenseDemands(ServiceInstance serviceInstance) {
        logger.trace("Building license information");
        List<Demand> licenseDemands = new ArrayList<>();
        List<GenericVnf> vnfList = serviceInstance.getVnfs();
        if (!vnfList.isEmpty()) {
            logger.debug("Adding vnfs to license demands list");
            for (GenericVnf vnf : vnfList) {
                Demand demand = buildDemand(vnf.getVnfId(), vnf.getModelInfoGenericVnf());
                licenseDemands.add(demand);
            }
        }
        return licenseDemands;
    }

    /**
     * Builds a single demand object
     *
     */
    private Demand buildDemand(String id, ModelInfoMetadata metadata) {
        logger.debug("Building demand for service or resource: " + id);
        Demand demand = new Demand();
        if (isNotBlank(id) && isNotBlank(metadata.getModelInstanceName())) {
            demand.setServiceResourceId(id);
            demand.setResourceModuleName(metadata.getModelInstanceName());
            demand.setModelInfo(buildModelInfo(metadata));
        } else {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + "modelInstanceName");
        }
        return demand;
    }

    /**
     * Builds the resource model info section
     *
     */
    private ModelInfo buildModelInfo(ModelInfoMetadata metadata) {
        ModelInfo object = new ModelInfo();
        String invariantUuid = metadata.getModelInvariantUuid();
        String modelUuid = metadata.getModelUuid();
        if (isNotBlank(invariantUuid) && isNotBlank(modelUuid)) {
            object.setModelInvariantId(invariantUuid);
            object.setModelVersionId(modelUuid);
            object.setModelName(metadata.getModelName());
            object.setModelVersion(metadata.getModelVersion());
        } else if (isNotBlank(invariantUuid)) {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + MODEL_VERSION_ID);
        } else {
            throw new BpmnError(UNPROCESSABLE, RESOURCE_MISSING_DATA + MODEL_INVARIANT_ID);
        }
        return object;
    }

    /**
     * Adds required, excluded, and existing candidates as well as filtering attributes to a demand
     *
     */
    private void addCandidates(SolutionCandidates candidates, Demand demand) {
        List<Candidate> required = candidates.getRequiredCandidates();
        List<Candidate> excluded = candidates.getExcludedCandidates();
        List<Candidate> existing = candidates.getExistingCandidates();
        List<Candidate> filtering = candidates.getFilteringAttributes();

        List<org.onap.so.client.sniro.beans.Candidate> candidateList = getCandidates(required);
        if (!candidateList.isEmpty()) {
            demand.setRequiredCandidates(candidateList);
        }
        candidateList = getCandidates(excluded);
        if (!candidateList.isEmpty()) {
            demand.setExcludedCandidates(candidateList);
        }
        candidateList = getCandidates(existing);
        if (!candidateList.isEmpty()) {
            demand.setExistingCandidates(candidateList);
        }

        candidateList = getCandidates(filtering);
        if (!candidateList.isEmpty()) {
            demand.setFilteringAttributes(candidateList);
        }
    }

    private List<org.onap.so.client.sniro.beans.Candidate> getCandidates(List<Candidate> candidates) {
        List<org.onap.so.client.sniro.beans.Candidate> candidateList = new ArrayList<>();
        if (!candidates.isEmpty()) {
            for (Candidate c : candidates) {
                org.onap.so.client.sniro.beans.Candidate can = new org.onap.so.client.sniro.beans.Candidate();
                can.setIdentifierType(
                        org.onap.so.client.sniro.beans.CandidateType.valueOf(c.getIdentifierType().name()));
                can.setIdentifiers(c.getIdentifiers());
                can.setCloudOwner(c.getCloudOwner());
                candidateList.add(can);
            }
        }
        return candidateList;
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
        List<ServiceProxy> serviceProxies = serviceInstance.getServiceProxies();

        logger.debug("Processing placement solution " + i + 1);
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
                for (ServiceProxy proxy : serviceProxies) {
                    if (placement.getString(SERVICE_RESOURCE_ID).equals(proxy.getId())) {
                        proxy.setServiceInstance(setSolution(solutionInfo, placement));
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
        String invalidMessage = "Sniro Managers Response contains invalid: ";

        JSONObject solution = placement.getJSONObject("solution");
        String identifierType = solution.getString(IDENTIFIER_TYPE);
        List<String> identifiersList = jsonUtils.StringArrayToList(solution.getJSONArray("identifiers").toString());
        String identifierValue = identifiersList.get(0);

        JSONArray assignments = placement.getJSONArray("assignmentInfo");
        Map<String, String> assignmentsMap = jsonUtils.entryArrayToMap(assignments.toString(), "key", "value");
        solutionInfo.setRehome(Boolean.parseBoolean(assignmentsMap.get("isRehome")));

        ServiceInstance si = new ServiceInstance();
        CloudRegion cloud = setCloud(assignmentsMap);
        if (identifierType.equals(CandidateType.SERVICE_INSTANCE_ID.toString())) {
            solutionInfo.setHomed(true);
            si.setServiceInstanceId(identifierValue);
            si.setOrchestrationStatus(OrchestrationStatus.CREATED);
            cloud.setLcpCloudRegionId(assignmentsMap.get("cloudRegionId"));
            if (assignmentsMap.containsKey(VNF_HOST_NAME) && !assignmentsMap.get(VNF_HOST_NAME).isEmpty()) {
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
        } else if (identifierType.equals(CandidateType.CLOUD_REGION_ID.toString())) {
            logger.debug("Resources has been homed to a cloud region");
            cloud.setLcpCloudRegionId(identifierValue);
            solutionInfo.setHomed(false);
            solutionInfo.setTargetedCloudRegion(cloud);
            si.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        } else {
            logger.debug(invalidMessage + IDENTIFIER_TYPE);
            throw new BpmnError(UNPROCESSABLE, invalidMessage + IDENTIFIER_TYPE);
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
        vnf.setVnfName(assignmentsMap.get(VNF_HOST_NAME));
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
