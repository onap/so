/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.openstack.beans;

import org.junit.Test;

import java.util.HashMap;

public class MsoTenantTest {

    MsoTenant msoTenant = new MsoTenant();

    @Test
    public void getTenantId() throws Exception {
        msoTenant.getTenantId();
    }

    @Test
    public void setTenantId() throws Exception {
        msoTenant.setTenantId("id-123");
    }

    @Test
    public void getTenantName() throws Exception {
        msoTenant.getTenantName();
    }

    @Test
    public void setTenantName() throws Exception {
        msoTenant.setTenantName("test");
    }

    @Test
    public void getMetadata() throws Exception {
        msoTenant.getMetadata();
    }

    @Test
    public void setMetadata() throws Exception {
        msoTenant.setMetadata(new HashMap<>());
    }

}