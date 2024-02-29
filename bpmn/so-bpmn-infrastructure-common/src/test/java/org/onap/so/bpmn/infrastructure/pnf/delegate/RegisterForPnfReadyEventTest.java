package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;

public class RegisterForPnfReadyEventTest {

    private static final String PNF_NAME = "pnfNameTest";
    private static final String PNF_ENTRY_NOTIFICATION_TIMEOUT = "P14D";
    private static final String PROCESS_INSTANCE_ID = "testInstanceId";

    private DelegateExecution delegateExecution;
    private ExtractPojosForBB extractPojosForBBMock;
    private KafkaClientTestImpl kafkaClientTest;
    private MessageCorrelationBuilder messageCorrelationBuilder;
    private ExceptionBuilder exceptionBuilderMock;
    private BuildingBlockExecution buildingBlockExecution;

    private RegisterForPnfReadyEvent testedObject;

    @Before
    public void init() {
        delegateExecution = prepareExecution();
        kafkaClientTest = new KafkaClientTestImpl();
        exceptionBuilderMock = mock(ExceptionBuilder.class);
        extractPojosForBBMock = mock(ExtractPojosForBB.class);
        buildingBlockExecution = new DelegateExecutionImpl(new HashMap<>());
        when(delegateExecution.getVariable("gBuildingBlockExecution")).thenReturn(buildingBlockExecution);
    }

    @Test
    public void shouldRegisterForKafkaClient() throws BBObjectNotFoundException {
        // given
        testedObject = new RegisterForPnfReadyEvent(kafkaClientTest, extractPojosForBBMock, exceptionBuilderMock,
                PNF_ENTRY_NOTIFICATION_TIMEOUT);
        Pnf pnf = new Pnf();
        pnf.setPnfName(PNF_NAME);
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF)).thenReturn(pnf);
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(delegateExecution).setVariable(ExecutionVariableNames.PNF_CORRELATION_ID, PNF_NAME);
        verify(delegateExecution).setVariable(ExecutionVariableNames.TIMEOUT_FOR_NOTIFICATION,
                PNF_ENTRY_NOTIFICATION_TIMEOUT);
        checkIfInformConsumerThreadIsRunProperly(kafkaClientTest);
    }

    @Test
    public void pnfNotFoundInBBexecution_WorkflowExIsThrown() throws BBObjectNotFoundException {
        // given
        testedObject = new RegisterForPnfReadyEvent(kafkaClientTest, extractPojosForBBMock, exceptionBuilderMock,
                PNF_ENTRY_NOTIFICATION_TIMEOUT);
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF))
                .thenThrow(BBObjectNotFoundException.class);
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(exceptionBuilderMock).buildAndThrowWorkflowException(delegateExecution, 7000,
                "pnf resource not found in buildingBlockExecution while registering to kafka listener");
    }

    @Test
    public void pnfNameIsNull_WorkflowExIsThrown() throws BBObjectNotFoundException {
        // given
        testedObject = new RegisterForPnfReadyEvent(kafkaClientTest, extractPojosForBBMock, exceptionBuilderMock,
                PNF_ENTRY_NOTIFICATION_TIMEOUT);
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF)).thenReturn(new Pnf());
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(exceptionBuilderMock).buildAndThrowWorkflowException(delegateExecution, 7000, "pnf name is not set");
    }

    @Test
    public void pnfEventNotificationTimeoutNotSet_WorkflowExIsThrown() throws BBObjectNotFoundException {
        // given
        testedObject = new RegisterForPnfReadyEvent(kafkaClientTest, extractPojosForBBMock, exceptionBuilderMock, null);
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF)).thenReturn(new Pnf());
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(exceptionBuilderMock).buildAndThrowWorkflowException(delegateExecution, 7000,
                "pnfEntryNotificationTimeout value not defined");
    }

    private void checkIfInformConsumerThreadIsRunProperly(KafkaClientTestImpl kafkaClientTest) {
        kafkaClientTest.getInformConsumer().run();
        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceId(PROCESS_INSTANCE_ID);
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    private DelegateExecution prepareExecution() {
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        ProcessEngineServices processEngineServices = mock(ProcessEngineServices.class);
        when(delegateExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(processEngineServices.getRuntimeService()).thenReturn(runtimeService);

        messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceId(any())).thenReturn(messageCorrelationBuilder);

        return delegateExecution;
    }

}
