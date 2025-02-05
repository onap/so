/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock;

import com.google.common.base.Strings;
import java.util.Optional;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.AbstractControllerExecution;
import org.onap.so.client.cds.PayloadConstants;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is used for {@ref BuildingBlockExecution} API based execution.
 *
 * it decides which controller implementation to use based on the parameters, like actor, scope, action.
 *
 * The following parameters are expected in the {@ref BuildingBlockExecution} context,
 * <ul>
 * <li>action: action to be executed</li>
 * <li>scope: type of the resource, i.e, pnf, vnf, vf</li>
 * <li>resource_customization_uuid: resource customization UUID</li>
 * <li>resource_type: resource type, optional. It's used to find the controller from controller_selection_reference
 * table. Same as VNF_TYPE in the table</li>
 * </ul>
 */
@Component
public class ControllerExecutionBB extends AbstractControllerExecution<BuildingBlockExecution> {

    @Autowired
    protected CatalogDbClient catalogDbClient;
    @Value("${controller-execution.timeout-for-controller-message:P1D}")
    private String timeoutForControllerMessage;

    public void execute(final BuildingBlockExecution execution) {
        execution.setVariable(PayloadConstants.TIMEOUT_CONTROLLER_MESSAGE, timeoutForControllerMessage);
        ControllerContext<BuildingBlockExecution> controllerContext = buildControllerContext(execution);
        controllerExecute(controllerContext);
    }

    @Override
    protected String getParameterFromExecution(BuildingBlockExecution execution, String parameterName) {
        Object object = execution.getVariable(parameterName);
        if (object != null) {
            String paramValue = String.valueOf(object);
            logger.debug("parameterName: {}, parameterValue: {}", parameterName, paramValue);
            return paramValue;
        }
        return "";
    }

    /**
     * this method is used to get the controller actor, there could be few places to get the actor(ordered by priority),
     *
     * <ol>
     * <li>Execution Context, i.e, BuildingBlockExecution</li>
     * <li>Resource customization table, pnf_resource_customization for PNF or vnf_resource_customization for VNF</li>
     * <li>controller_selection_reference, resource_type and action will be used to fetch from this table</li>
     * </ol>
     *
     * @param execution BuildingBlockExecution instance
     * @param controllerScope controller scope, e.g, pnf, vnf, vfModule
     * @param resourceCustomizationUuid resource customization UUID, e.g, pnfCustomizationUuid, vnfCustomizationUuid
     * @param controllerAction controller action, e.g, configAssign, configDeploy
     * @return controller actor name
     */
    @Override
    protected String getControllerActor(BuildingBlockExecution execution, String controllerScope,
            String resourceCustomizationUuid, String controllerAction) {

        /**
         * Firstly, check the execution context for actor parameter.
         */
        String controllerActor = getParameterFromExecution(execution, CONTROLLER_ACTOR_PARAM);

        /**
         * If no meaningful controller actor value found in the execution context and the value is not SO-REF-DATA.
         */
        if (Strings.isNullOrEmpty(controllerActor) && !isSoRefControllerActor(controllerActor)) {

            /**
             * For BuildingBlockExecution, we should try to get the resource information from Cached metadata.
             *
             * As the current cached metadata doesn't support controller actor, we use the
             * {@link org.onap.so.db.catalog.client.CatalogDbClient} to fetch information. Once the change is done in
             * cached metadata, this part should be refactored as well.
             */
            if (isPnfResourceScope(controllerScope)) {
                PnfResourceCustomization pnfResourceCustomization =
                        catalogDbClient.getPnfResourceCustomizationByModelCustomizationUUID(resourceCustomizationUuid);
                controllerActor = pnfResourceCustomization.getControllerActor();
            } else if (isServiceResourceScope(controllerScope)) {
                return controllerActor;
            } else if (isVnfResourceScope(controllerScope)) {
                VnfResourceCustomization vnfResourceCustomization =
                        catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(resourceCustomizationUuid);
                controllerActor = vnfResourceCustomization.getControllerActor();
            } else {
                logger.warn("Unrecognized scope: {}", controllerScope);
            }
        }

        /**
         * Lastly, can NOT find the controller actor information from resource customization table or the return value
         * is SO-REF-DATA.
         */
        if (Strings.isNullOrEmpty(controllerActor) || isSoRefControllerActor(controllerActor)) {
            String resourceType = getParameterFromExecution(execution, RESOURCE_TYPE_PARAM);
            ControllerSelectionReference reference = catalogDbClient
                    .getControllerSelectionReferenceByVnfTypeAndActionCategory(resourceType, controllerAction);

            controllerActor = reference.getControllerName();
        }
        return controllerActor;
    }

    @Override
    public void controllerExecute(ControllerContext<BuildingBlockExecution> controllerContext) {
        Optional<ControllerRunnable> optional = getController(controllerContext);
        if (optional.isPresent()) {
            ControllerRunnable controller = optional.get();
            if (controller.ready(controllerContext)) {
                controller.prepare(controllerContext);
                controller.run(controllerContext);
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(controllerContext.getExecution(), 9001,
                        "Controller is NOT Ready for the action", ONAPComponents.SO);
            }
        } else {
            exceptionBuilder.buildAndThrowWorkflowException(controllerContext.getExecution(), 9000,
                    "Unable to find the controller implementation", ONAPComponents.SO);
        }
    }

    public void handleFailure(final BuildingBlockExecution execution) {
        String errorMessage = execution.getVariable(PayloadConstants.CONTROLLER_ERROR_MESSAGE);

        if (Boolean.TRUE.equals(execution.getVariable(PayloadConstants.CONTROLLER_MSG_TIMEOUT_REACHED))) {
            logger.error(
                    "timeout-for-controller-message was reached. If the controller is still processing, this property should be reconfigured");
            errorMessage = "Controller response was not received within configured timeout";
        } else if (errorMessage == null) {
            errorMessage = "Controller call failed. No errormessage was captured.";
        }

        exceptionBuilder.buildAndThrowWorkflowException(execution, 9003, errorMessage, ONAPComponents.SO);
    }
}
