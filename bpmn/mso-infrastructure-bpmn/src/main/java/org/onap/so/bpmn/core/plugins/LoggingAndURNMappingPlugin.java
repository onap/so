/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.core.plugins;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;



/**
 * Plugin for MSO logging and URN mapping.
 */
@Component
public class LoggingAndURNMappingPlugin extends AbstractProcessEnginePlugin {

    public static final String SERVICE_INSTANCE_ID = "ServiceInstanceId";
    public static final String SERVICE_NAME = "ServiceName";

    @Autowired
    @Lazy
    private LoggingParseListener loggingParseListener;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
        if (preParseListeners == null) {
            preParseListeners = new ArrayList<>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }
        preParseListeners.add(loggingParseListener);
    }

    /**
     * Called when a process flow is parsed so we can inject listeners.
     */
    @Component
    public class LoggingParseListener extends AbstractBpmnParseListener {


        private void injectLogExecutionListener(ActivityImpl activity) {
            activity.addListener(ExecutionListener.EVENTNAME_END, new LoggingExecutionListener("END"));

            activity.addListener(ExecutionListener.EVENTNAME_START, new LoggingExecutionListener("START"));

            activity.addListener(ExecutionListener.EVENTNAME_TAKE, new LoggingExecutionListener("TAKE"));
        }

        @Override
        public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {}

        @Override
        public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
            // Inject these listeners only on the main start event for the flow, not on any embedded subflow start
            // events

            injectLogExecutionListener(startEventActivity);
        }

        @Override
        public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting,
                ActivityImpl timerActivity) {
            injectLogExecutionListener(timerActivity);
        }

        @Override
        public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting,
                ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration,
                ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
            // injectLogExecutionListener(activity);
        }

        @Override
        public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseMultiInstanceLoopCharacteristics(Element activityElement,
                Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
            injectLogExecutionListener(timerActivity);
        }

        @Override
        public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {

        }

        @Override
        public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition,
                ActivityImpl signalActivity) {
            injectLogExecutionListener(signalActivity);
        }

        @Override
        public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting,
                ActivityImpl signalActivity) {
            injectLogExecutionListener(signalActivity);
        }

        @Override
        public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseCompensateEventDefinition(Element compensateEventDefinition,
                ActivityImpl compensationActivity) {
            injectLogExecutionListener(compensationActivity);
        }

        @Override
        public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope,
                ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope,
                ActivityImpl activity) {
            injectLogExecutionListener(activity);
        }

        @Override
        public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement,
                ActivityImpl nestedActivity) {
            injectLogExecutionListener(nestedActivity);
        }

        @Override
        public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition,
                ActivityImpl nestedActivity) {
            injectLogExecutionListener(nestedActivity);
        }

        @Override
        public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting,
                ActivityImpl messageActivity) {
            injectLogExecutionListener(messageActivity);
        }
    }

    /**
     * Logs details about the current activity.
     */
    public class LoggingExecutionListener implements ExecutionListener {
        private final Logger logger = LoggerFactory.getLogger(LoggingExecutionListener.class);

        private String event;

        public LoggingExecutionListener() {
            this.event = "";
        }

        public LoggingExecutionListener(String event) {
            this.event = event;
        }

        public String getEvent() {
            return event;
        }

        @Override
        public void notify(DelegateExecution execution) throws Exception {
            // required for legacy groovy processing in camunda
            execution.setVariable("isDebugLogEnabled", "true");
            if (!isBlank(execution.getCurrentActivityName())) {
                try {

                    String id = execution.getId();
                    if (id != null) {
                        RepositoryService repositoryService =
                                execution.getProcessEngineServices().getRepositoryService();
                        String processName = repositoryService.createProcessDefinitionQuery()
                                .processDefinitionId(execution.getProcessDefinitionId()).singleResult().getName();


                        String requestId = (String) execution.getVariable("mso-request-id");
                        String svcid = (String) execution.getVariable("mso-service-instance-id");
                        try {
                            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
                            MDC.put(SERVICE_INSTANCE_ID, svcid);
                            MDC.put(SERVICE_NAME, processName);
                        } catch (Exception e) {
                            logger.error("Error trying to add variables to MDC", e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred", e);
                }
            }
        }

        private boolean isBlank(Object object) {
            return object == null || "".equals(object.toString().trim());
        }
    }
}
