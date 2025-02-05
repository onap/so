/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.listener.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.listener.ListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Component
public class WorkflowActionListenerRunner extends ListenerRunner {

    private static Logger logger = LoggerFactory.getLogger(WorkflowActionListenerRunner.class);

    protected List<WorkflowActionListener> workflowActionListeners;

    @PostConstruct
    protected void init() {
        workflowActionListeners = new ArrayList<>(Optional
                .ofNullable(context.getBeansOfType(WorkflowActionListener.class)).orElse(new HashMap<>()).values());
    }

    public void executeAsyncListeners(String bbName, DelegateExecution execution, String eventName) {
        try {
            logger.info("NotifyingWorkflowActionListeners");
            runNotifications(workflowActionListeners, bbName, execution, eventName);
        } catch (Exception e) {
            logger.error("Error in Notifying Workflow Action Listeners", e);
        }
    }

    protected void runNotifications(List<? extends WorkflowActionListener> listeners, String bbName,
            DelegateExecution execution, String eventName) {
        List<? extends WorkflowActionListener> filtered =
                filterListeners(listeners, (item -> item.shouldRunFor(bbName, eventName)));
        filtered.forEach(item -> item.executeListener(execution));
    }
}
