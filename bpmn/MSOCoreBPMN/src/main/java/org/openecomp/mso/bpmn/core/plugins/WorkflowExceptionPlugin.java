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
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

import org.openecomp.mso.bpmn.core.BPMNLogger;
import org.openecomp.mso.bpmn.core.WorkflowException;

/**
 * This plugin does the following:
 * <ol>
 * <li>
 * Adds logic at the start of every Call Activity to remove any existing
 * WorkflowException object from the execution (saving a copy of it in a
 * different variable).
 * </li>
 * <li>
 * Adds logic at the end of every Call Activity to generate a MSOWorkflowException
 * event if there is a WorkflowException object in the execution.
 * </li>
 * </ol>
 */
public class WorkflowExceptionPlugin extends AbstractProcessEnginePlugin {
	
	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<BpmnParseListener> preParseListeners =
			processEngineConfiguration.getCustomPreBPMNParseListeners();

		if (preParseListeners == null) {
			preParseListeners = new ArrayList<>();
			processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
		}

		preParseListeners.add(new WorkflowExceptionParseListener());
	}
	
	public static class WorkflowExceptionParseListener extends AbstractBpmnParseListener {
		@Override
		public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
			AtomicInteger triggerTaskIndex = new AtomicInteger(1);
			List<ActivityImpl> activities = new ArrayList<>(processDefinition.getActivities());
			recurse(activities, triggerTaskIndex);
		}

		/**
		 * Helper method that recurses (into subprocesses) over all the listed activities.
		 * @param activities a list of workflow activities
		 * @param triggerTaskIndex the index of the next trigger task (mutable)
		 */
		private void recurse(List<ActivityImpl> activities, AtomicInteger triggerTaskIndex) {
			for (ActivityImpl activity : activities) {
				String type = (String) activity.getProperty("type");

				if ("callActivity".equals(type)) {
					// Add a WorkflowExceptionResetListener to clear the WorkflowException
					// variable when each Call Activity starts.

					activity.addListener(
						ExecutionListener.EVENTNAME_START,
						new WorkflowExceptionResetListener());

					// Add a WorkflowExceptionTriggerTask after the call activity.
					// It must be a task because a listener cannot be used to generate
					// an event.  Throwing BpmnError from an execution listener will
					// cause the process to die.

					List<PvmTransition> outTransitions =
                        new ArrayList<>(activity.getOutgoingTransitions());

					for (PvmTransition transition : outTransitions) {
						String triggerTaskId = "WorkflowExceptionTriggerTask_" + triggerTaskIndex;

						ActivityImpl triggerTask = activity.getFlowScope().createActivity(triggerTaskId);

						ClassDelegateActivityBehavior behavior = new  ClassDelegateActivityBehavior(
								WorkflowExceptionTriggerTask.class.getName(),
                            new ArrayList<>(0));

						triggerTask.setActivityBehavior(behavior);
						triggerTask.setName("Workflow Exception Trigger Task " + triggerTaskIndex);
						triggerTaskIndex.getAndIncrement();

						TransitionImpl transitionImpl = (TransitionImpl) transition;
						TransitionImpl triggerTaskOutTransition = triggerTask.createOutgoingTransition();
						triggerTaskOutTransition.setDestination((ActivityImpl)transitionImpl.getDestination());
						transitionImpl.setDestination(triggerTask);
					}
				} else if ("subProcess".equals(type)) {
					recurse(new ArrayList<>(activity.getActivities()), triggerTaskIndex);
				}
			}
		}
	}
	
    /**
     * If there is a WorkflowException object in the execution, this method
     * removes it (saving a copy of it in a different variable).
     */
	public static class WorkflowExceptionResetListener implements ExecutionListener {
		public void notify(DelegateExecution execution) throws Exception {
			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				int index = 1;
				String saveName = "SavedWorkflowException" + index;
				while (execution.getVariable(saveName) != null) {
					saveName = "SavedWorkflowException" + (++index);
				}

				BPMNLogger.debug((String)execution.getVariable("isDebugLogEnabled"),
					"WorkflowExceptionResetTask is moving WorkflowException to " + saveName);

				execution.setVariable(saveName, workflowException);
				execution.setVariable("WorkflowException", null);
			}
		}
	}

    /**
     * Generates an MSOWorkflowException event if there is a WorkflowException
     * object in the execution.
     */
	public static class WorkflowExceptionTriggerTask implements JavaDelegate {
		public void execute(DelegateExecution execution) throws Exception {
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				BPMNLogger.debug((String)execution.getVariable("isDebugLogEnabled"),
					"WorkflowExceptionTriggerTask is generating a MSOWorkflowException event");
				throw new BpmnError("MSOWorkflowException");
			}
		}
	}
}
