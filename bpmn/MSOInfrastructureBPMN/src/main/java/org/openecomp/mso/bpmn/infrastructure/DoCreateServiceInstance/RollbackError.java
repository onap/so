package org.openecomp.mso.bpmn.infrastructure.DoCreateServiceInstance;

import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class RollbackError implements JavaDelegate {

	private static Logger LOGGER = Logger.getLogger("RollbackError");

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.info("Caught an Exception in DoCreateServiceInstanceRollbackV3");
		LOGGER.info("Unable to rollback DoCreateServiceInstanceV3");
	}

}
