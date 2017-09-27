/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.mso.cloud.authentication.AuthenticationWrapper;
import org.openecomp.mso.logger.MsoLogger;

public abstract class IdentityAuthenticationTypeAbstract {

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);
	// This map will prevent duplicates (as if it was an Enum).
	// Without this, using an instance specific field for the class could allow
	// different classes bound to the same entry name.
	private static final Map<String, IdentityAuthenticationTypeAbstract> entries = new ConcurrentHashMap<>();

	private String identityType;

	private Class<? extends AuthenticationWrapper> wrapperClass;

	protected IdentityAuthenticationTypeAbstract(String identityType, Class<? extends AuthenticationWrapper> wrapperClass) {
		try {
			this.identityType = identityType;
			this.wrapperClass = wrapperClass;
			entries.put(identityType, this);
			AuthenticationWrapper.register(this.toString(), wrapperClass);
		} catch (IllegalAccessException | InstantiationException e) {
			LOGGER.debug("Exception in Identity Authentication",e);
		}
	}

	public static final IdentityAuthenticationTypeAbstract valueOf(String serverType) {
		return entries.get(serverType);
	}

	@Override
	public final String toString() {
		return this.identityType;
	}

	public final String name() {
		return this.identityType;
	}

	public static final IdentityAuthenticationTypeAbstract[] values() {
		return (IdentityAuthenticationTypeAbstract[]) entries.values().stream().toArray(IdentityAuthenticationTypeAbstract[]::new);
	}

	public final Class<? extends AuthenticationWrapper> getWrapperClass() {
		return this.wrapperClass;
	}

	@Override
	public final boolean equals(Object other) {
		return (this.identityType != null) && (other != null) && (other instanceof IdentityAuthenticationTypeAbstract) && (this.identityType.equals(other.toString()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identityType == null) ? 0 : identityType.hashCode());
		return result;
	}

}
