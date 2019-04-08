/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.common.workflow.service;

import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;

/**
 * Exception thrown when an error occurs while processing the workflow. This encapsulates the workflow response so that
 * the same can be sent back to api handler.
 */
public class WorkflowProcessorException extends RuntimeException {
    WorkflowResponse workflowResponse;

    public WorkflowProcessorException(WorkflowResponse workflowResponse) {
        this.workflowResponse = workflowResponse;
    }

    public WorkflowResponse getWorkflowResponse() {
        return workflowResponse;
    }
}
