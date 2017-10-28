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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 10112215 on 2017/9/16.
 */
public abstract class AbstractSdncOperationTask extends BaseTask {

    private static final String DEFAULT_MSB_IP = "127.0.0.1";
    private static final int DEFAULT_MSB_Port = 80;
    private static final String SDCADAPTOR_INPUTS = "resourceParameters";
    public static final String ONAP_IP = "ONAP_IP";
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();

    private static final String postBodyTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://org.openecomp.mso/requestsdb\"><soapenv:Header/><soapenv:Body>\n"+
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


    private void updateResOperStatus(ResourceOperationStatus resourceOperationStatus) throws RouteException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "http://mso:8080/dbadapters/RequestsDbAdapter";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", "Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
        httpPost.addHeader("Content-type", "application/soap+xml");
        String postBody = getStringBody(resourceOperationStatus);
        httpPost.setEntity(new StringEntity(postBody, ContentType.APPLICATION_XML));
        String result;
        boolean var15 = false;

        String errorMsg;
        label91: {
            try {
                var15 = true;
                CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost);
                result = EntityUtils.toString(closeableHttpResponse.getEntity());
                if(closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RouteException(result, "SERVICE_GET_ERR");
                }

                closeableHttpResponse.close();
                var15 = false;
                break label91;
            } catch (IOException var19) {
                errorMsg = url + ":httpPostWithJSON connect faild";
                throwsRouteException(errorMsg, var19, "POST_CONNECT_FAILD");
                var15 = false;
            } finally {
                if(var15) {
                    try {
                        httpClient.close();
                    } catch (IOException var16) {
                        String errorMsg1 = url + ":close  httpClient faild";
                        throwsRouteException(errorMsg1, var16, "CLOSE_CONNECT_FAILD");
                    }

                }
            }

            try {
                httpClient.close();
            } catch (IOException var17) {
                errorMsg = url + ":close  httpClient faild";
                throwsRouteException(errorMsg, var17, "CLOSE_CONNECT_FAILD");
            }
        }

        try {
            httpClient.close();
        } catch (IOException var18) {
            errorMsg = url + ":close  httpClient faild";
            throwsRouteException(errorMsg, var18, "CLOSE_CONNECT_FAILD");
        }

        //requestsDB.updateResOperStatus(resourceOperationStatus);
    }

    private static void throwsRouteException(String errorMsg, Exception e, String errorCode) throws RouteException {
        String msg = errorMsg + ".errorMsg:" + e.getMessage();
        throw new RouteException(errorMsg, errorCode);
    }

    private String getStringBody(ResourceOperationStatus resourceOperationStatus) {
        String postBody = new String(postBodyTemplate);
        postBody.replace("$errorCode", resourceOperationStatus.getErrorCode());
        postBody.replace("$jobId", resourceOperationStatus.getJobId());
        postBody.replace("$operType", resourceOperationStatus.getOperType());
        postBody.replace("$operationId", resourceOperationStatus.getOperationId());
        postBody.replace("$progress", resourceOperationStatus.getProgress());
        postBody.replace("$resourceTemplateUUID", resourceOperationStatus.getResourceTemplateUUID());
        postBody.replace("$serviceId", resourceOperationStatus.getServiceId());
        postBody.replace("$status", resourceOperationStatus.getStatus());
        postBody.replace("$statusDescription", resourceOperationStatus.getStatusDescription());
        return postBody;
    }

    private ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID) throws RouteException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "http://mso:8080/dbadapters/RequestsDbAdapter";
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic QlBFTENsaWVudDpwYXNzd29yZDEk");
        httpGet.setHeader("Content-type", "application/soap+xml");
        boolean var16 = false;
        String result="";
        String errorMsg;
        label109: {
            label110: {
                try {
                    var16 = true;
                    CloseableHttpResponse e = httpClient.execute(httpGet);
                    result = EntityUtils.toString(e.getEntity());
                    if(e.getStatusLine().getStatusCode() != 200) {
                        throw new RouteException(result, "SERVICE_GET_ERR");
                    }

                    e.close();
                    var16 = false;
                    break label110;
                } catch (ClientProtocolException var21) {
                    errorMsg = url + ":httpGetWithJSON connect faild";
                    throwsRouteException(errorMsg, var21, "GET_CONNECT_FAILD");
                    var16 = false;
                } catch (IOException var22) {
                    errorMsg = url + ":httpGetWithJSON connect faild";
                    throwsRouteException(errorMsg, var22, "GET_CONNECT_FAILD");
                    var16 = false;
                    break label109;
                } finally {
                    if(var16) {
                        try {
                            httpClient.close();
                        } catch (IOException var17) {
                            String errorMsg1 = url + ":close  httpClient faild";
                            throwsRouteException(errorMsg1, var17, "CLOSE_CONNECT_FAILD");
                        }

                    }
                }

                try {
                    httpClient.close();
                } catch (IOException var19) {
                    errorMsg = url + ":close  httpClient faild";
                    throwsRouteException(errorMsg, var19, "CLOSE_CONNECT_FAILD");
                }

            }

            try {
                httpClient.close();
            } catch (IOException var20) {
                errorMsg = url + ":close  httpClient faild";
                throwsRouteException(errorMsg, var20, "CLOSE_CONNECT_FAILD");
            }

        }

        try {
            httpClient.close();
        } catch (IOException var18) {
            errorMsg = url + ":close  httpClient faild";
            throwsRouteException(errorMsg, var18, "CLOSE_CONNECT_FAILD");
        }

        ResourceOperationStatus resourceOperationStatus = getResourceOperationStatusFromXmlString(result);

        return resourceOperationStatus;

        //return requestsDB.getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
    }

    private ResourceOperationStatus getResourceOperationStatusFromXmlString(String result) {
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
        return resourceOperationStatus;
    }

    private String getValueByName(String Name, String xml) {
        String start = "<" + Name + ">";
        String end = "</" + Name + ">";
        return xml.substring(xml.indexOf(start), xml.indexOf(end)).replace(start, "");
    }

    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

    @Override
    public void execute(DelegateExecution execution) {
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient(execution);
//        updateProgress(execution, RequestsDbConstant.Status.PROCESSING, null, "10", "execute begin!");
        Map<String, String> inputs = getInputs(execution);
//        updateProgress(execution, null, null, "30", "getGenericResourceApiClient finished!");
        try {
            sendRestrequestAndHandleResponse(execution, inputs, genericResourceApiClient);
            execution.setVariable("SDNCA_SuccessIndicator", true);
//            updateProgress(execution, RequestsDbConstant.Status.FINISHED, null, RequestsDbConstant.Progress.ONE_HUNDRED, "execute finished!");
        } catch (Exception e) {
            e.printStackTrace();
            execution.setVariable("SDNCA_SuccessIndicator", false);
        }
    }

    protected Map<String, String> getInputs(DelegateExecution execution) {
        Map<String, String> inputs = new HashMap<>();
        String json = (String) execution.getVariable(SDCADAPTOR_INPUTS);
        JSONObject jsonObject = new JSONObject(json);
        JSONObject paras = jsonObject.getJSONObject("additionalParamForNs");
        paras.keySet().stream().forEach(key -> inputs.put(key, paras.getString((String) key)));
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
        String serviceId = (String) execution.getVariable("serviceId");
        String operationId = (String) execution.getVariable("operationId");
        String resourceTemplateUUID = (String) execution.getVariable("resourceUUID");
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
        } catch (Exception exception) {
            System.out.println(exception);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " updateProgress catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, exception.getClass().toString());
        }
    }

    private GenericResourceApi getGenericResourceApiClient(DelegateExecution execution) {
//        updateProgress(execution, null, null, "20", "getGenericResourceApiClient begin!");
        String msbIp = System.getenv().get(ONAP_IP);
        int msbPort = DEFAULT_MSB_Port;
        Map<String, String> properties = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
        if (properties != null) {
            if (StringUtils.isBlank(msbIp) || !isIp(msbIp)) {
                msbIp = properties.get("msb-ip");
                if (StringUtils.isBlank(msbIp)) {
                    msbIp = getString(properties, "msb.address", DEFAULT_MSB_IP);
                }
            }
            String strMsbPort = properties.get("msb-port");
            if (StringUtils.isBlank(strMsbPort)) {
                strMsbPort = getString(properties, "msb.port", String.valueOf(DEFAULT_MSB_Port));
            }
            msbPort = Integer.valueOf(strMsbPort);
        }
        MSBServiceClient msbClient = new MSBServiceClient(msbIp, msbPort);
        RestServiceCreater restServiceCreater = new RestServiceCreater(msbClient);
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
            System.out.println(e);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        }
        return defaultValue;
    }

    private Integer getInteger(DelegateExecution execution, String name, Integer defaultValue) {
        Integer vlaue = (Integer) execution.getVariable(name);
        try {
            if (vlaue != null) {
                return vlaue;
            }
        } catch (Exception e) {
            System.out.println(e);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        }
        return defaultValue;
    }

    public String getProcessKey(DelegateExecution execution) {
        return execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }
}
