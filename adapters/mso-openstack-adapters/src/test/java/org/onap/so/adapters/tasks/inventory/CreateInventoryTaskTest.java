/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.tasks.inventory;

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
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
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

    @Mock
    private AuditMDCSetup mdcSetup;

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
    public void testExecuteExternalTask_InventoryException() throws JsonProcessingException {
        AAIObjectAuditList object = new AAIObjectAuditList();
        AAIObjectAudit e = new AAIObjectAudit();
        e.setDoesObjectExist(true);
        object.getAuditList().add(e);
        GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
        doReturn(objectMapper.getMapper().writeValueAsString(e)).when(externalTask).getVariable("auditInventoryResult");
        inventoryTask.executeExternalTask(externalTask, externalTaskService);
        Mockito.verify(externalTaskService, times(1)).handleBpmnError(externalTask, "AAIInventoryFailure");
    }
}
