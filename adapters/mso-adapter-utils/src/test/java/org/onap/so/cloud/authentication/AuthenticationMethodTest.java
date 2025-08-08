/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 Samsung Electronics.
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

package org.onap.so.cloud.authentication;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.so.cloud.authentication.models.RackspaceAuthentication;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.utils.CryptoUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;

/**
 * A few JUnit tests to evaluate the new factory that manages authentication types and their associated wrapper classes.
 * Here it is assumed that core types only are tested.
 *
 */
public class AuthenticationMethodTest {

    private AuthenticationMethodFactory authenticationMethodFactory = new AuthenticationMethodFactory();

    /**
     * 
     */
    public AuthenticationMethodTest() {
        // TODO Auto-generated constructor stub
    }

    @Test
    public void testCustomRackspaceAuth() {
        CloudIdentity ci = new CloudIdentity();
        ci.setIdentityAuthenticationType(AuthenticationType.RACKSPACE_APIKEY);
        ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
        ci.setMsoId("test");

        Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
        assertEquals(RackspaceAuthentication.class, auth.getClass());

    }

    @Test
    public void testCoreUsernamePasswordAuth() {
        CloudIdentity ci = new CloudIdentity();
        ci.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);
        ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
        ci.setMsoId("someuser");

        Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
        assertEquals(UsernamePassword.class, auth.getClass());

    }

    @Test
    public void testCustomRackspaceAuthFromCloudIdentity() {
        CloudIdentity ci = new CloudIdentity();
        ci.setIdentityAuthenticationType(AuthenticationType.RACKSPACE_APIKEY);
        ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
        ci.setMsoId("test");

        Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
        assertEquals(RackspaceAuthentication.class, auth.getClass());
    }

    @Test
    public void testCoreUsernamePasswordAuthFromCloudIdentity() {
        CloudIdentity ci = new CloudIdentity();
        ci.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);
        ci.setMsoPass("FD205490A48D48475607C36B9AD902BF");
        ci.setMsoId("someuser");

        Authentication auth = authenticationMethodFactory.getAuthenticationFor(ci);
        assertEquals(UsernamePassword.class, auth.getClass());

    }

    @Test
    public void getAuthenticationForV3Test() throws JsonMappingException, IOException {

        CloudIdentity identity = new CloudIdentity();
        identity.setMsoId("my-username");
        identity.setMsoPass(CryptoUtils.encryptCloudConfigPassword("my-password"));
        identity.setProjectDomainName("test-domain");
        identity.setUserDomainName("user-domain");
        ObjectMapper mapper = new ObjectMapper();
        com.woorea.openstack.keystone.v3.model.Authentication expected = mapper.readValue(
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/KeystoneV3Payload.json"))),
                com.woorea.openstack.keystone.v3.model.Authentication.class);
        com.woorea.openstack.keystone.v3.model.Authentication actual =
                authenticationMethodFactory.getAuthenticationForV3(identity, "project-x");

        assertThat(actual, sameBeanAs(expected));
    }
}
