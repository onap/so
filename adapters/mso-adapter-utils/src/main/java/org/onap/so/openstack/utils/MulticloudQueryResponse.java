/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.openstack.utils;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "template_type",
        "workload_id",
        "workload_status"
})
public class MulticloudQueryResponse implements Serializable {
    private final static long serialVersionUID = -5215028275577848311L;

    @JsonProperty("template_type")
    private String templateType;
    @JsonProperty("workload_id")
    private String workloadId;
    @JsonProperty("workload_status")
    private String workloadStatus;

    @JsonCreator
    public MulticloudQueryResponse(
            @JsonProperty("template_type") String templateType,
            @JsonProperty("workload_id") String workloadId,
            @JsonProperty("workload_status") String workloadStatus) {
        this.templateType = templateType;
        this.workloadId = workloadId;
        this.workloadStatus = workloadStatus;
    }

    @JsonProperty("template_type")
    public String getTemplateType() {
        return templateType;
    }

    @JsonProperty("template_type")
    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    @JsonProperty("workload_id")
    public String getWorkloadId() {
        return workloadId;
    }

    @JsonProperty("workload_id")
    public void setWorkloadId(String workloadId) {
        this.workloadId = workloadId;
    }

    @JsonProperty("workload_status")
    public String getWorkloadStatus() {
        return workloadStatus;
    }

    @JsonProperty("workload_status")
    public void setWorkloadStatus(String workloadStatus) {
        this.workloadStatus = workloadStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("templateType", templateType).append("workloadId", workloadId).append("workloadStatus", workloadStatus).toString();
    }
}
