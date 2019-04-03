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

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.EXECUTION_OBJECT;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_BLUEPRINT_NAME;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_BLUEPRINT_VERSION;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for preparing CDS call.
 */
public abstract class PrepareCdsCallDelegate implements JavaDelegate {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final String ORIGINATOR_ID = "SO";

    protected String actionName;
    protected String mode;

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Override
    public void execute(DelegateExecution delegateExecution){

        logger.debug("Running execute block for activity:{}", delegateExecution.getCurrentActivityId());
        AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
        cdsPropertiesBean.setBlueprintName((String) delegateExecution.getVariable(PRC_BLUEPRINT_NAME));
        cdsPropertiesBean.setBlueprintVersion((String) delegateExecution.getVariable(PRC_BLUEPRINT_VERSION));
        cdsPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        cdsPropertiesBean.setActionName(getActionName());
        cdsPropertiesBean.setMode(getMode());
        cdsPropertiesBean.setRequestId((String) delegateExecution.getVariable(MSO_REQUEST_ID));
        cdsPropertiesBean.setSubRequestId((String) delegateExecution.getVariable(PNF_UUID));
        cdsPropertiesBean.setRequestObject(getRequestObject(delegateExecution));
        delegateExecution.setVariable(EXECUTION_OBJECT, cdsPropertiesBean);
    }

    /**
     * Return the request object sent to CDS call.
     *
     * @param delegateExecution BPMN delegateExecution context
     * @return string value of the request object
     */
    protected abstract String getRequestObject(final DelegateExecution delegateExecution);

    public String getActionName() {
        return actionName;
    }

    public String getMode() {
        return mode;
    }

}
