package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;

public class ActivateVfModuleTest extends BaseTaskTest {
	
	@InjectMocks
	private ActivateVfModule activateVfModule = new ActivateVfModule();
	
	@Test
	public void setWaitBeforeDurationTest() throws Exception {
		when(env.getProperty(ActivateVfModule.VF_MODULE_TIMER_DURATION_PATH, ActivateVfModule.DEFAULT_TIMER_DURATION)).thenReturn("PT300S");
		activateVfModule.setTimerDuration(execution);
		verify(env, times(1)).getProperty(ActivateVfModule.VF_MODULE_TIMER_DURATION_PATH, ActivateVfModule.DEFAULT_TIMER_DURATION);
		assertEquals("PT300S", (String) execution.getVariable("vfModuleActivateTimerDuration"));
	}

}
