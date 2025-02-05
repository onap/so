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

import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
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

}
