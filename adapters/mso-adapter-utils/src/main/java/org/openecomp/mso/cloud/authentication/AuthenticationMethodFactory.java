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

import com.woorea.openstack.keystone.model.Authentication;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openecomp.mso.cloud.CloudIdentity;

/**
 * This factory manages all the wrappers associated to authentication types.
 */
public final class AuthenticationMethodFactory {

	private static Map<String, AuthenticationWrapper> authWrappers = new ConcurrentHashMap<>();

	private AuthenticationMethodFactory() {}
	
	/**
	 * Function to be called by classes implementing the abstract {@link AuthenticationWrapper#register(String, Class)}.
	 */
	static final synchronized void register(String authenticationType, Class<? extends AuthenticationWrapper> wrapperClass) throws InstantiationException, IllegalAccessException {
		if ((authenticationType == null) || ("".equals(authenticationType))) {
			throw new IllegalArgumentException("Authentication Type to register cannot be null or an empty name string, provided value is " + authenticationType + ".");
		}
		if (wrapperClass == null) {
			throw new IllegalArgumentException("Wrapper Class to register for Authentication cannot be null");
		}

		if (!authWrappers.containsKey(authenticationType)) {
			authWrappers.put(authenticationType, wrapperClass.newInstance());
		}
	}
	
	public static final synchronized Authentication getAuthenticationFor(CloudIdentity cloudIdentity) {
		if (cloudIdentity == null) {
			throw new IllegalArgumentException("Cloud identity cannot be null");
		}
		if ((cloudIdentity.getIdentityAuthenticationType() == null) || ("".equals(cloudIdentity.getIdentityAuthenticationType().toString()))) {
			throw new IllegalArgumentException("Cloud identity authentication type cannot be null or empty, provided value is " + cloudIdentity.getIdentityAuthenticationType() + ".");
		}
		String authenticationType = cloudIdentity.getIdentityAuthenticationType().toString();
		
		if (authWrappers.containsKey(authenticationType)) {
			return authWrappers.get(authenticationType).getAuthentication(cloudIdentity);
		} else {
			throw new IllegalArgumentException("Provided authentication type (" + authenticationType + ") is not implemented by any wrapper.");
		}
	}
}
