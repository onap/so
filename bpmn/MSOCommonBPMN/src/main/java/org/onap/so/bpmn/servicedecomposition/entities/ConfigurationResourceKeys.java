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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;

public class ConfigurationResourceKeys implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String vfModuleCustomizationUUID;
	private String vnfResourceCustomizationUUID;
	private String cvnfcCustomizationUUID;
	private String vnfcName;
	
	public String getVfModuleCustomizationUUID() {
		return vfModuleCustomizationUUID;
	}
	public void setVfModuleCustomizationUUID(String vfModuleCustomizationUUID) {
		this.vfModuleCustomizationUUID = vfModuleCustomizationUUID;
	}
	public String getVnfResourceCustomizationUUID() {
		return vnfResourceCustomizationUUID;
	}
	public void setVnfResourceCustomizationUUID(String vnfResourceCustomizationUUID) {
		this.vnfResourceCustomizationUUID = vnfResourceCustomizationUUID;
	}
	public String getCvnfcCustomizationUUID() {
		return cvnfcCustomizationUUID;
	}
	public void setCvnfcCustomizationUUID(String cvnfcCustomizationUUID) {
		this.cvnfcCustomizationUUID = cvnfcCustomizationUUID;
	}
	public String getVnfcName() {
		return vnfcName;
	}
	public void setVnfcName(String vnfcName) {
		this.vnfcName = vnfcName;
	}
	
	
}
