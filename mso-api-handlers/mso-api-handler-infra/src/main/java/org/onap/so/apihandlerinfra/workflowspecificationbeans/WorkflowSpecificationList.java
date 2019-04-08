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
@JsonPropertyOrder({"workflowSpecification"})
public class WorkflowSpecificationList {

    @JsonProperty("workflowSpecification")
    private WorkflowSpecification workflowSpecification;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowSpecificationList() {}

    /**
     * 
     * @param workflowSpecification
     */
    public WorkflowSpecificationList(WorkflowSpecification workflowSpecification) {
        super();
        this.workflowSpecification = workflowSpecification;
    }

    @JsonProperty("workflowSpecification")
    public WorkflowSpecification getWorkflowSpecification() {
        return workflowSpecification;
    }

    @JsonProperty("workflowSpecification")
    public void setWorkflowSpecification(WorkflowSpecification workflowSpecification) {
        this.workflowSpecification = workflowSpecification;
    }

    public WorkflowSpecificationList withWorkflowSpecification(WorkflowSpecification workflowSpecification) {
        this.workflowSpecification = workflowSpecification;
        return this;
    }

}
