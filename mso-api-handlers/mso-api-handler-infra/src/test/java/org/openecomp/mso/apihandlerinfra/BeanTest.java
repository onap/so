/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfInputs;

public class BeanTest {

    @Test
    public void testVnfInputs() {
        VnfInputs bean = new VnfInputs();
        bean.setAicCloudRegion("south");
        bean.setAicNodeClli("38982");
        bean.setAsdcServiceModelVersion("v2");
        bean.setBackoutOnFailure(false);
        bean.setIsBaseVfModule(true);
        bean.setPersonaModelId("2017");
        bean.setPersonaModelVersion("v3");
        bean.setProvStatus("active");
        bean.setServiceId("123456");
        bean.setServiceInstanceId("aaa1234");
        bean.setServiceType("vnf");
        bean.setTenantId("89903042");
        bean.setVfModuleId("4993022");
        bean.setVfModuleModelName("m1");
        bean.setVnfId("34");
        bean.setVnfName("test");
        bean.setVnfPersonaModelId("1002");
        bean.setVnfPersonaModelVersion("v3");
        bean.setVnfType("fddf");
        bean.setVolumeGroupId("7889");
        bean.setVolumeGroupName("test");

        String str = bean.toString();
        assertTrue(str != null);

    }
}
