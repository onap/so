/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.nssmf.util;

import javax.net.ssl.*;
import javax.ws.rs.core.UriBuilder;
import java.net.SocketTimeoutException;
import java.net.URI;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.onap.aai.domain.yang.*;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.nssmf.entity.TokenRequest;
import org.onap.so.adapters.nssmf.entity.TokenResponse;
import org.onap.so.adapters.nssmf.enums.HttpMethod;
import org.onap.so.adapters.nssmf.entity.NssmfInfo;
import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.beans.nsmf.EsrInfo;
import org.onap.so.beans.nsmf.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.onap.so.adapters.nssmf.enums.HttpMethod.POST;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.BAD_REQUEST;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.marshal;
import static org.onap.so.adapters.nssmf.util.NssmfAdapterUtil.unMarshal;
import static org.onap.logging.filter.base.ErrorCode.AvailabilityError;
import static org.onap.so.logger.LoggingAnchor.FOUR;
import static org.onap.so.logger.MessageEnum.RA_NS_EXC;

@Component
public class RestUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestUtil.class);

    private static final int DEFAULT_TIME_OUT = 60000;

    private static final String NSSMI_ADAPTER = "NSSMI Adapter";

    private static final String TOKEN_URL = "/api/rest/securityManagement/v1" + "/oauth/token";

    @Autowired
    private AaiServiceProvider aaiSvcProv;

    public void createServiceInstance(ServiceInstance serviceInstance, ServiceInfo serviceInfo) {
        aaiSvcProv.invokeCreateServiceInstance(serviceInstance, serviceInfo.getGlobalSubscriberId(),
                serviceInfo.getSubscriptionServiceType(), serviceInfo.getNssiId());
    }

    public NssmfInfo getNssmfHost(EsrInfo esrInfo) throws ApplicationException {
        EsrThirdpartySdncList sdncList = aaiSvcProv.invokeGetThirdPartySdncList();
        if (sdncList != null && sdncList.getEsrThirdpartySdnc() != null) {
            for (EsrThirdpartySdnc sdncEsr : sdncList.getEsrThirdpartySdnc()) {

                EsrSystemInfoList sysInfoList =
                        aaiSvcProv.invokeGetThirdPartySdncEsrSystemInfo(sdncEsr.getThirdpartySdncId());

                if (sysInfoList != null && sysInfoList.getEsrSystemInfo() != null) {
                    for (EsrSystemInfo esr : sysInfoList.getEsrSystemInfo()) {
                        if (esr != null && esr.getType().equals(esrInfo.getNetworkType().getNetworkType())
                                && esr.getVendor().equals(esrInfo.getVendor())) {
                            logger.info("Found an entry with vendor name " + esrInfo.getVendor() + " and network type "
                                    + esrInfo.getNetworkType() + " in ESR.");
                            NssmfInfo nssmfInfo = new NssmfInfo();
                            nssmfInfo.setIpAddress(esr.getIpAddress());
                            nssmfInfo.setPort(esr.getPort());
                            nssmfInfo.setCacert(esr.getSslCacert());
                            nssmfInfo.setUserName(esr.getUserName());
                            nssmfInfo.setPassword(esr.getPassword());
                            String endPoint = UriBuilder.fromPath("").host(esr.getIpAddress())
                                    .port(Integer.valueOf(esr.getPort())).scheme("https").build().toString();
                            nssmfInfo.setUrl(endPoint);
                            return nssmfInfo;
                        }
                    }
                }

            }
        }

        throw new ApplicationException(BAD_REQUEST, "ESR information is improper");
    }


    public String getToken(NssmfInfo nssmfInfo) throws ApplicationException {


        TokenRequest req = new TokenRequest();
        req.setGrantType("password");
        req.setUserName(nssmfInfo.getUserName());
        req.setValue(nssmfInfo.getPassword());

        String tokenReq = marshal(req);

        logger.info("Sending token request to NSSMF: " + tokenReq);
        RestResponse tokenRes = send(nssmfInfo.getUrl() + TOKEN_URL, POST, tokenReq, null);

        TokenResponse res = unMarshal(tokenRes.getResponseContent(), TokenResponse.class);

        return res.getAccessToken();
    }


    public RestResponse send(String url, HttpMethod methodType, String content, Header header) {

        HttpRequestBase req = null;
        HttpResponse res = null;

        logger.debug("Beginning to send message {}: {}", methodType, url);

        try {
            int timeout = DEFAULT_TIME_OUT;

            RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            logger.debug("Sending request to NSSMF: " + content);
            req = getHttpReq(url, methodType, header, config, content);
            res = getHttpsClient().execute(req);

            String resContent = null;
            if (res.getEntity() != null) {
                resContent = EntityUtils.toString(res.getEntity(), "UTF-8");
            }

            int statusCode = res.getStatusLine().getStatusCode();
            String statusMessage = res.getStatusLine().getReasonPhrase();
            logger.info("NSSMF Response: {} {}", statusCode,
                    statusMessage + (resContent == null ? "" : System.lineSeparator() + resContent));

            if (res.getStatusLine().getStatusCode() >= 300) {
                String errMsg = "{\n  \"errorCode\": " + res.getStatusLine().getStatusCode()
                        + "\n  \"errorDescription\": " + statusMessage + "\n}";
                logError(errMsg);
                return createResponse(statusCode, errMsg);
            }
            if (null != req) {
                req.reset();
            }
            req = null;

            return createResponse(statusCode, resContent);

        } catch (SocketTimeoutException | ConnectTimeoutException e) {
            String errMsg = "Request to NSSMF timed out";
            logError(errMsg, e);
            return createResponse(408, errMsg);
        } catch (Exception e) {
            String errMsg = "Error processing request to NSSMF";
            logError(errMsg, e);
            return createResponse(500, errMsg);
        } finally {
            if (res != null) {
                try {
                    EntityUtils.consume(res.getEntity());
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                }
            }
            if (req != null) {
                try {
                    req.reset();
                } catch (Exception e) {
                    logger.debug("Exception :", e);
                }
            }
        }
    }

    public RestResponse createResponse(int statusCode, String errMsg) {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(statusCode);
        restResponse.setResponseContent(errMsg);
        return restResponse;
    }

    private HttpRequestBase getHttpReq(String url, HttpMethod method, Header header, RequestConfig config,
            String content) throws ApplicationException {
        HttpRequestBase base;
        switch (method) {
            case POST:
                HttpPost post = new HttpPost(url);
                post.setEntity(new StringEntity(content, APPLICATION_JSON));
                base = post;
                break;

            case GET:
                base = new HttpGet(url);
                break;

            case PUT:
                HttpPut put = new HttpPut(url);
                put.setEntity(new StringEntity(content, APPLICATION_JSON));
                base = put;
                break;

            case PATCH:
                base = new HttpPatch(url);
                break;

            case DELETE:
                HttpDeleteWithBody delete = new HttpDeleteWithBody(url);
                if (content != null) {
                    delete.setEntity(new StringEntity(content, APPLICATION_JSON));
                }
                base = delete;
                break;
            default:
                throw new ApplicationException(404, "invalid method: " + method);

        }
        base.setConfig(config);
        if (header != null) {
            base.setHeader(header);
        }
        return base;
    }

    public RestResponse sendRequest(String allocateUrl, HttpMethod post, String allocateReq, EsrInfo esrInfo)
            throws ApplicationException {
        NssmfInfo nssmfInfo = getNssmfHost(esrInfo);
        Header header = new BasicHeader("X-Auth-Token", getToken(nssmfInfo));
        String nssmfUrl = nssmfInfo.getUrl() + allocateUrl;
        return send(nssmfUrl, post, allocateReq, header);
    }

    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";

        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody() {
            super();
        }
    }


    public HttpClient getHttpsClient() {

        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            SSLConnectionSocketFactory sslsf =
                    new SSLConnectionSocketFactory(sc, new String[] {"TLSv1"}, null, (s, sslSession) -> true);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void logError(String errMsg, Throwable t) {
        logger.error(FOUR, RA_NS_EXC.toString(), NSSMI_ADAPTER, AvailabilityError.getValue(), errMsg, t);
    }

    private static void logError(String errMsg) {
        logger.error(FOUR, RA_NS_EXC.toString(), NSSMI_ADAPTER, AvailabilityError.toString(), errMsg);
    }
}

