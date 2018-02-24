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
import java.text.DateFormat;

public class ServiceToResourceCustomization implements Serializable {

	private static final long serialVersionUID = 768026109321305392L;
	
	private String modelType;
	private String serviceModelUUID;
	private Timestamp created;
	private String resourceModelCustomizationUUID;
	
	public ServiceToResourceCustomization() {}
	
	public String getServiceModelUUID() {
		return serviceModelUUID;
	}
	
	public void setServiceModelUUID(String serviceModelUUID) {
		this.serviceModelUUID = serviceModelUUID;
	}
	
	public String getModelType() {
		return modelType;
	}
	
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
	public Timestamp getCreated() {
		return created;
	}
	
	public void setCreated(Timestamp created) {
		this.created = created;
	}
	
	public String getResourceModelCustomizationUUID() {
		return resourceModelCustomizationUUID;
	}

	public void setResourceModelCustomizationUUID(String resourceModelCustomizationUUID) {
		this.resourceModelCustomizationUUID = resourceModelCustomizationUUID;
	}
	
	@Override
	public boolean equals(Object o) {
        if (!(o instanceof ServiceToResourceCustomization)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        ServiceToResourceCustomization strc = (ServiceToResourceCustomization) o;
        if (strc.getServiceModelUUID().equals(this.getServiceModelUUID()) 
        		&& strc.getResourceModelCustomizationUUID().equals(this.getResourceModelCustomizationUUID())
        		&& strc.getModelType().equals(this.getModelType())) {
            return true;
        }
        return false;
		
	}
	
	@Override 
	public int hashCode() {
		
		int code = this.modelType == null ? 0 : this.modelType.hashCode();
		code += this.serviceModelUUID == null ? 0 : this.serviceModelUUID.hashCode();
		code += this.resourceModelCustomizationUUID == null ? 0 : this.resourceModelCustomizationUUID.hashCode();
		
		return code;
		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServiceToResourceCustomization: modelType=").append(modelType).append(",serviceModelUUID=")
            .append(serviceModelUUID).append(",resourceModelCustomizationUUID=").append(resourceModelCustomizationUUID);
		if (created != null) {
		        sb.append (",created=");
		        sb.append (DateFormat.getInstance().format(created));
		}
		return sb.toString();
	}

}
