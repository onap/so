/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.serviceTask;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.codehaus.groovy.runtime.ExceptionUtils;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcOpticalServiceDeleteInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OpticalServiceDeleteInput;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OpticalServiceDeletePayload;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcOpticalServiceCreateOutputEntity;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SdncOpticalServiceDelete implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SdncOpticalServiceDelete.class);

    private static final String URL = "/restconf/operations/opticalservice:optical-service-delete";
    ExceptionUtils exceptionUtils = new ExceptionUtils();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        RpcOpticalServiceDeleteInputEntity inputEntity;
        ObjectMapper mapper = new ObjectMapper();
        try {
            inputEntity = buildOpticalServiceDeleteInput(execution);
            String output = send2SdncDirectly(HeaderUtil.DefaulAuth, inputEntity);
            logger.info("SDNC OUTPUT {}", output);
            RpcOpticalServiceCreateOutputEntity outputEntity =
                    mapper.readValue(output, RpcOpticalServiceCreateOutputEntity.class);
            processOutput(execution, outputEntity);
        } catch (Exception e) {
            logger.error("exception: SdncOpticalServiceDelete.fail!:", e);
            logger.error(Arrays.toString(e.getStackTrace()));
            execution.setVariable("Optical_Service_DELETE_Status", "FAILURE");
        }
    }

    private RpcOpticalServiceDeleteInputEntity buildOpticalServiceDeleteInput(DelegateExecution execution)
            throws Exception {

        RpcOpticalServiceDeleteInputEntity inputEntity = new RpcOpticalServiceDeleteInputEntity();
        OpticalServiceDeleteInput deleteinput = new OpticalServiceDeleteInput();
        // Domain Service Operation Id is mapped to Request id for SDNC (Delete)
        String requestId = (String) execution.getVariable("dcOperationId");
        logger.debug("request id " + requestId);
        OpticalServiceDeletePayload deletePayload = new OpticalServiceDeletePayload();
        String serviceName = (String) execution.getVariable("domainServiceName");
        deletePayload.setServiceName(serviceName);
        deleteinput.setPayload(deletePayload);
        deleteinput.setRequestId(requestId);
        inputEntity.setInput(deleteinput);
        logger.debug("input entity get request id " + inputEntity.getInput().getRequestId());
        return inputEntity;
    }

    private String send2SdncDirectly(String defaulAuth, RpcOpticalServiceDeleteInputEntity inputEntity)
            throws RouteException {
        String url = "http://" + getSdncIp() + ":" + getSdncPort() + URL;
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", defaulAuth);
        httpPost.addHeader("Content-type", "application/json");
        String postBody = getPostbody(inputEntity);
        logger.info(LoggingAnchor.THREE, MessageEnum.RA_SEND_REQUEST_SDNC, postBody, "SDNC");
        httpPost.setEntity(new StringEntity(postBody, ContentType.APPLICATION_XML));
        String result = httpPost(url, httpPost);
        return result;
    }

    protected String getSdncIp() {
        String sdncIp = null;
        sdncIp = UrnPropertiesReader.getVariable("sdnc-service-host");
        String returnIp = StringUtils.isBlank(sdncIp) || !isIp(sdncIp) ? null : sdncIp;
        logger.info("SdncOpticalServiceDelete.getSdncIp: sdncIp = {}", returnIp);
        return returnIp;
    }

    protected String getSdncPort() {
        String sdncPort = UrnPropertiesReader.getVariable("sdnc-service-port");
        String returnPort = StringUtils.isBlank(sdncPort) ? null : sdncPort;
        logger.info("SdncOpticalServiceDelete.getSdncPort: returnPort = {}", sdncPort);
        return returnPort;
    }

    protected boolean isIp(String msbIp) {
        return !StringUtils.isBlank(msbIp) && msbIp.split("\\.").length == 4;
    }

    protected String httpPost(String url, HttpPost httpPost) throws RouteException {
        String result = null;
        String errorMsg;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost);
            result = EntityUtils.toString(closeableHttpResponse.getEntity());
            logger.info("result = {}", result);
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                logger.info("exception: fail for status code = {}",
                        closeableHttpResponse.getStatusLine().getStatusCode());
                throw new RouteException(result, "SERVICE_GET_ERR");
            }

            closeableHttpResponse.close();
        } catch (IOException e) {
            errorMsg = url + ":httpPostWithJSON connect faild";
            logger.info("exception: POST_CONNECT_FAILD : {}", errorMsg);
            throwsRouteException(errorMsg, e, "POST_CONNECT_FAILD");
        }
        return result;
    }

    protected String getPostbody(Object inputEntity) {
        ObjectMapper objectMapper = new ObjectMapper();
        String postBody = null;
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            postBody = objectMapper.writeValueAsString(inputEntity);
        } catch (JsonProcessingException e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        return postBody;
    }

    private static void throwsRouteException(String errorMsg, Exception e, String errorCode) throws RouteException {
        String msg = errorMsg + ".errorMsg:" + e.getMessage();
        logger.info("exception: {}", msg);
        throw new RouteException(errorMsg, errorCode);
    }

    private void processOutput(DelegateExecution execution,
            RpcOpticalServiceCreateOutputEntity rpcOpticalServiceCreateOutputEntity) {

        if (rpcOpticalServiceCreateOutputEntity != null) {
            String responseCode = rpcOpticalServiceCreateOutputEntity.getOutput().getResponseCode();
            if (!responseCode.equals("200")) {
                execution.setVariable("Optical_Service_DELETE_Status", "FAILURE");
            } else {
                execution.setVariable("Optical_Service_DELETE_Status", "SUCCESS");
            }
            String ackFinal = rpcOpticalServiceCreateOutputEntity.getOutput().getAckFinalIndicator();
            if (ackFinal.equals("Y")) {
                execution.setVariable("Async_Required", "false");
                execution.setVariable("isAllDomainServiceSuccess", "true");
            } else {
                execution.setVariable("Async_Required", "true");
            }

        } else {
            logger.info("Rpc Optical Service output is empty");
        }

    }

}
