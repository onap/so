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


import javax.xml.bind.annotation.XmlRootElement;

import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.jboss.resteasy.annotations.providers.NoJackson;
import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("VfModuleRollback")
@XmlRootElement(name = "VfModuleRollback")
@NoJackson
public class VfModuleRollback {
	private String vnfId;
	private String vfModuleId;
	private String vfModuleStackId;
	private boolean vfModuleCreated = false;
	private String tenantId;
	private String cloudSiteId;
	private MsoRequest msoRequest;
	private String messageId;

	public VfModuleRollback() {
	}

	public VfModuleRollback(VnfRollback vrb, String vfModuleId, String vfModuleStackId, String messageId)
	{
		this.vnfId = vrb.getVnfId();
		this.vfModuleId = vfModuleId;
		this.vfModuleStackId = vfModuleStackId;
		this.vfModuleCreated = vrb.getVnfCreated();
		this.tenantId = vrb.getTenantId();
		this.cloudSiteId = vrb.getCloudSiteId();
		this.msoRequest = vrb.getMsoRequest();
		this.messageId = messageId;
	}

	public VfModuleRollback(String vnfId, String vfModuleId,
			String vfModuleStackId, boolean vfModuleCreated, String tenantId,
			String cloudSiteId,
			MsoRequest msoRequest,
			String messageId) {
		super();
		this.vnfId = vnfId;
		this.vfModuleId = vfModuleId;
		this.vfModuleStackId = vfModuleStackId;
		this.vfModuleCreated = vfModuleCreated;
		this.tenantId = tenantId;
		this.cloudSiteId = cloudSiteId;
		this.msoRequest = msoRequest;
		this.messageId = messageId;
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
	public boolean isVfModuleCreated() {
		return vfModuleCreated;
	}
	public void setVfModuleCreated(boolean vfModuleCreated) {
		this.vfModuleCreated = vfModuleCreated;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getCloudSiteId() {
		return cloudSiteId;
	}
	public void setCloudSiteId(String cloudSiteId) {
		this.cloudSiteId = cloudSiteId;
	}
	public MsoRequest getMsoRequest() {
		return msoRequest;
	}
	public void setMsoRequest(MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
}
