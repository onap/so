/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.sdnc.notify;


import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.openecomp.mso.utils.UUIDChecker;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.openecomp.mso.adapters.sdnc.impl.Constants;
import org.openecomp.mso.adapters.sdnc.impl.SDNCAdapterPortTypeImpl;
import org.openecomp.mso.adapters.sdnc.impl.SDNCResponse;
import org.openecomp.mso.adapters.sdnc.impl.SDNCRestClient;
import org.openecomp.mso.adapters.sdnc.util.SDNCRequestIdUtil;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.logger.MessageEnum;

//SDNC to SDNC Async Notifcations
@Path("/")
public class SDNCNotifyResource {

	private MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();
	
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

    @GET()
    public Response printMessage () {
        long startTime = System.currentTimeMillis ();
        UUIDChecker.generateUUID (msoLogger);
        String result = "SDNCAdapter Rest services";
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        return Response.status (HttpServletResponse.SC_OK).entity (result).build ();

    }

    @GET()
    @Path("/{param}")
    public Response printMessageParam (@PathParam("param") String msg) {
        long startTime = System.currentTimeMillis ();
        UUIDChecker.generateUUID (msoLogger);
        String result = "SDNCAdapter Rest services : " + msg;
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        return Response.status (HttpServletResponse.SC_OK).entity (result).build ();

    }

    @POST
    @Path("/SDNCNotify")
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response SDNCNotify (String reqXML, @Context HttpServletRequest request) {

        XPathFactory xpathFactory = XPathFactory.newInstance ();
        XPath xpath = xpathFactory.newXPath ();
        long startTime = System.currentTimeMillis ();

        msoLogger.info (MessageEnum.RA_RECEIVE_SDNC_NOTIF, reqXML, "SDNC", "SDNCNotify");

        InputSource source = new InputSource (new StringReader (reqXML));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();

        DocumentBuilder db;

        try {
            dbf.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
            db = dbf.newDocumentBuilder ();
        } catch (ParserConfigurationException e) {
            msoLogger.error (MessageEnum.RA_PARSING_REQUEST_ERROR, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.SchemaError, "Exception - Invalid XML request format", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Invalid XML request format");
            return Response.status (HttpServletResponse.SC_BAD_REQUEST).entity ("Invalid XML request format").build ();
        }

        Document doc = null;
        try {
            doc = db.parse (source);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.RA_PARSING_REQUEST_ERROR, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.SchemaError, "Exception - Invalid XML request format", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Invalid XML request format");
            return Response.status (HttpServletResponse.SC_BAD_REQUEST).entity ("Invalid XML request format").build ();
        }

        try {
            NodeList nl = (NodeList) xpath.evaluate (Constants.SDNC_SVCCFGRESP_ROOT, doc, XPathConstants.NODESET);
            if (nl.getLength () <= 0) {
                msoLogger.error (MessageEnum.RA_MISSING_PARAM, Constants.SDNC_SVCCFGRESP_ROOT, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.DataError, "Missing param");
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "Missing" + Constants.SDNC_SVCCFGRESP_ROOT);
                return Response.status (HttpServletResponse.SC_BAD_REQUEST)
                               .entity ("Missing " + Constants.SDNC_SVCCFGRESP_ROOT)
                               .build ();
            }
        } catch (Exception e) {
            msoLogger.error (MessageEnum.RA_MISSING_PARAM, Constants.SDNC_SVCCFGRESP_ROOT, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.DataError, "Exception - Missing param", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "Missing" + Constants.SDNC_SVCCFGRESP_ROOT);
            return Response.status (HttpServletResponse.SC_BAD_REQUEST)
                           .entity ("Missing " + Constants.SDNC_SVCCFGRESP_ROOT)
                           .build ();
        }

        String reqId;
        try {
            reqId = xpath.evaluate (Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_REQ_ID, doc);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.RA_MISSING_PARAM, Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_REQ_ID, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.DataError, "Exception - Missing param", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "Missing" + Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_REQ_ID);
            return Response.status (HttpServletResponse.SC_BAD_REQUEST)
                           .entity ("Missing " + Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_REQ_ID)
                           .build ();
        }

        MsoLogger.setLogContext (SDNCRequestIdUtil.getSDNCOriginalRequestId (reqId), "");

        String respCode;
        try {
            respCode = xpath.evaluate (Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_RESP_CODE, doc);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.RA_MISSING_PARAM,
                             Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_RESP_CODE, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.DataError, "Exception - Missing param",
                             e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.BadRequest, "Missing" + Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_RESP_CODE);
            return Response.status (HttpServletResponse.SC_BAD_REQUEST)
                           .entity ("Missing" + Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_RESP_CODE)
                           .build ();
        }

        String respMsg = "";
        try {
            respMsg = xpath.evaluate (Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_RESP_MSG, doc);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.RA_MISSING_PARAM, Constants.SDNC_SVCCFGRESP_ROOT + Constants.SDNC_RESP_MSG, "SDNC", "SDNCNotify", MsoLogger.ErrorCode.DataError, "Exception - Missing param", e);
        }

        String bpelUrl;
        /*
         * TODO Hibernate
         * try {
         * bpelUrl = RequestsDatabase.getBpelUrl(reqId);
         * }
         * catch (Exception e)
         * {
         * logger.error("Unable to get SDNC_CALLBACK_URL from ActiveRequests, using default for reqid:" + reqId, e);
         * }
         */
        
        bpelUrl = SDNCAdapterPortTypeImpl.getProperty (Constants.BPEL_URL_PROP, Constants.DEFAULT_BPEL_URL,msoPropertiesFactory);
        if (bpelUrl == null) {
            msoLogger.debug("bpelUrl is NULL:");
        }

        SDNCResponse sdncResp = new SDNCResponse (reqId);
        sdncResp.setRespCode (Integer.parseInt (respCode));
        sdncResp.setRespMsg (respMsg);
        sdncResp.setSdncRespXml (reqXML);
        long subStartTime = System.currentTimeMillis ();
        SDNCRestClient.sendRespToBpel (bpelUrl, sdncResp,msoPropertiesFactory);
        msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully send request to BPMN", "BPMN", bpelUrl, null);

        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        return Response.ok ().build ();
    }
}
