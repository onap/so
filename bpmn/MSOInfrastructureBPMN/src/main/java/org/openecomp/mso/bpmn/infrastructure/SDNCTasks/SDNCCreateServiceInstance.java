package org.openecomp.mso.bpmn.infrastructure.SDNCTasks;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.orchestration.SDNCOrchestrator;

public class SDNCCreateServiceInstance implements JavaDelegate {
	private static Logger LOGGER = Logger.getLogger("SDNCCreateServiceInstance");
	SDNCOrchestrator sdncO = new SDNCOrchestrator();
	ExceptionUtil exceptionUtil = new ExceptionUtil();

	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("**** Started SDNCCreateServiceInstance ****");
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition");
		if (serviceDecomp != null) {
			try {
				sdncO.createServiceInstance(serviceDecomp);
			} catch (Exception ex) {
				String msg = "Exception in sdncCreateServiceInstance. " + ex.getMessage();
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
			}
		}
		LOGGER.info("**** Finished SDNCCreateServiceInstance ****");
	}
}
