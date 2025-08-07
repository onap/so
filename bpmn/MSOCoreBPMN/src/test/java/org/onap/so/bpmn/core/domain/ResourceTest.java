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

public class ResourceTest {
    private Resource resource = new Resource() {
        private static final long serialVersionUID = 1L;


    };
    ModelInfo model = new ModelInfo();
    ResourceInstance ri = new ResourceInstance();
    HomingSolution hs = new HomingSolution();
    ResourceType rt = ResourceType.VNF;
    public long concurrencyCounter = 1L;
    long initval = resource.getConcurrencyCounter();

    @Test
    public void testResource() {
        resource.setResourceId("resourceId");
        resource.setModelInfo(model);
        resource.setResourceInstance(ri);
        resource.setHomingSolution(hs);
        resource.setCurrentHomingSolution(hs);
        resource.setResourceType(rt);
        resource.setToscaNodeType("toscaNodeType");
        resource.setResourceInstanceId("newInstanceId");
        resource.setResourceInstanceName("newInstanceName");
        resource.incrementConcurrencyCounter();
        assertEquals(resource.getResourceId(), "resourceId");
        assertEquals(resource.getModelInfo(), model);
        assertEquals(resource.getResourceInstance(), ri);
        assertEquals(resource.getHomingSolution(), hs);
        assertEquals(resource.getCurrentHomingSolution(), hs);
        assertEquals(resource.getResourceType(), rt);
        assertEquals(resource.getToscaNodeType(), "toscaNodeType");
        assertEquals(resource.getResourceInstanceId(), "newInstanceId");
        assertEquals(resource.getResourceInstanceName(), "newInstanceName");
        assertEquals(resource.getConcurrencyCounter(), initval + 1);



    }

}
