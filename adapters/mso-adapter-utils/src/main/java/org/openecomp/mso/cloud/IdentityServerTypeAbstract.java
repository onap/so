/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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
package org.openecomp.mso.cloud;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.mso.openstack.utils.MsoTenantUtils;


public abstract class IdentityServerTypeAbstract {

	// This map will prevent duplicates (as if it was an Enum).
	// Without this, using an instance specific field for the class could allow
	// different classes bound to the same entry name.
	private static final Map<String, IdentityServerTypeAbstract> entries = new ConcurrentHashMap<>();

	private String serverType;

	private Class<? extends MsoTenantUtils> utilsClass;

	protected IdentityServerTypeAbstract(String serverType, Class<? extends MsoTenantUtils> utilsClass) {
		if ((serverType == null) || (serverType.isEmpty())) {
			throw new IllegalArgumentException("Server Type name cannot be null or empty, provided value was " + serverType);
		}
		if (entries.containsKey(serverType)) {
			throw new IllegalArgumentException("Duplicate Server Type entry for registration: " + serverType);
		}
		this.serverType = serverType;
		this.utilsClass = utilsClass;
		entries.put(serverType, this);
	}

	public static final IdentityServerTypeAbstract valueOf(String serverType) {
		return entries.get(serverType);
	}

	@Override
	public final String toString() {
		return this.serverType;
	}

	public final String name() {
		return this.serverType;
	}

	public static final IdentityServerTypeAbstract[] values() {
		return (IdentityServerTypeAbstract[]) entries.values().stream().toArray(IdentityServerTypeAbstract[]::new);
	}

	public final Class<? extends MsoTenantUtils> getMsoTenantUtilsClass() {
		return this.utilsClass;
	}

	@Override
	public final boolean equals(Object other) {
		return ((this.serverType != null) && (other != null) && (other instanceof IdentityServerTypeAbstract) && (this.serverType.equals(other.toString())));
	}

}
