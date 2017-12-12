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

package org.openecomp.mso;


import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.core.Response;

public class HealthCheckUtils {

    private static final String HEALTH_CHECK = "HealthCheck";
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
    private static final String MSO_PROP_TOPOLOGY = "MSO_PROP_TOPOLOGY";
    private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
    private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
    private static final String NOT_FOUND = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Application Not Started</title></head><body>Application not started. Properties file missing or invalid or database Connection failed</body></html>";
    private static final String NOT_HEALTHY = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Application Not Started</title></head><body>Application not available or at least one of the sub-modules is not available.</body></html>";
    public static final Response HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_OK)
            .entity (CHECK_HTML)
            .build ();
    public static final Response HEALTH_CHECK_NOK_RESPONSE = Response.status (HttpStatus.SC_SERVICE_UNAVAILABLE)
            .entity (NOT_HEALTHY)
            .  build ();
    public static final Response NOT_STARTED_RESPONSE = Response.status (HttpStatus.SC_SERVICE_UNAVAILABLE)
            .entity (NOT_FOUND)
            .build ();

    public enum NodeType {APIH, RA, BPMN}

    public boolean catalogDBCheck (MsoLogger subMsoLogger, long startTime) {
        try(CatalogDatabase catalogDB = CatalogDatabase.getInstance()) {
            catalogDB.healthCheck ();
        } catch (Exception e) {
            subMsoLogger.error(MessageEnum.GENERAL_EXCEPTION, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Failed to check catalog database", e);
            subMsoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception during healthcheck");
            return false;
        }
        return true;
    }

    public boolean requestDBCheck (MsoLogger subMsoLogger, long startTime) {
        try {
            (RequestsDatabase.getInstance()).healthCheck ();
        } catch (Exception e) {
            subMsoLogger.error(MessageEnum.GENERAL_EXCEPTION, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Failed to check request database", e);
            subMsoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Exception during local healthcheck");
            return false;
        }
        return true;
    }

    public boolean siteStatusCheck(MsoLogger subMsoLogger) {
        // Check the Site Status value in DB first, if set to false, return NOK
        String site = getProperty("site-name");

        MsoStatusUtil statusUtil = new MsoStatusUtil ();
        if (!statusUtil.getSiteStatus (site)) {
            subMsoLogger.debug("This site is currently disabled for maintenance.");
            return false;
        }
        return true;
    }

    public boolean configFileCheck (MsoLogger subMsoLogger, long startTime, String propertiesFile) {
        if (null != propertiesFile) {
            MsoJavaProperties props = loadMsoProperties (propertiesFile);
            if (props == null) {
                subMsoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.ServiceNotAvailable, "Configuration file can not be loaded");
                return false;
            }
        }
        return true;
    }


    private MsoJavaProperties loadMsoProperties (String fileName) {
        MsoJavaProperties msoProperties;
        try {
            msoProperties = msoPropertiesFactory.getMsoJavaProperties(fileName);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.LOAD_PROPERTIES_FAIL, fileName, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Failed to load topology properties", e);
            return null;
        }
        if (msoProperties !=null && msoProperties.size() > 0) {
            return msoProperties;
        } else {
            msoLogger.error (MessageEnum.NO_PROPERTIES, fileName, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "No topology properties");
            return  null;
        }
    }

    protected boolean verifyLocalHealth(String ip, String apiPort, String url, String sslEnabled, String requestId) {
        String finalUrl = getFinalUrl(ip, apiPort, url, sslEnabled);
        long startTime = System.currentTimeMillis ();
        if (null != requestId) {
            finalUrl = finalUrl + "?requestId=" + requestId;
        }
        try {
            HttpResponse response;
            CloseableHttpClient client = getHttpClient ();
            HttpGet get = new HttpGet(finalUrl);
            msoLogger.debug("Get url is: " + finalUrl);
            response = client.execute(get);
            msoLogger.debug("Get response is: " + response);
            client.close (); //shut down the connection
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                msoLogger.debug("verifyLocalHealth - Successfully communicate with APIH/BPMN/RA");
                return true;
            }
            msoLogger.debug("verifyLocalHealth - Service not available");
        } catch (Exception e) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION, "", HEALTH_CHECK, MsoLogger.ErrorCode.UnknownError, "Error in local HealthCheck", e);
            msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with APIH/BPMN/RA", url, HEALTH_CHECK, null);
            msoLogger.debug("Exception while triggering local health check api:" + finalUrl);
        }
        return false;
    }

    protected CloseableHttpClient getHttpClient () {
        // set the connection timeout value to 30 seconds (30000 milliseconds)
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(30000);
        requestBuilder = requestBuilder.setConnectionRequestTimeout(30000);
        HttpClientBuilder builder = HttpClientBuilder.create ();
        builder.setDefaultRequestConfig (requestBuilder.build ());

        return builder.build ();
    }

    public MsoJavaProperties loadTopologyProperties() {
        MsoJavaProperties msoProperties;
        try {
            msoProperties = msoPropertiesFactory.getMsoJavaProperties(MSO_PROP_TOPOLOGY);
        } catch (Exception e) {
            msoLogger.error(MessageEnum.LOAD_PROPERTIES_FAIL, MSO_PROP_TOPOLOGY, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Not able to load topology properties", e);
            return null;
        }

        if (msoProperties != null && msoProperties.size() > 0) {
            return msoProperties;
        } else {
            msoLogger.error(MessageEnum.LOAD_PROPERTIES_FAIL, MSO_PROP_TOPOLOGY, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Not able to load topology properties");
            return null;
        }
    }

    public boolean verifyNodeHealthCheck (HealthCheckUtils.NodeType type, String requestId) {
        // Get info from topology properties file
        MsoJavaProperties topologyProp = this.loadTopologyProperties();
        if (null == topologyProp) {
            return false;
        }

        checkHealthForProperty(topologyProp, "", requestId);

        boolean healthCheck = false;
        switch (type) {
            case APIH:
                healthCheck = checkHealthForProperty(topologyProp, "apih-healthcheck-urn", requestId);
                break;
            case RA:
                healthCheck = checkHealthForProperty(topologyProp, "jra-healthcheck-urn", requestId);
                break;
            case BPMN:
                healthCheck = checkHealthForProperty(topologyProp, "camunda-healthcheck-urn", requestId);
                break;
            default:
                msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "Unknown NodeType:" + type, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Unknown NodeType:" + type);
                break;
        }

        return healthCheck;
    }

    public boolean verifyGlobalHealthCheck(boolean verifyBpmn, String requestId) {
        // Get info from topology properties file
        MsoJavaProperties topologyProp = this.loadTopologyProperties();
        if (null == topologyProp) {
            msoLogger.error (MessageEnum.GENERAL_EXCEPTION_ARG, "Not able to find the topology file", "", HEALTH_CHECK, MsoLogger.ErrorCode.PermissionError, "Not able to find the topology file");
            return false;
        }

        return verifyApihServersHealthCheck(topologyProp, requestId) &&
                verifyCamundaServersHealthCheck(topologyProp, requestId, verifyBpmn) &&
                verifyRaServersHealthCheck(topologyProp, requestId);
    }

    public String getProperty (String name) {
        MsoJavaProperties prop = this.loadTopologyProperties();

        return prop.getProperty(name, null);
    }

    protected String getFinalUrl (String ip, String port, String url, String sslEnabled) {
        if (null == port && null == sslEnabled) {
            int length = ip.length();
            if ("/".equals(ip.substring(length - 1))) {
                ip = ip.substring(0, length - 1);
            }
            return ip + url;
        } else if ("true".equalsIgnoreCase(sslEnabled)) {
            return "https://" + ip + ":" + port + url;
        } else {
            return "http://" + ip + ":" + port + url;
        }
    }

    private boolean verifyRaServersHealthCheck(MsoJavaProperties topologyProp, String requestId) {
        String jraLB = topologyProp.getProperty("jra-load-balancer", null);
        String jraApi = topologyProp.getProperty("jra-nodehealthcheck-urn", null);

        if (null == jraLB || null == jraApi || jraLB.isEmpty() || jraApi.isEmpty()) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "Key parameters are missing from the topology file", "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Key parameters are missing from the topology file");
            return false;
        }

        return verifyLocalHealth(jraLB, null, jraApi, null, requestId);
    }

    private boolean verifyCamundaServersHealthCheck(MsoJavaProperties topologyProp, String requestId, boolean verifyBpmn) {
        String bpmnLB = topologyProp.getProperty("camunda-load-balancer", null);
        String bpmnApi = topologyProp.getProperty("camunda-nodehealthcheck-urn", null);

        if (null == bpmnLB || null == bpmnApi || bpmnLB.isEmpty() || bpmnApi.isEmpty()) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "Key parameters are missing from the topology file", "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Key parameters are missing from the topology file");
            return false;
        }

        return !verifyBpmn || verifyLocalHealth(bpmnLB, null, bpmnApi, null, requestId);
    }

    private boolean verifyApihServersHealthCheck(MsoJavaProperties topologyProp, String requestId) {
        String apihLB = topologyProp.getProperty("apih-load-balancer", null);
        String apihApi = topologyProp.getProperty("apih-nodehealthcheck-urn", null);

        if (null == apihLB || null == apihApi || apihLB.isEmpty() || apihApi.isEmpty()) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "Key parameters are missing from the topology file", "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Key parameters are missing from the topology file");
            return false;
        }

        return verifyLocalHealth(apihLB, null, apihApi, null, requestId);
    }

    private boolean checkHealthForProperty(MsoJavaProperties topologyProp, String property, String requestId) {
        String apiList = topologyProp.getProperty(property, null);
        if (apiList == null) {
            String errorDescription = "Not able to get " + property + " parameter";
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, errorDescription, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, errorDescription);
            return false;
        }
        String[] apis = apiList.split(",");
        return checkHealthForEachApi(topologyProp, apis, requestId);
    }

    private boolean checkHealthForEachApi(MsoJavaProperties topologyProp, String[] apis, String requestId) {

        String port = topologyProp.getProperty("server-port", null);
        String ip = System.getProperty("jboss.qualified.host.name");
        String sslEnabled = topologyProp.getProperty("ssl-enable", null);

        if (null == port || null == ip || ip.isEmpty() || port.isEmpty()) {
            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "Not able to get the IP or the Port value. IP:" + ip + "; Port:" + port, "", HEALTH_CHECK, MsoLogger.ErrorCode.DataError, "Not able to get the IP or the Port value. IP:" + ip + "; Port:" + port);
            return false;
        }

        for (String url : apis) {
            // if any of the parameters is null or empty, no need to establish the health check request, just go to the next iteration
            if ((url == null) || url.isEmpty()) {
                continue;
            }
            // Exit the loop if local health check returns false from any of the sub component
            if (!this.verifyLocalHealth(ip, port, url, sslEnabled, requestId)) {
                return false;
            }
        }
        return true;
    }
}
