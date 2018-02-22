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
import java.text.DateFormat;

import java.io.Serializable;

public class VnfComponent implements Serializable {
    private int vnfId;
    private String componentType = null;
    private Integer heatTemplateId;
    private Integer heatEnvironmentId;
    public static final long serialVersionUID = -1322322139926390329L;

	private Timestamp created;
    
    public VnfComponent() {}

    public int getVnfId() {
        return vnfId;
    }
    public void setVnfId(int id) {
        this.vnfId = id;
    }

    public String getComponentType() {
        return componentType;
    }
    public void setComponentType(String ct) {
        this.componentType = ct;
    }

    public Integer getHeatTemplateId() {
        return heatTemplateId;
    }
    public void setHeatTemplateId(Integer ht) {
        this.heatTemplateId = ht;
    }

    public Integer getHeatEnvironmentId() {
        return heatEnvironmentId;
    }
    public void setHeatEnvironmentId(Integer he) {
        this.heatEnvironmentId = he;
    }

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VnfComponent: ");
        sb.append("vnfId=" + vnfId);
        sb.append(",componentType=" + componentType);
        sb.append(",heatTemplateId=" + heatTemplateId);
        sb.append(",heatEnvironmentId=" + heatEnvironmentId);
        
        if (created != null) {
	        sb.append (",created=");
	        sb.append (DateFormat.getInstance().format(created));
	    }
        return sb.toString();
    }
    
    @Override
    public boolean equals (Object o) {
        if (!(o instanceof VnfComponent)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        VnfComponent vnfComponent = (VnfComponent) o;
        if (vnfComponent.getVnfId() == this.vnfId && vnfComponent.componentType.equalsIgnoreCase(this.componentType)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // return the hashCode of the concat string of type+vnfId - should be okay.
        int result = 0;
        result = (this.componentType + this.vnfId).hashCode();
        return result;
    }
}
