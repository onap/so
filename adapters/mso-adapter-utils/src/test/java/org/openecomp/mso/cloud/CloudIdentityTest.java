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

package org.openecomp.mso.cloud;


import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class CloudIdentityTest {

    @Test
    public final void testCloudIdentity() {
        CloudIdentity id = new CloudIdentity();
        id.setAdminTenant("AdminTenant");
        id.setId("id");
//        id.setKeystoneUrl ("keystone");
        id.setIdentityUrl("keystone");
        id.setMemberRole("member");
        id.setMsoId("msoId");
        id.setMsoPass(CloudIdentity.encryptPassword("password"));
        id.setTenantMetadata(true);
        id.setIdentityServerType(null);
        id.setIdentityAuthenticationType(null);


        assertTrue(id.getAdminTenant().equals("AdminTenant"));
        assertTrue(id.getId().equals("id"));
//        assertTrue (id.getKeystoneUrl ().equals ("keystone"));
        assertTrue(id.getMemberRole().equals("member"));
        assertTrue(id.getMsoId().equals("msoId"));
        assertTrue(id.getMsoPass().equals("password"));
        assertTrue(id.hasTenantMetadata());
//        assertTrue (id.toString ().contains ("keystone"));
        assertTrue(id.toString().contains("null"));
    }

    @Test
    public final void testEncryption() {
        String encrypted = CloudIdentity.encryptPassword("password");
        assertTrue(encrypted != null);
        assertTrue(!encrypted.equals("password"));
    }

}
