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
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.PayloadConstants;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import static org.onap.so.client.cds.PayloadConstants.*;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;


@Component
public class GenericPnfCDSControllerRunnableBB implements ControllerRunnable<BuildingBlockExecution> {

    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String ORIGINATOR_ID = "SO";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String DEFAULT_SYNC_MODE = "sync";
    private static final String MSO_REQUEST_ID = "msoRequestId";

    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils;
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    public GenericPnfCDSControllerRunnableBB(AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils,
            ExtractPojosForBB extractPojosForBB) {
        this.abstractCDSProcessingBBUtils = abstractCDSProcessingBBUtils;
        this.extractPojosForBB = extractPojosForBB;
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

        final String action = getAction(buildingBlockExecution);
        final String requestPayload = String.valueOf(this.buildRequestPayload(action, buildingBlockExecution));
        final String requestId = getMSORequestId(buildingBlockExecution);
        final String mode = extractAndSetMode(buildingBlockExecution);
        final String blueprintName = getBlueprintName(buildingBlockExecution);
        final String blueprintVersion = getBlueprintVersion(buildingBlockExecution);
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean =
                prepareAndSetCdsPropertyBean(requestPayload, requestId, action, mode, blueprintName, blueprintVersion);

        buildingBlockExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> controllerContext) {
        BuildingBlockExecution buildingBlockExecution = controllerContext.getExecution();
        abstractCDSProcessingBBUtils.constructExecutionServiceInputObject(buildingBlockExecution);
        abstractCDSProcessingBBUtils.sendRequestToCDSClient(buildingBlockExecution);
    }

    private AbstractCDSPropertiesBean prepareAndSetCdsPropertyBean(String requestPayload, String requestId,
            String action, String mode, String blueprintName, String blueprintVersion) {
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();

        abstractCDSPropertiesBean.setRequestObject(requestPayload);
        abstractCDSPropertiesBean.setBlueprintName(blueprintName);
        abstractCDSPropertiesBean.setBlueprintVersion(blueprintVersion);
        abstractCDSPropertiesBean.setRequestId(requestId);
        abstractCDSPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        abstractCDSPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
        abstractCDSPropertiesBean.setActionName(action);
        abstractCDSPropertiesBean.setMode(mode);
        return abstractCDSPropertiesBean;
    }

    private Optional<String> buildRequestPayload(String action, BuildingBlockExecution execution) {
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
            exception.printStackTrace();
        }
        final JsonObject cdsPropertyObject = new JsonObject();
        cdsPropertyObject.addProperty(RESOLUTION_KEY, resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, pnfObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

    private String buildRequestJsonObject(JsonObject cdsPropertyObject, String action) {
        String requestBasedOnAction = action.concat(SEPARATOR).concat(REQUEST);
        JsonObject requestObject = new JsonObject();
        requestObject.add(requestBasedOnAction, cdsPropertyObject);
        return requestObject.toString();
    }

    private String extractAndSetMode(BuildingBlockExecution execution) {
        String mode = DEFAULT_SYNC_MODE;
        Object obj = execution.getVariable(PayloadConstants.MODE);
        if (obj != null && !String.valueOf(obj).isEmpty()) {
            mode = String.valueOf(obj);
        }
        return mode;
    }

    private void extractAndSetExecutionVariable(String jsonProperty, String executionProperty, JsonObject pnfObject) {
        if (executionProperty != null) {
            pnfObject.addProperty(jsonProperty, executionProperty);
        }
    }


    protected String getAction(BuildingBlockExecution buildingBlockExecution) {
        ExecuteBuildingBlock executeBuildingBlock = buildingBlockExecution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();

        return buildingBlock.getBpmnAction();
    }

    private Pnf getPnf(BuildingBlockExecution buildingBlockExecution) throws BBObjectNotFoundException {
        return extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.PNF);
    }

    protected String getMSORequestId(BuildingBlockExecution buildingBlockExecution) {
        return String.valueOf(buildingBlockExecution.getVariable(MSO_REQUEST_ID));
    }

    protected String getBlueprintName(BuildingBlockExecution buildingBlockExecution) {
        return String.valueOf(buildingBlockExecution.getVariable(PRC_BLUEPRINT_NAME));
    }

    protected String getBlueprintVersion(BuildingBlockExecution buildingBlockExecution) {
        return String.valueOf(buildingBlockExecution.getVariable(PRC_BLUEPRINT_VERSION));
    }
}
