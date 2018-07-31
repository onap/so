/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.serviceinstancebeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonRootName(value = "vnfs")
@JsonInclude(Include.NON_DEFAULT)
public class Vnfs implements Serializable{

	private static final long serialVersionUID = 8081495240474276501L;
	@JsonProperty("modelInfo")
	protected ModelInfo modelInfo;
	@JsonProperty("cloudConfiguration")
	protected CloudConfiguration cloudConfiguration;
	@JsonProperty("instanceName")
	protected String instanceName;
	@JsonProperty("platform")
	protected Platform platform;
	@JsonProperty("lineOfBusiness")
	protected LineOfBusiness lineOfBusiness;
	@JsonProperty("productFamilyId")
	protected String productFamilyId;
	@JsonProperty("instanceParams")
	private List<Map<String, String>> instanceParams = new ArrayList<>();
	@JsonProperty("vfModules")
	private List<VfModules> vfModules = new ArrayList<>();
	

	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	public CloudConfiguration getCloudConfiguration() {
		return cloudConfiguration;
	}

	public void setCloudConfiguration(CloudConfiguration cloudConfiguration) {
		this.cloudConfiguration = cloudConfiguration;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	public LineOfBusiness getLineOfBusiness() {
		return lineOfBusiness;
	}

	public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}

	public String getProductFamilyId() {
		return productFamilyId;
	}

	public void setProductFamilyId(String productFamilyId) {
		this.productFamilyId = productFamilyId;
	}

	public List<Map<String, String>> getInstanceParams() {
		return instanceParams;
	}

	public void setInstanceParams(List<Map<String, String>> instanceParams) {
		this.instanceParams = instanceParams;
	}

	public List<VfModules> getVfModules() {
		return vfModules;
	}

	public void setVfModules(List<VfModules> vfModules) {
		this.vfModules = vfModules;
	}

	@Override
	public String toString() {
		return "Vnfs [modelInfo=" + modelInfo + 
				", cloudConfiguration=" + cloudConfiguration + 
				", instanceName=" + instanceName + ", platform=" + platform + ", " + 
				"lineOfBusiness=" + lineOfBusiness + 
				", productFamilyId=" + productFamilyId + ", instanceParams=" + instanceParams + 
				", vfModules=" + vfModules + "]";
	}
	
}