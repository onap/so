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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.cds.prepare;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MODEL_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_CUSTOMIZATION_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.client.cds.beans.ConfigAssignPropertiesForPnf;
import org.onap.so.client.cds.beans.ConfigAssignRequestPnf;
import org.springframework.stereotype.Component;

@Component
public class PrepareConfigAssignBB extends PrepareCdsCallBB {

    public PrepareConfigAssignBB() {
        this.actionName = "config-assign";
        this.mode = "sync";
    }

    @Override
    protected String getRequestObject(BuildingBlockExecution buildingBlockExecution) {
        ConfigAssignPropertiesForPnf configAssignProperties = new ConfigAssignPropertiesForPnf();
        configAssignProperties.setServiceInstanceId((String) buildingBlockExecution.getVariable(SERVICE_INSTANCE_ID));

        /**
         * PNF Name matches the name in AAI, i.e., correlationID as in customized workflow.
         */
        configAssignProperties.setPnfName((String) buildingBlockExecution.getVariable(PNF_CORRELATION_ID));

        /**
         * PNF id match AAI entry, i.e, PNF UUID.
         */
        configAssignProperties.setPnfId((String) buildingBlockExecution.getVariable(PNF_UUID));
        configAssignProperties
                .setPnfCustomizationUuid((String) buildingBlockExecution.getVariable(PRC_CUSTOMIZATION_UUID));
        configAssignProperties.setServiceModelUuid((String) buildingBlockExecution.getVariable(MODEL_UUID));

        ConfigAssignRequestPnf configAssignRequest = new ConfigAssignRequestPnf();
        configAssignRequest.setConfigAssignPropertiesForPnf(configAssignProperties);

        /**
         * resolution key is the same as PNF name.
         */
        configAssignRequest.setResolutionKey((String) buildingBlockExecution.getVariable(PNF_CORRELATION_ID));

        return configAssignRequest.toString();
    }

    @Override
    public boolean understand(ControllerContext controllerContext) {
        return super.understand(controllerContext)
                && controllerContext.getControllerAction().trim().equals("config-assign");
    }
}
