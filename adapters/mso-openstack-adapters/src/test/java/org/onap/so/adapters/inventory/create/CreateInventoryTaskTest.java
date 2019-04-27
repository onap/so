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
import org.onap.so.adapters.audit.AAIObjectAudit;
import org.onap.so.adapters.audit.AAIObjectAuditList;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import com.fasterxml.jackson.core.JsonProcessingException;

public class CreateInventoryTaskTest {

    @Mock
    ExternalTask externalTask;

    @Mock
    CreateAAIInventory createAAIInventory;

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

    @Test
    public void testExecuteExternalTask_InventoryException() throws InventoryException, JsonProcessingException {
        AAIObjectAuditList object = new AAIObjectAuditList();
        AAIObjectAudit e = new AAIObjectAudit();
        e.setDoesObjectExist(true);
        object.getAuditList().add(e);
        GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
        doReturn(objectMapper.getMapper().writeValueAsString(e)).when(externalTask).getVariable("auditInventoryResult");
        Mockito.doThrow(InventoryException.class).when(createAAIInventory).createInventory(Mockito.any());
        inventoryTask.executeExternalTask(externalTask, externalTaskService);
        Mockito.verify(externalTaskService, times(1)).handleBpmnError(externalTask, "AAIInventoryFailure");
    }
}
