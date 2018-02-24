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

// an embeddable class to represent the Composite key for NetworkResource in the 1707 db refactoring
public class HeatTemplateArtifactUuidModelUuid implements Serializable {
	
	private String heatTemplateArtifactUuid;
	private String modelUuid;
	public static final long serialVersionUID = -1322322139926390329L;

	public HeatTemplateArtifactUuidModelUuid() {
	}
	
	public String getHeatTemplateArtifactUuid() {
		return this.heatTemplateArtifactUuid;
	}
	public void setHeatTemplateArtifactUuid(String heatTemplateArtifactUuid) {
		this.heatTemplateArtifactUuid = heatTemplateArtifactUuid;
	}
	public String getModelUuid() {
		return this.modelUuid;
	}
	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}

	@Override
	public String toString() {
		return "heatTemplateArtifactUuid=" + this.heatTemplateArtifactUuid + " modelUuid=" + this.modelUuid;
	}
	
    @Override
    public boolean equals (Object o) {
        if (!(o instanceof HeatTemplateArtifactUuidModelUuid)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        HeatTemplateArtifactUuidModelUuid htaumu = (HeatTemplateArtifactUuidModelUuid) o;
        if (htaumu.getHeatTemplateArtifactUuid().equals(this.getHeatTemplateArtifactUuid()) 
        		&& htaumu.getModelUuid().equals(this.getModelUuid())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result;
        result = this.heatTemplateArtifactUuid.hashCode() + this.modelUuid.hashCode();
        return result;
    }

}
