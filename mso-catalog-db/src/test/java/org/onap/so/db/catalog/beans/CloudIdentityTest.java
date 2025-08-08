/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.catalog.beans;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.onap.so.utils.CryptoUtils;

public class CloudIdentityTest {

    private CloudIdentity cloudIdentity = new CloudIdentity();
    private static final String ID = "testId";
    private static final String IDENTITY_URL = "testIdentityUrl";
    private static final String MSO_ID = "testMsoId";
    private static final String MSO_PASS = "testMsoPassword";
    private static final String ADMIN_TENANT = "testAdminTenant";
    private static final String MEMBER_ROLE = "testMemberRole";
    private static final Boolean TENANT_METADATA = true;

    @Test
    public final void testCloudIdentity() {
        CloudIdentity id = new CloudIdentity();
        id.setAdminTenant("AdminTenant");
        id.setId("id");
        // id.setKeystoneUrl ("keystone");
        id.setIdentityUrl("keystone");
        id.setMemberRole("member");
        id.setMsoId("msoId");
        id.setMsoPass(CryptoUtils.encryptCloudConfigPassword("password"));
        id.setTenantMetadata(true);
        id.setIdentityServerType(null);
        id.setIdentityAuthenticationType(null);


        assertTrue(id.getAdminTenant().equals("AdminTenant"));
        assertTrue(id.getId().equals("id"));
        // assertTrue (id.getKeystoneUrl ().equals ("keystone"));
        assertTrue(id.getMemberRole().equals("member"));
        assertTrue(id.getMsoId().equals("msoId"));
        assertTrue(CryptoUtils.decryptCloudConfigPassword(id.getMsoPass()).equals("password"));
        assertTrue(id.getTenantMetadata());
        // assertTrue (id.toString ().contains ("keystone"));
        assertTrue(id.toString().contains("null"));
    }

    @Test
    public final void testEncryption() {
        String encrypted = CryptoUtils.encryptCloudConfigPassword("password");
        assertTrue(encrypted != null);
        assertTrue(!encrypted.equals("password"));
    }

    @Test
    public void cloneTest() {
        cloudIdentity = setupCloudIdentity(cloudIdentity, ID, IDENTITY_URL, MSO_ID, MSO_PASS, ADMIN_TENANT, MEMBER_ROLE,
                TENANT_METADATA, ServerType.ORM, AuthenticationType.USERNAME_PASSWORD);
        CloudIdentity cloudIdentity2 = cloudIdentity.clone();

        assertEquals(cloudIdentity.getClass(), cloudIdentity2.getClass());
    }

    private CloudIdentity setupCloudIdentity(CloudIdentity obj, String id, String identityUrl, String msoId,
            String msoPass, String adminTenant, String memberRole, Boolean tenantMetadata,
            ServerType identityServerType, AuthenticationType identityAuthenticationType) {
        obj.setId(id);
        obj.setIdentityUrl(identityUrl);
        obj.setMsoId(msoId);
        obj.setMsoPass(msoPass);
        obj.setAdminTenant(adminTenant);
        obj.setMemberRole(memberRole);
        obj.setTenantMetadata(tenantMetadata);
        obj.setIdentityServerType(identityServerType);
        obj.setIdentityAuthenticationType(identityAuthenticationType);

        return obj;
    }
}
