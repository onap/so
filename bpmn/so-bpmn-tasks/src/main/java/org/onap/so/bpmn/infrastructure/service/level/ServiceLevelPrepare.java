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

/**
 * Interface for Service level upgrade Execution, it should be implemented for service level upgrade tasks.
 */
public interface ServiceLevelPrepare {
    static final String WORKFLOW_TO_INVOKE = "healthCheckWorkflow";
    static final String PNF_HC_WORKFLOW = "GenericPnfHealthCheck";
    static final String PNF_SOFTWARE_UP_WORKFLOW = "GenericPnfSoftwareUpgrade";
    static final String EXECUTION_SCOPE = "RESOURCE_TYPE";

    // TODO This value needs to be updated once vnf health check workflow is available
    static final String VNF_HC_WORKFLOW = "GenericVNFHealthCheck";

    /**
     * This method fetches workflow names to be invoked based on the controller scope .
     *
     * @param scope Controller scope
     * @return String value of Workflow name
     */
    String fetchWorkflowUsingScope(final String scope);

    /**
     * This method validates the execution parameters to be passed for health check workflow.
     *
     * @param execution Delegate execution obj
     * @param scope Controller scope * Throws workflow exception if validation fails
     */
    void validateParamsWithScope(DelegateExecution execution, String scope) throws Exception;


}
