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
package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda;

import com.google.common.base.Strings;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.AbstractControllerExecution;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used for camunda {@ref DelegateExecution} API based controller execution.
 *
 * The following parameters are expected in the {@ref DelegateExecution} context,
 *
 * <ul>
 * <li>action: action to be executed</li>
 * <li>scope: type of the resource, i.e, pnf, vnf, vf</li>
 * <li>resource_customization_uuid: resource customization UUID</li>
 * <li>resource_type: resource type, optional. It's used to find the controller from controller_selection_reference
 * table. Same as VNF_TYPE in the table</li>
 * </ul>
 */
@Component
public class ControllerExecutionDE extends AbstractControllerExecution<DelegateExecution> implements JavaDelegate {

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Override
    public void execute(final DelegateExecution execution) {
        ControllerContext<DelegateExecution> controllerContext = buildControllerContext(execution);
        controllerExecute(controllerContext);
    }

    /**
     * this method is used to get the controller actor, there could be few places to get the actor(ordered by priority),
     *
     * <ol>
     * <li>Execution Context, i.e, DelegateExecution</li>
     * <li>Resource customization table, pnf_resource_customization for PNF or vnf_resource_customization for VNF</li>
     * <li>controller_selection_reference, resource_type and action will be used to fetch from this table</li>
     * </ol>
     *
     * @param execution DelegateExecution instance
     * @param controllerScope controller scope, e.g, pnf, vnf, vfModule
     * @param resourceCustomizationUuid resource customization UUID, e.g, pnfCustomizationUuid, vnfCustomizationUuid
     * @param controllerAction controller action, e.g, configAssign, configDeploy
     * @return controller actor
     */
    protected String getControllerActor(DelegateExecution execution, String controllerScope,
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
             * secondly, if no meaningful actor from execution context, getting from resource table in database.
             */
            if (isPnfResourceScope(controllerScope)) {
                PnfResourceCustomization pnfResourceCustomization =
                        catalogDbClient.getPnfResourceCustomizationByModelCustomizationUUID(resourceCustomizationUuid);
                controllerActor = pnfResourceCustomization.getControllerActor();
            } else if (isVnfResourceScope(controllerScope)) {
                VnfResourceCustomization vnfResourceCustomization =
                        catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(resourceCustomizationUuid);
                controllerActor = vnfResourceCustomization.getControllerActor();
            } else {
                logger.warn("Unrecognized scope: {}", controllerScope);
            }
        }

        /**
         * Lastly, can NOT find the controller actor information from resource customization table or value is
         * SO-REF-DATA
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
    public void controllerExecute(ControllerContext<DelegateExecution> controllerContext) {
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

    @Override
    protected String getParameterFromExecution(DelegateExecution execution, String parameterName) {
        if (execution.hasVariable(parameterName)) {
            String paramValue = String.valueOf(execution.getVariable(parameterName));
            logger.debug("parameterName: {}, parameterValue: {}", parameterName, paramValue);
            return paramValue;
        } else {
            return "";
        }
    }
}
