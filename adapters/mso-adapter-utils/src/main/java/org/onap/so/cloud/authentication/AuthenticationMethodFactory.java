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

package org.onap.so.cloud.authentication;

import java.util.Collections;
import org.onap.so.cloud.authentication.models.RackspaceAuthentication;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.utils.CryptoUtils;
import org.springframework.stereotype.Component;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import com.woorea.openstack.keystone.v3.model.Authentication.Identity;
import com.woorea.openstack.keystone.v3.model.Authentication.Identity.Password;
import com.woorea.openstack.keystone.v3.model.Authentication.Identity.Password.User;
import com.woorea.openstack.keystone.v3.model.Authentication.Identity.Password.User.Domain;
import com.woorea.openstack.keystone.v3.model.Authentication.Scope;
import com.woorea.openstack.keystone.v3.model.Authentication.Scope.Project;

/**
 * This factory manages all the wrappers associated to authentication types.
 *
 */
@Component
public final class AuthenticationMethodFactory {

    public final Authentication getAuthenticationFor(CloudIdentity cloudIdentity) {
        if (cloudIdentity == null) {
            throw new IllegalArgumentException("Cloud identity cannot be null");
        }
        if ((cloudIdentity.getIdentityAuthenticationType() == null)
                || ("".equals(cloudIdentity.getIdentityAuthenticationType().toString()))) {
            throw new IllegalArgumentException(
                    "Cloud identity authentication type cannot be null or empty, provided value is "
                            + cloudIdentity.getIdentityAuthenticationType() + ".");
        }
        AuthenticationType authenticationType = cloudIdentity.getIdentityAuthenticationType();
        if (AuthenticationType.RACKSPACE_APIKEY.equals(authenticationType)) {
            return new RackspaceAuthentication(cloudIdentity.getMsoId(),
                    CryptoUtils.decryptCloudConfigPassword(cloudIdentity.getMsoPass()));
        } else {
            return new UsernamePassword(cloudIdentity.getMsoId(),
                    CryptoUtils.decryptCloudConfigPassword(cloudIdentity.getMsoPass()));
        }
    }


    public final com.woorea.openstack.keystone.v3.model.Authentication getAuthenticationForV3(
            CloudIdentity cloudIdentity, String tenantId) {
        Identity identity = new Identity();
        Password password = new Password();
        User user = new User();
        Domain userDomain = new Domain();
        Scope scope = new Scope();
        Project project = new Project();
        Project.Domain projectDomain = new Project.Domain();
        userDomain.setName(cloudIdentity.getUserDomainName());
        projectDomain.setName(cloudIdentity.getProjectDomainName());
        user.setName(cloudIdentity.getMsoId());
        user.setPassword(CryptoUtils.decryptCloudConfigPassword(cloudIdentity.getMsoPass()));
        user.setDomain(userDomain);
        password.setUser(user);
        project.setDomain(projectDomain);
        project.setId(tenantId);
        scope.setProject(project);
        identity.setPassword(password);
        identity.setMethods(Collections.singletonList("password"));
        com.woorea.openstack.keystone.v3.model.Authentication v3Auth =
                new com.woorea.openstack.keystone.v3.model.Authentication();
        v3Auth.setIdentity(identity);
        v3Auth.setScope(scope);
        return v3Auth;
    }

}
