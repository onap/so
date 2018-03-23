/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vdu;

/**
 * Cloud information structure for deploying/managing a VDU.  Includes the cloud site
 * as well as tenant information within the site.  Currently this is defined as a 
 * cloud site ID. which would map to a CloudConfig entry.
 * Perhaps the CloudConfig entry itself should be provided, instead of requiring each
 * plug-in to query it.
 * 
 * The meaning of 'tenant' may differ by cloud provider, but every cloud supports some
 * sort of tenant partitioning.
 * 
 */
public class CloudInfo {
	
	private String cloudSiteId;
	private String tenantId;  	
	private String tenantName;//bpmn query and pass
	
	public CloudInfo() {
	}
	
	public CloudInfo (String cloudSiteId, String tenantId, String tenantName) {
		this.cloudSiteId = cloudSiteId;
		this.tenantId = tenantId;
		this.tenantName = tenantName;
	}
	
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
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	
	
}