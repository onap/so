package org.onap.so.adapters.inventory.create;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CreateInventoryTaskTest {

    @Mock
    ExternalTask externalTask;

    @Mock
    ExternalTaskService externalTaskService;

    @InjectMocks
    CreateInventoryTask inventoryTask;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_Runtime_Parse_Exception() {
        doReturn(null).when(externalTask).getVariable("auditInventoryResult");
        inventoryTask.executeExternalTask(externalTask, externalTaskService);
        Mockito.verify(externalTaskService, times(1)).handleBpmnError(externalTask, "AAIInventoryFailure");
    }
}
