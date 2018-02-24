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
package org.openecomp.mso.db.catalog.beans;

import java.sql.Timestamp;
import java.io.Serializable;

public class NetworkResourceCustomization implements Serializable{

	// modelCustomizationUuid and networkResourceModelUuid form a composite primary key
	private String modelCustomizationUuid = null;
	private String networkResourceModelUuid = null;
	public static final long serialVersionUID = -1322322139926390329L;
	private String modelInstanceName;
	private Timestamp created;
	private String networkTechnology;
	private String networkType = null;
	private String networkScope;
	private String networkRole;

	// These fields are not in the table directly - but I'm adding them here for storage in the objects we're dealing with
	private NetworkResource networkResource = null;

	public NetworkResourceCustomization() {
		super();
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getNetworkResourceModelUuid() {
		return this.networkResourceModelUuid;
	}
	public void setNetworkResourceModelUuid(String networkResourceModelUuid) {
		this.networkResourceModelUuid = networkResourceModelUuid;
	}

	public String getModelInstanceName() {
		return  this.modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}

	public NetworkResource getNetworkResource() {
		return this.networkResource;
	}
	public void setNetworkResource(NetworkResource networkResource) {
		this.networkResource = networkResource;
	}

	public String getNetworkType() {
		return this.networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public Timestamp getCreated() {
		return this.created;
	}
	public void setCreated(java.sql.Timestamp timestamp) {
		this.created = timestamp;
	}

	public String getNetworkTechnology() {
		return this.networkTechnology;
	}
	public void setNetworkTechnology(String networkTechnology) {
		this.networkTechnology = networkTechnology;
	}
	public String getNetworkScope() {
		return this.networkScope;
	}
	public void setNetworkScope(String networkScope) {
		this.networkScope = networkScope;
	}
	public void setNetworkRole(String networkRole) {
		this.networkRole = networkRole;
	}
	public String getNetworkRole() {
		return this.networkRole;
	}

	@Override
	public String toString() {
		return "modelCustomizationUuid=" +
			this.modelCustomizationUuid +
			"networkResourceModelUuid=" +
			this.networkResourceModelUuid +
			"modelInstanceName=" +
			this.modelInstanceName +
			"networkType=" +
			this.networkType +
			"networkTechnology=" +
			this.networkTechnology +
			"networkScope=" +
			this.networkScope +
			"networkRole=" +
			this.networkRole;
	}

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof NetworkResourceCustomization)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        NetworkResourceCustomization nrc = (NetworkResourceCustomization) o;
        if (nrc.getModelCustomizationUuid().equals(this.getModelCustomizationUuid()) 
        		&& nrc.getNetworkResourceModelUuid().equals(this.getNetworkResourceModelUuid())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result;
        result = (this.modelCustomizationUuid != null ? this.modelCustomizationUuid.hashCode() : 0) + (this.networkResourceModelUuid != null ? this.networkResourceModelUuid.hashCode() : 0);
        return result;
    }

}
