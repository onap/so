package org.openecomp.mso.bpmn.core.decomposition;

import java.io.Serializable;

public class ServiceInstanceData extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String instanceId;
	private String instanceName;
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
}
