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

package org.onap.so.cloudify.v3.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
// @JsonRootName("deployment")
public class Deployment implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("blueprint_id")
    private String blueprintId;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("description")
    private String description;

    @JsonProperty("groups")
    private Map<String, Group> groups = null;

    @JsonProperty("id")
    private String id;

    @JsonProperty("inputs")
    private Map<String, Object> inputs = null;

    // TODO: Expand the definition of a PolicyTrigger
    @JsonProperty("policy_triggers")
    private List<Object> policyTriggers;

    // TODO: Expand the definition of a PolicyType
    @JsonProperty("policy_types")
    private List<Object> policyTypes;

    @JsonProperty("scaling_groups")
    private Map<String, ScalingGroup> scalingGroups = null;

    @JsonProperty("tenant_name")
    private String tenantName;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("workflows")
    private List<Workflow> workflows;

    public List<Object> getPolicyTriggers() {
        return policyTriggers;
    }

    public void setPolicyTriggers(List<Object> policyTriggers) {
        this.policyTriggers = policyTriggers;
    }

    public List<Object> getPolicyTypes() {
        return policyTypes;
    }

    public void setPolicyTypes(List<Object> policyTypes) {
        this.policyTypes = policyTypes;
    }

    public String getBlueprintId() {
        return blueprintId;
    }

    public void setBlueprintId(String blueprintId) {
        this.blueprintId = blueprintId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Group> getGroups() {
        return this.groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getInputs() {
        return this.inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Map<String, ScalingGroup> getScalingGroups() {
        return scalingGroups;
    }

    public void setScalingGroups(Map<String, ScalingGroup> scalingGroups) {
        this.scalingGroups = scalingGroups;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }

    /*
     * Nested subclasses for Group definitions
     */
    public static final class Group {
        @JsonProperty("policies")
        Object policies;

        @JsonProperty("members")
        List<String> members;

        public Object getPolicies() {
            return policies;
        }

        public void setPolicies(Object policies) {
            this.policies = policies;
        }

        public List<String> getMembers() {
            return members;
        }

        public void setMembers(List<String> members) {
            this.members = members;
        }
    }

    /*
     * Nested subclasses for Scaling Group definitions
     */
    public static final class ScalingGroup {
        @JsonProperty("properties")
        ScalingGroupProperties properties;

        @JsonProperty("members")
        List<String> members;

        public ScalingGroupProperties getProperties() {
            return properties;
        }

        public void setProperties(ScalingGroupProperties properties) {
            this.properties = properties;
        }

        public List<String> getMembers() {
            return members;
        }

        public void setMembers(List<String> members) {
            this.members = members;
        }
    }

    public static final class ScalingGroupProperties {
        @JsonProperty("current_instances")
        int currentInstances;

        @JsonProperty("default_instances")
        int defaultInstances;

        @JsonProperty("max_instances")
        int maxInstances;

        @JsonProperty("min_instances")
        int minInstances;

        @JsonProperty("planned_instances")
        int plannedInstances;

        public int getCurrentInstances() {
            return currentInstances;
        }

        public void setCurrentInstances(int currentInstances) {
            this.currentInstances = currentInstances;
        }

        public int getDefaultInstances() {
            return defaultInstances;
        }

        public void setDefaultInstances(int defaultInstances) {
            this.defaultInstances = defaultInstances;
        }

        public int getMaxInstances() {
            return maxInstances;
        }

        public void setMaxInstances(int maxInstances) {
            this.maxInstances = maxInstances;
        }

        public int getMinInstances() {
            return minInstances;
        }

        public void setMinInstances(int minInstances) {
            this.minInstances = minInstances;
        }

        public int getPlannedInstances() {
            return plannedInstances;
        }

        public void setPlannedInstances(int plannedInstances) {
            this.plannedInstances = plannedInstances;
        }
    }

    /*
     * Nested subclass for Deployment Workflow entities. Note that Blueprint class also contains a slightly different
     * Workflow structure.
     */
    public static final class Workflow {
        @JsonProperty("name")
        private String name;
        @JsonProperty("created_at")
        private Date createdAt;
        @JsonProperty("parameters")
        private Map<String, ParameterDefinition> parameters;

        public Workflow() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Map<String, ParameterDefinition> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, ParameterDefinition> parameters) {
            this.parameters = parameters;
        }
    }

    /*
     * Return an output as a Json-mapped Object of the provided type. This is useful for json-object outputs.
     */
    public <T> T getMapValue(Map<String, Object> map, String key, Class<T> type) {

        ObjectMapper mapper = new ObjectMapper();
        if (map.containsKey(key)) {
            try {
                String s = mapper.writeValueAsString(map.get(key));
                return (mapper.readValue(s, type));
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Deployment{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", blueprintId='"
                + blueprintId + '\'' + ", createdBy='" + createdBy + '\'' + ", tenantName='" + tenantName + '\''
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", inputs='" + inputs + '\''
                + ", workflows=" + workflows + ", groups=" + groups + '}';
    }

}
