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

import org.openecomp.mso.cloud.CloudIdentity;

import com.woorea.openstack.keystone.model.Authentication;

/**
 * This abstract class provides the necessary method for registering authentication
 * types with wrapper classes, and also defines the contract for providing
 * Openstack-compatible Authentication implementations for said authentication types.
 *
 */
public abstract class AuthenticationWrapper {

	/**
	 * Registers the implementing class to the list of Authentication Wrappers.
	 *
	 * @param authenticationType The authentication type that is provided by the implementing class
	 * @param wrapperClass The implementing class Class object
	 * @throws InstantiationException If the provided implementing class cannot be instantiated
	 * @throws IllegalAccessException If the provided implementing class cannot be instantiated
	 */
	public static final void register(String authenticationType, Class<? extends AuthenticationWrapper> wrapperClass) throws InstantiationException, IllegalAccessException {
		AuthenticationMethodFactory.register(authenticationType, wrapperClass);
	}
	
	/**
	 * Returns an OpenStack Authentication object for the provided CloudIdentity.
	 *
	 * @param cloudIdentity The input Cloud Identity instance
	 * @return the OpenStack Authentication associated with this cloud identity instance
	 */
	protected abstract Authentication getAuthentication(CloudIdentity cloudIdentity);

}
