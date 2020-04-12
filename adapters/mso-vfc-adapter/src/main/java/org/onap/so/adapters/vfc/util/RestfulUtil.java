/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.vfc.util;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.logger.LoggingAnchor;
import org.apache.http.Header;
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
import org.onap.so.adapters.vfc.model.RestfulResponse;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * <br>
 * <p>
 * </p>
 * utility to invoke restclient
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
@Component
public class RestfulUtil {

    /**
     * Log service
     */
    private static final Logger logger = LoggerFactory.getLogger(RestfulUtil.class);

    private static final int DEFAULT_TIME_OUT = 60000;

    private static final String ONAP_IP = "ONAP_IP";

    private static final String DEFAULT_MSB_IP = "127.0.0.1";

    private static final Integer DEFAULT_MSB_PORT = 80;

    private static final String VFC_ADAPTER = "VFC Adapter";

    @Autowired
    private Environment env;

    @Autowired
    private HttpClient client;

    public String getMsbHost() {
        // MSB_IP will be set as ONAP_IP environment parameter in install flow.
        String msbIp = System.getenv().get(ONAP_IP);
        // if ONAP IP is not set. get it from config file.
        if (null == msbIp || msbIp.isEmpty()) {
            msbIp = env.getProperty("mso.msb-ip", DEFAULT_MSB_IP);
        }
        Integer msbPort = env.getProperty("mso.msb-port", Integer.class, DEFAULT_MSB_PORT);

        String msbEndpoint = UriBuilder.fromPath("").host(msbIp).port(msbPort).scheme("http").build().toString();
        logger.debug("msbEndpoint in vfc adapter: {}", msbEndpoint);

        return msbEndpoint;
    }

    private RestfulUtil() {

    }


    public RestfulResponse send(String msbUrl, String methodType, String content, Map<String, String> requestHeader) {
        // String msbUrl = getMsbHost() + url;
        logger.debug("Begin to sent message " + methodType + ": " + msbUrl);

        HttpRequestBase method = null;
        HttpResponse httpResponse = null;

        try {
            int timeout = DEFAULT_TIME_OUT;

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();

            HttpClient client = HttpClientBuilder.create().build();

            if ("POST".equalsIgnoreCase(methodType)) {
                HttpPost httpPost = new HttpPost(msbUrl);
                httpPost.setConfig(requestConfig);
                for (String key : requestHeader.keySet()) {
                    httpPost.setHeader(key, requestHeader.get(key));
                }
                httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPost;
            } else if ("PUT".equalsIgnoreCase(methodType)) {
                HttpPut httpPut = new HttpPut(msbUrl);
                httpPut.setConfig(requestConfig);
                httpPut.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPut;
            } else if ("GET".equalsIgnoreCase(methodType)) {
                HttpGet httpGet = new HttpGet(msbUrl);
                httpGet.setConfig(requestConfig);
                method = httpGet;
            } else if ("DELETE".equalsIgnoreCase(methodType)) {
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
            Map<String, String> responseHeader = new HashMap<>();
            String responseContent = null;
            if (httpResponse.getEntity() != null) {
                responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                Header[] httpResponseAllHeaders = httpResponse.getAllHeaders();
                for (Header header : httpResponseAllHeaders) {
                    responseHeader.put(header.getName(), header.getValue());

                }
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

            logger.debug("VFC Response: {} {}", statusCode,
                    statusMessage + (responseContent == null ? "" : System.lineSeparator() + responseContent));

            if (httpResponse.getStatusLine().getStatusCode() >= 300) {
                String errMsg = "VFC returned " + statusCode + " " + statusMessage;
                logError(errMsg);
                return createResponse(statusCode, errMsg);
            }

            httpResponse = null;

            if (null != method) {
                method.reset();
            } else {
                logger.debug("method is NULL:");
            }

            method = null;
            return createResponse(statusCode, responseContent, responseHeader);

        } catch (SocketTimeoutException | ConnectTimeoutException e) {
            String errMsg = "Request to VFC timed out";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, errMsg);

        } catch (Exception e) {
            String errMsg = "Error processing request to VFC";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, errMsg);

        } finally {
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                }
            }

            if (method != null) {
                try {
                    method.reset();
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                }
            }
        }
    }

    public RestfulResponse send(String msbUrl, String methodType, String content) {
        if (!msbUrl.contains("http")) {
            msbUrl = getMsbHost() + msbUrl;
        }
        // String msbUrl = getMsbHost() + url;
        logger.debug("Begin to sent message " + methodType + ": " + msbUrl);

        HttpRequestBase method = null;
        HttpResponse httpResponse = null;

        try {
            int timeout = DEFAULT_TIME_OUT;

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();

            if ("POST".equalsIgnoreCase(methodType)) {
                HttpPost httpPost = new HttpPost(msbUrl);
                httpPost.setConfig(requestConfig);
                httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPost;
            } else if ("PUT".equalsIgnoreCase(methodType)) {
                HttpPut httpPut = new HttpPut(msbUrl);
                httpPut.setConfig(requestConfig);
                httpPut.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                method = httpPut;
            } else if ("GET".equalsIgnoreCase(methodType)) {
                HttpGet httpGet = new HttpGet(msbUrl);
                httpGet.setConfig(requestConfig);
                method = httpGet;
            } else if ("DELETE".equalsIgnoreCase(methodType)) {
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
            Map<String, String> responseHeader = new HashMap<>();
            String responseContent = null;
            if (httpResponse.getEntity() != null) {
                responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                Header[] httpResponseAllHeaders = httpResponse.getAllHeaders();
                for (Header header : httpResponseAllHeaders) {
                    responseHeader.put(header.getName(), header.getValue());

                }
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

            logger.debug("VFC Response: " + statusCode + " " + statusMessage
                    + (responseContent == null ? "" : System.lineSeparator() + responseContent));

            if (httpResponse.getStatusLine().getStatusCode() >= 300) {
                String errMsg = "VFC returned " + statusCode + " " + statusMessage;
                logError(errMsg);
                return createResponse(statusCode, errMsg);
            }

            httpResponse = null;

            if (null != method) {
                method.reset();
            } else {
                logger.debug("method is NULL:");
            }

            method = null;
            return createResponse(statusCode, responseContent, responseHeader);

        } catch (SocketTimeoutException | ConnectTimeoutException e) {
            String errMsg = "Request to VFC timed out";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, errMsg);

        } catch (Exception e) {
            String errMsg = "Error processing request to VFC";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, errMsg);

        } finally {
            if (httpResponse != null) {
                try {
                    EntityUtils.consume(httpResponse.getEntity());
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                }
            }

            if (method != null) {
                try {
                    method.reset();
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                }
            }
        }
    }

    public RestfulResponse getNfvoFromAAI(String nfvo) {
        HttpRequestBase method = null;
        HttpResponse httpResponse = null;
        String endPoint = getMsbHost() + "/api/aai-esr-server/v1/nfvos/" + nfvo;
        logger.info("Endpoint URL" + endPoint);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DEFAULT_TIME_OUT)
                .setConnectTimeout(DEFAULT_TIME_OUT).setConnectionRequestTimeout(DEFAULT_TIME_OUT).build();
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(endPoint);
        httpGet.setConfig(requestConfig);
        String encoding = Base64.getEncoder().encodeToString(("AAI:AAI").getBytes());
        httpGet.setHeader("Authorization", "Basic " + encoding);
        method = httpGet;
        String responseContent = null;
        Map<String, String> responseHeader = null;
        try {
            httpResponse = client.execute(method);
            if (httpResponse.getEntity() != null) {
                responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

            logger.debug("AAI Response: " + statusCode + " " + statusMessage
                    + (responseContent == null ? "" : System.lineSeparator() + responseContent));

            if (httpResponse.getStatusLine().getStatusCode() >= 300) {
                String errMsg = "AAI returned " + statusCode + " " + statusMessage;
                logError(errMsg);
                return createResponse(statusCode, errMsg);
            }
        } catch (Exception e) {
            String errMsg = "Error processing request to AAI";
            logError(errMsg, e);
            return createResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, errMsg);
        }
        return createResponse(200, responseContent);
    }

    private static void logError(String errMsg, Throwable t) {
        logger.error(LoggingAnchor.FOUR, MessageEnum.RA_NS_EXC.toString(), VFC_ADAPTER,
                ErrorCode.AvailabilityError.getValue(), errMsg, t);
    }

    private static void logError(String errMsg) {
        logger.error(LoggingAnchor.FOUR, MessageEnum.RA_NS_EXC.toString(), VFC_ADAPTER,
                ErrorCode.AvailabilityError.toString(), errMsg);
    }

    private static RestfulResponse createResponse(int statusCode, String content) {
        RestfulResponse rsp = new RestfulResponse();
        rsp.setStatus(statusCode);
        rsp.setResponseContent(content);
        return rsp;
    }

    private static RestfulResponse createResponse(int statusCode, String content, Map<String, String> responseHeader) {
        RestfulResponse rsp = new RestfulResponse();
        rsp.setStatus(statusCode);
        rsp.setRespHeaderMap(responseHeader);
        rsp.setResponseContent(content);
        return rsp;
    }

}
