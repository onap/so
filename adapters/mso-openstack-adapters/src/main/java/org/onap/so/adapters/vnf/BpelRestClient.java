/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.so.adapters.vnf;


import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * This is the class that is used to POST replies from the MSO adapters to the BPEL engine. It can be configured via
 * property file, or modified using the member methods. The properties to use are: org.onap.so.adapters.vnf.bpelauth
 * encrypted authorization string to send to BEPL engine org.onap.so.adapters.vnf.sockettimeout socket timeout value
 * org.onap.so.adapters.vnf.connecttimeout connect timeout value org.onap.so.adapters.vnf.retrycount number of times to
 * retry failed connections org.onap.so.adapters.vnf.retryinterval interval (in seconds) between retries
 * org.onap.so.adapters.vnf.retrylist list of response codes that will trigger a retry (the special code 900 means
 * "connection was not established")
 */
@Component
@Scope("prototype")
public class BpelRestClient {
    public static final String MSO_PROP_VNF_ADAPTER = "MSO_PROP_VNF_ADAPTER";
    private static final String PROPERTY_DOMAIN = "org.onap.so.adapters.vnf";
    private static final String BPEL_AUTH_PROPERTY = PROPERTY_DOMAIN + ".bpelauth";
    private static final String SOCKET_TIMEOUT_PROPERTY = PROPERTY_DOMAIN + ".sockettimeout";
    private static final String CONN_TIMEOUT_PROPERTY = PROPERTY_DOMAIN + ".connecttimeout";
    private static final String RETRY_COUNT_PROPERTY = PROPERTY_DOMAIN + ".retrycount";
    private static final String RETRY_INTERVAL_PROPERTY = PROPERTY_DOMAIN + ".retryinterval";
    private static final String RETRY_LIST_PROPERTY = PROPERTY_DOMAIN + ".retrylist";
    private static final String ENCRYPTION_KEY_PROP = "org.onap.so.adapters.network.encryptionKey";
    private static final Logger logger = LoggerFactory.getLogger(BpelRestClient.class);

    /** Default socket timeout (in seconds) */
    public static final int DEFAULT_SOCKET_TIMEOUT = 5;
    /** Default connect timeout (in seconds) */
    public static final int DEFAULT_CONNECT_TIMEOUT = 5;
    /** By default, retry up to five times */
    public static final int DEFAULT_RETRY_COUNT = 5;
    /** Default interval to wait between retries (in seconds), negative means use backoff algorithm */
    public static final int DEFAULT_RETRY_INTERVAL = -15;
    /** Default list of response codes to trigger a retry */
    public static final String DEFAULT_RETRY_LIST = "408,429,500,502,503,504,900"; // 900 is "connection failed"
    /** Default credentials */
    public static final String DEFAULT_CREDENTIALS = "";

    @Autowired
    private Environment env;
    // Properties of the BPEL client -- all are configurable
    private int socketTimeout;
    private int connectTimeout;
    private int retryCount;
    private int retryInterval;
    private Set<Integer> retryList;
    private String credentials;

    // last response from BPEL engine
    private int lastResponseCode;
    private String lastResponse;

    /**
     * Create a client to send results to the BPEL engine, using configuration from the MSO_PROP_VNF_ADAPTER properties.
     */
    public BpelRestClient() {
        socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        retryCount = DEFAULT_RETRY_COUNT;
        retryInterval = DEFAULT_RETRY_INTERVAL;
        setRetryList(DEFAULT_RETRY_LIST);
        credentials = DEFAULT_CREDENTIALS;
        lastResponseCode = 0;
        lastResponse = "";

    }

    @PostConstruct
    protected void init() {

        socketTimeout = env.getProperty(SOCKET_TIMEOUT_PROPERTY, Integer.class, DEFAULT_SOCKET_TIMEOUT);
        connectTimeout = env.getProperty(CONN_TIMEOUT_PROPERTY, Integer.class, DEFAULT_CONNECT_TIMEOUT);
        retryCount = env.getProperty(RETRY_COUNT_PROPERTY, Integer.class, DEFAULT_RETRY_COUNT);
        retryInterval = env.getProperty(RETRY_INTERVAL_PROPERTY, Integer.class, DEFAULT_RETRY_INTERVAL);
        setRetryList(env.getProperty(RETRY_LIST_PROPERTY, DEFAULT_RETRY_LIST));
        credentials = getEncryptedProperty(BPEL_AUTH_PROPERTY, DEFAULT_CREDENTIALS, ENCRYPTION_KEY_PROP);
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        int newRetryCount = retryCount;
        if (newRetryCount < 0)
            newRetryCount = DEFAULT_RETRY_COUNT;
        this.retryCount = newRetryCount;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getRetryList() {
        if (retryList.isEmpty())
            return "";
        String t = retryList.toString();
        return t.substring(1, t.length() - 1);
    }

    public void setRetryList(String retryList) {
        Set<Integer> s = new TreeSet<>();
        for (String t : retryList.split("[, ]")) {
            try {
                s.add(Integer.parseInt(t));
            } catch (NumberFormatException x) {
                // ignore
            }
        }
        this.retryList = s;
    }

    public int getLastResponseCode() {
        return lastResponseCode;
    }

    public String getLastResponse() {
        return lastResponse;
    }

    /**
     * Post a response to the URL of the BPEL engine. As long as the response code is one of those in the retryList, the
     * post will be retried up to "retrycount" times with an interval (in seconds) of "retryInterval". If retryInterval
     * is negative, then each successive retry interval will be double the previous one.
     * 
     * @param toBpelStr the content (XML or JSON) to post
     * @param bpelUrl the URL to post to
     * @param isxml true if the content is XML, otherwise assumed to be JSON
     * @return true if the post succeeded, false if all retries failed
     */
    public boolean bpelPost(final String toBpelStr, final String bpelUrl, final boolean isxml) {
        int totalretries = 0;
        int retryint = retryInterval;
        while (true) {
            sendOne(toBpelStr, bpelUrl, isxml);
            // Note: really should handle response code 415 by switching between content types if needed
            if (!retryList.contains(lastResponseCode)) {
                debug("Got response code: " + lastResponseCode + ": returning.");
                return true;
            }
            if (totalretries >= retryCount) {
                debug("Retried " + totalretries + " times, giving up.");
                logger.error("{} {} Could not deliver response to BPEL after {} tries: {}",
                        MessageEnum.RA_SEND_VNF_NOTIF_ERR, ErrorCode.BusinessProcessError.getValue(), totalretries,
                        toBpelStr);
                return false;
            }
            totalretries++;
            int sleepinterval = retryint;
            if (retryint < 0) {
                // if retry interval is negative double the retry on each pass
                sleepinterval = -retryint;
                retryint *= 2;
            }
            debug("Sleeping for " + sleepinterval + " seconds.");
            try {
                Thread.sleep(sleepinterval * 1000L);
            } catch (InterruptedException e) {
                logger.debug("Exception while Thread sleep", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void debug(String m) {
        logger.debug(m);
    }

    private void sendOne(final String toBpelStr, final String bpelUrl, final boolean isxml) {
        logger.debug("Sending to BPEL server: {}", bpelUrl);
        logger.debug("Content is: {}", toBpelStr);

        // POST
        HttpPost post = new HttpPost(bpelUrl);
        if (credentials != null && !credentials.isEmpty())
            post.addHeader("Authorization", "Basic " + DatatypeConverter.printBase64Binary(credentials.getBytes()));

        logger.debug("HTTPPost Headers: {}", post.getAllHeaders());

        // ContentType
        ContentType ctype = isxml ? ContentType.APPLICATION_XML : ContentType.APPLICATION_JSON;
        post.setEntity(new StringEntity(toBpelStr, ctype));

        // Timeouts
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout * 1000)
                .setConnectTimeout(connectTimeout * 1000).build();
        post.setConfig(requestConfig);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(post);
            if (response != null) {
                lastResponseCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                lastResponse = (entity != null) ? EntityUtils.toString(entity) : "";
            } else {
                lastResponseCode = 900;
                lastResponse = "";
            }
        } catch (Exception e) {
            logger.error("{} {} Exception - Error sending Bpel notification: {} ", MessageEnum.RA_SEND_VNF_NOTIF_ERR,
                    ErrorCode.BusinessProcessError.getValue(), toBpelStr, e);
            lastResponseCode = 900;
            lastResponse = "";
        }

        logger.debug("Response code from BPEL server: {}", lastResponseCode);
        logger.debug("Response body is: {}", lastResponse);
    }

    private String getEncryptedProperty(String key, String defaultValue, String encryptionKey) {
        if (env.getProperty(key) != null) {
            try {
                return CryptoUtils.decrypt(env.getProperty(key), env.getProperty(encryptionKey));
            } catch (GeneralSecurityException e) {
                logger.debug("Exception while decrypting property: {} ", env.getProperty(key), e);
            }
        }
        return defaultValue;
    }

}
