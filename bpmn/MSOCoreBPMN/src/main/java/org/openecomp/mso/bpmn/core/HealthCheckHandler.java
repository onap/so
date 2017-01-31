/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.core;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.ProcessEngines;

@Path("/")
public class HealthCheckHandler  {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
    private static final String SITENAME = "mso.sitename";
    private static final String ADPTER_ENDPOINT = "mso.adapters.db.endpoint";
    private static final String CONFIG = "mso.bpmn.urn.properties";

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

    @HEAD
    @GET
    @Path("/healthcheck")
    @Produces("text/html")
    public Response healthcheck (@QueryParam("requestId") String requestId) {
        MsoLogger.setServiceName ("Healthcheck");
        verifyOldUUID(requestId);

        PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
        Map<String,String> props = propertyConfiguration.getProperties(CONFIG);

        if (props == null) {

            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(),  MsoLogger.ErrorCode.AvailabilityError, "Unable to load " + CONFIG);

            return NOT_STARTED_RESPONSE;
        }

        String siteName = props.get(SITENAME);
        String endpoint = props.get(ADPTER_ENDPOINT);

        if (null == siteName || siteName.length () == 0 || null == endpoint || endpoint.length () == 0) {

            msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, "Unable to load key attributes (" + SITENAME + " or " + ADPTER_ENDPOINT + ") from the config file:" + CONFIG);

            return NOT_STARTED_RESPONSE;
        }

        try {
            if (!this.getSiteStatus (endpoint, siteName)) {
                msoLogger.debug("This site is currently disabled for maintenance.");
                return HEALTH_CHECK_NOK_RESPONSE;
            }
        } catch (Exception e) {

            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception while getting SiteStatus", e);

            msoLogger.debug("Exception while getting SiteStatus");
            return NOT_STARTED_RESPONSE;
        }

        try {
            ProcessEngines.getDefaultProcessEngine().getIdentityService().createGroupQuery().list();
        } catch (final Exception e) {

            msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception while verifying Camunda engine", e);

            msoLogger.debug("Exception while verifying Camunda engine");
            return NOT_STARTED_RESPONSE;
        }

        return HEALTH_CHECK_RESPONSE;
    }


    private String verifyOldUUID (String oldId) {
        if (!isValidUUID(oldId)) {
            String newId = UUID.randomUUID().toString();
            MsoLogger.setLogContext(newId, null);
            return newId;
        }
        MsoLogger.setLogContext(oldId, null);
        return oldId;
    }


    private boolean isValidUUID (String id) {
        try {
            if (null == id) {
                return false;
            }
            UUID uuid = UUID.fromString(id);
            return uuid.toString().equalsIgnoreCase(id);
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    private boolean getSiteStatus (String url, String site) throws Exception {
        HttpResponse response;
        // set the connection timeout value to 30 seconds (30000 milliseconds)
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(30000);
        requestBuilder = requestBuilder.setConnectionRequestTimeout(30000);
        HttpClientBuilder builder = HttpClientBuilder.create ();
        builder.setDefaultRequestConfig (requestBuilder.build ());

        HttpPost post = new HttpPost(url);
        msoLogger.debug("Post url is: " + url);

        //now create a soap request message as follows:
        final StringBuffer payload = new StringBuffer();
        payload.append("\n");
        payload.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:req=\"http://com.att.mso/requestsdb\">\n");
        payload.append("<soapenv:Header/>\n");
        payload.append("<soapenv:Body>\n");
        payload.append("<req:getSiteStatus>\n");
        payload.append("<siteName>" + site + "</siteName>\n");
        payload.append("</req:getSiteStatus>\n");
        payload.append("</soapenv:Body>\n");
        payload.append("</soapenv:Envelope>\n");

        msoLogger.debug ("Initialize SOAP request to url:" + url);
        msoLogger.debug ("The payload of the request is:" + payload);
        HttpEntity entity = new StringEntity(payload.toString(),"UTF-8");
        post.setEntity(entity);

        try (CloseableHttpClient client = builder.build()) {
            response = client.execute(post);
            msoLogger.debug("Response received is:" + response);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {

                msoLogger.error(MessageEnum.GENERAL_EXCEPTION_ARG, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError,
                        "Communication with DB Adapter failed, The response received from DB Adapter is with failed status code:" + statusCode);

                Exception e = new Exception("Communication with DB Adapter failed");
                throw e;
            }
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            msoLogger.debug("Content of the response is:" + result);
            String status = result.substring(result.indexOf("<return>") + 8, result.indexOf("</return>"));

            return Boolean.valueOf(status);
        }
    }
}
