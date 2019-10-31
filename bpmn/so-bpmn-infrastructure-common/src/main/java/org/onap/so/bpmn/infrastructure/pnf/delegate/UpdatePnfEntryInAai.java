/*
 * ============LICENSE_START======================================================= Copyright (C) 2020 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.client.cds.PayloadConstants.PRC_TARGET_SOFTWARE_VERSION;


@Component
public class UpdatePnfEntryInAai implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(UpdatePnfEntryInAai.class);

    @Autowired
    private PnfManagement pnfManagement;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String pnfCorrelationId = String.valueOf(execution.getVariable(PNF_CORRELATION_ID));
        Optional<Pnf> pnfAaiEntry = pnfManagement.getEntryFor(pnfCorrelationId);

        if (!pnfAaiEntry.isPresent()) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 404,
                    "AAI entry for PNF: " + PNF_CORRELATION_ID + " does not exist");
        }

        Pnf pnf = pnfAaiEntry.get();
        String pnfSoftwareVersion = String.valueOf(execution.getVariable(PRC_TARGET_SOFTWARE_VERSION));
        pnf.setSwVersion(pnfSoftwareVersion);

        pnfManagement.updateEntry(pnfCorrelationId, pnf);
        logger.debug("AAI entry is updated for pnf correlation id: {}, pnf uuid: {} with swVersion: {}",
                pnf.getPnfName(), pnfCorrelationId, pnfSoftwareVersion);
    }
}
