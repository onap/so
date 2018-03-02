package org.openecomp.mso.bpmn.infrastructure.AAITasks;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.orchestration.AAIOrchestrator;

public class AAICreateOwningEntity implements JavaDelegate {
	private static Logger LOGGER = Logger.getLogger("AAICreateOwningEntity");
	AAIOrchestrator aaiO = new AAIOrchestrator();
	ExceptionUtil exceptionUtil = new ExceptionUtil();

	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("**** Started AAICreateOwningEntity ****");
		ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition");
		if (serviceDecomp.getServiceInstance() != null && serviceDecomp.getOwningEntity() != null) {
			try {
				aaiO.createOwningEntityandConnectServiceInstance(serviceDecomp);
			} catch (Exception ex) {
				String msg = "Exception in AAICreateOwningEntity. " + ex.getMessage();
				LOGGER.info(msg);
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
			}
		}
		LOGGER.info("**** Finished AAICreateOwningEntity ****");
	}
}
