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
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpStatus;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
@Path("/")
@Component
@Api(value = "/", description = "Root of SDNCAdapterRestImpl")
public class SDNCAdapterRestImpl {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, SDNCAdapterRestImpl.class);
	
	private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";
	 
	public static final Response HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_OK)
	            .entity (CHECK_HTML)
	            .build ();
	
	@Context
	private HttpHeaders headers;
	@Context HttpServletRequest request;

	@Autowired
	private MapRequestTunables tunablesMapper;
	
	@Autowired
	private SDNCRestClient sdncClient;
	
	@POST
	@Path("/MSORequest")
	@Consumes("application/xml")
	@Produces("application/xml")
	@ApiOperation(value="Calls SDNC with an MSORequest object in SDNCRestImpl",response = Response.class)
	public Response MSORequest(String reqXML) {
	    msoLogger.debug("***Received MSO Rest Request. XML:" + reqXML);

	    Document reqDoc = null;
    	SDNCResponse sdncResp = null;
    	RequestTunables rt = null;
    	String reqId = "";
    	long startTime = System.currentTimeMillis();
    	MsoLogger.setServiceName("/MSORequest");
		String action = "";
		String operation = "";
	    try {

	    	reqId = headers.getRequestHeader("mso-request-id").get(0);
	    	action = headers.getRequestHeader("mso-request-action").get(0);
	    	operation = headers.getRequestHeader("mso-request-operation").get(0);

	    	MsoLogger.setLogContext(reqId, "");

	    	msoLogger.debug ("Received MSO Rest Request XML: " + reqXML);
			rt = new RequestTunables(reqId, "", operation, action);
			rt = tunablesMapper.setTunables(rt);

	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
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
			sdncResp = sdncClient.getSdncResp(Utils.genSdncReq(reqDoc, rt), rt);
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
    @Path("/healthcheck")
    @Produces("text/html")
    @ApiOperation(value="HealthCheck for SDNCRestImpl",response = Response.class)
    public Response headHealthcheck (@QueryParam("requestId") String requestId) {
    	MsoLogger.setServiceName("Healthcheck");	
		return HEALTH_CHECK_RESPONSE;
    }
    
    @GET
    @Path("/healthcheck")
    @Produces("text/html")
    @ApiOperation(value="HealthCheck for SDNCRestImpl",response = Response.class)
    public Response healthcheck (@QueryParam("requestId") String requestId) {
    	MsoLogger.setServiceName("Healthcheck");		
		return HEALTH_CHECK_RESPONSE;
    }

	@HEAD
	@Path("/globalhealthcheck")
	@Produces("text/html")
	public Response headGlobalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn) {
		MsoLogger.setServiceName("Healthcheck");		
		return HEALTH_CHECK_RESPONSE;
	}
	
	@GET
	@Path("/globalhealthcheck")
	@Produces("text/html")
	public Response globalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn) {
		MsoLogger.setServiceName("Healthcheck");		
		return HEALTH_CHECK_RESPONSE;
	}

	@HEAD
	@Path("/nodehealthcheck")
	@Produces("text/html")
	@ApiOperation(value="NodeHealthCheck for SDNCRestImpl",response = Response.class)
	public Response headNodeHealthcheck () throws UnknownHostException {
		MsoLogger.setServiceName("Healthcheck");		
		return HEALTH_CHECK_RESPONSE;
	}
	
	@GET
	@Path("/nodehealthcheck")
	@Produces("text/html")
	@ApiOperation(value="NodeHealthCheck for SDNCRestImpl",response = Response.class)
	public Response nodeHealthcheck () {
		MsoLogger.setServiceName("Healthcheck");		
		return HEALTH_CHECK_RESPONSE;
	}

}
