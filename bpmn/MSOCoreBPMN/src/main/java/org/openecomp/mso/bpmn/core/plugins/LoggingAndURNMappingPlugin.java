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

package org.openecomp.mso.bpmn.core.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

import org.openecomp.mso.bpmn.core.BPMNLogger;
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.bpmn.core.mybatis.CustomMyBatisSessionFactory;
import org.openecomp.mso.bpmn.core.mybatis.URNMapping;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Plugin for MSO logging and URN mapping.
 */
public class LoggingAndURNMappingPlugin extends AbstractProcessEnginePlugin {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static final String FSPROPKEY = "URNMapping.FileSystemLoading.Enabled";

	@Override
	public void preInit(
			ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<BpmnParseListener> preParseListeners = processEngineConfiguration
				.getCustomPreBPMNParseListeners();
		if (preParseListeners == null) {
			preParseListeners = new ArrayList<>();
			processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
		}
		preParseListeners.add(new LoggingParseListener());
	}
	
	/**
	 * Called when a process flow is parsed so we can inject listeners.
	 */
	public static class LoggingParseListener extends AbstractBpmnParseListener {
		private void injectLogExecutionListener(ActivityImpl activity) {
			activity.addListener(
					ExecutionListener.EVENTNAME_END,
					new LoggingExecutionListener("END"));

			activity.addListener(
					ExecutionListener.EVENTNAME_START,
					new LoggingExecutionListener("START"));

			activity.addListener(
					ExecutionListener.EVENTNAME_TAKE,
					new LoggingExecutionListener("TAKE"));
		}

                @Override
		public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
		}

                @Override
		public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
			// Inject these listeners only on the main start event for the flow, not on any embedded subflow start events
			if (scope instanceof ProcessDefinitionEntity) {
				startEventActivity.addListener(ExecutionListener.EVENTNAME_START, new URNMappingInitializerListener("START"));
				startEventActivity.addListener(ExecutionListener.EVENTNAME_START, new LoggingInitializerListener("START"));
			}

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
		public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
			injectLogExecutionListener(timerActivity);
		}

                @Override
		public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
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
		public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
			injectLogExecutionListener(activity);
		}

                @Override
		public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
			//injectLogExecutionListener(activity);
		}

                @Override
		public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
			injectLogExecutionListener(activity);
		}

                @Override
		public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
			injectLogExecutionListener(activity);
		}

                @Override
		public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
			injectLogExecutionListener(timerActivity);
		}

                @Override
		public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
			//injectLogExecutionListener(activity);
		}

                @Override
		public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
			injectLogExecutionListener(activity);
		}

                @Override
		public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
			injectLogExecutionListener(signalActivity);
		}

                @Override
		public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
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
		public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
			injectLogExecutionListener(compensationActivity);
		}

                @Override
		public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
			injectLogExecutionListener(activity);
		}

                @Override
		public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
			injectLogExecutionListener(activity);
		}

                @Override
		public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
			injectLogExecutionListener(nestedActivity);
		}

                @Override
		public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
			injectLogExecutionListener(nestedActivity);
		}

                @Override
		public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
			injectLogExecutionListener(messageActivity);
		}
	}

	/**
	 * Initializes URN mapping variables on process entry.
	 */
	public static class URNMappingInitializerListener implements ExecutionListener {
		private String event;

		public URNMappingInitializerListener(String eventData) {
			this.event = eventData;
		}

		public String getEvent() {
			return event;
		}

                @Override
		public void notify(DelegateExecution execution) throws Exception {
			ProcessEngineConfigurationImpl processEngineConfiguration =
				Context.getProcessEngineConfiguration();
			loadURNProperties(execution, processEngineConfiguration);
		}

		private void loadURNProperties(DelegateExecution execution,
				ProcessEngineConfigurationImpl processEngineConfiguration) {
			Map<String,String> bpmnProps = PropertyConfiguration.getInstance().getProperties("mso.bpmn.properties");
			if (bpmnProps == null) {
				LOGGER.debug("Unable to load mso.bpmn.properties; loading URN Mapping from DB");
				
				LOGGER.error (MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, 
					"Unable to load mso.bpmn.properties; loading URN Mapping from DB");
				
				loadFromDB(execution, processEngineConfiguration);
			} else {
				String fsEnabled = bpmnProps.get(FSPROPKEY);
				if (fsEnabled != null) {
					if (Boolean.parseBoolean(fsEnabled)) {
						LOGGER.debug("File system loading is enabled; loading URN properties from File system");
						LOGGER.info(MessageEnum.BPMN_GENERAL_INFO,  "BPMN",  "File system loading is enabled; loading URN properties from File System");
						loadFromFileSystem(execution);
					} else {
						LOGGER.debug("File system loading is disabled; loading URN properties from DB");
						LOGGER.info (MessageEnum.BPMN_GENERAL_INFO, "BPMN", "File system loading is disabled; loading URN properties from DB");
						
						loadFromDB(execution, processEngineConfiguration);
					}
				} else {
					
					LOGGER.error (MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, 
						"Unable to retrieve URNMapping.FileSystemLoading.Enabled from mso.bpmn.properties; loading URN Mapping from DB");
					
					loadFromDB(execution, processEngineConfiguration);
				}
			}
		}

		private void loadFromFileSystem(DelegateExecution execution) {
			PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
			Map<String,String> props = propertyConfiguration.getProperties("mso.bpmn.urn.properties");
			for (String key : props.keySet()) {
				String varName = URNMapping.createIdentifierFromURN(key);
				String varValue = props.get(key);
				execution.setVariable(varName, varValue);
			}
		}

		private void loadFromDB(DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
			Command<List<URNMapping>> command = commandContext -> (List<URNMapping>) commandContext.getDbSqlSession()
				.selectList("mso.urnMapping.selectAll", null);

			CustomMyBatisSessionFactory sessionFactory = new CustomMyBatisSessionFactory();
			sessionFactory.initFromProcessEngineConfiguration(processEngineConfiguration,
				"customMyBatisConfiguration.xml");

			List<URNMapping> mappings = sessionFactory.getCommandExecutorTxRequired().execute(command);

			if (mappings != null && !mappings.isEmpty()) {
				for (URNMapping mapping : mappings) {
					String varName = URNMapping.createIdentifierFromURN(mapping.getName());
					String varValue = mapping.getValue();

					LOGGER.debug("URN Mapping = '" + mapping.getName()
						+ "', setting variable '" + varName + "' to '" + varValue + "'");

					execution.setVariable(varName, varValue);
				}
			}
		}
	}

	/**
	 * Sets the isDebugLogEnabled variable on process entry.
	 */
	public static class LoggingInitializerListener implements ExecutionListener {
		private String event;

		public LoggingInitializerListener(String eventData) {
			this.event = eventData;
		}

		public String getEvent() {
			return event;
		}

                @Override
		public void notify(DelegateExecution execution) throws Exception {
			String processKey = execution.getProcessEngineServices().getRepositoryService()
				.getProcessDefinition(execution.getProcessDefinitionId()).getKey();

			// If a "true" value is already injected, e.g. from a top-level flow, it SHOULD NOT be
			// overridden by the value in the URN mapping. This allows a top-level flow and all
			// invoked subflows to be debugged by turning on the debug flag for just the top-level
			// flow, assuming the isDebugEnabled flag variable is passed from the top-level flow to
			// its subflows.

			// If a "false" value is already injected, e.g. from a top-level flow, it SHOULD be
			// overridden by the value in the URN mapping.  This allows a subflow to be debugged
			// without turning on the the debug flag for the top-level flow.

			String injectedValue = (String) execution.getVariable("isDebugLogEnabled");
			String urnValue = "true".equals(execution.getVariable("URN_log_debug_" + processKey)) ? "true" : "false";

			if ("true".equals(injectedValue)) {
				LOGGER.debug("Setting isDebugLogEnabled to \"" + injectedValue + "\" for process: " + processKey + " (injected value)");
				execution.setVariable("isDebugLogEnabled", injectedValue);
			} else {
				LOGGER.debug("Setting isDebugLogEnabled to \"" + urnValue + "\" for process: " + processKey + " (from URN mapping)");
				execution.setVariable("isDebugLogEnabled", urnValue);
			}
		}
	}
	
	/**
	 * Logs details about the current activity.
	 */
	public static class LoggingExecutionListener implements ExecutionListener {
		private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
		private static ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();

		private String event;

		public LoggingExecutionListener(String event) {
			this.event = event;
		}

		public String getEvent() {
			return event;
		}

		public void notify(DelegateExecution execution) throws Exception {
			BPMNLogger.debug(
				(String) execution.getVariable("isDebugLogEnabled"),
				"Logging for activity---------------:" + event + ":"
						+ execution.getCurrentActivityName()
						+ ", processDefinitionId="
						+ execution.getProcessDefinitionId() + ", activtyId="
						+ execution.getCurrentActivityId() + ", activtyName='"
						+ execution.getCurrentActivityName() + "'"
						+ ", processInstanceId="
						+ execution.getProcessInstanceId() + ", businessKey="
						+ execution.getProcessBusinessKey() + ", executionId="
						+ execution.getId());

			if (!isBlank(execution.getCurrentActivityName())) {
				try {
					String id = execution.getId();
					if ("START".equals(event) && id != null ) {
						startTimes.put(id, System.currentTimeMillis());
					} else if ("END".equals(event) && id != null) {
						String prefix = (String) execution.getVariable("prefix");

						if (prefix != null ) {
							MsoLogger.setServiceName("MSO." + prefix.substring(0,prefix.length()-1));
						}

						String requestId = (String) execution.getVariable("mso-request-id");
						String svcid = (String) execution.getVariable("mso-service-instance-id");
						MsoLogger.setLogContext(requestId, svcid);
						long startTime = startTimes.remove(id);

						if (startTime != 0) {
							
							LOGGER.recordMetricEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
									event + ": " + execution.getCurrentActivityName(), "BPMN", execution.getCurrentActivityName(), null);
							
						}
					}
				} catch(Exception e) {
					LOGGER.debug("Exception at notify: " + e);
				}
			}
		}

		private boolean isBlank(Object object) {
			return object == null || "".equals(object.toString().trim());
		}
	}
}
