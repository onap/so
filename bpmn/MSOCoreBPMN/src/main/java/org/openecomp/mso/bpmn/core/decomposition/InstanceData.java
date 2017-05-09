package org.openecomp.mso.bpmn.core.decomposition;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class InstanceData  extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String instanceId;
	private String instanceName;
	private HomingSolution homingSolution;
	
	/*
	 * GET && SET
	 */
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
	public HomingSolution getHomingSolution() {
		return homingSolution;
	}
	public void setHomingSolution(HomingSolution homingSolution) {
		this.homingSolution = homingSolution;
	}
}
