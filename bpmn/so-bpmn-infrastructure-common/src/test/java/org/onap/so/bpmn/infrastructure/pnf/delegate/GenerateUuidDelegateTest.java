package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class GenerateUuidDelegateTest {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB]{1}[0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";
    private GenerateUuidDelegate delegate;

    @Before
    public void setUp() {
        delegate = new GenerateUuidDelegate();
    }

    @Test
    public void execute_shouldSetValidUuidAsPnfUuid() {
        // given
        DelegateExecution execution = mock(DelegateExecution.class);
        // when
        delegate.execute(execution);
        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(execution).setVariable(eq(PNF_UUID), captor.capture());
        assertTrue(captor.getValue().matches(UUID_REGEX));
    }
}