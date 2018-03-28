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

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.openpojo.business.annotation.BusinessKey;

public class HeatTemplateParam implements Serializable {

	@BusinessKey
	private String heatTemplateArtifactUuid = null;
	@BusinessKey
	private String paramName = null;
	private boolean required;
	private String paramType = null;
	private String paramAlias = null;
    public static final long serialVersionUID = -1322322139926390329L;
	
	public HeatTemplateParam() {}
	
	public String getHeatTemplateArtifactUuid() {
		return this.heatTemplateArtifactUuid;
	}
	public void setHeatTemplateArtifactUuid(String heatTemplateArtifactUuid) {
		this.heatTemplateArtifactUuid = heatTemplateArtifactUuid;
	}

	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String getParamAlias() {
		return paramAlias;
	}
	public void setParamAlias(String paramAlias) {
		this.paramAlias = paramAlias;
	}
	
	public String getParamType() {
	    return paramType;
	}
	public void setParamType (String paramType) {
	    this.paramType = paramType;
	}
	
	
	@Override
	public String toString () {
		return "Param=" + paramName + ",type=" + paramType + ",required=" + required + ",paramAlias=" + paramAlias + ", heatTemplateArtifactUuid=" + this.heatTemplateArtifactUuid;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof HeatTemplateParam)) {
			return false;
		}
		HeatTemplateParam castOther = (HeatTemplateParam) other;
		return new EqualsBuilder().append(heatTemplateArtifactUuid, castOther.heatTemplateArtifactUuid)
				.append(paramName, castOther.paramName).isEquals();
	}

    @Override
    public int hashCode () {
        int result;
        result = this.paramName == null ? 0 : this.paramName.hashCode() + this.heatTemplateArtifactUuid == null ? 0 : this.heatTemplateArtifactUuid.hashCode();
        return result;
	}
}
