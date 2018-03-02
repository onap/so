package org.openecomp.mso.bpmn.infrastructure.DoCreateServiceInstance;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.orchestration.AAIOrchestrator;

public class RollbackAAIServiceInstance implements JavaDelegate {
	private static Logger LOGGER = Logger.getLogger("RollbackAAIServiceInstance");
	ExceptionUtil exceptionUtil = new ExceptionUtil();
	
	public void execute(DelegateExecution execution) throws Exception {
		boolean aaiServiceInstanceRollback = (boolean) execution.getVariable("aaiServiceInstanceRollback");
		if (aaiServiceInstanceRollback) {
			LOGGER.info("**** Starting RollbackAAIServiceInstance ****");
			try {
				ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("ServiceDecomposition");
				AAIOrchestrator aaiO = new AAIOrchestrator();
				aaiO.deleteServiceInstance(serviceDecomp);
			} catch (Exception ex) {
				String msg = "Error Response from AAI for aaiServiceInstanceRollback";
				execution.setVariable("rollbackError", msg);
				LOGGER.info(msg);
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
			}
			LOGGER.info("**** Finished RollbackAAIServiceInstance ****");
		} else {
			LOGGER.info("**** Skipping RollbackAAIServiceInstance ****");
		}
	}

}
