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

package org.onap.so.adapters.tasks.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.so.adapters.tasks.inventory.CreateAAIInventory;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.objects.audit.AAIObjectAuditList;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateAAIInventoryTest extends CreateAAIInventory {

    @InjectMocks
    private CreateAAIInventory createAAIInventory = new CreateAAIInventory();

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private AAIResourcesClient mockClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AuditInventory auditInventory = new AuditInventory();

    AAIObjectAuditList auditListSuccess;

    AAIObjectAuditList auditListFailure;

    AAIObjectAuditList missingSubInterfaces;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        auditInventory.setCloudOwner("cloudOwner");
        auditInventory.setCloudRegion("cloudRegion");
        auditInventory.setTenantId("tenantId");
        auditInventory.setHeatStackName("stackName");
        MockitoAnnotations.initMocks(this);
        auditListSuccess = objectMapper.readValue(new File("src/test/resources/ExpectedVServerFound.json"),
                AAIObjectAuditList.class);
        auditListFailure = objectMapper.readValue(new File("src/test/resources/Vserver2_Found_VServer1_Not_Found.json"),
                AAIObjectAuditList.class);
        missingSubInterfaces = objectMapper.readValue(new File("src/test/resources/AuditResultsMissSub.json"),
                AAIObjectAuditList.class);
        doReturn(auditInventory).when(mockExternalTask).getVariable("auditInventory");
    }

    @Test
    public void determineAuditResult_Test() throws Exception {
        boolean actual = createAAIInventory.didAuditFailVserverLInterfaces(auditListSuccess);
        assertEquals(false, actual);
    }

    @Test
    public void determineAuditResult_Failure_Test() throws Exception {
        boolean actual = createAAIInventory.didAuditFailVserverLInterfaces(auditListFailure);
        assertEquals(true, actual);
    }

    @Test
    public void missing_Sub_Interfaces_Test() throws Exception {
        AAIResourceUri aaiURI2 = AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, "cloudOwner",
                "regionOne", "0422ffb57ba042c0800a29dc85ca70f8", "92272b67-d23f-42ca-87fa-7b06a9ec81f3",
                "tsbc0005v_tsbc0005vm002_svc1_port_0", "tsbc0005v_tsbc0005vm002_subint_untrusted_svc1_81");
        AAIResourceUri aaiURI1 = AAIUriFactory.createResourceUri(AAIObjectType.SUB_L_INTERFACE, "cloudOwner",
                "regionOne", "0422ffb57ba042c0800a29dc85ca70f8", "92272b67-d23f-42ca-87fa-7b06a9ec81f3",
                "tsbc0005v_tsbc0005vm002_svc2_port_0", "tsbc0005v_tsbc0005vm002_subint_untrusted_svc2_103");
        ArgumentCaptor<Optional> captor = ArgumentCaptor.forClass(Optional.class);
        ArgumentCaptor<AAIResourceUri> uriCaptor = ArgumentCaptor.forClass(AAIResourceUri.class);

        createAAIInventory.setAaiClient(mockClient);
        createAAIInventory.createInventory(missingSubInterfaces);
        Mockito.verify(mockClient, times(2)).createIfNotExists(uriCaptor.capture(), captor.capture());

        List<AAIResourceUri> capturedURI = uriCaptor.getAllValues();
        assertTrue(capturedURI.stream().anyMatch(item -> aaiURI1.build().toString().equals(item.build().toString())));
        assertTrue(capturedURI.stream().anyMatch(item -> aaiURI2.build().toString().equals(item.build().toString())));



    }
}
