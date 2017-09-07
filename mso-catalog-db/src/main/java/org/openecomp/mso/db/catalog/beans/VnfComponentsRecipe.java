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

package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;

public class VnfComponentsRecipe extends Recipe implements Serializable {
	
	private static final long serialVersionUID = 768026109321305392L;

	private String vnfType;
	private String vnfComponentParamXSD;
	private String vnfComponentType;
	private String vfModuleModelUUId; 

	public VnfComponentsRecipe() {}

	public String getVnfType() {
		return vnfType;
	}
	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public String getVnfComponentParamXSD() {
		return vnfComponentParamXSD;
	}
	public void setVnfComponentParamXSD(String vnfComponentParamXSD) {
		this.vnfComponentParamXSD = vnfComponentParamXSD;
	}
	
	public String getVnfComponentType() {
		return vnfComponentType;
	}
	public void setVnfComponentType(String vnfComponentType) {
		this.vnfComponentType = vnfComponentType;
	}
	
    public String getVfModuleModelUUId() {
		return vfModuleModelUUId;
	}

	public void setVfModuleModelUUId(String vfModuleModelUUId) {
		this.vfModuleModelUUId = vfModuleModelUUId;
	}

	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append (",vnfComponentParamXSD=" + vnfComponentParamXSD);
		sb.append (",serviceType=" + getServiceType ());
		sb.append (",vnfComponentType=" + getVnfComponentType ());
		sb.append (",vfModuleId=" + getVfModuleModelUUId());
		return sb.toString();
	}
}
