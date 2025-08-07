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

package org.onap.so.bpmn.common;

import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetVolumeGroupById;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;

/**
 * Unit test cases for ConfirmVolumeGroupName.bpmn
 */

public class ConfirmVolumeGroupNameIT extends BaseIntegrationTest {
    /**
     * Sunny day scenario.
     * 
     * @throws Exception
     */
    @Test
    public void sunnyDay() throws Exception {
        logStart();
        MockGetVolumeGroupById(wireMockServer, "MDTWNJ21", "VOLUME_GROUP_ID_1", "aai-volume-group-id-info.xml");

        Map<String, Object> variables = new HashMap<>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("ConfirmVolumeGroupName_volumeGroupId", "VOLUME_GROUP_ID_1");
        variables.put("ConfirmVolumeGroupName_volumeGroupName", "VOLUME_GROUP_ID_1_NAME");
        variables.put("ConfirmVolumeGroupName_aicCloudRegion", "MDTWNJ21");
        variables.put("mso-request-id", UUID.randomUUID().toString());
        String processId = invokeSubProcess("ConfirmVolumeGroupName", variables);
        String responseCode = BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupName",
                "CVGN_queryVolumeGroupResponseCode", processId);

        assertEquals("200", responseCode);

        logEnd();
    }

    /**
     * Rainy day scenario - nonexisting volume group id.
     * 
     * @throws Exception
     */
    @Test
    public void rainyDayNoVolumeGroupId() throws Exception {
        logStart();

        // does not exist would return a 404 from AAI
        MockGetVolumeGroupById(wireMockServer, "MDTWNJ21", "VOLUME_GROUP_ID_THAT_DOES_NOT_EXIST",
                "aai-volume-group-id-info.xml", 404);

        Map<String, Object> variables = new HashMap<>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("ConfirmVolumeGroupName_aicCloudRegion", "MDTWNJ21");
        variables.put("ConfirmVolumeGroupName_volumeGroupId", "VOLUME_GROUP_ID_THAT_DOES_NOT_EXIST");
        variables.put("ConfirmVolumeGroupName_volumeGroupName", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
        variables.put("mso-request-id", UUID.randomUUID().toString());
        String processId = invokeSubProcess("ConfirmVolumeGroupName", variables);
        String responseCode = BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupName",
                "CVGN_queryVolumeGroupResponseCode", processId);

        assertEquals("404", responseCode);

        logEnd();
    }

    /**
     * Rainy day scenario - volume group name does not match the name in AAI
     *
     * 
     * @throws Exception
     */
    @Test
    public void rainyDayNameDoesNotMatch() throws Exception {
        logStart();

        MockGetVolumeGroupById(wireMockServer, "MDTWNJ21", "VOLUME_GROUP_ID_1", "aai-volume-group-id-info.xml", 200);

        Map<String, Object> variables = new HashMap<>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("ConfirmVolumeGroupName_volumeGroupId", "VOLUME_GROUP_ID_1");
        variables.put("ConfirmVolumeGroupName_volumeGroupName", "BAD_VOLUME_GROUP_NAME");
        variables.put("ConfirmVolumeGroupName_aicCloudRegion", "MDTWNJ21");
        variables.put("mso-request-id", UUID.randomUUID().toString());
        String processId = invokeSubProcess("ConfirmVolumeGroupName", variables);
        String volumeGroupNameMatches =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupName", "CVGN_volumeGroupNameMatches", processId);

        assertEquals("false", volumeGroupNameMatches);

        logEnd();
    }
}
