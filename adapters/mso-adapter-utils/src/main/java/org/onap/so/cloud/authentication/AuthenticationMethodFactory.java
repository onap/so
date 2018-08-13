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

import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.cloud.authentication.models.RackspaceAuthentication;
import org.onap.so.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;

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
		if ((cloudIdentity.getIdentityAuthenticationType() == null)|| ("".equals(cloudIdentity.getIdentityAuthenticationType().toString()))) {
			throw new IllegalArgumentException("Cloud identity authentication type cannot be null or empty, provided value is " + cloudIdentity.getIdentityAuthenticationType() + ".");
		}
		AuthenticationType authenticationType = cloudIdentity.getIdentityAuthenticationType();
		if (AuthenticationType.RACKSPACE_APIKEY.equals(authenticationType)) {
			return new RackspaceAuthentication (cloudIdentity.getMsoId (), CryptoUtils.decryptCloudConfigPassword(cloudIdentity.getMsoPass ()));
		} else {
			return new UsernamePassword (cloudIdentity.getMsoId (), CryptoUtils.decryptCloudConfigPassword(cloudIdentity.getMsoPass ()));
		}
	}
}
