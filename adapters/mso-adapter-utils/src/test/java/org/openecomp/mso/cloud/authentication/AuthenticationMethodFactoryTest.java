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

import static org.assertj.core.api.Assertions.assertThat;

import com.woorea.openstack.keystone.model.Authentication;
import org.junit.Test;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudIdentity.IdentityAuthenticationType;
import org.openecomp.mso.cloud.authentication.wrappers.RackspaceAPIKeyWrapper;

public class AuthenticationMethodFactoryTest {

    private static final Class WRAPPER_CLASS = RackspaceAPIKeyWrapper.class;
    private static final String AUTHENTICATION_TYPE = "authenticationTest";

    @Test
    public void register_NoExceptionThrown() throws IllegalAccessException, InstantiationException {
        AuthenticationMethodFactory.register(AUTHENTICATION_TYPE, WRAPPER_CLASS);
    }

    @Test
    public void register_throwExceptionWhenAuthTypeIsNull() throws InstantiationException, IllegalAccessException {
        try {
            AuthenticationMethodFactory.register(null, WRAPPER_CLASS);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isNotEmpty().contains("Authentication Type to register cannot be null "
                    + "or an empty name string");
        }
    }

    @Test
    public void register_throwExceptionWhenAuthTypeIsEmpty() throws InstantiationException, IllegalAccessException {
        try {
            AuthenticationMethodFactory.register("", WRAPPER_CLASS);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isNotEmpty().contains("Authentication Type to register cannot be null "
                    + "or an empty name string");
        }
    }

    @Test
    public void register_throwExceptionWhenWrapperIsNull() throws IllegalAccessException, InstantiationException {
        try {
            AuthenticationMethodFactory.register(AUTHENTICATION_TYPE, null);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isNotEmpty()
                    .contains("Wrapper Class to register for Authentication cannot be null");
        }
    }

    @Test
    public void getAuthentication_NoExceptionThrown() {
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setIdentityAuthenticationType(IdentityAuthenticationType.RACKSPACE_APIKEY);
        cloudIdentity.setMsoId("msoIdTest");
        cloudIdentity.setMsoPass("123");
        Authentication result = AuthenticationMethodFactory.getAuthenticationFor(cloudIdentity);
        assertThat(result).isNotNull();
    }

    @Test
    public void getAuthentication_ThrowExWhenCloudSiteIsNull() {
        try {
            AuthenticationMethodFactory.getAuthenticationFor(null);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isNotEmpty().contains("Cloud identity cannot be null");
        }
    }

    @Test
    public void getAuthentication_ThrowExWhenIdentityAuthenticationTypeIsNotSet() {
        try {
            AuthenticationMethodFactory.getAuthenticationFor(new CloudIdentity());
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isNotEmpty()
                    .contains("Cloud identity authentication type cannot be null or empty");
        }
    }

}
