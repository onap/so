/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.tasks.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.so.adapters.tasks.audit.AuditCreateStackService;
import org.onap.so.adapters.tasks.audit.AuditDataService;
import org.onap.so.adapters.tasks.audit.AuditQueryStackService;
import org.onap.so.adapters.tasks.audit.HeatStackAudit;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuditStackServiceDataTest extends AuditCreateStackService {

    @InjectMocks
    private AuditCreateStackService auditStackService = new AuditCreateStackService();

    @InjectMocks
    private AuditQueryStackService auditQueryStackService = new AuditQueryStackService();

    @Mock
    private HeatStackAudit heatStackAuditMock;

    @Mock
    private Environment mockEnv;

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService mockExternalTaskService;

    @Mock
    private AuditDataService auditDataService;

    @Mock
    private AuditMDCSetup mdcSetup;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AuditInventory auditInventory = new AuditInventory();

    Optional<AAIObjectAuditList> auditListOptSuccess;

    Optional<AAIObjectAuditList> auditListOptFailure;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        auditInventory.setCloudOwner("cloudOwner");
        auditInventory.setCloudRegion("cloudRegion");
        auditInventory.setTenantId("tenantId");
        auditInventory.setHeatStackName("stackName");
        MockitoAnnotations.initMocks(this);

        AAIObjectAuditList auditListSuccess = objectMapper
                .readValue(new File("src/test/resources/ExpectedVServerFound.json"), AAIObjectAuditList.class);
        auditListOptSuccess = Optional.of(auditListSuccess);

        AAIObjectAuditList auditListFailure = objectMapper.readValue(
                new File("src/test/resources/Vserver2_Found_VServer1_Not_Found.json"), AAIObjectAuditList.class);
        auditListOptFailure = Optional.of(auditListFailure);
        String[] retrySequence = new String[8];
        retrySequence[0] = "1";
        retrySequence[1] = "1";
        retrySequence[2] = "2";
        retrySequence[3] = "3";
        retrySequence[4] = "5";
        retrySequence[5] = "8";
        retrySequence[6] = "13";
        retrySequence[7] = "20";
        doReturn(auditInventory).when(mockExternalTask).getVariable("auditInventory");
        doReturn("6000").when(mockEnv).getProperty("mso.workflow.topics.retryMultiplier", "6000");
        doReturn(retrySequence).when(mockEnv).getProperty("mso.workflow.topics.retrySequence", String[].class);
        doReturn("aasdfasdf").when(mockExternalTask).getId();
    }

    @Test
    public void execute_external_task_audit_success_Test() {
        doReturn(auditListOptSuccess).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId",
                "stackName");
        auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<ExternalTask> taskCaptor = ArgumentCaptor.forClass(ExternalTask.class);
        Mockito.verify(mockExternalTaskService).complete(taskCaptor.capture(), captor.capture());
        Map actualMap = captor.getValue();
        assertEquals(true, actualMap.get("auditIsSuccessful"));
        assertNotNull(actualMap.get("auditInventoryResult"));
    }

    @Test
    public void executeExternalTaskQueryAuditTest() throws JsonProcessingException {
        doReturn(auditListOptSuccess).when(heatStackAuditMock).queryHeatStack("cloudOwner", "cloudRegion", "tenantId",
                "stackName");
        Mockito.doNothing().when(auditDataService).writeStackDataToRequestDb(Mockito.any(AuditInventory.class),
                Mockito.any(AAIObjectAuditList.class));
        auditQueryStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<ExternalTask> taskCaptor = ArgumentCaptor.forClass(ExternalTask.class);
        Mockito.verify(mockExternalTaskService).complete(taskCaptor.capture(), captor.capture());
        Mockito.verify(auditDataService).writeStackDataToRequestDb(Mockito.any(AuditInventory.class),
                Mockito.any(AAIObjectAuditList.class));
    }

    @Test
    public void execute_external_task_audit_first_failure_Test() {
        doReturn(auditListOptFailure).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId",
                "stackName");
        doReturn(null).when(mockExternalTask).getRetries();
        auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
        Mockito.verify(mockExternalTaskService).handleFailure(mockExternalTask,
                "Unable to find all VServers and L-Interaces in A&AI",
                "Unable to find all VServers and L-Interaces in A&AI", 8, 10000L);
    }

    @Test
    public void execute_external_task_audit_intermediate_failure_Test() {
        doReturn(auditListOptFailure).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId",
                "stackName");
        doReturn(6).when(mockExternalTask).getRetries();
        auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
        Mockito.verify(mockExternalTaskService).handleFailure(mockExternalTask,
                "Unable to find all VServers and L-Interaces in A&AI",
                "Unable to find all VServers and L-Interaces in A&AI", 5, 12000L);

    }

    @Test
    public void execute_external_task_audit_final_failure_Test() {
        doReturn(auditListOptFailure).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId",
                "stackName");
        doReturn(1).when(mockExternalTask).getRetries();
        auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<ExternalTask> taskCaptor = ArgumentCaptor.forClass(ExternalTask.class);
        Mockito.verify(mockExternalTaskService).complete(taskCaptor.capture(), captor.capture());
        Map actualMap = captor.getValue();
        assertEquals(false, actualMap.get("auditIsSuccessful"));
        assertNotNull(actualMap.get("auditInventoryResult"));
    }

    @Test
    public void determineAuditResult_Test() throws Exception {
        boolean actual = auditStackService.didCreateAuditFail(auditListOptSuccess);
        assertEquals(false, actual);
    }

    @Test
    public void determineAuditResult_Failure_Test() throws Exception {
        boolean actual = auditStackService.didCreateAuditFail(auditListOptFailure);
        assertEquals(true, actual);
    }
}
