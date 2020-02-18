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
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.db.request.beans.OperationStatusId;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OpticalServiceEnd;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OpticalServiceInput;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OpticalServicePayload;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcOpticalServiceCreateInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcOpticalServiceCreateOutputEntity;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SdncOpticalServiceCreate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SdncOpticalServiceCreate.class);

    private static final String URL = "/restconf/operations/opticalservice:optical-service-create";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        RpcOpticalServiceCreateInputEntity inputEntity;
        ObjectMapper mapper = new ObjectMapper();
        String aServiceID = (String) execution.getVariable("serviceInstanceId");
        String aOperationID = (String) execution.getVariable("operationId");
        String dServiceID = (String) execution.getVariable("domainServiceInstanceId");
        String dOperationID = (String) execution.getVariable("domainOperationId");
        Boolean flag = true;
        OperationStatusId aId = new OperationStatusId(aServiceID, aOperationID);
        OperationStatusId dId = new OperationStatusId(dServiceID, dOperationID);
        updateProgress(execution, dId, RequestsDbConstant.Status.PROCESSING, "10", "execute begin!");
        try {
            inputEntity = buildOpticalServiceInput(execution);
            String defaulAuth = HeaderUtil.DefaulAuth;
            String output = send2SdncDirectly(execution, defaulAuth, inputEntity, dId);
            logger.info("SDNC OUTPUT {}", output);
            if (output == null) {
                execution.setVariable("Optical_Service_Status", "FAILURE");
                updateProgress(execution, dId, RequestsDbConstant.Status.ERROR, "0", "FAILURE");
            } else {
                updateProgress(execution, aId, RequestsDbConstant.Status.PROCESSING, "30", "creating");
                RpcOpticalServiceCreateOutputEntity outputEntity =
                        mapper.readValue(output, RpcOpticalServiceCreateOutputEntity.class);
                logger.info("Optical Service create output {}", outputEntity);
                String respCode = processOutput(execution, outputEntity);
                if (!respCode.equals("200")) {
                    execution.setVariable("Optical_Service_Status", "FAILURE");
                    updateProgress(execution, dId, RequestsDbConstant.Status.ERROR, "0", "FAILURE");
                } else {
                    execution.setVariable("Optical_Service_Status", "SUCCESS");
                    updateProgress(execution, dId, RequestsDbConstant.Status.PROCESSING, "80", "processing");
                    updateProgress(execution, aId, RequestsDbConstant.Status.PROCESSING, "50", "processing");
                }
            }
        } catch (Exception e) {
            logger.error("exception: SdncOpticalServiceCreate.fail!:", e);
            logger.error(Arrays.toString(e.getStackTrace()));
            execution.setVariable("Optical_Service_Status", "FAILURE");
            updateProgress(execution, dId, RequestsDbConstant.Status.ERROR, "0", "FAILURE");
            updateProgress(execution, aId, RequestsDbConstant.Status.ERROR, "0", "FAILURE");
        }
    }

    private String send2SdncDirectly(DelegateExecution execution, String defaulAuth,
            RpcOpticalServiceCreateInputEntity inputEntity, OperationStatusId dId) throws RouteException {
        updateProgress(execution, dId, RequestsDbConstant.Status.PROCESSING, "30", "creating");
        String url = "http://" + getSdncIp() + ":" + getSdncPort() + URL;
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", defaulAuth);
        httpPost.addHeader("Content-type", "application/json");
        String postBody = getPostbody(inputEntity);
        logger.info(LoggingAnchor.THREE, MessageEnum.RA_SEND_REQUEST_SDNC, postBody, "SDNC");
        httpPost.setEntity(new StringEntity(postBody, ContentType.APPLICATION_XML));
        String result = httpPost(url, httpPost);
        updateProgress(execution, dId, RequestsDbConstant.Status.PROCESSING, "50", "processing");
        return result;
    }

    private RpcOpticalServiceCreateInputEntity buildOpticalServiceInput(DelegateExecution execution) throws Exception {

        RpcOpticalServiceCreateInputEntity inputEntity = new RpcOpticalServiceCreateInputEntity();
        OpticalServiceInput input = new OpticalServiceInput();
        String requestId = (String) execution.getVariable("domainOperationId");
        String serviceId = (String) execution.getVariable("domainServiceInstanceId");
        String serviceType = (String) execution.getVariable("serviceType");
        String notifUrl = "http://so-bpmn-infra.onap:8081/mso/async/services/updateStatus";
        String globalCustomerId = (String) execution.getVariable("globalCustomerId");
        input.setRequestId(requestId);
        input.setServiceId(serviceId);
        input.setSource("SO");
        input.setServiceType(serviceType);
        input.setNotificationUrl(notifUrl);
        input.setCustId(globalCustomerId);
        logger.info("Sdnc Input: " + input.toString());
        OpticalServicePayload opticalServicePayload = new OpticalServicePayload();
        String codingFunc = (String) execution.getVariable("codingFunc");
        String domainType = (String) execution.getVariable("domainType");
        String serviceName = (String) execution.getVariable("domainServiceInstanceName");
        String serviceRate = (String) execution.getVariable("serviceRate");
        String serviceLayer = (String) execution.getVariable("serviceLayer");
        String protocol = (String) execution.getVariable("protocol");
        String aEndPortId = (String) execution.getVariable("aEndPortId");
        String aEndPortName = (String) execution.getVariable("aEndPortName");
        String zEndPortId = (String) execution.getVariable("zEndPortId");
        String zEndPortName = (String) execution.getVariable("zEndPortName");

        opticalServicePayload.setCodingFunc(codingFunc);
        opticalServicePayload.setDomainType(domainType);
        opticalServicePayload.setServiceName(serviceName);
        opticalServicePayload.setServiceRate(serviceRate);
        opticalServicePayload.setProtocol(protocol);
        opticalServicePayload.setServiceLayer(serviceLayer);
        logger.info("Sdnc Payload: " + opticalServicePayload.toString());

        OpticalServiceEnd serviceAEnd = new OpticalServiceEnd();
        serviceAEnd.setPortId(aEndPortId);
        serviceAEnd.setPortName(aEndPortName);

        OpticalServiceEnd serviceZEnd = new OpticalServiceEnd();
        serviceZEnd.setPortId(zEndPortId);
        serviceZEnd.setPortName(zEndPortName);
        opticalServicePayload.setServiceAEnd(serviceAEnd);
        opticalServicePayload.setServiceZEnd(serviceZEnd);

        input.setPayload(opticalServicePayload);
        inputEntity.setInput(input);

        return inputEntity;
    }

    public void updateProgress(DelegateExecution execution, OperationStatusId id, String reason, String progress,
            String statusDescription) throws RouteException {

        String operationContent = "Execute service creation";
        String requestdbEndPoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint", execution);
        HttpPost httpPost = new HttpPost(requestdbEndPoint);
        httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk");
        httpPost.addHeader("Content-type", "application/soap+xml");
        String serviceId = id.getServiceId();
        String operationId = id.getOperationId();
        String userId = "";

        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
                + "xmlns:ns=\"http://org.onap.so/requestsdb\">\n" + "<soapenv:Header/>\n" + "<soapenv:Body>\n"
                + "<ns:updateServiceOperationStatus xmlns:ns=\"http://org.onap.so/requestsdb\">\n" + "<serviceId>"
                + serviceId + "</serviceId>\n" + "<operationId>" + operationId + "</operationId>\n"
                + "<operationType>CREATE</operationType>\n" + "<userId>" + userId + "</userId>\n" + "<result>"
                + statusDescription + "</result>\n" + "<operationContent>" + operationContent + "</operationContent>\n"
                + "<progress>" + progress + "</progress>\n" + "<reason>" + reason + "</reason>\n"
                + "</ns:updateServiceOperationStatus>\n" + "</soapenv:Body>\n" + "</soapenv:Envelope>";
        httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
        httpPost(requestdbEndPoint, httpPost);
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

    protected String getSdncIp() {
        String sdncIp = null;
        sdncIp = UrnPropertiesReader.getVariable("sdnc-service-host");
        String returnIp = StringUtils.isBlank(sdncIp) || !isIp(sdncIp) ? null : sdncIp;
        logger.info("SdncOpticalServiceCreate.getSdncIp: sdncIp = {}", returnIp);
        return returnIp;
    }

    protected String getSdncPort() {
        String sdncPort = UrnPropertiesReader.getVariable("sdnc-service-port");
        String returnPort = StringUtils.isBlank(sdncPort) ? null : sdncPort;
        logger.info("SdncOpticalServiceCreate.getSdncPort: returnPort = {}", sdncPort);
        return returnPort;
    }

    protected boolean isIp(String msbIp) {
        return !StringUtils.isBlank(msbIp) && msbIp.split("\\.").length == 4;
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

    private String processOutput(DelegateExecution execution,
            RpcOpticalServiceCreateOutputEntity rpcOpticalServiceCreateOutputEntity) {

        if (rpcOpticalServiceCreateOutputEntity != null) {
            String responseCode = rpcOpticalServiceCreateOutputEntity.getOutput().getResponseCode();
            if (!responseCode.equals("200")) {
                execution.setVariable("Optical_Service_Status", "FAILURE");
                execution.setVariable("Async_Required", "false");
            } else {
                execution.setVariable("Optical_Service_Status", "SUCCESS");
                String ackFinal = rpcOpticalServiceCreateOutputEntity.getOutput().getAckFinalIndicator();
                if (ackFinal != null && ackFinal.equals("Y")) {
                    execution.setVariable("Async_Required", "true");
                } else {
                    execution.setVariable("Async_Required", "false");
                }
            }
            return responseCode;
        } else {
            logger.info("Rpc Optical Service output is empty");
            return "500";
        }

    }

}
