/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.vfc.util;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openecomp.mso.adapters.vfc.model.RestfulResponse;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

/**
 * <br>
 * <p>
 * </p>
 * utility to invoke restclient
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class RestfulUtil {

    /**
     * Log service
     */
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

    private static final int DEFAULT_TIME_OUT = 60000;

    private static final MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

    public static String getMsbHost() {
        String msbIp = "10.229.32.131";
        String msbPort = "8090";
        try {
            msbIp = msoPropertiesFactory.getMsoJavaProperties("MSO_PROP_TOPOLOGY").getProperty("msb-ip",
                    "10.229.32.131");
            msbPort = msoPropertiesFactory.getMsoJavaProperties("MSO_PROP_TOPOLOGY").getProperty("msb-port", "8099");

        } catch(MsoPropertiesException e) {
            LOGGER.error(MessageEnum.RA_NS_EXC, "VFC", "", MsoLogger.ErrorCode.AvailabilityError,
                    "Get msb properties failed");
            e.printStackTrace();
        }
        return "http://" + msbIp + ":" + msbPort;
    }

    private RestfulUtil() {

    }

    public static RestfulResponse send(String url, String methodType, String content) {
        String msbUrl = getMsbHost() + url;
        LOGGER.info(MessageEnum.RA_NS_EXC, msbUrl, "VFC", "");
        LOGGER.debug("VFC Request Body:\n" + content);

        HttpRequestBase method = null;
        HttpResponse httpResponse = null;

        try {
            int timeout = DEFAULT_TIME_OUT;

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();

            HttpClient client = HttpClientBuilder.create().build();

            if("POST".equals(methodType.toUpperCase())) {
                HttpPost httpPost = new HttpPost(msbUrl);
                httpPost.setConfig(requestConfig);
                httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPost;
            } else if("PUT".equals(methodType.toUpperCase())) {
                HttpPut httpPut = new HttpPut(msbUrl);
                httpPut.setConfig(requestConfig);
                httpPut.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPut;
            } else if("GET".equals(methodType.toUpperCase())) {
                HttpGet httpGet = new HttpGet(msbUrl);
                httpGet.setConfig(requestConfig);
                method = httpGet;
            } else if("DELETE".equals(methodType.toUpperCase())) {
                HttpDelete httpDelete = new HttpDelete(msbUrl);
                httpDelete.setConfig(requestConfig);
                method = httpDelete;
            }

            // now VFC have no auth
            // String userCredentials =
            // SDNCAdapterProperties.getEncryptedProperty(Constants.SDNC_AUTH_PROP,
            // Constants.DEFAULT_SDNC_AUTH, Constants.ENCRYPTION_KEY);
            // String authorization = "Basic " +
            // DatatypeConverter.printBase64Binary(userCredentials.getBytes());
            // method.setHeader("Authorization", authorization);

            httpResponse = client.execute(method);

            String responseContent = null;
            if(httpResponse.getEntity() != null) {
                responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

            LOGGER.debug("VFC Response: " + statusCode + " " + statusMessage
                    + (responseContent == null ? "" : System.lineSeparator() + responseContent));

            if(httpResponse.getStatusLine().getStatusCode() >= 300) {
                String errMsg = "VFC returned " + statusCode + " " + statusMessage;
                logError(errMsg);
                return createResponse(statusCode, errMsg);
            }

            httpResponse = null;

            if(null != method) {
                method.reset();
            } else {
                LOGGER.debug("method is NULL:");
            }

            method = null;

            LOGGER.info(MessageEnum.RA_RESPONSE_FROM_SDNC, responseContent, "VFC", "");
            return createResponse(statusCode, responseContent);

        } catch(SocketTimeoutException e) {
            String errMsg = "Request to VFC timed out";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, errMsg);

        } catch(ConnectTimeoutException e) {
            String errMsg = "Request to VFC timed out";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, errMsg);

        } catch(Exception e) {
            String errMsg = "Error processing request to VFC";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, errMsg);

        } finally {
            if(httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                } catch(Exception e) {
                    LOGGER.debug("Exception :", e);
                }
            }

            if(method != null) {
                try {
                    method.reset();
                } catch(Exception e) {
                    LOGGER.debug("Exception :", e);
                }
            }
        }
    }

    private static void logError(String errMsg, Throwable t) {
        LOGGER.error(MessageEnum.RA_NS_EXC, "VFC", "", MsoLogger.ErrorCode.AvailabilityError, errMsg, t);
        ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, errMsg);
    }

    private static void logError(String errMsg) {
        LOGGER.error(MessageEnum.RA_NS_EXC, "VFC", "", MsoLogger.ErrorCode.AvailabilityError, errMsg);
        ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, errMsg);
    }

    private static RestfulResponse createResponse(int statusCode, String content) {
        RestfulResponse rsp = new RestfulResponse();
        rsp.setStatus(statusCode);
        rsp.setResponseContent(content);
        return rsp;
    }

    /**
     * @param request
     * @return
     */
    // public static String getRequestBody(HttpServletRequest request) {
    // String body = null;
    // StringBuilder stringBuilder = new StringBuilder();
    // BufferedReader bufferedReader = null;
    // try {
    // InputStream inputStream = request.getInputStream();
    // if (inputStream != null) {
    // bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    // char[] charBuffer = new char[128];
    // int bytesRead = -1;
    // while ((bytesRead = bufferedReader.read(charBuffer)) > 0)
    // stringBuilder.append(charBuffer, 0, bytesRead);
    // }
    // } catch (IOException ex) {
    // LOGGER.error(MessageEnum.RA_NS_EXC, "VFC", "", MsoLogger.ErrorCode.AvailabilityError,
    // "read inputStream buffer catch exception:", ex);
    // } finally {
    // if (bufferedReader != null) {
    // try {
    // bufferedReader.close();
    // } catch (IOException ex) {
    // LOGGER.error(MessageEnum.RA_NS_EXC, "VFC", "", MsoLogger.ErrorCode.AvailabilityError,
    // "close buffer catch exception:", ex);
    // }
    // }
    // }
    //
    // body = stringBuilder.toString();
    // return body;
    // }

}
