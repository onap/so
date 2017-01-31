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


public class VnfRecipe extends Recipe {

	private String vnfType;
	private String vnfParamXSD;
	private String vfModuleId; 

	public VnfRecipe() {}

	public String getVnfType() {
		return vnfType;
	}
	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public String getVnfParamXSD() {
		return vnfParamXSD;
	}
	public void setVnfParamXSD(String vnfParamXSD) {
		this.vnfParamXSD = vnfParamXSD;
	}
	
	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}
	
	@Override
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append (",vnfParamXSD=" + vnfParamXSD);
		sb.append (",serviceType=" + getServiceType ());
		sb.append (",vfModuleId=" + getVfModuleId ());
		return sb.toString();
	}
}
