/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.apihandlerinfra.workflowspecificationbeans;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"artifactInfo", "activitySequence", "workflowInputParameters"})
public class WorkflowSpecification {

    @JsonProperty("artifactInfo")
    private ArtifactInfo artifactInfo;
    @JsonProperty("activitySequence")
    private List<ActivitySequence> activitySequence = null;
    @JsonProperty("workflowInputParameters")
    private List<WorkflowInputParameter> workflowInputParameters = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowSpecification() {}

    /**
     * 
     * @param activitySequence
     * @param artifactInfo
     * @param workflowInputParameters
     */
    public WorkflowSpecification(ArtifactInfo artifactInfo, List<ActivitySequence> activitySequence,
            List<WorkflowInputParameter> workflowInputParameters) {
        super();
        this.artifactInfo = artifactInfo;
        this.activitySequence = activitySequence;
        this.workflowInputParameters = workflowInputParameters;
    }

    @JsonProperty("artifactInfo")
    public ArtifactInfo getArtifactInfo() {
        return artifactInfo;
    }

    @JsonProperty("artifactInfo")
    public void setArtifactInfo(ArtifactInfo artifactInfo) {
        this.artifactInfo = artifactInfo;
    }

    public WorkflowSpecification withArtifactInfo(ArtifactInfo artifactInfo) {
        this.artifactInfo = artifactInfo;
        return this;
    }

    @JsonProperty("activitySequence")
    public List<ActivitySequence> getActivitySequence() {
        return activitySequence;
    }

    @JsonProperty("activitySequence")
    public void setActivitySequence(List<ActivitySequence> activitySequence) {
        this.activitySequence = activitySequence;
    }

    public WorkflowSpecification withActivitySequence(List<ActivitySequence> activitySequence) {
        this.activitySequence = activitySequence;
        return this;
    }

    @JsonProperty("workflowInputParameters")
    public List<WorkflowInputParameter> getWorkflowInputParameters() {
        return workflowInputParameters;
    }

    @JsonProperty("workflowInputParameters")
    public void setWorkflowInputParameters(List<WorkflowInputParameter> workflowInputParameters) {
        this.workflowInputParameters = workflowInputParameters;
    }

    public WorkflowSpecification withWorkflowInputParameters(List<WorkflowInputParameter> workflowInputParameters) {
        this.workflowInputParameters = workflowInputParameters;
        return this;
    }

}
