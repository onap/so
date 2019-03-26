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
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MODEL_UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for preparing CDS call.
 */
public abstract class PrepareCdsCallDelegate implements JavaDelegate {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final String ORIGINATOR_ID = "SO";
    protected static final String MODE = "sync";
    protected static final int ERROR_CODE = 7001;

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception{
        try {
            String serviceModelUuid = (String) delegateExecution.getVariable(MODEL_UUID);
            Service serviceByID = catalogDbClient.getServiceByID(serviceModelUuid);
            delegateExecution.setVariable(EXECUTION_OBJECT, buildRequest(delegateExecution, serviceByID));
        } catch(BpmnError e){
            throw e;
        } catch (Exception e){
            logger.warn(e.getMessage(), e);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE, e);
        }
    }

    protected abstract AbstractCDSPropertiesBean buildRequest(DelegateExecution delegateExecution, Service service) throws Exception;
}
