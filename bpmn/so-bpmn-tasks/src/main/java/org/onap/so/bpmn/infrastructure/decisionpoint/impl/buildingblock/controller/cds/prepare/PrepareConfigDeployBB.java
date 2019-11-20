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
import java.io.IOException;
import java.util.Optional;
import org.onap.aai.domain.yang.Pnf;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.cds.beans.ConfigDeployPropertiesForPnf;
import org.onap.so.client.cds.beans.ConfigDeployRequestPnf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrepareConfigDeployBB extends PrepareCdsCallBB {

    @Autowired
    private PnfManagement pnfManagement;

    public PrepareConfigDeployBB() {
        this.actionName = "config-deploy";
        this.mode = "async";
    }

    @Override
    protected String getRequestObject(BuildingBlockExecution buildingBlockExecution) {
        ConfigDeployPropertiesForPnf configDeployProperties = new ConfigDeployPropertiesForPnf();

        configDeployProperties.setServiceInstanceId((String) buildingBlockExecution.getVariable(SERVICE_INSTANCE_ID));

        /**
         * PNF Name matches the name in AAI, i.e., correlationID as in customized workflow.
         */
        configDeployProperties.setPnfName((String) buildingBlockExecution.getVariable(PNF_CORRELATION_ID));

        /**
         * PNF id match AAI entry, i.e, PNF UUID.
         */
        configDeployProperties.setPnfId((String) buildingBlockExecution.getVariable(PNF_UUID));
        configDeployProperties
                .setPnfCustomizationUuid((String) buildingBlockExecution.getVariable(PRC_CUSTOMIZATION_UUID));
        configDeployProperties.setServiceModelUuid((String) buildingBlockExecution.getVariable(MODEL_UUID));
        setIpAddress(configDeployProperties, buildingBlockExecution);

        ConfigDeployRequestPnf configDeployRequest = new ConfigDeployRequestPnf();
        configDeployRequest.setConfigDeployPropertiesForPnf(configDeployProperties);

        /**
         * Resolution key is the same as PNF name.
         */
        configDeployRequest.setResolutionKey((String) buildingBlockExecution.getVariable(PNF_CORRELATION_ID));

        return configDeployRequest.toString();
    }

    private void setIpAddress(ConfigDeployPropertiesForPnf configDeployProperties,
            BuildingBlockExecution buildingBlockExecution) {

        /**
         * Retrieve PNF entry from AAI.
         */
        try {
            String pnfName = (String) buildingBlockExecution.getVariable(PNF_CORRELATION_ID);
            Optional<Pnf> pnfOptional = pnfManagement.getEntryFor(pnfName);
            if (pnfOptional.isPresent()) {
                Pnf pnf = pnfOptional.get();

                /**
                 * PRH patches the AAI with oam address. Use ipaddress-v4-oam and ipaddress-v6-oam for the config deploy
                 * request.
                 */
                configDeployProperties.setPnfIpV4Address(pnf.getIpaddressV4Oam());
                configDeployProperties.setPnfIpV6Address(pnf.getIpaddressV6Oam());
            } else {
                exceptionUtil.buildAndThrowWorkflowException(buildingBlockExecution, ERROR_CODE,
                        "AAI entry for PNF: " + pnfName + " does not exist", ONAPComponents.SO);
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            exceptionUtil.buildAndThrowWorkflowException(buildingBlockExecution, ERROR_CODE,
                    "Unable to fetch from AAI" + e.getMessage(), ONAPComponents.SO);
        }
    }

    @Override
    public boolean understand(ControllerContext controllerContext) {
        return super.understand(controllerContext)
                && controllerContext.getControllerAction().trim().equals("config-deploy");
    }
}
