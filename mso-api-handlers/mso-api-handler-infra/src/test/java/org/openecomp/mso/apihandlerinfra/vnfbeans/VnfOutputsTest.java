/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.vnfbeans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VnfOutputsTest {

    VnfOutputs test = new VnfOutputs();

    @Test
    public void verifyVnfOutputs() throws Exception {

        test.setVnfId("id");
        test.setVfModuleId("vfID");
        test.setVnfName("name");
        test.setVfModuleName("vfName");
        test.setAicNodeClli("clli");
        test.setTenantId("tenIDl");
        test.setVolumeGroupName("grpName");
        test.setVolumeGroupId("grpId");

        assertEquals(test.getVnfId(),"id");
        assertEquals(test.getVfModuleId(),"vfID");
        assertEquals(test.getVnfName(),"name");
        assertEquals(test.getVfModuleName(),"vfName");
        assertEquals(test.getAicNodeClli(),"clli");
        assertEquals(test.getTenantId(),"tenIDl");
        assertEquals(test.getVolumeGroupName(),"grpName");
        assertEquals(test.getVolumeGroupId(),"grpId");
    }

}
