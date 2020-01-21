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

public class RegisterForDmaapClientTest {

    private DelegateExecution delegateExecution;
    private ExtractPojosForBB extractPojosForBBMock;
    private DmaapClientTestImpl dmaapClientTest;
    private MessageCorrelationBuilder messageCorrelationBuilder;
    private ExceptionBuilder exceptionBuilderMock;
    private BuildingBlockExecution buildingBlockExecution;

    private RegisterForDmaapClient testedObject;

    @Before
    public void init() {
        delegateExecution = prepareExecution();
        dmaapClientTest = new DmaapClientTestImpl();
        exceptionBuilderMock = mock(ExceptionBuilder.class);
        extractPojosForBBMock = mock(ExtractPojosForBB.class);
        testedObject = new RegisterForDmaapClient(dmaapClientTest, extractPojosForBBMock, exceptionBuilderMock);
        buildingBlockExecution = new DelegateExecutionImpl(new HashMap<>());
        when(delegateExecution.getVariable("gBuildingBlockExecution")).thenReturn(buildingBlockExecution);
    }

    @Test
    public void shouldRegisterForDmaapClient() throws BBObjectNotFoundException {
        // given
        Pnf pnf = new Pnf();
        pnf.setPnfName("pnfNameTest");
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF)).thenReturn(pnf);
        // when
        testedObject.execute(delegateExecution);
        // then
        checkIfInformConsumerThreadIsRunProperly(dmaapClientTest);
    }

    @Test
    public void pnfNotFoundInBBexecution_WorkflowExIsThrown() throws BBObjectNotFoundException {
        // given
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF))
                .thenThrow(BBObjectNotFoundException.class);
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(exceptionBuilderMock).buildAndThrowWorkflowException(delegateExecution, 7000,
                "pnf resource not found in buildingBlockExecution while registering to dmaap listener");
    }

    @Test
    public void pnfNameIsNull_WorkflowExIsThrown() throws BBObjectNotFoundException {
        // given;;
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.PNF)).thenReturn(new Pnf());
        // when
        testedObject.execute(delegateExecution);
        // then
        verify(exceptionBuilderMock).buildAndThrowWorkflowException(delegateExecution, 7000, "pnf name is not set");
    }

    private void checkIfInformConsumerThreadIsRunProperly(DmaapClientTestImpl dmaapClientTest) {
        dmaapClientTest.getInformConsumer().run();
        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceBusinessKey("testBusinessKey");
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    private DelegateExecution prepareExecution() {
        DelegateExecution delegateExecution = mock(DelegateExecution.class);
        when(delegateExecution.getProcessBusinessKey()).thenReturn("testBusinessKey");
        ProcessEngineServices processEngineServices = mock(ProcessEngineServices.class);
        when(delegateExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(processEngineServices.getRuntimeService()).thenReturn(runtimeService);

        messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey(any())).thenReturn(messageCorrelationBuilder);

        return delegateExecution;
    }
}
