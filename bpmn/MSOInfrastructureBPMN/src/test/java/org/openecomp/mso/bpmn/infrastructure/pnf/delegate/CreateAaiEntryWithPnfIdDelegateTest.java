package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class CreateAaiEntryWithPnfIdDelegateTest {

    @Test
    public void shouldSetPnfIdAndPnfName() throws Exception {
        // given
        CreateAaiEntryWithPnfIdDelegate delegate = new CreateAaiEntryWithPnfIdDelegate();
        AaiConnectionTestImpl aaiConnection = new AaiConnectionTestImpl();
        delegate.setAaiConnection(aaiConnection);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(eq(CORRELATION_ID))).thenReturn("testCorrelationId");
        // when
        delegate.execute(execution);
        // then
        assertThat(aaiConnection.getCreated().get("testCorrelationId").getPnfId()).isEqualTo("testCorrelationId");
        assertThat(aaiConnection.getCreated().get("testCorrelationId").getPnfName()).isEqualTo("testCorrelationId");
    }
}