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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"artifactType", "artifactUuid", "artifactName", "artifactVersion", "artifactDescription",
        "workflowName", "operationName", "workflowSource", "workflowResourceTarget"})
public class ArtifactInfo {

    @JsonProperty("artifactType")
    private String artifactType;
    @JsonProperty("artifactUuid")
    private String artifactUuid;
    @JsonProperty("artifactName")
    private String artifactName;
    @JsonProperty("artifactVersion")
    private String artifactVersion;
    @JsonProperty("artifactDescription")
    private String artifactDescription;
    @JsonProperty("workflowName")
    private String workflowName;
    @JsonProperty("operationName")
    private String operationName;
    @JsonProperty("workflowSource")
    private String workflowSource;
    @JsonProperty("workflowResourceTarget")
    private String workflowResourceTarget;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ArtifactInfo() {}

    /**
     * 
     * @param artifactName
     * @param workflowName
     * @param artifactType
     * @param operationName
     * @param artifactVersion
     * @param workflowResourceTarget
     * @param workflowSource
     * @param artifactUuid
     * @param artifactDescription
     */
    public ArtifactInfo(String artifactType, String artifactUuid, String artifactName, String artifactVersion,
            String artifactDescription, String workflowName, String operationName, String workflowSource,
            String workflowResourceTarget) {
        super();
        this.artifactType = artifactType;
        this.artifactUuid = artifactUuid;
        this.artifactName = artifactName;
        this.artifactVersion = artifactVersion;
        this.artifactDescription = artifactDescription;
        this.workflowName = workflowName;
        this.operationName = operationName;
        this.workflowSource = workflowSource;
        this.workflowResourceTarget = workflowResourceTarget;
    }

    @JsonProperty("artifactType")
    public String getArtifactType() {
        return artifactType;
    }

    @JsonProperty("artifactType")
    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public ArtifactInfo withArtifactType(String artifactType) {
        this.artifactType = artifactType;
        return this;
    }

    @JsonProperty("artifactUuid")
    public String getArtifactUuid() {
        return artifactUuid;
    }

    @JsonProperty("artifactUuid")
    public void setArtifactUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public ArtifactInfo withArtifactUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
        return this;
    }

    @JsonProperty("artifactName")
    public String getArtifactName() {
        return artifactName;
    }

    @JsonProperty("artifactName")
    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public ArtifactInfo withArtifactName(String artifactName) {
        this.artifactName = artifactName;
        return this;
    }

    @JsonProperty("artifactVersion")
    public String getArtifactVersion() {
        return artifactVersion;
    }

    @JsonProperty("artifactVersion")
    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public ArtifactInfo withArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
        return this;
    }

    @JsonProperty("artifactDescription")
    public String getArtifactDescription() {
        return artifactDescription;
    }

    @JsonProperty("artifactDescription")
    public void setArtifactDescription(String artifactDescription) {
        this.artifactDescription = artifactDescription;
    }

    public ArtifactInfo withArtifactDescription(String artifactDescription) {
        this.artifactDescription = artifactDescription;
        return this;
    }

    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public ArtifactInfo withWorkflowName(String workflowName) {
        this.workflowName = workflowName;
        return this;
    }

    @JsonProperty("operationName")
    public String getOperationName() {
        return operationName;
    }

    @JsonProperty("operationName")
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public ArtifactInfo withOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    @JsonProperty("workflowSource")
    public String getWorkflowSource() {
        return workflowSource;
    }

    @JsonProperty("workflowSource")
    public void setWorkflowSource(String workflowSource) {
        this.workflowSource = workflowSource;
    }

    public ArtifactInfo withWorkflowSource(String workflowSource) {
        this.workflowSource = workflowSource;
        return this;
    }

    @JsonProperty("workflowResourceTarget")
    public String getWorkflowResourceTarget() {
        return workflowResourceTarget;
    }

    @JsonProperty("workflowResourceTarget")
    public void setWorkflowResourceTarget(String workflowResourceTarget) {
        this.workflowResourceTarget = workflowResourceTarget;
    }

    public ArtifactInfo withWorkflowResourceTarget(String workflowResourceTarget) {
        this.workflowResourceTarget = workflowResourceTarget;
        return this;
    }

}
