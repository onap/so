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

public class TempNetworkHeatTemplateLookup implements Serializable {
	
	private String networkResourceModelName;
	private String heatTemplateArtifactUuid;
	private String aicVersionMin;
	private String aicVersionMax;
    public static final long serialVersionUID = -1322322139926390329L;

	public TempNetworkHeatTemplateLookup() {
		super();
	}
	
	public String getNetworkResourceModelName() {
		return this.networkResourceModelName;
	}
	public void setNetworkResourceModelName(String networkResourceModelName) {
		this.networkResourceModelName = networkResourceModelName;
	}
	
	public String getHeatTemplateArtifactUuid() {
		return this.heatTemplateArtifactUuid;
	}
	public void setHeatTemplateArtifactUuid(String heatTemplateArtifactUuid) {
		this.heatTemplateArtifactUuid = heatTemplateArtifactUuid;
	}
	public String getAicVersionMin() {
		return this.aicVersionMin;
	}
	
	public void setAicVersionMin(String aicVersionMin) {
		this.aicVersionMin = aicVersionMin;
	}
	
	public String getAicVersionMax() {
		return this.aicVersionMax;
	}
	
	public void setAicVersionMax(String aicVersionMax) {
		this.aicVersionMax = aicVersionMax;
	}

	@Override
	public String toString() {
		return "NetworkResourceModelName=" + this.networkResourceModelName + "HeatTemplateArtifactUuid="
			+ this.heatTemplateArtifactUuid + "aicVersionMin=" + this.aicVersionMin + "aicVersionMax="
			+ this.aicVersionMax;
	}

	@Override
    public boolean equals (Object o) {
        if (!(o instanceof TempNetworkHeatTemplateLookup)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        TempNetworkHeatTemplateLookup tnhtl = (TempNetworkHeatTemplateLookup) o;
        if (tnhtl.getHeatTemplateArtifactUuid().equals(this.getHeatTemplateArtifactUuid()) && tnhtl.getNetworkResourceModelName().equals(this.getNetworkResourceModelName())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result;
        result = (this.networkResourceModelName != null ? this.networkResourceModelName.hashCode() : 0) + (this.heatTemplateArtifactUuid != null ? this.heatTemplateArtifactUuid.hashCode() : 0);
        return result;
    }

}
