/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.PreCheckPropertiesForPnf;
import org.onap.so.client.cds.beans.PreCheckRequestPnf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

/**
 * This implementation of {@link JavaDelegate} is used to prepare for pre-check.
 *
 * It queries the PNF resource customization table and construct the {@link AbstractCDSPropertiesBean} as
 * executionObject.
 */
@Component
public class PreparePreCheckDelegate extends PrepareCdsCallDelegate {

    @Autowired
    private PnfManagement pnfManagement;

    public PreparePreCheckDelegate() {
        this.actionName = "preCheck";
        this.mode = "sync";
    }

    @Override
    protected String getRequestObject(final DelegateExecution delegateExecution) {

        PreCheckPropertiesForPnf preCheckProperties = new PreCheckPropertiesForPnf();
        preCheckProperties.setServiceInstanceId((String) delegateExecution.getVariable(SERVICE_INSTANCE_ID));

        /**
         * PNF Name matches the name in AAI, i.e., correlationID as in customized workflow.
         */
        preCheckProperties.setPnfName((String) delegateExecution.getVariable(PNF_CORRELATION_ID));

        /**
         * PNF id match AAI entry, i.e, PNF UUID.
         */
        preCheckProperties.setPnfCustomizationUuid((String) delegateExecution.getVariable(PRC_CUSTOMIZATION_UUID));
        preCheckProperties.setServiceModelUuid((String) delegateExecution.getVariable(MODEL_UUID));
        preCheckProperties.setPnfId((String) delegateExecution.getVariable(PNF_UUID));
        preCheckProperties.setSoftwareVersion((String) delegateExecution.getVariable(NF_SOFTWARE_VERSION));
        setIpAddress(preCheckProperties, delegateExecution);

        PreCheckRequestPnf preCheckRequest = new PreCheckRequestPnf();
        preCheckRequest.setPreCheckPropertiesForPnf(preCheckProperties);

        /**
         * resolution key is the same as PNF name.
         */
        preCheckRequest.setResolutionKey((String) delegateExecution.getVariable(PNF_CORRELATION_ID));

        return preCheckRequest.toString();
    }

    private void setIpAddress(PreCheckPropertiesForPnf preCheckProperties, DelegateExecution delegateExecution) {

        /**
         * Retrieve PNF entry from AAI.
         */
        try {
            String pnfName = (String) delegateExecution.getVariable(PNF_CORRELATION_ID);
            Optional<Pnf> pnfOptional = pnfManagement.getEntryFor(pnfName);
            if (pnfOptional.isPresent()) {
                Pnf pnf = pnfOptional.get();

                /**
                 * Use ipaddress-v4-oam and ipaddress-v6-oam for the pre-check
                 * request.
                 */
                preCheckProperties.setPnfIpV4Address(pnf.getIpaddressV4Oam());
                preCheckProperties.setPnfIpV6Address(pnf.getIpaddressV6Oam());
            } else {
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                        "AAI entry for PNF: " + pnfName + " does not exist");
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to fetch from AAI" + e.getMessage());
        }
    }
}
