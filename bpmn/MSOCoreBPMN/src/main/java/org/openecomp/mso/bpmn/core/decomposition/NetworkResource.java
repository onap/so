package org.openecomp.mso.bpmn.core.decomposition;

import org.codehaus.jackson.map.annotate.JsonRootName;


/**
 * Encapsulates Network resource data set
 * @author
 *
 */
@JsonRootName("networkResource")
public class NetworkResource extends ResourceDecomposition {

	private static final long serialVersionUID = 1L;
	/*
	 * set resourceType for this object
	 */
	public NetworkResource(){
		resourceType = "networkResource";
	}
	/*
	 * fields specific to VNF resource type
	 */
	private String networkType;
	private String networkRole;
	
	/*
	 * GET and SET
	 */
	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public String getNetworkRole() {
		return networkRole;
	}
	public void setNetworkRole(String networkRole) {
		this.networkRole = networkRole;
	}
}
