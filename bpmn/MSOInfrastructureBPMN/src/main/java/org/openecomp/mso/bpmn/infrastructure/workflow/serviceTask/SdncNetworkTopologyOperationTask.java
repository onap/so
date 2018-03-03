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

import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationOutputEntity;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class SdncNetworkTopologyOperationTask extends AbstractSdncOperationTask {
    private static final Logger logger = LoggerFactory.getLogger(SdncNetworkTopologyOperationTask.class);


    private static final String URL = "/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation";

    @Override
    public void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                 Map<String, String> inputs,
                                                 GenericResourceApi genericResourceApiClient) throws Exception {
        logger.info("SdncNetworkTopologyOperationTask.sendRestrequestAndHandleResponse begin!");
        updateProgress(execution, RequestsDbConstant.Status.PROCESSING, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkRpcInputEntityBuilder builder = new NetworkRpcInputEntityBuilder();
        RpcNetworkTopologyOperationInputEntity inputEntity = builder.build(execution, inputs);
        updateProgress(execution, RequestsDbConstant.Status.PROCESSING, null, "50", "RequestBody build finished!");
        RpcNetworkTopologyOperationOutputEntity outputEntity;
        if (!isSend2SdncDirectly()) {
            outputEntity = genericResourceApiClient.postNetworkTopologyOperation
                    (HeaderUtil.DefaulAuth, inputEntity).execute().body();
            updateProgress(execution, null, null, "90", "sendRestrequestAndHandleResponse finished!");
            saveOutput(execution, outputEntity);
        } else {
            Send2SdncDirectly(HeaderUtil.DefaulAuth, inputEntity);
        }
        updateProgress(execution, RequestsDbConstant.Status.FINISHED, null, RequestsDbConstant.Progress.ONE_HUNDRED, "execute finished!");
        logger.info("SdncNetworkTopologyOperationTask.sendRestrequestAndHandleResponse end!");
    }

    private void Send2SdncDirectly(String defaulAuth,
                                   RpcNetworkTopologyOperationInputEntity inputEntity) throws RouteException {
        logger.info("SdncNetworkTopologyOperationTask.Send2SdncDirectly begin!");
        String url = "http://" + getSdncIp() + ":" + getSdncPort() + URL;
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", defaulAuth);
        httpPost.addHeader("Content-type", "application/json");
        String postBody = getPostbody(inputEntity);
        LOGGER.info(MessageEnum.RA_SEND_REQUEST_SDNC, postBody, "SDNC", "");
        httpPost.setEntity(new StringEntity(postBody, ContentType.APPLICATION_XML));
        httpPost(url, httpPost);
        logger.info("SdncNetworkTopologyOperationTask.Send2SdncDirectly end!");
    }

    private void saveOutput(DelegateExecution execution, RpcNetworkTopologyOperationOutputEntity output) throws Exception {
        logger.info("SdncNetworkTopologyOperationTask.saveOutput begin!");
        String responseCode = output.getOutput().getResponseCode();
        if (!"200".equals(responseCode)) {
            String processKey = getProcessKey(execution);
            int errorCode = Integer.valueOf(responseCode);
            String errorMessage = output.getOutput().getResponseMessage();
            WorkflowException workflowException = new WorkflowException(processKey, errorCode, errorMessage);
            execution.setVariable("SDNCA_SuccessIndicator", workflowException);
            updateProgress(execution, RequestsDbConstant.Status.ERROR, String.valueOf(errorCode), "100", errorMessage);
            logger.info("exception: SdncNetworkTopologyOperationTask.saveOutput fail!");
            throw new Exception("");
        }
        
        logger.info("SdncNetworkTopologyOperationTask.saveOutput end!");
    }

}
