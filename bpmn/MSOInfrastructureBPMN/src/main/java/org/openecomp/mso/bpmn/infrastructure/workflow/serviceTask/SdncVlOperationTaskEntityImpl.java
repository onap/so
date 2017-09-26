package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRpcInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRpcOutputEntity;
import org.openecomp.mso.requestsdb.RequestsDbConstant;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class SdncVlOperationTaskEntityImpl extends AbstractSdncVlOperationTask {
    @Override
    public void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                 Map<String, String> inputs,
                                                 GenericResourceApi genericResourceApiClient) throws Exception {
        updateProgress(execution, null, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkRpcInputEntityBuilder builder = new NetworkRpcInputEntityBuilder();
        NetworkRpcInputEntity body = builder.build(inputs);
        updateProgress(execution, null, null, "50", "RequestBody build finished!");
        NetworkRpcOutputEntity networkRpcOutputEntiy = genericResourceApiClient.postNetworkTopologyPeration(body).execute().body();
        updateProgress(execution, null, null, "90", "sendRestrequestAndHandleResponse finished!");
        saveOutput(execution, networkRpcOutputEntiy);
    }

    private void saveOutput(DelegateExecution execution, NetworkRpcOutputEntity output) throws Exception {
        String responseCode = output.getOutput().getResponseCode();
        if (!"200".equals(responseCode)) {
            String processKey = getProcessKey(execution);
            int errorCode = Integer.valueOf(responseCode);
            String errorMessage = output.getOutput().getResponseMessage();
            WorkflowException workflowException = new WorkflowException(processKey, errorCode, errorMessage);
            execution.setVariable("SDNCA_SuccessIndicator", workflowException);
            updateProgress(execution, RequestsDbConstant.Status.ERROR, String.valueOf(errorCode), null, errorMessage);
            throw new Exception("");
        }
    }

}
