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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.tasks;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NF_INST_ID_PARAM_NAME;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public abstract class MonitorSol003AdapterNodeTask extends AbstractNetworkServiceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorSol003AdapterNodeTask.class);
    private final AaiServiceProvider aaiServiceProvider;

    public MonitorSol003AdapterNodeTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
    }

    /**
     * Check the final status of vnf in A&AI
     *
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void getNodeStatus(final DelegateExecution execution) {
        try {
            LOGGER.debug("Executing getNodeStatus  ...");
            final String vnfId = (String) execution.getVariable(NF_INST_ID_PARAM_NAME);

            LOGGER.debug("Query A&AI for generic VNF using vnfID: {}", vnfId);
            final Optional<GenericVnf> aaiGenericVnfOptional = aaiServiceProvider.getGenericVnf(vnfId);

            if (!aaiGenericVnfOptional.isPresent()) {
                abortOperation(execution, "Unable to invoke Sol003 adapter for create and instantiate vnfId" + vnfId);
            }
            final GenericVnf genericVnf = aaiGenericVnfOptional.get();
            final String orchestrationStatus = genericVnf.getOrchestrationStatus();
            LOGGER.debug("Found generic vnf with orchestration status : {}", orchestrationStatus);

            execution.setVariable(getNodeStatusVariableName(), isOrchestrationStatusValid(orchestrationStatus));

        } catch (final Exception exception) {
            LOGGER.error("Unable to get vnf from AAI", exception);
            abortOperation(execution, "Unable to get vnf from AAI");
        }
    }

    /**
     * Get variable to store in execution context
     *
     * @return the variable name
     */
    public abstract String getNodeStatusVariableName();

    /**
     * @param orchestrationStatus the orchestration status from A&AI
     * @return true if valid
     */
    public abstract boolean isOrchestrationStatusValid(final String orchestrationStatus);

    public void timeOutLogFailue(final DelegateExecution execution) {
        final String message = "Node operation time out";
        LOGGER.error(message);
        abortOperation(execution, message);
    }
}
