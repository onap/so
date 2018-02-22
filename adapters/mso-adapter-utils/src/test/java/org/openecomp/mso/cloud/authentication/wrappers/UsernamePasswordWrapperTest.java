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

package org.openecomp.mso.cloud.authentication.wrappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import org.junit.Test;

public class UsernamePasswordWrapperTest {

    @Test
    public void getAuthenticationSuccessful() {
        UsernamePasswordWrapper testedObject = new UsernamePasswordWrapper();
        Authentication authentication = testedObject.getAuthentication(WrapperTestUtility.createCloudIdentity());

        assertThat(authentication).isInstanceOf(UsernamePassword.class);
        UsernamePassword usernamePassword = (UsernamePassword) authentication;
        assertThat(usernamePassword.getPasswordCredentials().getUsername())
                .isEqualTo(WrapperTestUtility.CLOUD_IDENTITY_MSO_ID);
        assertThat(usernamePassword.getPasswordCredentials().getPassword())
                .isEqualTo(WrapperTestUtility.CLOUD_IDENTITY_MSO_PASS);
    }

    @Test
    public void getAuthenticationThrowsException() {
        try {
            new UsernamePasswordWrapper().getAuthentication(null);
            fail("Exception should be thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo(WrapperTestUtility.EXCEPTION_MESSAGE);
        }
    }

}
