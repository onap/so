/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 *
 * ================================================================================
 * Copyright (c) 2020 Ericsson. All rights reserved
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

package org.onap.so.bpmn.infrastructure.service.level;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract class of the Service level upgrade Execution, it should be extended for service level upgrade tasks.
 */
public abstract class AbstractServiceUpgradeDE {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected static final String PNF_HC_WORKFLOW = "GenericPnfHealthCheck";

    // TODO This value needs to be updated once vnf health check workflow is available
    protected static final String VNF_HC_WORKFLOW = "GenericVNFHealthCheck";

    protected static final List<String> PNF_HC_PARAMS = Arrays.asList("SERVICE_MODEL_INFO", "SERVICE_INSTANCE_NAME",
            "PNF_CORRELATION_ID", "MODEL_UUID", "PNF_UUID", "PRC_BLUEPRINT_NAME", "PRC_BLUEPRINT_VERSION",
            "PRC_CUSTOMIZATION_UUID", "RESOURCE_CUSTOMIZATION_UUID_PARAM", "PRC_INSTANCE_NAME", "PRC_CONTROLLER_ACTOR",
            "REQUEST_PAYLOAD");

    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    /**
     * This method fetches workflow names to be invoked based on the controller scope .
     *
     * @param scope Controller scope
     * @return String value of Workflow name
     */
    protected abstract String fetchWorkflowUsingScope(final String scope);

    /**
     * This method validates the execution parameters to be passed for health check workflow.
     *
     * @param execution Delegate execution obj
     * @param scope Controller scope *
     * @return boolean result of validation
     */
    protected abstract boolean validateParamsWithScope(DelegateExecution execution, String scope);


}
