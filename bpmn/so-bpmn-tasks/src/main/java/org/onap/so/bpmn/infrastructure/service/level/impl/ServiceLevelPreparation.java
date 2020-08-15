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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.service.level.AbstractServiceLevelPreparable;
import org.onap.so.db.catalog.beans.Workflow;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Fetches health check workflow based on the controller_scope. Invoke the corresponding health check workflow after
 * validation.
 */
@Component("ServiceLevelPreparation")
public class ServiceLevelPreparation extends AbstractServiceLevelPreparable implements JavaDelegate {

    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String RESOURCE_TYPE = "resourceType";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String PNF_NAME = "pnfName";

    // Health check parameters to be validated for pnf resource
    private static final Map<String, List<String>> HEALTH_CHECK_PARAMS_MAP =
            Map.of(PNF, Arrays.asList(SERVICE_INSTANCE_ID, RESOURCE_TYPE, BPMN_REQUEST, PNF_NAME));


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariable(RESOURCE_TYPE) && execution.getVariable(RESOURCE_TYPE) != null) {
            final String controllerScope = (String) execution.getVariable(RESOURCE_TYPE);
            LOG.debug("Scope retrieved from delegate execution: " + controllerScope);
            if (VALID_CONTROLLER_SCOPE.contains(controllerScope)) {
                final String wflName = fetchWorkflowUsingScope(execution, controllerScope);
                LOG.debug("Health check workflow fetched for the scope: {}", wflName);
                validateParamsWithScope(execution, controllerScope, HEALTH_CHECK_PARAMS_MAP.get(controllerScope));
                LOG.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(HEALTH_CHECK_WORKFLOW_TO_INVOKE, wflName);
                execution.setVariable(CONTROLLER_STATUS, "");
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                        "Invalid Controller scope to prepare resource level health check");
            }
        } else {
            exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                    "Resource type not found in the execution to invoke resource level health check");
        }
    }

    @Override
    public String fetchWorkflowUsingScope(DelegateExecution execution, final String scope) {
        Optional<String> wflName = Optional.empty();
        try {
            List<Workflow> workflows = catalogDbClient.findWorkflowByOperationName(HEALTH_CHECK_OPERATION);
            if (!workflows.isEmpty()) {
                wflName = Optional.ofNullable(
                        workflows.stream().filter(workflow -> workflow.getResourceTarget().equalsIgnoreCase(scope))
                                .findFirst().get().getName());
            }
        } catch (Exception e) {
            // do nothing and assign the default workflow in finally
            LOG.error("Error occurred while fetching health check workflow name from CatalogDb {}", e);
        } finally {
            if (wflName.isEmpty()) {
                wflName = Optional.of(DEFAULT_HEALTH_CHECK_WORKFLOWS.get(scope));
            }
        }
        return wflName.get();
    }

}
