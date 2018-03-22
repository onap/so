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
package org.openecomp.mso.adapters.tenant;

import org.junit.Test;
import org.openecomp.mso.adapters.tenantrest.TenantRollback;
import org.openecomp.mso.entity.MsoRequest;

import javax.validation.constraints.Null;
import javax.xml.ws.Holder;
import java.util.HashMap;

public class MsoTenantAdapterImplTest {

    // TODO: following test case is done for coverage
    // later it should be modified for proper test.

    MsoTenantAdapterImpl msoTenantAdapter = new MsoTenantAdapterImpl();

    @Test
    public void healthCheck() throws Exception {
        msoTenantAdapter.healthCheck();
    }

    @Test(expected = NullPointerException.class)
    public void createTenant() throws Exception {
        msoTenantAdapter.createTenant("site", "tenant", new HashMap<>(),
                true, true, new MsoRequest(), new Holder(), new Holder());
    }

    @Test(expected = NullPointerException.class)
    public void queryTenant() throws Exception {
        msoTenantAdapter.queryTenant("site", "tenant", new MsoRequest(),
                new Holder<>(), new Holder<>(), new Holder<>());
    }

    @Test(expected = NullPointerException.class)
    public void deleteTenant() throws Exception {
        msoTenantAdapter.deleteTenant("cloud", "tenant", new MsoRequest(), new Holder());
    }

    @Test
    public void rollbackTenant() throws Exception {
        msoTenantAdapter.rollbackTenant(new TenantRollback());
    }

}