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

package org.openecomp.mso.cloud.servertype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudIdentity.IdentityServerType;
import org.openecomp.mso.cloud.IdentityServerTypeAbstract;
import org.openecomp.mso.openstack.exceptions.MsoException;

public class ServerTypeTest {

    @Test
    @Ignore // IGNORED FOR 1710 MERGE TO ONAP
    public void testKeystoneServerType() {
        IdentityServerTypeAbstract keystoneServerType = IdentityServerType.valueOf("KEYSTONE");
        assertNotNull(keystoneServerType);
    }

    @Test
    public void testNewServerType() {
        IdentityServerTypeAbstract customServerType = null;
        try {
            customServerType = new IdentityServerType("NewServerType", NewServerTypeUtils.class);

        } catch (IllegalArgumentException e) {
            fail("An exception should not be raised when we register a new server type for the first time");
        } finally {
            System.out.println(IdentityServerType.values().toString());
            assertEquals(customServerType, IdentityServerType.valueOf("NewServerType"));
        }

        // Create it a second time
        IdentityServerTypeAbstract customServerType2 = null;
        try {
            customServerType2 = new IdentityServerType("NewServerType", NewServerTypeUtils.class);
            fail("An exception should be raised as server type does not exist");
        } catch (IllegalArgumentException e) {
            // Fail silently -- it simply indicates we already registered it
            customServerType2 = IdentityServerType.valueOf("NewServerType");
        } finally {
            System.out.println(IdentityServerType.values().toString());
            assertEquals(customServerType2, IdentityServerType.valueOf("NewServerType"));
        }

        // Check the KeystoneURL for this custom TenantUtils
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setIdentityUrl("LocalIdentity");
        cloudIdentity.setIdentityAuthenticationType(CloudIdentity.IdentityAuthenticationType.RACKSPACE_APIKEY);
        cloudIdentity.setIdentityServerType((CloudIdentity.IdentityServerType) CloudIdentity.IdentityServerType.
                valueOf("NewServerType"));
        String regionId = "RegionA";
        String msoPropID = "12345";
        try {
            assertEquals(cloudIdentity.getKeystoneUrl(regionId, msoPropID), msoPropID + ":" + regionId +
                    ":NewServerTypeKeystoneURL/" + cloudIdentity.getIdentityUrl());
        } catch (MsoException e) {
            fail("No MSO Exception should have occured here");
        }
    }
}
