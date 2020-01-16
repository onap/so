package org.onap.so.bpmn.infrastructure.pnf.bbtasks;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementTestImpl;
import org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementThrowingException;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementTestImpl.ID_WITHOUT_ENTRY;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementTestImpl.ID_WITH_ENTRY;
import static org.onap.so.bpmn.infrastructure.pnf.bbtasks.PnfTasksUtils.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.bbtasks.PnfTasksUtils.preparePnf;

@RunWith(Enclosed.class)
public class CheckAaiForPnfCorrelationIdTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class ConnectionOkTests {

        @Mock
        private ExtractPojosForBB extractPojosForBB;
        @Mock
        private ExceptionBuilder exceptionUtil;
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @InjectMocks
        private CheckAaiForPnfCorrelationId task = new CheckAaiForPnfCorrelationId();
        private PnfManagement pnfManagementTest = new PnfManagementTestImpl();

        @Before
        public void setUp() {
            task.setPnfManagement(pnfManagementTest);
        }

        @Test
        public void shouldThrowExceptionWhenPnfCorrelationIdIsNotSet() throws Exception {
            // given
            when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.PNF))).thenReturn(preparePnf(null, PNF_UUID));
            BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
            doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(eq(execution),
                    anyInt(), anyString());
            expectedException.expect(BpmnError.class);
            // when
            task.execute(execution);
            // then
            verify(exceptionUtil).buildAndThrowWorkflowException(eq(execution), anyInt(), anyString());
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiDoesNotContainInfoAboutPnf() throws Exception {
            // given
            when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.PNF)))
                    .thenReturn(preparePnf(ID_WITHOUT_ENTRY, PNF_UUID));
            BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
            // when
            task.execute(execution);
            // then
            verify(execution).setVariable(AAI_CONTAINS_INFO_ABOUT_PNF, false);
        }

        @Test
        public void shouldSetCorrectVariablesWhenAaiContainsInfoAboutPnfWithoutIp() throws Exception {
            // given
            when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.PNF)))
                    .thenReturn(preparePnf(ID_WITH_ENTRY, PNF_UUID));
            BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
            // when
            task.execute(execution);
            // then
            verify(execution).setVariable(AAI_CONTAINS_INFO_ABOUT_PNF, true);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class NoConnectionTests {

        @Mock
        private ExtractPojosForBB extractPojosForBB;
        @Mock
        private ExceptionBuilder exceptionUtil;
        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @InjectMocks
        private CheckAaiForPnfCorrelationId task = new CheckAaiForPnfCorrelationId();
        private PnfManagement pnfManagementTest = new PnfManagementThrowingException();

        @Before
        public void setUp() throws Exception {
            task.setPnfManagement(pnfManagementTest);
            when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.PNF)))
                    .thenReturn(preparePnf(PNF_CORRELATION_ID, PNF_UUID));
        }

        @Test
        public void shouldThrowExceptionWhenIoExceptionOnConnectionToAai() {
            // given
            BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
            doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(eq(execution),
                    anyInt(), any(IOException.class));
            expectedException.expect(BpmnError.class);
            // when
            task.execute(execution);
            // then
            verify(exceptionUtil).buildAndThrowWorkflowException(eq(execution), anyInt(), any(IOException.class));
        }
    }
}
