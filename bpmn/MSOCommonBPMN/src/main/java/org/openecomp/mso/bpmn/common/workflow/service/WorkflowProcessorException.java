package org.openecomp.mso.bpmn.common.workflow.service;

import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;

/**
 * Exception thrown when an error occurs while processing the workflow.
 * This encapsulates the workflow response so that the same can be sent back to api handler.
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
