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

package org.openecomp.mso.adapters.vnfrest;

import org.openecomp.mso.adapters.NetworkMessageCommon;
import org.openecomp.mso.entity.MsoRequest;

import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.NoJackson;

import org.codehaus.jackson.map.annotate.JsonRootName;

/* README
* 1) Used JAXB/Jettison - see @NoJackson annotation on class to get RootElements REad by RestEasy
* 2) due to 1) Maps need to use this format

"vfModuleParams": {"entry": [
                         	{"key": "network_id",
                         	"value": "59ed7b41-2983-413f-ba93-e7d437433916"},
                         	{"key": "subnet_id",
                         	"value": "086c9298-5c57-49b7-bb2b-6fd5730c5d92"},
                         	{"key": "server_name_0",
                         	"value": "RaaVnf1"}
                         	]},
 * 3) to output json see toJSonString method below which required the @JsonRootName annotation and the WRAP_ROOT feature enabled
 * 4) Tryong to work with RESTEASY JACKSON and JAXB/JETTISON to conform to Json input/output specs
*/
@JsonRootName("createVfModuleRequest")
@XmlRootElement(name = "createVfModuleRequest")
@NoJackson
public class CreateVfModuleRequest extends NetworkMessageCommon {
	private String cloudSiteId;
	private String tenantId;

	private String vnfId;
	private String vnfType;
	private String vnfVersion;

	private String vfModuleId;
	private String vfModuleName;
	private String vfModuleType;

	private String volumeGroupId;
	private String volumeGroupStackId;
	private String baseVfModuleId;
	private String baseVfModuleStackId;
	private String modelCustomizationUuid;

	private String requestType;
	private Boolean failIfExists;
	private Boolean backout;

	private Map<String,String> vfModuleParams = new HashMap<>();
	private MsoRequest msoRequest = new MsoRequest();

	public String getCloudSiteId() {
		return cloudSiteId;
	}

	public void setCloudSiteId(String cloudSiteId) {
		this.cloudSiteId = cloudSiteId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getVnfId() {
		return vnfId;
	}

	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}

	public String getVfModuleName() {
		return vfModuleName;
	}

	public void setVfModuleName(String vfModuleName) {
		this.vfModuleName = vfModuleName;
	}

	public String getVnfType() {
		return vnfType;
	}

	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public String getVnfVersion() {
		return vnfVersion;
	}

	public void setVnfVersion(String vnfVersion) {
		this.vnfVersion = vnfVersion;
	}

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public String getVfModuleType() {
		return vfModuleType;
	}

	public void setVfModuleType(String vfModuleType) {
		this.vfModuleType = vfModuleType;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public String getVolumeGroupStackId() {
		return volumeGroupStackId;
	}

	public void setVolumeGroupStackId(String volumeGroupStackId) {
		this.volumeGroupStackId = volumeGroupStackId;
	}

	public String getBaseVfModuleId() {
		return baseVfModuleId;
	}

	public void setBaseVfModuleId(String baseVfModuleId) {
		this.baseVfModuleId = baseVfModuleId;
	}

	public String getBaseVfModuleStackId() {
		return baseVfModuleStackId;
	}

	public void setBaseVfModuleStackId(String baseVfModuleStackId) {
		this.baseVfModuleStackId = baseVfModuleStackId;
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}

	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Boolean getFailIfExists() {
		return failIfExists;
	}

	public void setFailIfExists(Boolean failIfExists) {
		this.failIfExists = failIfExists;
	}

	public Boolean getBackout() {
		return backout;
	}

	public void setBackout(Boolean backout) {
		this.backout = backout;
	}

	public Map<String, String> getVfModuleParams() {
		return vfModuleParams;
	}

	public void setVfModuleParams(Map<String, String> vfModuleParams) {
		this.vfModuleParams = vfModuleParams;
	}

	public MsoRequest getMsoRequest() {
		return msoRequest;
	}

	public void setMsoRequest(MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}
}
