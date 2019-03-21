/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.etsi.vnf.tasks;

import static org.onap.so.bpmn.infrastructure.etsi.vnf.tasks.EtsiVnfInstantiateTaskConstants.CREATE_VNF_REQUEST_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.etsi.vnf.tasks.EtsiVnfInstantiateTaskConstants.CREATE_VNF_RESPONSE_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.etsi.vnf.tasks.EtsiVnfInstantiateTaskConstants.DOT;
import static org.onap.so.bpmn.infrastructure.etsi.vnf.tasks.EtsiVnfInstantiateTaskConstants.SPACE;
import static org.onap.so.bpmn.infrastructure.etsi.vnf.tasks.EtsiVnfInstantiateTaskConstants.UNDERSCORE;
import static org.onap.so.bpmn.servicedecomposition.entities.ResourceKey.GENERIC_VNF_ID;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.vnfmadapter.v1.model.CreateVnfRequest;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.onap.vnfmadapter.v1.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

/**
 * @author waqas.ikram@est.tech
 */
@Component
public class EtsiVnfInstantiateTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiVnfInstantiateTask.class);
    private final ExtractPojosForBB extractPojosForBB;

    private final ExceptionBuilder exceptionUtil;
    private final VnfmAdapterServiceProvider vnfmAdapterServiceProvider;

    @Autowired
    public EtsiVnfInstantiateTask(final ExceptionBuilder exceptionUtil, final ExtractPojosForBB extractPojosForBB,
            final VnfmAdapterServiceProvider vnfmAdapterServiceProvider) {
        this.exceptionUtil = exceptionUtil;
        this.extractPojosForBB = extractPojosForBB;
        this.vnfmAdapterServiceProvider = vnfmAdapterServiceProvider;
    }

    /**
     * Create {@link CreateVnfRequest} object with required fields and store it in
     * {@link org.camunda.bpm.engine.delegate.DelegateExecution}
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void createSoV2VnfCreateRequest(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing createSoV2VnfCreateRequest  ...");

            final GeneralBuildingBlock buildingBlock = execution.getGeneralBuildingBlock();
            final CloudRegion cloudRegion = buildingBlock.getCloudRegion();

            final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID,
                    execution.getLookupMap().get(GENERIC_VNF_ID));
            final ModelInfoGenericVnf modelInfoGenericVnf = vnf.getModelInfoGenericVnf();

            final CreateVnfRequest createVnfRequest = new CreateVnfRequest();

            createVnfRequest.setName(getName(vnf.getVnfName(), modelInfoGenericVnf.getModelInstanceName()));
            createVnfRequest.setTenant(getTenant(cloudRegion));

            LOGGER.info("CreateVnfRequest : {}", createVnfRequest);

            execution.setVariable(CREATE_VNF_REQUEST_PARAM_NAME, createVnfRequest);

            LOGGER.debug("Finished executing createSoV2VnfCreateRequest ...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to execute createSoV2VnfCreateRequest", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1200, exception);
        }
    }

    /**
     * Invoke VNFM adapter to create and instantiate VNF
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void invokeVnfmAdapter(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing invokeVnfmAdapter  ...");
            final CreateVnfRequest request = execution.getVariable(CREATE_VNF_REQUEST_PARAM_NAME);

            final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID,
                    execution.getLookupMap().get(GENERIC_VNF_ID));

            final Optional<CreateVnfResponse> response =
                    vnfmAdapterServiceProvider.invokeCreateInstantiationRequest(vnf.getVnfId(), request);

            if (!response.isPresent()) {
                final String errorMessage = "Unexpected error while processing create and instantiation request";
                LOGGER.error(errorMessage);
                exceptionUtil.buildAndThrowWorkflowException(execution, 1201, errorMessage);
            }

            final CreateVnfResponse vnfResponse = response.get();

            LOGGER.debug("Vnf instantiation response: {}", vnfResponse);
            execution.setVariable(CREATE_VNF_RESPONSE_PARAM_NAME, vnfResponse);

            LOGGER.debug("Finished executing invokeVnfmAdapter ...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke create and instantiation request", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1202, exception);
        }
    }

    private Tenant getTenant(final CloudRegion cloudRegion) {
        final Tenant tenant = new Tenant();
        tenant.setCloudOwner(cloudRegion.getCloudOwner());
        tenant.setRegionName(cloudRegion.getLcpCloudRegionId());
        tenant.setTenantId(cloudRegion.getTenantId());
        return tenant;
    }

    private static String getName(final String vnfName, final String modelInstanceName) {
        if (modelInstanceName != null) {
            return (vnfName + DOT + modelInstanceName).replaceAll(SPACE, UNDERSCORE);
        }
        return vnfName != null ? vnfName.replaceAll(SPACE, UNDERSCORE) : vnfName;
    }

}
