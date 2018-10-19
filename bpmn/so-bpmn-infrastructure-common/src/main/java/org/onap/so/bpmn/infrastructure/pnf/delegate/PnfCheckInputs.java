/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION;

import com.google.common.base.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PnfCheckInputs implements JavaDelegate {

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL, PnfCheckInputs.class);

    private String defaultTimeout;

    @Autowired
    public PnfCheckInputs(@Value("${aai.pnfEntryNotificationTimeout}") String defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String correlationId = (String) execution.getVariable(CORRELATION_ID);
        if (Strings.isNullOrEmpty(correlationId)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, "correlationId variable not defined");
        }
        String timeout = (String) execution.getVariable(TIMEOUT_FOR_NOTIFICATION);
        if (Strings.isNullOrEmpty(timeout)) {
            LOGGER.debug("timeoutForPnfEntryNotification variable not found, setting default");
            if (defaultTimeout == null) {
                new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999,
                        "default timeoutForPnfEntryNotification value not defined");
            }
            execution.setVariable(TIMEOUT_FOR_NOTIFICATION, defaultTimeout);
        }
    }

}
