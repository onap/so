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
import org.onap.so.client.adapter.cnf.entities.UpgradeInstanceRequest;
import org.onap.so.client.adapter.cnf.entities.UpgradeInstanceResponse;
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
public class CnfAdapterUpgradeTasks {
    private static final Logger logger = LoggerFactory.getLogger(CnfAdapterUpgradeTasks.class);

    public static final String SDNCQUERY_RESPONSE = "SDNCQueryResponse_";

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
     * This method is used for updating the request for an Instance in Multicloud K8s Plugin.
     *
     * @param execution
     * @return
     */
    public void upgradeInstance(BuildingBlockExecution execution) {
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
            UpgradeInstanceRequest upgradeInstanceRequest =
                    upgradeInstanceRequest(vfModule, cloudRegion, sdncDirectives);
            String heatStackId = vfModule.getHeatStackId();
            UpgradeInstanceResponse response = cnfAdapterClient.upgradeVfModule(upgradeInstanceRequest, heatStackId);
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

    protected UpgradeInstanceRequest upgradeInstanceRequest(VfModule vfModule, CloudRegion cloudRegion,
            Map<String, String> sdncDirectives) {

        UpgradeInstanceRequest request = new UpgradeInstanceRequest();

        request.setModelInvariantId(vfModule.getModelInfoVfModule().getModelInvariantUUID());
        request.setModelCustomizationId(vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        request.setCloudRegion(cloudRegion.getLcpCloudRegionId());
        request.setVfModuleUUID(vfModule.getVfModuleId());
        request.setProfileName(sdncDirectives.get("k8s-rb-profile-name"));
        request.setLabels(sdncDirectives);
        if (sdncDirectives.containsKey("k8s-rb-instance-status-check"))
            request.setStatusCheck(sdncDirectives.get("k8s-rb-instance-status-check").equalsIgnoreCase("true"));
        request.setOverrideValues(sdncDirectives);
        return request;
    }

}
