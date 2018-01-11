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

import org.openecomp.mso.adapters.NetworkMessageRequest;
import org.openecomp.mso.entity.MsoRequest;

import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.resteasy.annotations.providers.NoJackson;
import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("deleteVfModuleRequest")
@XmlRootElement(name = "deleteVfModuleRequest")
@NoJackson
public class DeleteVfModuleRequest extends NetworkMessageRequest {
	private String cloudSiteId;
	private String tenantId;
	private String vnfId;
	private String vfModuleId;
	private String vfModuleStackId;

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

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public String getVfModuleStackId() {
		return vfModuleStackId;
	}

	public void setVfModuleStackId(String vfModuleStackId) {
		this.vfModuleStackId = vfModuleStackId;
	}

	public MsoRequest getMsoRequest() {
		return msoRequest;
	}

	public void setMsoRequest(MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}
}
