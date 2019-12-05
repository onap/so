/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
 * ================================================================================
 * Modifications Copyright (C) 2020 Nordix
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

package org.onap.so.client.cds;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.PayloadGenerationException;
import java.util.Optional;
import java.util.UUID;

public class GeneratePayloadForCds {

    private static final String ORIGINATOR_ID = "SO";
    private static final String MODE = "sync";
    private static final String MSO_REQUEST_ID = "msoRequestId";
    private BuildingBlockExecution execution;
    private String scope;
    private String action;
    private CDSRequestProvider configuration;

    // This is getting used by https://gerrit.onap.org/r/#/c/so/+/99221/
    private DelegateExecution delegateExecution;

    private static final String BUILDING_BLOCK = "buildingBlock";
    private ExtractPojosForBB extractPojosForBB;


    public GeneratePayloadForCds(BuildingBlockExecution execution, ExtractPojosForBB extractPojosForBB) {
        this.execution = execution;

        ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();

        this.scope = buildingBlock.getBpmnScope();
        this.action = buildingBlock.getBpmnAction();

        this.extractPojosForBB = extractPojosForBB;

        // Setting scope and action for UpdateAAI BPMN process.
        execution.setVariable(PayloadConstants.SCOPE, scope);
        execution.setVariable(PayloadConstants.ACTION, action);
    }

    public GeneratePayloadForCds(DelegateExecution execution, String scope, String action) {
        this.delegateExecution = execution;
        this.scope = scope;
        this.action = action;
    }

    /**
     * Build the payload based on SO scope and action.
     *
     * @return Payload to push to CDS.
     */
    protected Optional<String> generateConfigPropertiesPayload() throws PayloadGenerationException {
        switch (scope) {
            case "vnf":
                configuration = new VnfCDSRequestProvider(extractPojosForBB);
                configuration.setExecutionObject(execution);
                return configuration.buildRequestPayload(action);

            case "vfModule":
                configuration = new VfModuleCDSRequestProvider(extractPojosForBB);
                configuration.setExecutionObject(execution);
                return configuration.buildRequestPayload(action);

            case "service":
                configuration = new ServiceCDSRequestProvider(extractPojosForBB);
                configuration.setExecutionObject(execution);
                return configuration.buildRequestPayload(action);

            case "pnf":
                configuration = new PnfCDSRequestProvider();
                configuration.setExecutionObject(delegateExecution);
                return configuration.buildRequestPayload(action);
        }

        return Optional.empty();
    }

    /**
     * Build properties like (blueprint name, version, action etc..) along with the request payload.
     *
     * @return AbstractCDSPropertiesBean - A POJO which contains CDS related information.
     * @throws PayloadGenerationException - Throw an exception if it fails to build payload for CDS.
     */
    public AbstractCDSPropertiesBean buildCdsPropertiesBean() throws PayloadGenerationException {
        final AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
        final String requestPayload = generateConfigPropertiesPayload()
                .orElseThrow(() -> new PayloadGenerationException("Failed to build payload for CDS"));
        final String requestId = getRequestIdUsingScope();

        cdsPropertiesBean.setRequestObject(requestPayload);
        cdsPropertiesBean.setBlueprintName(configuration.getBlueprintName());
        cdsPropertiesBean.setBlueprintVersion(configuration.getBlueprintVersion());
        cdsPropertiesBean.setRequestId(requestId);
        cdsPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        cdsPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
        cdsPropertiesBean.setActionName(action);
        cdsPropertiesBean.setMode(MODE);

        return cdsPropertiesBean;
    }

    private String getRequestIdUsingScope() {
        if ("pnf".equalsIgnoreCase(scope))
            return String.valueOf(delegateExecution.getVariable(MSO_REQUEST_ID));
        else
            return execution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId();
    }
}
