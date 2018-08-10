/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.catalog.beans;

import java.io.Serializable;

import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;


public class ControllerSelectionReferenceId implements Serializable {

	private static final long serialVersionUID = 1L;
	@BusinessKey
	private String vnfType;
	@BusinessKey
    private String controllerName;
	@BusinessKey
    private String actionCategory;

	
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ControllerSelectionReferenceId)) {
			return false;
		}
		ControllerSelectionReferenceId castOther = (ControllerSelectionReferenceId) other;
		return new EqualsBuilder().append(vnfType, castOther.vnfType).append(controllerName, castOther.controllerName)
				.append(actionCategory, castOther.actionCategory).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(vnfType).append(controllerName).append(actionCategory).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("vnfType", vnfType).append("controllerName", controllerName)
				.append("actionCategory", actionCategory).toString();
	}
	
	public String getVnfType() {
		return vnfType;
	}
	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}
	public String getControllerName() {
		return controllerName;
	}
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}
	public String getActionCategory() {
		return actionCategory;
	}
	public void setActionCategory(String actionCategory) {
		this.actionCategory = actionCategory;
	}	
	

}
