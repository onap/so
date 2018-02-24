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

public class ServiceToAllottedResources implements Serializable {

	private String serviceModelUuid;
	private String arModelCustomizationUuid;
	private Timestamp created;

	public static final long serialVersionUID = -1322322139926390329L;

	public ServiceToAllottedResources() {
		super();
	}

	public String getServiceModelUuid() {
		return this.serviceModelUuid;
	}
	public void setServiceModelUuid(String serviceModelUuid) {
		this.serviceModelUuid = serviceModelUuid;
	}
	public String getArModelCustomizationUuid() {
		return this.arModelCustomizationUuid;
	}
	public void setArModelCustomizationUuid(String arModelCustomizationUuid) {
		this.arModelCustomizationUuid = arModelCustomizationUuid;
	}
	public Timestamp getCreated() {
		return this.created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Override
	public boolean equals(Object o) {
        if (!(o instanceof ServiceToAllottedResources)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        ServiceToAllottedResources star = (ServiceToAllottedResources) o;
        if (star.getArModelCustomizationUuid().equals(this.getArModelCustomizationUuid())
        		&& star.getServiceModelUuid().equals(this.getServiceModelUuid())) {
            return true;
        } 
        return false;
	}
	
	@Override 
	public int hashCode() {

		int code = this.arModelCustomizationUuid == null ? 0 : this.arModelCustomizationUuid.hashCode();
		code += this.serviceModelUuid == null ? 0 : this.serviceModelUuid.hashCode();
		
		return code;
	}

	@Override
	public String toString() {
		return "serviceModelUuid=" + this.serviceModelUuid + "arModelCustomizationUuid=" + this.arModelCustomizationUuid;
	}

}
