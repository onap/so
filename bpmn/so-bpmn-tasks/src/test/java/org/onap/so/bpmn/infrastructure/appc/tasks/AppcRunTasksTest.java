package org.onap.so.bpmn.infrastructure.appc.tasks;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.common.BuildingBlockExecution;

public class AppcRunTasksTest {

	
	private AppcRunTasks appcRunTasks = new AppcRunTasks();
	@Test
	public void mapRollbackVariablesTest() {
		
		BuildingBlockExecution mock = mock(BuildingBlockExecution.class);
		
		appcRunTasks.mapRollbackVariables(mock, Action.Lock, "1");
		verify(mock, times(0)).setVariable(any(String.class), any());
		appcRunTasks.mapRollbackVariables(mock, Action.Lock, "0");
		verify(mock, times(1)).setVariable("rollbackVnfLock", true);
		appcRunTasks.mapRollbackVariables(mock, Action.Unlock, "0");
		verify(mock, times(1)).setVariable("rollbackVnfLock", false);
		appcRunTasks.mapRollbackVariables(mock, Action.Start, "0");
		verify(mock, times(1)).setVariable("rollbackVnfStop", false);
		appcRunTasks.mapRollbackVariables(mock, Action.Stop, "0");
		verify(mock, times(1)).setVariable("rollbackVnfStop", true);
		appcRunTasks.mapRollbackVariables(mock, Action.QuiesceTraffic, "0");
		verify(mock, times(1)).setVariable("rollbackQuiesceTraffic", true);
		appcRunTasks.mapRollbackVariables(mock, Action.ResumeTraffic, "0");
		verify(mock, times(1)).setVariable("rollbackQuiesceTraffic", false);
	}
}
