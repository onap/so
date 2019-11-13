/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class GenericCDSProcessing {

    private static final Logger logger = LoggerFactory.getLogger(GenericCDSProcessing.class);
    private static final String ORIGINATOR_ID = "SO";
    private static final String MODE = "sync";

    private final ExtractPojosForBB extractPojosForBB;
    private final ExceptionBuilder exceptionBuilder;

    @Autowired
    public GenericCDSProcessing(ExtractPojosForBB extractPojosForBB, ExceptionBuilder exceptionBuilder) {
        this.extractPojosForBB = extractPojosForBB;
        this.exceptionBuilder = exceptionBuilder;
    }

    /**
     *
     * @param execution
     */
    public void buildPayloadBasedOnScopeAndAction(BuildingBlockExecution execution) {

        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ExecuteBuildingBlock executeBuildingBlock = execution.getVariable("buildingBlock");
            BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();

            // TODO For now I hardcoded scope and execution this will come from BuildingBlock
            // object(https://gerrit.onap.org/r/#/c/so/+/98166/)
            // String scope = buildingBlock.getBpmnScope();
            // String action = buildingBlock.getBpmnAction();

            GeneratePayloadForCds configurePayloadForCds = new GeneratePayloadForCds(execution, extractPojosForBB);

            String requestPayload = configurePayloadForCds.buildPayloadForCds()
                    .orElseThrow(() -> new Exception("Failed to build payload for CDS"));

            AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();

            // TODO Get rid of hardcoded blueprint name and blueprint version, actionName.
            abstractCDSPropertiesBean.setRequestObject(requestPayload);
            abstractCDSPropertiesBean.setBlueprintName("ubuntu");
            abstractCDSPropertiesBean.setBlueprintVersion("1.0.0");
            abstractCDSPropertiesBean.setRequestId(gBBInput.getRequestContext().getMsoRequestId());
            abstractCDSPropertiesBean.setOriginatorId(ORIGINATOR_ID);
            abstractCDSPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
            abstractCDSPropertiesBean.setActionName("assign");
            abstractCDSPropertiesBean.setMode(MODE);

            execution.setVariable("executionObject", abstractCDSPropertiesBean);
        } catch (Exception ex) {
            logger.error("An exception occurred when creating payload for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
