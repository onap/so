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
        assertEquals(test.getVnfId(),"id");
        test.setVfModuleId("vfID");
        assertEquals(test.getVfModuleId(),"vfID");
        test.setVnfName("name");
        assertEquals(test.getVnfName(),"name");
        test.setVfModuleName("vfName");
        assertEquals(test.getVfModuleName(),"vfName");
        test.setAicNodeClli("clli");
        assertEquals(test.getAicNodeClli(),"clli");
        test.setTenantId("tenIDl");
        assertEquals(test.getTenantId(),"tenIDl");
        test.setVolumeGroupName("grpName");
        assertEquals(test.getVolumeGroupName(),"grpName");
        test.setVolumeGroupId("grpId");
        assertEquals(test.getVolumeGroupId(),"grpId");
    }

}
