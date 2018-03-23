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