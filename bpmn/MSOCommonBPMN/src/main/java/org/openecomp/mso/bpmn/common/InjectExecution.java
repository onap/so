package org.openecomp.mso.bpmn.common;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component
public class InjectExecution {

	public DelegateExecutionImpl execute (DelegateExecution execution, DelegateExecutionImpl impl) {
		
		impl.setDelegateExecution(execution);
		return impl;
	}
}
