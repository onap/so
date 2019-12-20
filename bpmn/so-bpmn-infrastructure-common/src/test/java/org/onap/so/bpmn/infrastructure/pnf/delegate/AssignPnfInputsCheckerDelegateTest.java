package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfInputCheckersTestUtils.DelegateExecutionBuilder;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfInputCheckersTestUtils.RESERVED_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfInputCheckersTestUtils.VALID_UUID;

public class AssignPnfInputsCheckerDelegateTest {

    private DelegateExecutionBuilder delegateExecutionBuilder;
    private AssignPnfInputsCheckerDelegate testedObject;
    private DelegateExecution execution;

    @Before
    public void setUp() {
        testedObject = new AssignPnfInputsCheckerDelegate();
        delegateExecutionBuilder = new DelegateExecutionBuilder();
    }

    @Test
    public void shouldThrowException_whenPnfCorrelationIdNotSet() {
        execution = delegateExecutionBuilder.setPnfCorrelationId(null).setPnfUuid(VALID_UUID).build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsNotSet() {
        execution = delegateExecutionBuilder.setPnfUuid(null).build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsEmptyString() {
        execution = delegateExecutionBuilder.setPnfUuid(StringUtils.EMPTY).build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    @Test
    public void shouldThrowException_whenPnfUuidIsReservedUuid() {
        execution = delegateExecutionBuilder.setPnfUuid(RESERVED_UUID).build();
        assertThatSutExecutionThrowsExceptionOfInstance(BpmnError.class);
    }

    private void assertThatSutExecutionThrowsExceptionOfInstance(Class<?> type) {
        assertThatThrownBy(() -> testedObject.execute(execution)).isInstanceOf(type);
    }
}
