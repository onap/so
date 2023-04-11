/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;

/**
 *
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 *
 */
public class StubbedBuildingBlockExecution implements BuildingBlockExecution {

    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String LCP_CLOUD_REGION_ID = "RegionOne";
    private static final String MODEL_VERSION_ID = UUID.randomUUID().toString();
    private static final String SERVICE_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String DEPLOYMENT_ID = UUID.randomUUID().toString();
    private static final String SERVICE_INSTANCE_NAME = "test";
    private static final String INSTANCE_NAME = "instanceTest";
    private static final String TENANT_ID = UUID.randomUUID().toString();
    private final Map<String, Serializable> execution = new HashMap<>();
    private final GeneralBuildingBlock generalBuildingBlock;

    StubbedBuildingBlockExecution() {

        generalBuildingBlock = getGeneralBuildingBlockValue();
        setVariable(BUILDING_BLOCK, getExecuteBuildingBlock());
    }

    @Override
    public GeneralBuildingBlock getGeneralBuildingBlock() {
        return generalBuildingBlock;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getVariable(final String key) {
        return (T) execution.get(key);
    }

    @Override
    public <T> T getRequiredVariable(final String key) throws RequiredExecutionVariableExeception {
        return null;
    }

    @Override
    public void setVariable(final String key, final Serializable value) {
        execution.put(key, value);
    }

    @Override
    public Map<ResourceKey, String> getLookupMap() {
        return Collections.emptyMap();
    }

    @Override
    public String getFlowToBeCalled() {
        return null;
    }

    @Override
    public int getCurrentSequence() {
        return 0;
    }

    public static String getTenantId() {
        return TENANT_ID;
    }

    private GeneralBuildingBlock getGeneralBuildingBlockValue() {
        final GeneralBuildingBlock buildingBlock = new GeneralBuildingBlock();
        buildingBlock.setServiceInstance(getServiceInstance());
        return buildingBlock;
    }

    private ExecuteBuildingBlock getExecuteBuildingBlock() {
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
        executeBuildingBlock.setRequestDetails(getRequestDetails());
        return executeBuildingBlock;
    }

    private RequestDetails getRequestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setModelInfo(getModelInfo());
        requestDetails.setCloudConfiguration(getCloudConfiguration());
        requestDetails.setRequestInfo(getRequestInfo());
        requestDetails.setRequestParameters(getRequestParameters());
        return requestDetails;
    }

    private ModelInfo getModelInfo() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId(MODEL_VERSION_ID);
        return modelInfo;
    }

    private ServiceInstance getServiceInstance() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(SERVICE_INSTANCE_ID);
        serviceInstance.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        return serviceInstance;
    }

    private RequestInfo getRequestInfo() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName(INSTANCE_NAME);
        return requestInfo;
    }

    private RequestParameters getRequestParameters() {
        Map<String, Object> deploymentItemMap = new HashMap<>();
        deploymentItemMap.put("deploymentItemsId", DEPLOYMENT_ID);
        deploymentItemMap.put("lifecycleParameterKeyValues", new Object());
        List<Object> deploymentItems = new ArrayList<>();
        deploymentItems.add(deploymentItemMap);
        Map<String, Object> userParamsMap = new HashMap<>();
        userParamsMap.put("deploymentItems", deploymentItems);
        userParamsMap.put("namespace", "default");
        List<Map<String, Object>> userParams = new ArrayList<>();
        userParams.add(userParamsMap);
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(userParams);
        return requestParameters;
    }

    private CloudConfiguration getCloudConfiguration() {
        final CloudConfiguration cloudConfiguration = new CloudConfiguration();
        cloudConfiguration.setCloudOwner(CLOUD_OWNER);
        cloudConfiguration.setLcpCloudRegionId(LCP_CLOUD_REGION_ID);
        cloudConfiguration.setTenantId(TENANT_ID);
        return cloudConfiguration;
    }

}
