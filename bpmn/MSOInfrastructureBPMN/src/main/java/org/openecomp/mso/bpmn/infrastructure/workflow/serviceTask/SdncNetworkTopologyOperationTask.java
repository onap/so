/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationOutputEntity;
import org.openecomp.mso.requestsdb.RequestsDbConstant;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class SdncNetworkTopologyOperationTask extends AbstractSdncOperationTask {
    @Override
    public void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                 Map<String, String> inputs,
                                                 GenericResourceApi genericResourceApiClient) throws Exception {
        updateProgress(execution, null, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkRpcInputEntityBuilder builder = new NetworkRpcInputEntityBuilder();
        RpcNetworkTopologyOperationInputEntity inputEntity = builder.build(execution, inputs);
        updateProgress(execution, null, null, "50", "RequestBody build finished!");
        RpcNetworkTopologyOperationOutputEntity outputEntity = genericResourceApiClient.postNetworkTopologyOperation(inputEntity).execute().body();
        updateProgress(execution, null, null, "90", "sendRestrequestAndHandleResponse finished!");
        saveOutput(execution, outputEntity);
    }

    private void saveOutput(DelegateExecution execution, RpcNetworkTopologyOperationOutputEntity output) throws Exception {
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
        updateProgress(execution, RequestsDbConstant.Status.FINISHED, null, RequestsDbConstant.Progress.ONE_HUNDRED, "execute finished!");

    }

}
