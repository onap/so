package org.openecomp.mso.bpmn.common;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class MockLoggerDelegate implements JavaDelegate {
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("\n\n ..." + MockLoggerDelegate.class.getName() + " invoked by " + "processDefinitionId="
				+ execution.getProcessDefinitionId() + ", activtyId=" + execution.getCurrentActivityId()
				+ ", activtyName='" + execution.getCurrentActivityName() + "'" + ", processInstanceId="
				+ execution.getProcessInstanceId() + ", businessKey=" + execution.getProcessBusinessKey()
				+ ", executionId=" + execution.getId() + " \n\n");
	}
}