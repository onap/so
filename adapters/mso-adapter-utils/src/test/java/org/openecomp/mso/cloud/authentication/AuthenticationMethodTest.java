/*
 * ============LICENSE_START==========================================
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 *
 */

package org.openecomp.mso.cloud.authentication;

import static org.junit.Assert.assertTrue;

import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import org.junit.Test;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.authentication.models.RackspaceAuthentication;

/**
 * A few JUnit tests to evaluate the new factory that manages authentication
 * types and their associated wrapper classes. Here it is assumed that core types
 * only are tested.
 */
public class AuthenticationMethodTest {

    /**
     *
     */
    public AuthenticationMethodTest() {
        // TODO Auto-generated constructor stub
    }

    @Test
    public void testCustomRackspaceAuthFromCloudIdentity() {
        CloudIdentity ci = new CloudIdentity();
        ci.setIdentityAuthenticationType(CloudIdentity.IdentityAuthenticationType.RACKSPACE_APIKEY);
        ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
        ci.setMsoId("test");
        Authentication auth = ci.getAuthentication();
        assertTrue(RackspaceAuthentication.class.equals(auth.getClass()));
    }

    @Test
    public void testCoreUsernamePasswordAuthFromCloudIdentity() {
        CloudIdentity ci = new CloudIdentity();
        ci.setIdentityAuthenticationType(CloudIdentity.IdentityAuthenticationType.USERNAME_PASSWORD);
        ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
        ci.setMsoId("someuser");
        Authentication auth = ci.getAuthentication();
        assertTrue(UsernamePassword.class.equals(auth.getClass()));
    }
}
