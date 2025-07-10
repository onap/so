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

package org.onap.so.bpmn.infrastructure.adapter.cnf.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.adapter.cnf.CnfAdapterClient;
import org.onap.so.client.adapter.cnf.entities.CnfAaiUpdateRequest;
import org.onap.so.client.adapter.cnf.entities.CnfAaiUpdateResponse;
import org.onap.so.client.adapter.cnf.entities.InstanceRequest;
import org.onap.so.client.adapter.cnf.entities.InstanceResponse;
import org.onap.so.client.adapter.vnf.mapper.AttributeNameValue;
import org.onap.so.client.adapter.vnf.mapper.Attributes;
import org.onap.so.client.adapter.vnf.mapper.VnfAdapterVfModuleObjectMapper;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.openstack.utils.MsoMulticloudUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CnfAdapterCreateTasks {
    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterCreateTasks.class);
    public static final String SDNCQUERY_RESPONSE = "SDNCQueryResponse_";
    private static final String CNF_ADAPTER_MESSAGE_TYPE = "CNFCallback";
    private static final String CNF_ADAPTER_CALLBACK_TIMEOUT = "PT30M";

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private CnfAdapterClient cnfAdapterClient;
    @Autowired
    private VnfAdapterVfModuleObjectMapper vfModuleMapper;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * This method is used for creating the request for an Instance in Multicloud K8s Plugin.
     *
     * @param execution
     * @return
     */
    public void createInstance(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            RequestContext requestContext = gBBInput.getRequestContext();
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            String sdncVfModuleQueryResponse = execution.getVariable(SDNCQUERY_RESPONSE + vfModule.getVfModuleId());
            String sdncVnfQueryResponse = execution.getVariable(SDNCQUERY_RESPONSE + genericVnf.getVnfId());
            Map<String, Object> paramsMap = vfModuleMapper.buildVfModuleParamsMap(requestContext, serviceInstance,
                    genericVnf, vfModule, sdncVnfQueryResponse, sdncVfModuleQueryResponse);
            Map<String, String> sdncDirectives = getSdncDirectives(paramsMap);
            InstanceRequest createInstanceRequest = createInstanceRequest(vfModule, cloudRegion, sdncDirectives);
            InstanceResponse response = cnfAdapterClient.createVfModule(createInstanceRequest);
            execution.setVariable("heatStackId", response.getId());
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    protected Map<String, String> getSdncDirectives(Map<String, Object> paramsMap)
            throws JsonParseException, JsonMappingException, IOException {
        Map<String, String> sdncDirectivesMap = new HashMap<>();
        String sdncDirectivesString = (String) paramsMap.get(MsoMulticloudUtils.SDNC_DIRECTIVES);
        Attributes sdncDirectives = mapper.readValue(sdncDirectivesString, Attributes.class);
        for (AttributeNameValue nameVal : sdncDirectives.getAttributes()) {
            sdncDirectivesMap.put(nameVal.getAttributeName(), (String) nameVal.getAttributeValue());
        }
        return sdncDirectivesMap;
    }

    protected InstanceRequest createInstanceRequest(VfModule vfModule, CloudRegion cloudRegion,
            Map<String, String> sdncDirectives) {
        InstanceRequest request = new InstanceRequest();
        request.setModelInvariantId(vfModule.getModelInfoVfModule().getModelInvariantUUID());
        request.setModelVersionId(vfModule.getModelInfoVfModule().getModelUUID());
        request.setModelCustomizationId(vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        request.setCloudRegion(cloudRegion.getLcpCloudRegionId());
        request.setVfModuleUUID(vfModule.getVfModuleId());
        request.setProfileName(sdncDirectives.get("k8s-rb-profile-name"));
        request.setReleaseName(sdncDirectives.get("k8s-rb-instance-release-name"));
        if (sdncDirectives.containsKey("k8s-rb-instance-status-check"))
            request.setStatusCheck(sdncDirectives.get("k8s-rb-instance-status-check").equalsIgnoreCase("true"));
        request.setOverrideValues(sdncDirectives);
        return request;
    }

    public void prepareForCnfUpdateOrDelete(BuildingBlockExecution execution) {

        GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
        GenericVnf genericVnfId = null;
        VfModule vfModuleId = null;
        try {
            genericVnfId = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            vfModuleId = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
        }

        String heatStackId = execution.getVariable("heatStackId");

        logger.debug("heatStackId: {}", heatStackId);
        CloudRegion cloudRegion = gBBInput.getCloudRegion();
        String requestId = execution.getVariable("mso-request-id");

        String callbackUrl =
                "http://so-bpmn-infra.onap:8081/mso/WorkflowMessage/" + CNF_ADAPTER_MESSAGE_TYPE + "/" + requestId;

        CnfAaiUpdateRequest aaiRequest =
                createCnfAaiUpdateRequest(heatStackId, cloudRegion, callbackUrl, genericVnfId, vfModuleId);

        logger.debug("aaiRequest: {}", aaiRequest);

        String cnfRequestPayload = "";
        try {
            cnfRequestPayload = mapper.writeValueAsString(aaiRequest);
        } catch (JsonProcessingException e) {
            logger.error("Exception occurred", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }

        // Prepare values to pass in execution variable for CNF Adapter async Handling
        execution.setVariable("messageType", CNF_ADAPTER_MESSAGE_TYPE);
        execution.setVariable("correlator", requestId);
        execution.setVariable("timeout", CNF_ADAPTER_CALLBACK_TIMEOUT);

        String currentFlow = execution.getFlowToBeCalled();
        logger.debug("currentFlow: {}", currentFlow);

        String apiPath = "http://so-cnf-adapter:8090";
        if ("CreateVfModuleBB".equals(currentFlow) || ("UpgradeVfModuleBB".equals(currentFlow))) {
            apiPath = apiPath + "/api/cnf-adapter/v1/aai-update/";
        } else if ("DeleteVfModuleBB".equals(currentFlow)) {
            apiPath = apiPath + "/api/cnf-adapter/v1/aai-delete/";
        }

        // Set variables in execution variable\
        execution.setVariable("apiPath", apiPath);
        execution.setVariable("cnfRequestPayload", cnfRequestPayload);
    }

    public void processCnfUpdateOrDeleteAsyncResponse(BuildingBlockExecution execution) {

        String asyncResponse = execution.getVariable("asyncCallbackResponse");

        CnfAaiUpdateResponse response = new CnfAaiUpdateResponse();

        logger.debug("asyncResponse: {}", asyncResponse);

        try {
            response = mapper.readValue(asyncResponse, CnfAaiUpdateResponse.class);
        } catch (JsonProcessingException e) {
            logger.error("Error in parsing Cnf AAI update Response");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }

        if (!"COMPLETED".equalsIgnoreCase(response.getStatus())) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, new RuntimeException("Cannot update in AAI"));
        }
    }

    protected CnfAaiUpdateRequest createCnfAaiUpdateRequest(String heatStackId, CloudRegion cloudRegion,
            String callbackUrl, GenericVnf genericVnfId, VfModule vfModuleId) {
        CnfAaiUpdateRequest request = new CnfAaiUpdateRequest();
        request.setCallbackUrl(callbackUrl);
        request.setCloudOwner(cloudRegion.getCloudOwner());
        request.setCloudRegion(cloudRegion.getLcpCloudRegionId());
        request.setTenantId(cloudRegion.getTenantId());
        request.setInstanceId(heatStackId);
        request.setGenericVnfId(genericVnfId.getVnfId());
        request.setVfModuleId(vfModuleId.getVfModuleId());
        return request;
    }

}
