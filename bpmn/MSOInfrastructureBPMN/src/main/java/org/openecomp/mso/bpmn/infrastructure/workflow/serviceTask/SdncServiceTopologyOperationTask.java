package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.ServiceRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcServiceTopologyOperationInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcServiceTopologyOperationOutputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.ServiceTopologyOperationOutputEntity;
import org.openecomp.mso.requestsdb.RequestsDbConstant;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class SdncServiceTopologyOperationTask extends AbstractSdncOperationTask {
    @Override
    public void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                 Map<String, String> inputs,
                                                 GenericResourceApi genericResourceApiClient) throws Exception {
        ServiceRpcInputEntityBuilder builder = new ServiceRpcInputEntityBuilder();
        RpcServiceTopologyOperationInputEntity inputEntity = builder.build(execution, inputs);
        RpcServiceTopologyOperationOutputEntity outputEntity = genericResourceApiClient.postServiceTopologyOperation(inputEntity).execute().body();
        saveOutput(execution, outputEntity);
    }

    private void saveOutput(DelegateExecution execution, RpcServiceTopologyOperationOutputEntity output) throws Exception {
        String responseCode = output.getOutput().getResponseCode();
        if (!responseCode.equals("200")) {
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
