package org.onap.so.bpmn.infrastructure.pnf.bbtasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.pnf.delegate.PnfManagementTestImpl;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.bbtasks.PnfTasksUtils.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.bbtasks.PnfTasksUtils.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.bbtasks.PnfTasksUtils.preparePnf;

@RunWith(MockitoJUnitRunner.class)
public class CreatePnfEntryInAaiTest {

    @Mock
    private ExtractPojosForBB extractPojosForBB;
    @InjectMocks
    private CreatePnfEntryInAai task = new CreatePnfEntryInAai();
    private PnfManagementTestImpl pnfManagementTest = new PnfManagementTestImpl();

    @Before
    public void setUp() throws Exception {
        task.setPnfManagement(pnfManagementTest);
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.PNF)))
                .thenReturn(preparePnf(PNF_CORRELATION_ID, PNF_UUID));
    }

    @Test
    public void shouldSetPnfIdAndPnfName() throws Exception {
        // when
        task.execute(mock(BuildingBlockExecution.class));
        // then
        Pnf createdEntry = pnfManagementTest.getCreated().get(PNF_CORRELATION_ID);
        assertThat(createdEntry.getPnfId()).isEqualTo(PNF_UUID);
        assertThat(createdEntry.getPnfName()).isEqualTo(PNF_CORRELATION_ID);
        assertThat(createdEntry.isInMaint()).isNull();
    }

}
