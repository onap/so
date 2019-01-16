/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.infrastructure.pnf.implementation.AaiConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateRelation implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateRelation.class);

    private AaiConnection aaiConnectionImpl;

    @Autowired
    public CreateRelation(AaiConnection aaiConnectionImpl) {
        this.aaiConnectionImpl = aaiConnectionImpl;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String serviceInstanceId = (String) delegateExecution.getVariable("serviceInstanceId");
        String pnfName = (String) delegateExecution.getVariable("correlationId");
        try {
            aaiConnectionImpl.createRelation(serviceInstanceId, pnfName);
        } catch (Exception e) {
            new ExceptionUtil().buildAndThrowWorkflowException(delegateExecution, 9999,
                    "An exception occurred when making service and pnf relation. Exception: " + e.getMessage());
        }
        logger.debug("The relation has been made between service with id: {} and pnf with name: {}",
                serviceInstanceId, pnfName);
    }

}
