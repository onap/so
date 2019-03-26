/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;

import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.ConfigDeployPropertiesForPnf;
import org.onap.so.client.cds.beans.ConfigDeployRequestPnf;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link JavaDelegate} is used to prepare for config Deploy.
 *
 * It queries the PNF resource customization table and construct the {@link AbstractCDSPropertiesBean} as
 * executionObject.
 */
@Component
public class PrepareConfigDeployDelegate extends PrepareCdsCallDelegate {

    private static final String ACTION_NAME = "config-deploy";

    @Override
    protected AbstractCDSPropertiesBean buildRequest(DelegateExecution delegateExecution, Service service)
        throws Exception {
        List<PnfResourceCustomization> pnfCustomizations = service.getPnfCustomizations();
        if (pnfCustomizations == null || pnfCustomizations.size() == 0) {
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                "unable to find the PNF resource customization");
        }
        PnfResourceCustomization pnfResourceCustomization = pnfCustomizations.get(0);
        ConfigDeployPropertiesForPnf configDeployProperties = new ConfigDeployPropertiesForPnf();
        configDeployProperties.setServiceInstanceId((String) delegateExecution.getVariable(SERVICE_INSTANCE_ID));
        configDeployProperties.setPnfName(pnfResourceCustomization.getModelInstanceName());
        configDeployProperties.setPnfId((String) delegateExecution.getVariable(PNF_CORRELATION_ID));
        configDeployProperties.setPnfCustomizationUuid(pnfResourceCustomization.getModelCustomizationUUID());
        configDeployProperties.setServiceModelUuid(service.getModelUUID());

        ConfigDeployRequestPnf configDeployRequest = new ConfigDeployRequestPnf();
        configDeployRequest.setConfigDeployPropertiesForPnf(configDeployProperties);
        configDeployRequest.setResolutionKey(pnfResourceCustomization.getModelInstanceName());

        AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
        cdsPropertiesBean.setBlueprintName(pnfResourceCustomization.getBlueprintName());
        cdsPropertiesBean.setBlueprintVersion(pnfResourceCustomization.getBlueprintVersion());
        cdsPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        cdsPropertiesBean.setActionName(ACTION_NAME);
        cdsPropertiesBean.setMode(MODE);
        cdsPropertiesBean.setRequestId((String) delegateExecution.getVariable(MSO_REQUEST_ID));
        cdsPropertiesBean.setSubRequestId((String) delegateExecution.getVariable(PNF_UUID));
        cdsPropertiesBean.setRequestObject(configDeployRequest.toString());
        return cdsPropertiesBean;
    }
}
