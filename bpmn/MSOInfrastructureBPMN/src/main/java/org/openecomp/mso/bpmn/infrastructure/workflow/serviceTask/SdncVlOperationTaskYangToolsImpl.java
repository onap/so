package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.NetworkTopologyOperationOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkTopologyRequestBodyBuilder;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.yangDecoder.transform.api.ITransformJava2StringService;
import org.openecomp.mso.yangDecoder.transform.impl.TransfromJava2StringFactory;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class SdncVlOperationTaskYangToolsImpl extends AbstractSdncVlOperationTask {

    public void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                 Map<String, String> inputs,
                                                 GenericResourceApi genericResourceApiClient) throws Exception {
        updateProgress(execution, null, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkTopologyRequestBodyBuilder builder = new NetworkTopologyRequestBodyBuilder();
        RequestBody body = builder.build(inputs);
        updateProgress(execution, null, null, "50", "RequestBody build finished!");
        ResponseBody responseBody = genericResourceApiClient.postNetworkTopologyPeration(body).execute().body();
        updateProgress(execution, null, null, "90", "sendRestrequestAndHandleResponse finished!");
        saveResponse(execution, responseBody);
    }

    private void saveResponse(DelegateExecution execution, ResponseBody responseBody) throws Exception {
        ITransformJava2StringService java2jsonService = TransfromJava2StringFactory.getJava2jsonService();
        NetworkTopologyOperationOutput output = (NetworkTopologyOperationOutput) java2jsonService.
                transformRpcDataObjectFromString(NetworkTopologyRequestBodyBuilder.URI_PATH, responseBody.string());
        String responseCode = output.getResponseCode();
        if (!responseCode.equals("200")) {
            String processKey = getProcessKey(execution);
            int errorCode = Integer.valueOf(responseCode);
            String errorMessage = output.getResponseMessage();
            WorkflowException workflowException = new WorkflowException(processKey, errorCode, errorMessage);
            execution.setVariable("SDNCA_SuccessIndicator", workflowException);
            updateProgress(execution, RequestsDbConstant.Status.ERROR, String.valueOf(errorCode), null, errorMessage);
            throw new Exception("");
        }
    }
}
