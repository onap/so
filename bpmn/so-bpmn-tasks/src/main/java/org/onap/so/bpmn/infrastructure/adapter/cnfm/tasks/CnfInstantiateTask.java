/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Ericsson. All rights reserved.
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

import static java.util.Objects.isNull;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_OWNER_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_REGION_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.SERVICE_INSTANCE_ID_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.SERVICE_INSTANCE_NAME_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.TENANT_ID_PARAM_KEY;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;
import org.onap.so.cnfm.lcm.model.AsInfoModificationRequestDeploymentItems;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * This class performs CNF Instantiation
 *
 * @author sagar.shetty@est.tech
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@Component
public class CnfInstantiateTask {
    private static final String CREATE_AS_REQUEST_OBJECT = "CreateAsRequestObject";
    private static final String INSTANTIATE_AS_REQUEST_OBJECT = "InstantiateAsRequest";
    private static final String CNFM_REQUEST_STATUS_CHECK_URL = "CnfmStatusCheckUrl";
    private static final String AS_INSTANCE_ID = "asInstanceid";
    private static final Logger LOGGER = LoggerFactory.getLogger(CnfInstantiateTask.class);
    private final ExceptionBuilder exceptionUtil;
    private final CnfmHttpServiceProvider cnfmHttpServiceProvider;

    @Autowired
    public CnfInstantiateTask(final CnfmHttpServiceProvider cnfmHttpServiceProvider,
            final ExceptionBuilder exceptionUtil) {
        this.cnfmHttpServiceProvider = cnfmHttpServiceProvider;
        this.exceptionUtil = exceptionUtil;
    }

    public void createCreateAsRequest(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing createAsRequest task  ...");
            final ExecuteBuildingBlock executeBuildingBlock =
                    (ExecuteBuildingBlock) execution.getVariable("buildingBlock");

            final GeneralBuildingBlock generalBuildingBlock = execution.getGeneralBuildingBlock();

            final RequestDetails requestDetails = executeBuildingBlock.getRequestDetails();
            LOGGER.debug("RequestDetails: {}", requestDetails);

            if (isNull(requestDetails) && isNull(requestDetails.getModelInfo())
                    && isNull(requestDetails.getRequestInfo()) && isNull(requestDetails.getCloudConfiguration())
                    && isNull(generalBuildingBlock)) {
                LOGGER.error("Missing Mandatory attribute from RequestDetails: {} or GeneralBuildingBlock: {}",
                        requestDetails, generalBuildingBlock);
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000,
                        "Missing Mandatory attribute from RequestDetails or GeneralBuildingBlock", ONAPComponents.SO);
            }

            final ModelInfo modelInfo = requestDetails.getModelInfo();
            final CloudConfiguration cloudConfiguration = requestDetails.getCloudConfiguration();
            final ServiceInstance serviceInstance = generalBuildingBlock.getServiceInstance();

            final CreateAsRequest createAsRequest = new CreateAsRequest().asdId(modelInfo.getModelVersionId())
                    .asInstanceName(requestDetails.getRequestInfo().getInstanceName())
                    .additionalParams(Maps.of(CLOUD_OWNER_PARAM_KEY, cloudConfiguration.getCloudOwner(),
                            CLOUD_REGION_PARAM_KEY, cloudConfiguration.getLcpCloudRegionId(), TENANT_ID_PARAM_KEY,
                            cloudConfiguration.getTenantId(), SERVICE_INSTANCE_ID_PARAM_KEY,
                            serviceInstance.getServiceInstanceId(), SERVICE_INSTANCE_NAME_PARAM_KEY,
                            serviceInstance.getServiceInstanceName()));

            LOGGER.debug("Adding CreateAsRequest to execution {}", createAsRequest);

            execution.setVariable(CREATE_AS_REQUEST_OBJECT, createAsRequest);
            LOGGER.debug("Finished executing createAsRequest task ...");

        } catch (final Exception exception) {
            LOGGER.error("Unable to create CreateAsRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2001, exception);
        }
    }

    public void invokeCnfmWithCreateAsRequest(final BuildingBlockExecution execution) {
        try {
            final CreateAsRequest createAsRequest = execution.getVariable(CREATE_AS_REQUEST_OBJECT);

            final Optional<AsInstance> optional = cnfmHttpServiceProvider.invokeCreateAsRequest(createAsRequest);

            if (optional.isEmpty()) {
                LOGGER.error("Unable to invoke CNFM for CreateAsRequest : {}", createAsRequest);
                exceptionUtil.buildAndThrowWorkflowException(execution, 2003,
                        "Unable to invoke CNFM for CreateAsRequest", ONAPComponents.SO);
            }

            final AsInstance asInstance = optional.get();
            execution.setVariable(AS_INSTANCE_ID, asInstance.getAsInstanceid());
            LOGGER.debug("Successfully invoked CNFM response: {}", asInstance);

        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke CNFM AsCreateRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2004, exception);
        }
    }

    public void createAsInstanceRequest(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing createAsInstanceRequest task  ...");
            final ExecuteBuildingBlock executeBuildingBlock =
                    (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
            final RequestDetails requestDetails = executeBuildingBlock.getRequestDetails();
            final InstantiateAsRequest instantiateAsRequest = new InstantiateAsRequest();
            if (requestDetails != null && requestDetails.getRequestParameters() != null) {
                List<Map<String, Object>> userParams = requestDetails.getRequestParameters().getUserParams();
                if (userParams != null && !userParams.isEmpty()) {
                    List deploymentItems = new ArrayList<Object>();
                    List deploymentItemsReq = new ArrayList<AsInfoModificationRequestDeploymentItems>();
                    for (Map<String, Object> userParam : userParams) {
                        if (userParam.containsKey("deploymentItems")) {
                            deploymentItems = (ArrayList<Object>) userParam.get("deploymentItems");
                            break;
                        }
                    }
                    for (Object deploymentItem : deploymentItems) {
                        Map<String, Object> deploymentItemMap = (Map<String, Object>) deploymentItem;
                        AsInfoModificationRequestDeploymentItems item = new AsInfoModificationRequestDeploymentItems();
                        item.setDeploymentItemsId(deploymentItemMap.get("deploymentItemsId").toString());
                        item.setLifecycleParameterKeyValues(deploymentItemMap.get("lifecycleParameterKeyValues"));
                        deploymentItemsReq.add(item);
                    }
                    instantiateAsRequest.setDeploymentItems(deploymentItemsReq);
                    ;
                }
            }
            LOGGER.debug("Adding InstantiateAsRequest to execution {}", instantiateAsRequest);

            execution.setVariable(INSTANTIATE_AS_REQUEST_OBJECT, instantiateAsRequest);
            LOGGER.debug("Finished executing createAsInstanceRequest task ...");

        } catch (final Exception exception) {
            LOGGER.error("Unable to create CreateAsInstanceRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2002, exception);
        }
    }

    public void invokeCnfmWithInstantiateAsRequest(final BuildingBlockExecution execution) {
        try {
            final InstantiateAsRequest instantiateAsRequest = execution.getVariable(INSTANTIATE_AS_REQUEST_OBJECT);
            final String asInstanceId = execution.getVariable(AS_INSTANCE_ID);
            Optional<URI> cnf_status_check_url =
                    cnfmHttpServiceProvider.invokeInstantiateAsRequest(instantiateAsRequest, asInstanceId);
            execution.setVariable(CNFM_REQUEST_STATUS_CHECK_URL, cnf_status_check_url.get());
            LOGGER.debug("Successfully invoked CNFM instantiate AS request: {}", asInstanceId);
        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke CNFM InstantiateAsRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2005, exception);
        }
    }

}
