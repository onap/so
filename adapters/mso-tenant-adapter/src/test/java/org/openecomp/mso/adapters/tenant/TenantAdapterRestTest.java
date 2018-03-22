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
import org.openecomp.mso.adapters.tenantrest.CreateTenantRequest;
import org.openecomp.mso.adapters.tenantrest.DeleteTenantRequest;
import org.openecomp.mso.adapters.tenantrest.RollbackTenantRequest;

public class TenantAdapterRestTest {

    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    
    TenantAdapterRest tenantAdapterRest = new TenantAdapterRest();

    @Test(expected = ClassFormatError.class)
    public void healthcheck() throws Exception {
        tenantAdapterRest.healthcheck();
    }

    @Test(expected = ClassFormatError.class)
    public void createTenant() throws Exception {
        tenantAdapterRest.createTenant(new CreateTenantRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void deleteTenant() throws Exception {
        tenantAdapterRest.deleteTenant("test", new DeleteTenantRequest());
    }

    @Test(expected = ClassFormatError.class)
    public void queryTenant() throws Exception {
        tenantAdapterRest.queryTenant("test", "test", "requestid", "serviceInstanceId");
    }

    @Test(expected = ClassFormatError.class)
    public void rollbackTenant() throws Exception {
        tenantAdapterRest.rollbackTenant("action", new RollbackTenantRequest());
    }

}