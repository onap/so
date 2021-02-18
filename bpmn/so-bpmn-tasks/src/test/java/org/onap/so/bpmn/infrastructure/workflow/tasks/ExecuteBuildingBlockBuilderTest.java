/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ExecuteBuildingBlockBuilderTest {

    final private ExecuteBuildingBlockBuilder executeBBBuilder = new ExecuteBuildingBlockBuilder();

    @Test
    public void sortVfModulesByBaseFirstTest() {
        List<Resource> resources = new ArrayList<>();
        Resource resource1 = new Resource(WorkflowType.VFMODULE, "111", false);
        resource1.setBaseVfModule(false);
        resources.add(resource1);
        Resource resource2 = new Resource(WorkflowType.VFMODULE, "222", false);
        resource2.setBaseVfModule(false);
        resources.add(resource2);
        Resource resource3 = new Resource(WorkflowType.VFMODULE, "333", false);
        resource3.setBaseVfModule(true);
        resources.add(resource3);

        List<Resource> result = executeBBBuilder.sortVfModulesByBaseFirst(resources);
        assertEquals("333", result.get(0).getResourceId());
        assertEquals("222", result.get(1).getResourceId());
        assertEquals("111", result.get(2).getResourceId());
    }

    @Test
    public void sortVfModulesByBaseLastTest() {
        List<Resource> resources = new ArrayList<>();
        Resource resource1 = new Resource(WorkflowType.VFMODULE, "111", false);
        resource1.setBaseVfModule(true);
        resources.add(resource1);
        Resource resource2 = new Resource(WorkflowType.VFMODULE, "222", false);
        resource2.setBaseVfModule(false);
        resources.add(resource2);
        Resource resource3 = new Resource(WorkflowType.VFMODULE, "333", false);
        resource3.setBaseVfModule(false);
        resources.add(resource3);
        List<Resource> result = executeBBBuilder.sortVfModulesByBaseLast(resources);
        assertEquals("333", result.get(0).getResourceId());
        assertEquals("222", result.get(1).getResourceId());
        assertEquals("111", result.get(2).getResourceId());
    }

    @Test
    public void verifyLackOfNullPointerExceptionForNullResource() {
        ExecuteBuildingBlock result = null;
        try {
            result = executeBBBuilder.buildExecuteBuildingBlock(new OrchestrationFlow(), null, null, null, null, null,
                    false, null, null, null, false, null, null, true, null);
        } catch (NullPointerException e) {
            fail("NullPointerException should not be thrown when 'resource' is null");
        }
        assertNotNull(result);
    }

    @Test
    public void getConfigurationResourceKeysTest() {
        String vnfcName = "vnfc";
        String vfModuleCustomizationId = "1a2b3c4e5d";
        String cvnfModuleCustomizationId = "2b1a3c";
        String vnfCustomizationId = "zz12aa";

        Resource resource = new Resource(WorkflowType.SERVICE, "123", true);

        resource.setCvnfModuleCustomizationId(vfModuleCustomizationId);
        resource.setCvnfModuleCustomizationId(cvnfModuleCustomizationId);
        resource.setVnfCustomizationId(vnfCustomizationId);

        ConfigurationResourceKeys confResourceKeys = executeBBBuilder.getConfigurationResourceKeys(resource, vnfcName);

        assertNotNull(confResourceKeys);
        assertEquals(vnfcName, confResourceKeys.getVnfcName());
        assertEquals(cvnfModuleCustomizationId, confResourceKeys.getCvnfcCustomizationUUID());
        assertEquals(vnfCustomizationId, confResourceKeys.getVnfResourceCustomizationUUID());

    }
}
