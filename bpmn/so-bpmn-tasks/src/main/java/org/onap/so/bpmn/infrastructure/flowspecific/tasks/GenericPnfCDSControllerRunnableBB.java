/*
 * ============LICENSE_START======================================================= Copyright (C) 2020 Nokia. All rights
 * reserved. ================================================================================ Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import com.google.gson.JsonObject;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.PayloadConstants;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;
import static org.onap.so.client.cds.PayloadConstants.*;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;

@Component
public class GenericPnfCDSControllerRunnableBB implements ControllerRunnable<BuildingBlockExecution> {

    private static final Logger logger = LoggerFactory.getLogger(GenericPnfCDSControllerRunnableDE.class);
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String ORIGINATOR_ID = "SO";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String DEFAULT_SYNC_MODE = "sync";
    private static final String MSO_REQUEST_ID = "msoRequestId";

    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils;
    private ExtractPojosForBB extractPojosForBB;
    private ExceptionBuilder exceptionBuilder;

    @Autowired
    public GenericPnfCDSControllerRunnableBB(AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils,
            ExtractPojosForBB extractPojosForBB, ExceptionBuilder exceptionBuilder) {
        this.abstractCDSProcessingBBUtils = abstractCDSProcessingBBUtils;
        this.extractPojosForBB = extractPojosForBB;
        this.exceptionBuilder = exceptionBuilder;
    }

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> controllerContext) {
        return PayloadConstants.CDS_ACTOR.equalsIgnoreCase(controllerContext.getControllerActor())
                && PayloadConstants.PNF_SCOPE.equalsIgnoreCase(controllerContext.getControllerScope());
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> controllerContext) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> controllerContext) {
        BuildingBlockExecution buildingBlockExecution = controllerContext.getExecution();
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean =
                prepareAndSetCdsPropertyBean(buildingBlockExecution);

        buildingBlockExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> controllerContext) {
        BuildingBlockExecution buildingBlockExecution = controllerContext.getExecution();
        abstractCDSProcessingBBUtils.constructExecutionServiceInputObject(buildingBlockExecution);
        abstractCDSProcessingBBUtils.sendRequestToCDSClient(buildingBlockExecution);
    }

    protected AbstractCDSPropertiesBean prepareAndSetCdsPropertyBean(BuildingBlockExecution buildingBlockExecution) {
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();

        if (getAction(buildingBlockExecution) == null) {
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 7000, "Action is null!");
        }

        abstractCDSPropertiesBean
                .setRequestObject(this.buildRequestPayload(getAction(buildingBlockExecution), buildingBlockExecution));
        abstractCDSPropertiesBean.setBlueprintName(buildingBlockExecution.getVariable(PRC_BLUEPRINT_NAME));
        abstractCDSPropertiesBean.setBlueprintVersion(buildingBlockExecution.getVariable(PRC_BLUEPRINT_VERSION));
        abstractCDSPropertiesBean.setRequestId(buildingBlockExecution.getVariable(MSO_REQUEST_ID));
        abstractCDSPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        abstractCDSPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
        abstractCDSPropertiesBean.setActionName(getAction(buildingBlockExecution));
        abstractCDSPropertiesBean.setMode(extractAndSetMode(buildingBlockExecution));
        return abstractCDSPropertiesBean;
    }

    private String buildRequestPayload(String action, BuildingBlockExecution execution) {
        final JsonObject pnfObject = new JsonObject();
        String resolutionKey = null;
        try {
            final Pnf pnf = getPnf(execution);
            resolutionKey = pnf.getPnfName();

            extractAndSetExecutionVariable("service-instance-id", pnf.getModelInfoPnf().getModelInstanceName(),
                    pnfObject);
            extractAndSetExecutionVariable("service-model-uuid", pnf.getModelInfoPnf().getModelUuid(), pnfObject);
            extractAndSetExecutionVariable("pnf-id", pnf.getPnfId(), pnfObject);
            extractAndSetExecutionVariable("pnf-name", pnf.getPnfName(), pnfObject);
            extractAndSetExecutionVariable("pnf-customization-uuid", pnf.getModelInfoPnf().getModelCustomizationUuid(),
                    pnfObject);

        } catch (BBObjectNotFoundException exception) {
            logger.error("An exception occurred when creating payload for CDS request", exception);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, exception);
        }
        final JsonObject cdsPropertyObject = new JsonObject();
        cdsPropertyObject.addProperty(RESOLUTION_KEY, resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, pnfObject);

        return buildRequestJsonObject(cdsPropertyObject, action);
    }

    private String buildRequestJsonObject(JsonObject cdsPropertyObject, String action) {
        String requestBasedOnAction = action.concat(SEPARATOR).concat(REQUEST);
        JsonObject requestObject = new JsonObject();
        requestObject.add(requestBasedOnAction, cdsPropertyObject);
        return requestObject.toString();
    }

    private String extractAndSetMode(BuildingBlockExecution buildingBlockExecution) {
        String mode = DEFAULT_SYNC_MODE;
        Object object = buildingBlockExecution.getVariable(PayloadConstants.MODE);
        if (object != null && !String.valueOf(object).isEmpty()) {
            mode = String.valueOf(object);
        }
        return mode;
    }

    private void extractAndSetExecutionVariable(String jsonProperty, String executionProperty, JsonObject pnfObject) {
        if (executionProperty != null) {
            pnfObject.addProperty(jsonProperty, executionProperty);
        }
    }

    private String getAction(BuildingBlockExecution buildingBlockExecution) {
        ExecuteBuildingBlock executeBuildingBlock = buildingBlockExecution.getVariable(BUILDING_BLOCK);
        return executeBuildingBlock.getBuildingBlock().getBpmnAction();
    }

    private Pnf getPnf(BuildingBlockExecution buildingBlockExecution) throws BBObjectNotFoundException {
        return extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.PNF);
    }
}
