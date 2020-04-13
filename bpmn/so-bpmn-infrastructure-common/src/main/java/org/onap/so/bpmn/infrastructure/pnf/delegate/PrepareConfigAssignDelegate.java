/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MODEL_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_CUSTOMIZATION_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.ConfigAssignPropertiesForPnf;
import org.onap.so.client.cds.beans.ConfigAssignRequestPnf;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link JavaDelegate} is used to prepare for config Assign.
 *
 * It queries the PNF resource customization table and construct the {@link AbstractCDSPropertiesBean} as
 * executionObject.
 */
@Component
public class PrepareConfigAssignDelegate extends PrepareCdsCallDelegate {

    public PrepareConfigAssignDelegate() {
        this.actionName = "config-assign";
        this.mode = "sync";
    }

    @Override
    protected String getRequestObject(final DelegateExecution delegateExecution) {

        ConfigAssignPropertiesForPnf configAssignProperties = new ConfigAssignPropertiesForPnf();
        configAssignProperties.setServiceInstanceId((String) delegateExecution.getVariable(SERVICE_INSTANCE_ID));

        /**
         * PNF Name matches the name in AAI, i.e., correlationID as in customized workflow.
         */
        configAssignProperties.setPnfName((String) delegateExecution.getVariable(PNF_CORRELATION_ID));

        /**
         * PNF id match AAI entry, i.e, PNF UUID.
         */
        configAssignProperties.setPnfId((String) delegateExecution.getVariable(PNF_UUID));
        configAssignProperties.setPnfCustomizationUuid((String) delegateExecution.getVariable(PRC_CUSTOMIZATION_UUID));
        configAssignProperties.setServiceModelUuid((String) delegateExecution.getVariable(MODEL_UUID));

        ConfigAssignRequestPnf configAssignRequest = new ConfigAssignRequestPnf();
        configAssignRequest.setConfigAssignPropertiesForPnf(configAssignProperties);

        /**
         * resolution key is the same as PNF name.
         */
        configAssignRequest.setResolutionKey((String) delegateExecution.getVariable(PNF_CORRELATION_ID));

        return configAssignRequest.toString();
    }

}
