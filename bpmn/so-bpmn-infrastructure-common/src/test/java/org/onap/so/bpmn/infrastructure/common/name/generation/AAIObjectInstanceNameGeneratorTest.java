/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.common.name.generation;


import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;


public class AAIObjectInstanceNameGeneratorTest {

    @Before
    public void before() {}

    @Test
    public void generateInstanceGroupNameTest() throws Exception {

        ModelInfoInstanceGroup modelVnfc = new ModelInfoInstanceGroup();
        modelVnfc.setFunction("vre");
        modelVnfc.setType("VNFC");

        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setId("test-001");
        instanceGroup.setModelInfoInstanceGroup(modelVnfc);
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnf-123");
        vnf.setVnfName("test-vnf");

        assertEquals("test-vnf_vre",
                new AAIObjectInstanceNameGenerator().generateInstanceGroupName(instanceGroup, vnf));
    }

}
