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
package org.onap.so.bpmn.common.recipe;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.so.BaseTest;

public class ResourceRecipeRequestTest extends BaseTest {

    ResourceRecipeRequest rr = new ResourceRecipeRequest();
    BpmnParam bp = new BpmnParam();

    @Test
    public void test() {
        rr.setResourceInput(bp);
        rr.setHost(bp);
        rr.setRequestId(bp);
        rr.setRequestAction(bp);
        rr.setServiceInstanceId(bp);
        rr.setServiceType(bp);
        rr.setRecipeParams(bp);
        assertEquals(rr.getResourceInput(), bp);
        assertEquals(rr.getHost(), bp);
        assertEquals(rr.getRequestId(), bp);
        assertEquals(rr.getRequestAction(), bp);
        assertEquals(rr.getServiceInstanceId(), bp);
        assertEquals(rr.getServiceType(), bp);
        assertEquals(rr.getRecipeParams(), bp);
    }

    @Test
    public void testToString() {
        assert (rr.toString() != null);
    }
}
