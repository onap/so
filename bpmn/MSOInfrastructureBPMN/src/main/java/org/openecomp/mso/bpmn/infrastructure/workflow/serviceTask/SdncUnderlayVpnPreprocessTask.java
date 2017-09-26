package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.core.BaseTask;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class SdncUnderlayVpnPreprocessTask extends BaseTask {
    public static final String RESOURCE_OPER_TYPE = "resourceOperType";
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();

    @Override
    public void execute(DelegateExecution execution) {
        String operType = getOperType(execution);
        execution.setVariable(RESOURCE_OPER_TYPE, operType);
    }

    private String getOperType(DelegateExecution execution) {
        String serviceId = (String) execution.getVariable("serviceId");
        String operationId = (String) execution.getVariable("operationId");
        String resourceTemplateUUID = (String) execution.getVariable("resourceTemplateUUID");
        ResourceOperationStatus resourceOperationStatus = requestsDB.getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
        return resourceOperationStatus.getOperType();
    }
}
