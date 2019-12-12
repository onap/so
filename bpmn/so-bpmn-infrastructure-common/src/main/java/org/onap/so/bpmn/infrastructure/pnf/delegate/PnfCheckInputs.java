/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
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

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;

import com.google.common.base.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PnfCheckInputs implements JavaDelegate {

    private AssignPnfInputsCheckerDelegate assignPnfInputsCheckerDelegate;
    private String pnfEntryNotificationTimeout;

    @Autowired
    public PnfCheckInputs(@Value("${aai.pnfEntryNotificationTimeout}") String pnfEntryNotificationTimeout,
                          AssignPnfInputsCheckerDelegate assignPnfInputsCheckerDelegate) {
        this.assignPnfInputsCheckerDelegate = assignPnfInputsCheckerDelegate;
        this.pnfEntryNotificationTimeout = pnfEntryNotificationTimeout;
    }

    @Override
    public void execute(DelegateExecution execution) {
        assignPnfInputsCheckerDelegate.execute(execution);
        validateTimeout(execution);
        validateServiceInstanceId(execution);
    }

    private void validateTimeout(DelegateExecution execution) {
        if (Strings.isNullOrEmpty(pnfEntryNotificationTimeout)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999,
                    "timeoutForPnfEntryNotification value not defined");
        }
        execution.setVariable(TIMEOUT_FOR_NOTIFICATION, pnfEntryNotificationTimeout);
    }

    private void validateServiceInstanceId(DelegateExecution execution) {
        String serviceInstanceId = (String) execution.getVariable(SERVICE_INSTANCE_ID);
        if (Strings.isNullOrEmpty(serviceInstanceId)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999,
                    "serviceInstanceId variable not defined");
        }
    }
}
