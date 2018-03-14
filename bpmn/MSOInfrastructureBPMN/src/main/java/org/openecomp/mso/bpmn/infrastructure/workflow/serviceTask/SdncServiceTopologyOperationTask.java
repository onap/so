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


import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.ServiceRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcServiceTopologyOperationInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcServiceTopologyOperationOutputEntity;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class SdncServiceTopologyOperationTask extends AbstractSdncOperationTask {
    private static final Logger sdncLogger = LoggerFactory.getLogger(SdncServiceTopologyOperationTask.class);


    private static final String URL = "/restconf/operations/GENERIC-RESOURCE-API:service-topology-operation";

    @Override
    public void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                 Map<String, String> inputs,
                                                 GenericResourceApi genericResourceApiClient) throws Exception {
        sdncLogger.info("SdncServiceTopologyOperationTask.sendRestrequestAndHandleResponse begin!");
        updateProgress(execution, null, null, "40", "sendRestrequestAndHandleResponse begin!");
        ServiceRpcInputEntityBuilder builder = new ServiceRpcInputEntityBuilder();
        RpcServiceTopologyOperationInputEntity inputEntity = builder.build(execution, inputs);
        updateProgress(execution, null, null, "50", "RequestBody build finished!");
        RpcServiceTopologyOperationOutputEntity outputEntity;
        if (!isSend2SdncDirectly()) {
            outputEntity = genericResourceApiClient.postServiceTopologyOperation
                    (HeaderUtil.DefaulAuth, inputEntity).execute().body();
        updateProgress(execution, null, null, "90", "sendRestrequestAndHandleResponse finished!");
        saveOutput(execution, outputEntity);
        } else {
            send2SdncDirectly(HeaderUtil.DefaulAuth, inputEntity);
        }
        sdncLogger.info("SdncServiceTopologyOperationTask.sendRestrequestAndHandleResponse end!");

    }

    private void send2SdncDirectly(String defaulAuth,
                                   RpcServiceTopologyOperationInputEntity inputEntity) throws RouteException {
        sdncLogger.info("SdncServiceTopologyOperationTask.send2SdncDirectly begin!");
        String url = "http://" + getSdncIp() + ":" + getSdncPort() + URL;
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", defaulAuth);
        httpPost.addHeader("Content-type", "application/json");
        String postBody = getPostbody(inputEntity);
        msoLogger.info(MessageEnum.RA_SEND_REQUEST_SDNC, postBody, "SDNC", "");
        httpPost.setEntity(new StringEntity(postBody, ContentType.APPLICATION_XML));
        httpPost(url, httpPost);
        sdncLogger.info("SdncServiceTopologyOperationTask.send2SdncDirectly end!");
    }

    private void saveOutput(DelegateExecution execution, RpcServiceTopologyOperationOutputEntity output) throws Exception {
        sdncLogger.info("SdncServiceTopologyOperationTask.saveOutput begin!");
        String responseCode = output.getOutput().getResponseCode();
        if (!"200".equals(responseCode)) {
            String processKey = getProcessKey(execution);
            int errorCode = Integer.parseInt(responseCode);
            String errorMessage = output.getOutput().getResponseMessage();
            WorkflowException workflowException = new WorkflowException(processKey, errorCode, errorMessage);
            execution.setVariable("SDNCA_SuccessIndicator", workflowException);
            updateProgress(execution, RequestsDbConstant.Status.ERROR, String.valueOf(errorCode), null, errorMessage);
            sdncLogger.info("exception: SdncServiceTopologyOperationTask.saveOutput fail!");
            throw new RouteException();
        }
        sdncLogger.info("SdncServiceTopologyOperationTask.saveOutput end!");
    }
}
