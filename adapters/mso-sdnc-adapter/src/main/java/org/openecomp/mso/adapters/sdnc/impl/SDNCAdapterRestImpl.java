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

package org.openecomp.mso.adapters.sdnc.impl;


import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.utils.UUIDChecker;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.logger.MessageEnum;
@Path("/")
public class SDNCAdapterRestImpl {

	private MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	public static final String MSO_PROP_ADAPTER = "MSO_PROP_SDNC_ADAPTER";

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

	@Context
	private HttpHeaders headers;
	@Context HttpServletRequest request;

	@POST
	@Path("/MSORequest")
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response MSORequest(String reqXML) {
	    msoLogger.debug("***Received MSO Rest Request. XML:" + reqXML);

	    Document reqDoc = null;
    	SDNCResponse sdncResp = null;
    	RequestTunables rt = null;
    	String reqId = "";
    	long startTime = System.currentTimeMillis();
    	MsoLogger.setServiceName("UNKNOWN");
		String action = "";
		String operation = "";
	    try {

	    	reqId = headers.getRequestHeader("mso-request-id").get(0);
	    	action = headers.getRequestHeader("mso-request-action").get(0);
	    	operation = headers.getRequestHeader("mso-request-operation").get(0);

	    	MsoLogger.setLogContext(reqId, "");

	    	msoLogger.debug ("Received MSO Rest Request XML: " + reqXML);
			rt = new RequestTunables(reqId, "", operation, action, msoPropertiesFactory);
	    	rt.setTunables();

	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
	    	DocumentBuilder db = dbf.newDocumentBuilder();

	    	InputSource source = new InputSource(new StringReader(reqXML));

	    	reqDoc = db.parse(source);

	    } catch (Exception e) {
	    	msoLogger.error(MessageEnum.RA_PARSING_REQUEST_ERROR, reqXML, "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception - Invalid XML request format", e);
	    	sdncResp = new SDNCResponse(reqId, HttpServletResponse.SC_BAD_REQUEST, "Invalid XML request format");
	    }

		if (reqDoc != null) {
			msoLogger.debug("***Getting response from sdnc***");
			long subStartTime = System.currentTimeMillis ();
			sdncResp = SDNCRestClient.getSdncResp(Utils.genSdncReq(reqDoc, rt), rt,msoPropertiesFactory);
			msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from SDNC", "SDNC", action + "." + operation, null);
		}

		if (sdncResp == null) {
			msoLogger.debug("An Internal Server error has occurred in SDNC Adapter");
			sdncResp = new SDNCResponse(reqId, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MSO - SDNCA Internal Error");
		}

		if (sdncResp.getSdncRespXml() == null) {
            sdncResp.setSdncRespXml(Utils.genMsoFailResp(sdncResp));
        }

		msoLogger.debug("***Completed MSO Rest Request." + sdncResp.toString());
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
		return Response.status(sdncResp.getRespCode()).entity(sdncResp.getSdncRespXml()).build();
	}
	
    @HEAD
    @GET
    @Path("/healthcheck")
    @Produces("text/html")
    public Response healthcheck (@QueryParam("requestId") String requestId) {
		long startTime = System.currentTimeMillis ();
		MsoLogger.setServiceName ("Healthcheck");
		UUIDChecker.verifyOldUUID(requestId, msoLogger);
		HealthCheckUtils healthCheck = new HealthCheckUtils ();
		if (!healthCheck.siteStatusCheck(msoLogger)) {
			return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}

		if (!healthCheck.configFileCheck(msoLogger, startTime, MSO_PROP_ADAPTER)) {
			return HealthCheckUtils.NOT_STARTED_RESPONSE;
		}
		msoLogger.debug("healthcheck - Successful");
		return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
    }

	@HEAD
	@GET
	@Path("/globalhealthcheck")
	@Produces("text/html")
	public Response globalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn) {
		long startTime = System.currentTimeMillis ();
		MsoLogger.setServiceName ("GlobalHealthcheck");
		// Generate a Request Id
		String requestId = UUIDChecker.generateUUID(msoLogger);
		HealthCheckUtils healthCheck = new HealthCheckUtils ();
		if (!healthCheck.siteStatusCheck (msoLogger)) {
			return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}

		if (healthCheck.verifyGlobalHealthCheck(enableBpmn, requestId)) {
			msoLogger.debug("globalHealthcheck - Successful");
			return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
		} else {
			msoLogger.debug("globalHealthcheck - At leaset one of the sub-modules is not available.");
			return  HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}
	}

	@HEAD
	@GET
	@Path("/nodehealthcheck")
	@Produces("text/html")
	public Response nodeHealthcheck () {
		long startTime = System.currentTimeMillis ();
		MsoLogger.setServiceName ("NodeHealthcheck");
		// Generate a Request Id
		String requestId = UUIDChecker.generateUUID(msoLogger);
		HealthCheckUtils healthCheck = new HealthCheckUtils ();
		if (!healthCheck.siteStatusCheck (msoLogger)) {
			return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}

		if (healthCheck.verifyNodeHealthCheck(HealthCheckUtils.NodeType.RA, requestId)) {
			msoLogger.debug("nodeHealthcheck - Successful");
			return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
		} else {
			msoLogger.debug("nodeHealthcheck - At leaset one of the sub-modules is not available.");
			return  HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
		}
	}

}
