/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.audit;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AuditTasksTest extends BaseTaskTest {

    @InjectMocks
    private AuditTasks auditTasks = new AuditTasks();
    private ServiceInstance serviceInstance;
    private GenericVnf genericVnf;
    private VfModule vfModule;


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void before() throws BBObjectNotFoundException, JsonProcessingException {
        serviceInstance = setServiceInstance();
        genericVnf = setGenericVnf();
        vfModule = setVfModule();
        buildRequestContext();
        setCloudRegion();
        setRequestContext();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
        execution.setVariable("auditQuerySuccess", true);
        AAIObjectAuditList auditList = new AAIObjectAuditList();
        auditList.setHeatStackName("testHeatStackName");
        AAIObjectAudit audit = new AAIObjectAudit();
        Vserver vserver = new Vserver();
        vserver.setVserverId("testVserverId");
        audit.setAaiObject(vserver);
        auditList.getAuditList().add(audit);
        GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
        String auditListString = objectMapper.getMapper().writeValueAsString(audit);
        execution.setVariable("auditList", auditListString);
    }

    @Test
    public void setupAuditVariableTest() throws Exception {
        AuditInventory expectedAuditInventory = new AuditInventory();
        expectedAuditInventory.setCloudOwner("testCloudOwner");
        expectedAuditInventory.setCloudRegion("testLcpCloudRegionId");
        expectedAuditInventory.setHeatStackName("testVfModuleName1");
        expectedAuditInventory.setVfModuleId("testVfModuleId1");
        expectedAuditInventory.setTenantId("testTenantId");
        expectedAuditInventory.setGenericVnfId("testVnfId1");
        expectedAuditInventory.setMsoRequestId("fb06f44c-c797-4f38-9b17-b4b975344600");
        auditTasks.setupAuditVariable(execution);
        assertThat((AuditInventory) execution.getVariable("auditInventory"), sameBeanAs(expectedAuditInventory));
    }

}
