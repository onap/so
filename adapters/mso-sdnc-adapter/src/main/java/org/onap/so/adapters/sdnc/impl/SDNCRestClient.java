/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.sdnc.impl;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.onap.so.utils.CryptoUtils;
import org.onap.so.adapters.sdnc.SDNCAdapterRequest;
import org.onap.so.adapters.sdnc.client.CallbackHeader;
import org.onap.so.adapters.sdnc.client.SDNCAdapterCallbackRequest;
import org.onap.so.adapters.sdnc.client.SDNCCallbackAdapterPortType;
import org.onap.so.adapters.sdnc.client.SDNCCallbackAdapterService;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Component
public class SDNCRestClient{

	@Autowired
	private Environment env;
	
	@Autowired
	private MapRequestTunables tunablesMapper;

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA,SDNCRestClient.class);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
    private static final String EXCEPTION_MSG="Exception while evaluate xpath";
    private static final String MSO_INTERNAL_ERROR="MsoInternalError";
    private static final String CAMUNDA="Camunda";
	@Async
	public void executeRequest(SDNCAdapterRequest bpelRequest)
	{
		
		msoLogger.debug("BPEL Request:" + bpelRequest.toString());

		// Added delay to allow completion of create request to SDNC
		// before executing activate of create request.
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String action = bpelRequest.getRequestHeader().getSvcAction();
		String operation = bpelRequest.getRequestHeader().getSvcOperation();
		String bpelReqId = bpelRequest.getRequestHeader().getRequestId();
		String callbackUrl = bpelRequest.getRequestHeader().getCallbackUrl();

		String sdncReqBody = null;

	

		RequestTunables rt = new RequestTunables(bpelReqId,
				bpelRequest.getRequestHeader().getMsoAction(),
				bpelRequest.getRequestHeader().getSvcOperation(),
				bpelRequest.getRequestHeader().getSvcAction());		
		rt = tunablesMapper.setTunables(rt);
		rt.setSdncaNotificationUrl(env.getProperty(Constants.MY_URL_PROP));


		if ("POST".equals(rt.getReqMethod()))
		{
			Node node = (Node) 	bpelRequest.getRequestData();
    		Document reqDoc = node.getOwnerDocument();
			sdncReqBody = Utils.genSdncReq(reqDoc, rt);
		}
		else if("PUT".equals(rt.getReqMethod())){
			Node node = (Node) 	bpelRequest.getRequestData();
    		Document reqDoc = node.getOwnerDocument();
			sdncReqBody = Utils.genSdncPutReq(reqDoc, rt);
		}
		long sdncStartTime = System.currentTimeMillis();
		SDNCResponse sdncResp = getSdncResp(sdncReqBody, rt);
		msoLogger.recordMetricEvent (sdncStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from SDNC", "SDNC", action + "." + operation, null);
		msoLogger.debug ("Got the SDNC Response: " + sdncResp.getSdncRespXml());
		long bpelStartTime = System.currentTimeMillis();
		sendRespToBpel(callbackUrl, sdncResp);
		msoLogger.recordMetricEvent (bpelStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully send reauest to BPEL", "BPMN", callbackUrl, null);
		return;
	}

	public SDNCResponse getSdncResp(String sdncReqBody, RequestTunables rt)
	{

		URL url;
		HttpURLConnection con = null;
		DataOutputStream out = null;
		BufferedReader in = null;
		SDNCResponse sdncResp = new SDNCResponse(rt.getReqId());
		StringBuilder response = new StringBuilder();

		msoLogger.info(MessageEnum.RA_SEND_REQUEST_SDNC.name() + ":\n" + rt.toString(), "SDNC", "");
		msoLogger.trace("SDNC Request Body:\n" + sdncReqBody);

		try {

			url = new URL(rt.getSdncUrl());

			con = (HttpURLConnection) url.openConnection();
		    con.setConnectTimeout(Integer.parseInt(env.getProperty(Constants.SDNC_CONNECTTIME_PROP)));
		    con.setReadTimeout(Integer.parseInt(rt.getTimeout()));
			con.setRequestProperty("Accept", "application/yang.data+xml"); //for response in xml
			String userCredentials = CryptoUtils.decryptProperty(env.getProperty(Constants.SDNC_AUTH_PROP), Constants.DEFAULT_SDNC_AUTH, Constants.ENCRYPTION_KEY);

			String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
			con.setRequestProperty ("Authorization", basicAuth);
		    con.setRequestMethod(rt.getReqMethod());

			// Add request headers
		    if ("POST".equals(rt.getReqMethod()) || "PUT".equals(rt.getReqMethod()))
		    {
		    	con.setRequestProperty("Content-type", "application/xml");
		    	con.setRequestProperty("Content-length",String.valueOf(sdncReqBody.length()));
				con.setDoOutput(true);
				out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(sdncReqBody);
				out.flush();
				out.close();
		    }

			//Get response
			sdncResp.setRespCode(con.getResponseCode());
			sdncResp.setRespMsg(con.getResponseMessage());

			if (con.getResponseCode()>= 200 && con.getResponseCode()<=299) { 
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));	
				String inputLine;
				//Not parsing the response -it contains a responseHdr section and data section
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
			
			sdncResp.setSdncRespXml(response.toString());
			msoLogger.info(MessageEnum.RA_RESPONSE_FROM_SDNC.name() + ":\n" + sdncResp.toString(), "SDNC", "");
			return(sdncResp);
		}
		catch (Exception e)
		{
			msoLogger.error(MessageEnum.RA_EXCEPTION_COMMUNICATE_SDNC, "SDNC", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception processing request to SDNC", e);
			//default
			sdncResp.setRespCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
			String respMsg = "Error processing request to SDNC. ";
			StringBuilder sdncErrMsg = new StringBuilder();

			if (e instanceof java.net.SocketTimeoutException )
			{
				sdncResp.setRespCode(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
				respMsg = "Request to SDNC timed out. ";
			}
			if (con != null)
			{
				try { //e1
					if (con.getResponseCode() != HttpURLConnection.HTTP_OK) //seen in SocketException connection reset 
						sdncResp.setRespCode(con.getResponseCode());
					respMsg = respMsg + con.getResponseMessage() + ". ";
					InputStream is = con.getErrorStream();
					if (is != null)
					{
						XPathFactory xpathFactory = XPathFactory.newInstance();
					    XPath xpath = xpathFactory.newXPath();
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
						dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
						dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
						DocumentBuilder db;
						Document doc = null;
						try { //e2
							db = dbf.newDocumentBuilder();
							doc = db.parse(is);
							NodeList errors = (NodeList)xpath.evaluate("errors/error", doc, XPathConstants.NODESET);
							for (int i = 0; i < errors.getLength(); i++)
							{
								Element error = (Element) errors.item(i);
								String eType = null;
								try {
									eType = xpath.evaluate("error-type", error);
									sdncErrMsg = new StringBuilder(". SDNC Returned-[error-type:" + eType);
								} catch (Exception e3) {
								    msoLogger.error (MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-type", error.toString(), "SDNC", "", MsoLogger.ErrorCode.DataError, EXCEPTION_MSG, e3);
								}

								String eTag = null;
								try {
									eTag = xpath.evaluate( "error-tag", error);
									sdncErrMsg.append(", error-tag:").append(eTag);
								} catch (Exception e3) {
									msoLogger.error (MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-tag", error.toString(), "SDNC", "", MsoLogger.ErrorCode.DataError, EXCEPTION_MSG, e3);
								}

								String eMsg = null;
								try {
									eMsg = xpath.evaluate("error-message", error);
									sdncErrMsg.append(", error-message:").append(eMsg).append("]");
								} catch (Exception e3) {
									msoLogger.error (MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-message", error.toString(), "SDNC", "", MsoLogger.ErrorCode.DataError, EXCEPTION_MSG, e3);
								}
							}
						} catch (Exception e2) {
						    msoLogger.error (MessageEnum.RA_ANALYZE_ERROR_EXC, "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception while analyse error", e2);
						}
					} //is != null
				} catch (Exception e1) {
					msoLogger.error (MessageEnum.RA_ERROR_GET_RESPONSE_SDNC, "SDNC", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception while get SDNC response", e1);
				}
			} //con != null

			if (e.getMessage() != null) {
                respMsg = respMsg + e.getMessage();
            }
            respMsg = respMsg + sdncErrMsg;

			sdncResp.setRespMsg(respMsg);

			msoLogger.error(MessageEnum.RA_EXCEPTION_COMMUNICATE_SDNC, "SDNC", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with SDNC", e);
			alarmLogger.sendAlarm(MSO_INTERNAL_ERROR, MsoAlarmLogger.CRITICAL, respMsg);
			return sdncResp;
		}
		finally
		{
			if (con != null) {
                con.disconnect();
            }
		}
	}

	public void sendRespToBpel(String bpelUrl, SDNCResponse sdncResp)
	{
		String error;
		try
		{
			SDNCAdapterCallbackRequest cbReq = new SDNCAdapterCallbackRequest();
			cbReq.setCallbackHeader(new CallbackHeader(sdncResp.getReqId(), Integer.toString(sdncResp.getRespCode()), sdncResp.getRespMsg()));
			if (sdncResp.getSdncRespXml() != null)
			{
				cbReq.setRequestData(sdncResp.getSdncRespXml());
			}
			msoLogger.info(MessageEnum.RA_CALLBACK_BPEL.name() + ":\n" + cbReq.toString(), CAMUNDA, "");

			URL wsdlUrl = null;
			try {
				wsdlUrl = new URL (bpelUrl);
			} catch (MalformedURLException e1) {
				error = "Caught exception initializing Callback wsdl " + e1.getMessage();
				msoLogger.error(MessageEnum.RA_INIT_CALLBACK_WSDL_ERR, CAMUNDA, "", MsoLogger.ErrorCode.DataError, "Exception initializing Callback wsdl", e1);
				alarmLogger.sendAlarm(MSO_INTERNAL_ERROR, MsoAlarmLogger.CRITICAL, error);
			}

			SDNCCallbackAdapterService cbSvc = new SDNCCallbackAdapterService();

			SDNCCallbackAdapterPortType cbPort = cbSvc.getSDNCCallbackAdapterSoapHttpPort();

			BindingProvider bp = (BindingProvider)cbPort;
			
			if(null != wsdlUrl) {
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlUrl.toExternalForm());
			}
			else {
			    msoLogger.debug("wsdlUrl is NULL:");
			}

			//authentication
			try
			{
				Map<String, Object> reqCtx = bp.getRequestContext();
				Map<String, List<String>> headers = new HashMap<>();
				String userCredentials = CryptoUtils.decryptProperty(env.getProperty(Constants.BPEL_AUTH_PROP), Constants.DEFAULT_BPEL_AUTH, Constants.ENCRYPTION_KEY);

				String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
				reqCtx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
				headers.put ("Authorization", Collections.singletonList(basicAuth));
			}
			catch (Exception e2) {
				error = "Unable to set authorization in callback request " + e2.getMessage();
				msoLogger.error(MessageEnum.RA_SET_CALLBACK_AUTH_EXC, CAMUNDA, "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception - Unable to set authorization in callback request", e2);
				alarmLogger.sendAlarm(MSO_INTERNAL_ERROR, MsoAlarmLogger.CRITICAL, error);
			}

			msoLogger.debug("Invoking Bpel Callback. BpelCallbackUrl:" + bpelUrl);
			cbPort.sdncAdapterCallback(cbReq);

		}
		catch (Exception e)
		{
			error = "Error sending BpelCallback request" + e.getMessage();
			msoLogger.error("Error " + MsoLogger.ErrorCode.BusinessProcesssError + " - " + MessageEnum.RA_CALLBACK_BPEL_EXC + " - " + error, e);
			alarmLogger.sendAlarm(MSO_INTERNAL_ERROR, MsoAlarmLogger.CRITICAL, error);
		}
		msoLogger.info(MessageEnum.RA_CALLBACK_BPEL_COMPLETE.name(), CAMUNDA, "");
		return;
	}

}
