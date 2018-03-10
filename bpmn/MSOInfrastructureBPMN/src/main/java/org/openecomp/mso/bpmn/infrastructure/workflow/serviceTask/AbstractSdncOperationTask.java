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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.onap.msb.sdk.discovery.common.RouteException;
import org.onap.msb.sdk.httpclient.RestServiceCreater;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.openecomp.mso.bpmn.core.BaseTask;
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractSdncOperationTask extends BaseTask {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSdncOperationTask.class);

    private static final String DEFAULT_MSB_IP = "127.0.0.1";
    private static final int DEFAULT_MSB_PORT = 80;
    private static final String SDCADAPTOR_INPUTS = "resourceParameters";
    private static final String TOPOLOGY_PROPERTIES = "topology.properties";
    public static final String ONAP_IP = "ONAP_IP";

    private static final String POST_BODY_TEMPLATE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://org.openecomp.mso/requestsdb\"><soapenv:Header/><soapenv:Body>\n"+
            "     <ns:updateResourceOperationStatus>\n"+
            "                <errorCode>$errorCode</errorCode>\n"+
            "                <jobId>$jobId</jobId>\n"+
            "                <operType>$operType</operType>\n"+
            "                <operationId>$operationId</operationId>\n"+
            "                <progress>$progress</progress>\n"+
            "                <resourceTemplateUUID>$resourceTemplateUUID</resourceTemplateUUID>\n"+
            "                <serviceId>$serviceId</serviceId>\n"+
            "                <status>$status</status>\n"+
            "                <statusDescription>$statusDescription</statusDescription>\n"+
            "     </ns:updateResourceOperationStatus></soapenv:Body></soapenv:Envelope>";

    private static final String GET_BODY_TEMPLATE = " <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://org.openecomp.mso/requestsdb\"><soapenv:Header/><soapenv:Body>\n" +
            "     <ns:getResourceOperationStatus>\n" +
            "                <operationId>$operationId</operationId>\n" +
            "                <resourceTemplateUUID>$resourceTemplateUUID</resourceTemplateUUID>\n" +
            "                <serviceId>$serviceId</serviceId>\n" +
            "     </ns:getResourceOperationStatus></soapenv:Body></soapenv:Envelope>";


    private void updateResOperStatus(ResourceOperationStatus resourceOperationStatus) throws RouteException {
        logger.info("AbstractSdncOperationTask.updateResOperStatus begin!");
        String url = "http://mso:8080/dbadapters/RequestsDbAdapter";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", "Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
        httpPost.addHeader("Content-type", "application/soap+xml");
        String postBody = getPostStringBody(resourceOperationStatus);
        httpPost.setEntity(new StringEntity(postBody, ContentType.APPLICATION_XML));
        httpPost(url, httpPost);
        logger.info("AbstractSdncOperationTask.updateResOperStatus end!");
    }

    protected String getPostbody(Object inputEntity) {
        ObjectMapper objectMapper = new ObjectMapper();
        String postBody = null;
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            postBody = objectMapper.writeValueAsString(inputEntity);
        } catch (JsonProcessingException e) {
            logger.error(e.getStackTrace().toString());
        }
        return postBody;
    }

    protected String httpPost(String url, HttpPost httpPost) throws RouteException {
        logger.info("AbstractSdncOperationTask.httpPost begin!");
        String result = null;

        String errorMsg;
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost);
            result = EntityUtils.toString(closeableHttpResponse.getEntity());
            logger.info("result = {}", result);
            if(closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                logger.info("exception: fail for status code = {}", closeableHttpResponse.getStatusLine().getStatusCode());
                throw new RouteException(result, "SERVICE_GET_ERR");
            }

            closeableHttpResponse.close();
        } catch (IOException e) {
            errorMsg = url + ":httpPostWithJSON connect faild";
            logger.info("exception: POST_CONNECT_FAILD : {}", errorMsg);
            throwsRouteException(errorMsg, e, "POST_CONNECT_FAILD");
        }

        logger.info("AbstractSdncOperationTask.httpPost end!");
        return result;
    }

    private static void throwsRouteException(String errorMsg, Exception e, String errorCode) throws RouteException {
        String msg = errorMsg + ".errorMsg:" + e.getMessage();
        logger.info("exception: {}", msg);
        throw new RouteException(errorMsg, errorCode);
    }

    private String getPostStringBody(ResourceOperationStatus resourceOperationStatus) {
        logger.info("AbstractSdncOperationTask.getPostStringBody begin!");
        String postBody = POST_BODY_TEMPLATE;
        postBody = postBody.replace("$errorCode", resourceOperationStatus.getErrorCode());
        postBody = postBody.replace("$jobId", resourceOperationStatus.getJobId());
        postBody = postBody.replace("$operType", resourceOperationStatus.getOperType());
        postBody = postBody.replace("$operationId", resourceOperationStatus.getOperationId());
        postBody = postBody.replace("$progress", resourceOperationStatus.getProgress());
        postBody = postBody.replace("$resourceTemplateUUID", resourceOperationStatus.getResourceTemplateUUID());
        postBody = postBody.replace("$serviceId", resourceOperationStatus.getServiceId());
        postBody = postBody.replace("$status", resourceOperationStatus.getStatus());
        postBody = postBody.replace("$statusDescription", resourceOperationStatus.getStatusDescription());
        logger.info("AbstractSdncOperationTask.getPostStringBody end!");
        return postBody;
    }

    private String getGetStringBody(String serviceId, String operationId, String resourceTemplateUUID) {
        logger.info("AbstractSdncOperationTask.getGetStringBody begin!");
        String getBody = GET_BODY_TEMPLATE;
        getBody = getBody.replace("$operationId", operationId);
        getBody = getBody.replace("$resourceTemplateUUID", resourceTemplateUUID);
        getBody = getBody.replace("$serviceId", serviceId);
        logger.info("AbstractSdncOperationTask.getGetStringBody end!");
        return getBody;
    }

    private ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID) throws RouteException {
        logger.info("AbstractSdncOperationTask.getResourceOperationStatus begin!");
        String url = "http://mso:8080/dbadapters/RequestsDbAdapter";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", "Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
        httpPost.addHeader("Content-type", "application/soap+xml");
        String getBody = getGetStringBody(serviceId, operationId, resourceTemplateUUID);
        httpPost.setEntity(new StringEntity(getBody, ContentType.APPLICATION_XML));
        String result = httpPost(url, httpPost);
        ResourceOperationStatus resourceOperationStatus = getResourceOperationStatusFromXmlString(result);
        logger.info("AbstractSdncOperationTask.getResourceOperationStatus end!");
        return resourceOperationStatus;
    }

    private ResourceOperationStatus getResourceOperationStatusFromXmlString(String result) {
        logger.info("AbstractSdncOperationTask.getResourceOperationStatusFromXmlString begin!");
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus();
        resourceOperationStatus.setErrorCode(getValueByName("errorCode", result));
        resourceOperationStatus.setJobId(getValueByName("jobId", result));
        resourceOperationStatus.setOperType(getValueByName("operType", result));
        resourceOperationStatus.setOperationId(getValueByName("operationId", result));
        resourceOperationStatus.setProgress(getValueByName("progress", result));
        resourceOperationStatus.setResourceTemplateUUID(getValueByName("resourceTemplateUUID", result));
        resourceOperationStatus.setServiceId(getValueByName("serviceId", result));
        resourceOperationStatus.setStatus(getValueByName("status", result));
        resourceOperationStatus.setStatusDescription(getValueByName("statusDescription", result));
        logger.info("AbstractSdncOperationTask.getResourceOperationStatusFromXmlString end!");
        return resourceOperationStatus;
    }

    private String getValueByName(String name, String xml) {
        if (!StringUtils.isBlank(xml) && xml.contains(name)) {
            String start = "<" + name + ">";
            String end = "</" + name + ">";
            return xml.substring(xml.indexOf(start), xml.indexOf(end)).replace(start, "");
        }
        return "";
    }

    protected static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("AbstractSdncOperationTask.execute begin!");
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient();
        try {
            Map<String, String> inputs = getInputs(execution);

            sendRestrequestAndHandleResponse(execution, inputs, genericResourceApiClient);
            execution.setVariable("SDNCA_SuccessIndicator", true);
        } catch (Exception e) {
            logger.info("exception: AbstractSdncOperationTask.fail!");
            logger.error("exception: AbstractSdncOperationTask.fail!:", e);
            logger.error(e.getStackTrace().toString());
            execution.setVariable("SDNCA_SuccessIndicator", false);
            updateProgress(execution, RequestsDbConstant.Status.ERROR, null, "100", "sendRestrequestAndHandleResponse finished!");

        }
        logger.info("AbstractSdncOperationTask.execute end!");
    }

    protected Map<String, String> getInputs(DelegateExecution execution) {
        logger.info("AbstractSdncOperationTask.getInputs begin!");
        Map<String, String> inputs = new HashMap<>();
        String json = (String) execution.getVariable(SDCADAPTOR_INPUTS);
        if (!StringUtils.isBlank(json)) {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject paras = jsonObject.getJSONObject("additionalParamForNs");
            Iterator<String> iterator = paras.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                inputs.put(key, paras.getString(key));
            }
        }
        logger.info("AbstractSdncOperationTask.getInputs end!");
        return inputs;
    }

    public abstract void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                          Map<String, String> inputs,
                                                          GenericResourceApi genericResourceApiClient) throws Exception;

    public void updateProgress(DelegateExecution execution,
                               String status,
                               String errorCode,
                               String progress,
                               String statusDescription) {
        logger.info("AbstractSdncOperationTask.updateProgress begin!");
        String serviceId = (String) execution.getVariable("serviceId");
        serviceId = StringUtils.isBlank(serviceId) ? (String) execution.getVariable("serviceInstanceId") : serviceId;
        String operationId = (String) execution.getVariable("operationId");
        String resourceTemplateUUID = (String) execution.getVariable("resourceUUID");
        resourceTemplateUUID = StringUtils.isBlank(resourceTemplateUUID) ? (String) execution.getVariable("resourceTemplateId") : resourceTemplateUUID;
        try {
            ResourceOperationStatus resourceOperationStatus = getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
            if (!StringUtils.isBlank(status)) {
                resourceOperationStatus.setStatus(status);
            }
            if (!StringUtils.isBlank(errorCode)) {
                resourceOperationStatus.setErrorCode(errorCode);
            }
            if (!StringUtils.isBlank(progress)) {
                resourceOperationStatus.setProgress(progress);
            }
            if (!StringUtils.isBlank(statusDescription)) {
                resourceOperationStatus.setStatusDescription(statusDescription);
            }
            updateResOperStatus(resourceOperationStatus);
            logger.info("AbstractSdncOperationTask.updateProgress end!");
        } catch (Exception exception) {
            logger.info("exception: AbstractSdncOperationTask.updateProgress fail!");
            logger.error("exception: AbstractSdncOperationTask.updateProgress fail:", exception);
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION, " updateProgress catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, exception.getClass().toString());
        }
    }


    protected boolean isSend2SdncDirectly() {
        logger.info("AbstractSdncOperationTask.isSend2SdncDirectly begin!");
        Map<String, String> properties = PropertyConfiguration.getInstance().getProperties(TOPOLOGY_PROPERTIES);
        if (properties != null) {
            String sdncIp = properties.get("sdnc-ip");
            String sdncPort = properties.get("sdnc-port");
            if (!StringUtils.isBlank(sdncIp) && isIp(sdncIp) && !StringUtils.isBlank(sdncPort)) {
                logger.info("AbstractSdncOperationTask.isSend2SdncDirectly = true.");
                return true;
            }
        }
        logger.info("AbstractSdncOperationTask.isSend2SdncDirectly = false.");
        return false;
    }

    protected String getSdncIp() {
        logger.info("AbstractSdncOperationTask.getSdncIp begin.");
        String sdncIp = null;
        Map<String, String> properties = PropertyConfiguration.getInstance().getProperties("topology.properties");
        if (properties != null) {
            sdncIp = properties.get("sdnc-ip");
        }
        String returnIp = StringUtils.isBlank(sdncIp) || !isIp(sdncIp) ? null : sdncIp;
        logger.info("AbstractSdncOperationTask.getSdncIp: sdncIp = {}", returnIp);
        return returnIp;
    }

    protected String getSdncPort() {
        logger.info("AbstractSdncOperationTask.getSdncPort begin.");
        String sdncPort = null;
        Map<String, String> properties = PropertyConfiguration.getInstance().getProperties("topology.properties");
        if (properties != null) {
            sdncPort = properties.get("sdnc-port");
        }
        String returnPort = StringUtils.isBlank(sdncPort) ? null : sdncPort;
        logger.info("AbstractSdncOperationTask.getSdncPort: returnPort = {}", sdncPort);
        return returnPort;
    }

    private GenericResourceApi getGenericResourceApiClient() {
        logger.info("AbstractSdncOperationTask.getGenericResourceApiClient begin!");
        String msbIp = System.getenv().get(ONAP_IP);
        int msbPort = DEFAULT_MSB_PORT;
        Map<String, String> properties = PropertyConfiguration.getInstance().getProperties("topology.properties");
        if (properties != null) {
            if (StringUtils.isBlank(msbIp) || !isIp(msbIp)) {
                msbIp = properties.get("msb-ip");
                if (StringUtils.isBlank(msbIp)) {
                    msbIp = getString(properties, "msb.address", DEFAULT_MSB_IP);
                }
            }
            String strMsbPort = properties.get("msb-port");
            if (StringUtils.isBlank(strMsbPort)) {
                strMsbPort = getString(properties, "msb.port", String.valueOf(DEFAULT_MSB_PORT));
            }
            msbPort = Integer.valueOf(strMsbPort);
        }
        logger.info("AbstractSdncOperationTask.getGenericResourceApiClient msbIp = " + msbIp + " msbPort = " + msbPort);
        MSBServiceClient msbClient = new MSBServiceClient(msbIp, msbPort);
        RestServiceCreater restServiceCreater = new RestServiceCreater(msbClient);
        logger.info("AbstractSdncOperationTask.getGenericResourceApiClient end!");
        return restServiceCreater.createService(GenericResourceApi.class);
    }

    protected boolean isIp(String msbIp) {
        return !StringUtils.isBlank(msbIp) && msbIp.split("\\.").length == 4;
    }

    private String getString(Map<String, String> properties, String name, String defaultValue) {
        String vlaue = properties.get(name);
        try {
            if (!StringUtils.isBlank(vlaue)) {
                return vlaue;
            }
        } catch (Exception e) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        }
        return defaultValue;
    }

    public String getProcessKey(DelegateExecution execution) {
        return execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }
}
