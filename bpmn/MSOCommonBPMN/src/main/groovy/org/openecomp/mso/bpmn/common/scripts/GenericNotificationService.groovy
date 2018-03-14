package org.openecomp.mso.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import java.text.SimpleDateFormat

public class GenericNotificationService  extends AbstractServiceTaskProcessor {	
	
	

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	
	public void preProcessRequest (DelegateExecution execution) {
		
	}

}
