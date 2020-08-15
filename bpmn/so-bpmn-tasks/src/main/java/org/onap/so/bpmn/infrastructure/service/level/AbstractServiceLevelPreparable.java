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

package org.onap.so.bpmn.infrastructure.service.level;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for Service level upgrade Execution, it should be extended for service level upgrade tasks.
 */
public abstract class AbstractServiceLevelPreparable {

    protected static final String HEALTH_CHECK_WORKFLOW_TO_INVOKE = "healthCheckWorkflow";
    protected static final String SOFTWARE_WORKFLOW_TO_INVOKE = "softwareUpgradeWorkflow";
    protected static final String HEALTH_CHECK_OPERATION = "ResourceHealthCheck";
    protected static final String SOFTWARE_UP_OPERATION = "SoftwareUpgrade";
    protected static final String RESOURCE_TYPE = "resourceType";
    protected static final String CONTROLLER_STATUS = "ControllerStatus";
    protected static final int ERROR_CODE = 601;
    protected static final String PNF = "pnf";
    protected static final String VNF = "vnf";

    protected static final Map<String, String> DEFAULT_HEALTH_CHECK_WORKFLOWS =
            Map.of(PNF, "GenericPnfHealthCheck", VNF, "GenericVNFHealthCheck");
    protected static final Map<String, String> DEFAULT_SOFTWARE_UP_WORKFLOWS =
            Map.of(PNF, "GenericPnfSoftwareUpgrade", VNF, "GenericVnfSoftwareUpgrade");
    protected static final List<String> VALID_CONTROLLER_SCOPE = Arrays.asList(PNF, VNF);

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractServiceLevelPreparable.class);

    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    @Autowired
    protected CatalogDbClient catalogDbClient;

    /**
     * This method fetches workflow names to be invoked based on the controller scope .
     *
     * @param scope Controller scope
     * @return String value of Workflow name
     */
    protected abstract String fetchWorkflowUsingScope(DelegateExecution execution, final String scope);

    /**
     * This method validates the execution parameters to be passed for health check workflow.
     *
     * @param execution Delegate execution obj
     * @param scope Controller scope * Throws workflow exception if validation fails
     */
    protected void validateParamsWithScope(DelegateExecution execution, final String scope, List<String> params)
            throws Exception {
        List<String> invalidVariables = new ArrayList<>();
        for (String param : params) {
            if (!execution.hasVariable(param) || execution.getVariable(param) == null
                    || String.valueOf(execution.getVariable(param)).isEmpty()) {
                invalidVariables.add(param);
            }
        }
        if (invalidVariables.size() > 0) {
            LOG.error("Validation error for the {} health check attributes: {}", scope, invalidVariables);
            exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                    "Validation of health check workflow parameters failed for the scope: " + scope);
        }

    }

}
