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

package org.onap.so.openstack.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.StubOpenStack;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;

public class MsoKeystoneUtilsTest extends BaseTest {

    @Autowired
    private MsoKeystoneUtils msoKeystoneUtils;

    @Before
    public void before() throws IOException {
        StubOpenStack.mockOpenStackResponseAccess(wireMockPort);
    }

    @Test
    public void createTenantTest() throws Exception {
        StubOpenStack.mockOpenStackPostTenantWithBodyFile_200();

        StubOpenStack.mockOpenStackGetUserById("john");
        StubOpenStack.mockOpenStackGetRoles_200("OS-KSADM");
        String response = msoKeystoneUtils.createTenant("tenant", "regionOne", new HashMap<>(), true);

        Assert.assertEquals("tenantId", response);
    }

    @Test
    public void createTenantTest_FindUserByName() throws Exception {
        StubOpenStack.mockOpenStackPostTenantWithBodyFile_200();

        StubOpenStack.mockOpenStackGetUserByName("john");
        StubOpenStack.mockOpenStackGetRoles_200("OS-KSADM");
        String response = msoKeystoneUtils.createTenant("tenant", "regionOne", new HashMap<>(), true);
        Assert.assertEquals("tenantId", response);

    }

    @Test
    public void createTenantTest_Exception() throws Exception {
        expectedException.expect(MsoException.class);
        StubOpenStack.mockOpenStackPostTenantWithBodyFile_200();
        StubOpenStack.mockOpenStackGetUserByName_500("john");
        StubOpenStack.mockOpenStackGetRoles_200("OS-KSADM");
        msoKeystoneUtils.createTenant("tenant", "regionOne", new HashMap<>(), true);
    }

    @Test
    public void queryTenantTest() throws Exception {
        StubOpenStack.mockOpenStackGetTenantById("tenantId");

        MsoTenant msoTenant = msoKeystoneUtils.queryTenant("tenantId", "regionOne");

        Assert.assertEquals("testingTenantName", msoTenant.getTenantName());
    }

    @Test
    public void queryTenantByNameTest() throws Exception {
        StubOpenStack.mockOpenStackGetTenantByName("tenant");

        MsoTenant msoTenant = msoKeystoneUtils.queryTenantByName("tenant", "regionOne");

        Assert.assertEquals("testingTenantName", msoTenant.getTenantName());
    }

    @Test
    public void deleteTenantTest() throws Exception {
        StubOpenStack.mockOpenStackGetTenantById("tenantId");
        StubOpenStack.mockOpenStackDeleteTenantById_200("tenantId");
        boolean result = msoKeystoneUtils.deleteTenant("tenantId", "regionOne");

        Assert.assertTrue(result);
    }

    @Test
    public void deleteTenantByNameTest() throws Exception {
        StubOpenStack.mockOpenStackGetTenantByName("tenant");
        StubOpenStack.mockOpenStackDeleteTenantById_200("tenantId");
        boolean result = msoKeystoneUtils.deleteTenantByName("tenant", "regionOne");

        Assert.assertTrue(result);
    }
}
