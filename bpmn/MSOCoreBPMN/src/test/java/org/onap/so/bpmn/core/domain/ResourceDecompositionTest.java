/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.core.domain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ResourceDecompositionTest {

    private ResourceDecomposition rd = new ResourceDecomposition() {
        private static final long serialVersionUID = 1L;
    };
    ModelInfo model = new ModelInfo();
    ResourceInstance ri = new ResourceInstance();



    @Test
    public void testResourceDecomposition() {
        rd.setModelInfo(model);
        rd.setInstanceData(ri);
        rd.setResourceType("resourceType");
        rd.setResourceInstanceId("newInstanceId");
        rd.setResourceInstanceName("newInstanceName");
        assertEquals(rd.getResourceModel(), model);
        assertEquals(rd.getModelInfo(), model);
        assertEquals(rd.getInstanceData(), ri);
        assertEquals(rd.getResourceInstanceId(), "newInstanceId");
        assertEquals(rd.getResourceInstanceName(), "newInstanceName");
    }

}
