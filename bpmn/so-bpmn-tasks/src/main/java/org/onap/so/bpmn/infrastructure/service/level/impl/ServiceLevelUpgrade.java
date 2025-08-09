/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.service.level.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.service.level.ServiceLevel;
import org.springframework.stereotype.Component;

@Component
public class ServiceLevelUpgrade extends ServiceLevel implements JavaDelegate {

    private static final List<String> PNF_SOFTWARE_UP_PARAMS = Arrays.asList(ServiceLevelConstants.SERVICE_INSTANCE_ID,
            ServiceLevelConstants.RESOURCE_TYPE, ServiceLevelConstants.BPMN_REQUEST);

    // TODO Update the list with vnf software upgrade parameters if any validation needed
    private static final List<String> VNF_SOFTWARE_UP_PARAMS = Collections.emptyList();

    private static final Map<String, List<String>> SOFTWARE_UP_PARAMS_MAP = Map.of(ServiceLevelConstants.PNF,
            PNF_SOFTWARE_UP_PARAMS, ServiceLevelConstants.VNF, VNF_SOFTWARE_UP_PARAMS);


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOG.debug("Running execute block for activity id: {}, name: {}", execution.getCurrentActivityId(),
                execution.getCurrentActivityName());

        if (execution.hasVariable(ServiceLevelConstants.RESOURCE_TYPE)
                && execution.getVariable(ServiceLevelConstants.RESOURCE_TYPE) != null) {
            final String controllerScope = (String) execution.getVariable(ServiceLevelConstants.RESOURCE_TYPE);
            LOG.debug("Scope retrieved from delegate execution: {}", controllerScope);
            if (ServiceLevelConstants.VALID_CONTROLLER_SCOPE.contains(controllerScope)) {
                final String wflName = fetchWorkflowUsingScope(controllerScope, ServiceLevelConstants.SW_UP_OPERATION);
                LOG.debug("Software Upgrade workflow fetched for the scope: {} is: {}", controllerScope, wflName);
                validateParamsWithScope(execution, controllerScope, SOFTWARE_UP_PARAMS_MAP.get(controllerScope));
                LOG.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(ServiceLevelConstants.SOFTWARE_WORKFLOW_TO_INVOKE, wflName);
                execution.setVariable(ServiceLevelConstants.CONTROLLER_STATUS, ServiceLevelConstants.EMPTY_STRING);
                execution.setVariable(ServiceLevelConstants.PNF_COUNTER, ServiceLevelConstants.COUNT_ZERO);
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(execution, ServiceLevelConstants.ERROR_CODE,
                        "Invalid Controller scope for resource level software upgrade");
            }
        } else {
            exceptionBuilder.buildAndThrowWorkflowException(execution, ServiceLevelConstants.ERROR_CODE,
                    "Resource type not found in the execution to invoke resource level software upgrade");
        }
    }
}
