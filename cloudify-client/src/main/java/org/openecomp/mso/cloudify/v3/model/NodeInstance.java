package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("node_instance")
public class NodeInstance implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("deployment_id")
    private String deploymentId;

    @JsonProperty("host_id")
    private String hostId;

    @JsonProperty("id")
    private String id;
    
    @JsonProperty("node_id")
    private String nodeId;
    
    @JsonProperty("relationships")
    private List<Object> relationships = null;
    
    @JsonProperty("runtime_properties")
    private Map<String, Object> runtimeProperties = null;
    
    @JsonProperty("scaling_groups")
    private List<ScalingGroupIdentifier> scalingGroups;
    
    @JsonProperty("state")
    private String state;

    @JsonProperty("tenant_name")
    private String tenantName;

    @JsonProperty("version")
    private String version;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public List<Object> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<Object> relationships) {
		this.relationships = relationships;
	}

	public Map<String, Object> getRuntimeProperties() {
		return runtimeProperties;
	}

	public void setRuntimeProperties(Map<String, Object> runtimeProperties) {
		this.runtimeProperties = runtimeProperties;
	}

	public List<ScalingGroupIdentifier> getScalingGroups() {
		return scalingGroups;
	}

	public void setScalingGroups(List<ScalingGroupIdentifier> scalingGroups) {
		this.scalingGroups = scalingGroups;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/*
	 * Nested structure representing scaling groups in which this node is a member
	 */
	public static final class ScalingGroupIdentifier
	{
		@JsonProperty("name")
		private String name;
		
		@JsonProperty("id")
		private String id;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		public String toString() {
			return "Scaling Group{ name=" + name + ", id=" + id + "}";
		}
	}
	
	@Override
    public String toString() {
        return "Deployment{" +
                "id='" + id + '\'' +
                "nodeId='" + nodeId + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", state=" + state +
                ", deploymentId=" + deploymentId +
                ", hostId='" + hostId + '\'' +
                ", version='" + version + '\'' +
                ", relationships=" + relationships +
                ", runtimeProperties=" + runtimeProperties +
                ", scalingGroups=" + scalingGroups +
                '}';
    }

	// TODO:  Need an object structure for Relationships
}
