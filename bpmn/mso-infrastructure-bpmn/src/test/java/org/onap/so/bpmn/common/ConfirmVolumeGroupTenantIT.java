/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
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
 * Please describe the ConfirmVolumeGroupTenantTest.java class
 *
 */

public class ConfirmVolumeGroupTenantIT extends BaseIntegrationTest {

    @Test
    public void testRemoveLayer3Service_success() {
        MockGetVolumeGroupById(wireMockServer, "MDTWNJ21", "a8399879-31b3-4973-be26-0a0cbe776b58",
                "CRTGVNF_queryAAIResponseVolume.xml");

        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        String processId = invokeSubProcess("ConfirmVolumeGroupTenant", variables);

        String actualNameMatch =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupTenant", "groupNamesMatch", processId);
        String actualIdMatch =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupTenant", "tenantIdsMatch", processId);
        String actualResponse =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupTenant", "volumeHeatStackId", processId);

        assertEquals("Response", "true", actualNameMatch);
        assertEquals("Response", "true", actualIdMatch);
        assertEquals("Response", "MoG_CinderVolumes_2/19387dc6-060f-446e-b41f-dcfd29c73845", actualResponse);
    }

    @Test
    public void testRemoveLayer3Service_idsNotMatch() {
        MockGetVolumeGroupById(wireMockServer, "MDTWNJ21", "a8399879-31b3-4973-be26-0a0cbe776b58",
                "CRTGVNF_queryAAIResponseVolume_idsNotMatch.xml");

        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        String processId = invokeSubProcess("ConfirmVolumeGroupTenant", variables);


        String actualNameMatch =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupTenant", "groupNamesMatch", processId);
        String actualIdMatch =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupTenant", "tenantIdsMatch", processId);
        String actualResponse =
                BPMNUtil.getVariable(processEngine, "ConfirmVolumeGroupTenant", "WorkflowException", processId);

        assertEquals("Response", "true", actualNameMatch);
        assertEquals("Response", "false", actualIdMatch);
        assertEquals("Response",
                "WorkflowException[processKey=ConfirmVolumeGroupTenant,errorCode=1,errorMessage=Volume Group a8399879-31b3-4973-be26-0a0cbe776b58 does not belong to your tenant,workStep=*]",
                actualResponse);

    }

    private void setVariables(Map<String, Object> variables) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("volumeGroupId", "a8399879-31b3-4973-be26-0a0cbe776b58");
        variables.put("tenantId", "7dd5365547234ee8937416c65507d266");
        variables.put("aicCloudRegion", "MDTWNJ21");
        variables.put("mso-request-id", UUID.randomUUID().toString());
    }
}
