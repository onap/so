package org.openecomp.mso.bpmn.infrastructure.AAITasks;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.orchestration.AAIOrchestrator;

public class AAICreateServiceInstance implements JavaDelegate {
	private static Logger LOGGER = Logger.getLogger("AAICreateServiceInstance");
	AAIOrchestrator aaiO = new AAIOrchestrator();
	ExceptionUtil exceptionUtil = new ExceptionUtil();

	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("**** Started AAICreateServiceInstance ****");
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition");
		execution.setVariable("aaiServiceInstanceRollback", false);
		try {
			aaiO.createServiceInstance(serviceDecomp);
		} catch (Exception ex) {
			String msg = "Exception in AAICreateServiceInstance. " + ex.getMessage();
			LOGGER.info(msg);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
		}
		execution.setVariable("aaiServiceInstanceRollback", true);
		LOGGER.info("**** Finished AAICreateServiceInstance ****");
	}
}
