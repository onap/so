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

import java.io.Serializable;
import java.sql.Timestamp;

public class ServiceToNetworks implements Serializable {

	// This maps to SERVICE.SERVICE_NAME_VERSION_ID / Service.serviceNameVersionId in SERVICE/Service table
	private String serviceModelUuid;
	// This maps to NETWORK_RESOURCE_CUSTOMIZATION.MODEL_CUSTOMIZATION_UUID / NetworkResourceCustomization.ModelCustomizationUuid
	private String networkModelCustomizationUuid;
	private Timestamp created;
	public static final long serialVersionUID = -1322322139926390329L;

	public ServiceToNetworks() {
		super();
	}

	public String getServiceModelUuid() {
		return this.serviceModelUuid;
	}
	public void setServiceModelUuid(String serviceModelUuid) {
		this.serviceModelUuid = serviceModelUuid;
	}

	public String getNetworkModelCustomizationUuid() {
		return this.networkModelCustomizationUuid;
	}
	public void setNetworkModelCustomizationUuid(String networkCustomizationUuid) {
		this.networkModelCustomizationUuid = networkCustomizationUuid;
	}

	public Timestamp getCreated() {
		return this.created;
	}
	public void setCreated(Timestamp timestamp) {
		this.created = timestamp;
	}

	@Override
	public boolean equals(Object o) {
        if (!(o instanceof ServiceToNetworks)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        ServiceToNetworks stn = (ServiceToNetworks) o;
        if (stn.getNetworkModelCustomizationUuid().equals(this.getNetworkModelCustomizationUuid())
        		&& stn.getServiceModelUuid().equals(this.getServiceModelUuid())) {
            return true;
        } 
        return false;
	}
	
	@Override 
	public int hashCode() {
		
		int code = this.networkModelCustomizationUuid == null ? 0 : this.networkModelCustomizationUuid.hashCode();
		code += this.serviceModelUuid == null ? 0 : this.serviceModelUuid.hashCode();
		
		return code;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServiceToNetworks mapping: ");
		sb.append("serviceModelUuid=").append(this.serviceModelUuid);
		sb.append(",networkModelCustomizationUuid=").append(networkModelCustomizationUuid);
		return sb.toString();
	}

}
