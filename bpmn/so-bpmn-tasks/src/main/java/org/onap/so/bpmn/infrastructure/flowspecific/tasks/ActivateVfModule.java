package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ActivateVfModule {

    private static final Logger logger = LoggerFactory.getLogger(ActivateVfModule.class);
	
	protected static final String VF_MODULE_TIMER_DURATION_PATH = "mso.workflow.vfModuleActivate.timer.duration";
	protected static final String DEFAULT_TIMER_DURATION = "PT180S";
	
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	@Autowired
    private Environment environment;


	public void setTimerDuration(BuildingBlockExecution execution) {
		try {
			String waitDuration = this.environment.getProperty(VF_MODULE_TIMER_DURATION_PATH, DEFAULT_TIMER_DURATION);
			logger.debug("Sleeping before proceeding with SDNC activate. Timer duration: {}", waitDuration);
			execution.setVariable("vfModuleActivateTimerDuration", waitDuration);
		} catch (Exception e) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
		}
	}
}
