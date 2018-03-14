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

package org.openecomp.mso.client.sdnc.sync;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openecomp.mso.client.sdnc.beans.SDNCRequest;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;

//SDNCAdapter to SDNC Rest Client
public class SDNCSyncRpcClient implements Runnable {

	private MsoPropertiesFactory msoPropertiesFactory;
	
	private SDNCRequest bpelRequest;

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	public static final String MSO_PROP_SDNC_ADAPTER="MSO_PROP_SDNC_ADAPTER";


	public SDNCSyncRpcClient(SDNCRequest bpelRequest,MsoPropertiesFactory msoPropFactory) {
		this.bpelRequest = bpelRequest;
		msoPropertiesFactory = msoPropFactory;
	}

	@Override
	public void run()
	{
		String action = bpelRequest.getSvcAction().toString();
		String operation = bpelRequest.getSvcOperation().toString();
		String bpelReqId = bpelRequest.getRequestId();
		String msoAction = bpelRequest.getMsoAction();
		MsoLogger.setLogContext(SDNCRequestIdUtil.getSDNCOriginalRequestId (bpelReqId), bpelRequest.getSvcInstanceId());
		MsoLogger.setServiceName("SDNCRestClient");
		String sdncReqBody = "";

		msoLogger.debug("BPEL Request:" + bpelRequest.toString());
		RequestTunables rt = new RequestTunables(bpelReqId, msoAction, operation, action, msoPropertiesFactory);
		rt.setTunables();
		rt.setSdncaNotificationUrl(SDNCAdapterPortTypeImpl.getProperty(Constants.MY_URL_PROP, Constants.DEFAULT_MY_URL,msoPropertiesFactory));

		if ("POST".equals(rt.getReqMethod())) {
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(bpelRequest.getRequestData()));
				Document reqDoc = db.parse(is);
				sdncReqBody = Utils.genSdncReq(reqDoc, rt);
			}catch(Exception ex) {
				throw new IllegalStateException();
			}
		} else if("PUT".equals(rt.getReqMethod())) {
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(bpelRequest.getRequestData()));
				Document reqDoc = db.parse(is);
				sdncReqBody = Utils.genSdncPutReq(reqDoc, rt);
			}catch(Exception ex) {
				throw new IllegalStateException();
			}
		}
		long sdncStartTime = System.currentTimeMillis();
		SDNCResponse sdncResp = getSdncResp(sdncReqBody, rt, msoPropertiesFactory);
		msoLogger.recordMetricEvent (sdncStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from SDNC", "SDNC", action + "." + operation, null);
		msoLogger.debug ("Got the SDNC Code : " + sdncResp.getRespCode());
		msoLogger.debug ("Got the SDNC Response Message:" + sdncResp.getRespMsg());
		validateSDNCResponse(sdncResp.getSdncResp());
		return;
	}

	public static SDNCResponse getSdncResp(String sdncReqBody, RequestTunables rt, MsoPropertiesFactory msoPropertiesFactoryp) {
		URL url;
		HttpURLConnection con = null;
		DataOutputStream out = null;
		BufferedReader in = null;
		SDNCResponse sdncResp = new SDNCResponse(rt.getReqId());
		StringBuffer response = new StringBuffer();

		msoLogger.info(MessageEnum.RA_SEND_REQUEST_SDNC, rt.toString(), "SDNC", "");
		msoLogger.debug("SDNC Request Body:\n" + sdncReqBody);

		try {
			msoLogger.debug("url is: " + rt.getSdncUrl());
			url = new URL(rt.getSdncUrl());
			con = (HttpURLConnection) url.openConnection();
		    con.setConnectTimeout(Integer.parseInt(SDNCAdapterPortTypeImpl.getProperty(Constants.SDNC_CONNECTTIME_PROP, "2000",msoPropertiesFactoryp)));
		    con.setReadTimeout(Integer.parseInt(rt.getTimeout()));
			con.setRequestProperty("Accept", "application/json");
			String userCredentials = msoPropertiesFactoryp.getMsoJavaProperties(MSO_PROP_SDNC_ADAPTER).getEncryptedProperty(Constants.SDNC_AUTH_PROP, Constants.DEFAULT_SDNC_AUTH, Constants.ENCRYPTION_KEY);

			String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
			con.setRequestProperty ("Authorization", basicAuth);
		    con.setRequestMethod(rt.getReqMethod());

		    if ("POST".equals(rt.getReqMethod()) || "PUT".equals(rt.getReqMethod())) {
		    	con.setRequestProperty("Content-type", "application/json");
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
			
			sdncResp.setSdncResp(response.toString());
			msoLogger.info(MessageEnum.RA_RESPONSE_FROM_SDNC, sdncResp.toString(), "SDNC", "");
			return(sdncResp);
		} catch (Exception e) {
			msoLogger.error(MessageEnum.RA_EXCEPTION_COMMUNICATE_SDNC, "SDNC", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception processing request to SDNC", e);
			//default
			sdncResp.setRespCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
			String respMsg = "Error processing request to SDNC. ";
			String sdncErrMsg = null;

			if (e instanceof java.net.SocketTimeoutException ) {
				sdncResp.setRespCode(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
				respMsg = "Request to SDNC timed out. ";
			}
			if (con != null) {
				try { //e1
					if (con.getResponseCode() != HttpURLConnection.HTTP_OK) //seen in SocketException connection reset 
						sdncResp.setRespCode(con.getResponseCode());
					respMsg = respMsg + con.getResponseMessage() + ". ";
					InputStream is = con.getErrorStream();
					if (is != null) {
						XPathFactory xpathFactory = XPathFactory.newInstance();
					    XPath xpath = xpathFactory.newXPath();
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
						DocumentBuilder db;
						Document doc = null;
						try { //e2
							db = dbf.newDocumentBuilder();
							doc = db.parse(is);
							NodeList errors = (NodeList)xpath.evaluate("errors/error", doc, XPathConstants.NODESET);
							for (int i = 0; i < errors.getLength(); i++) {
								Element error = (Element) errors.item(i);
								String eType = null;
								try {
									eType = xpath.evaluate("error-type", error);
									sdncErrMsg = ". SDNC Returned-[error-type:" + eType;
								} catch (Exception e3) {
								    msoLogger.error (MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-type", error.toString(), "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception while evaluate xpath", e3);
								}

								String eTag = null;
								try {
									eTag = xpath.evaluate( "error-tag", error);
									sdncErrMsg = sdncErrMsg + ", error-tag:" + eTag;
								} catch (Exception e3) {
									msoLogger.error (MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-tag", error.toString(), "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception while evaluate xpath", e3);
								}

								String eMsg = null;
								try {
									eMsg = xpath.evaluate("error-message", error);
									sdncErrMsg = sdncErrMsg + ", error-message:" + eMsg + "]";
								} catch (Exception e3) {
									msoLogger.error (MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-message", error.toString(), "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception while evaluate xpath", e3);
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
			if (sdncErrMsg != null) {
                respMsg = respMsg + sdncErrMsg;
            }

			sdncResp.setRespMsg(respMsg);

			msoLogger.error(MessageEnum.RA_EXCEPTION_COMMUNICATE_SDNC, "SDNC", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with SDNC", e);
			alarmLogger.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, respMsg);
			return(sdncResp);
		}
		finally {
			if (con != null) {
                con.disconnect();
            }
		}
	}
	public void validateSDNCResponse (String sdncResponse){
		String msg;
		msoLogger.debug ("Starting validate sdnc response");
		String responseMessage = "";
		String responseCode = "";
		if (sdncResponse != null || !sdncResponse.equals("")){
			try{
				msoLogger.debug ("Got the SDNC Response: " + sdncResponse);
				JSONObject jsonObj = new JSONObject(sdncResponse);
				msoLogger.debug ("jsonObj has been created");
				
				JSONObject requestData = jsonObj.getJSONObject("v1:RequestData");
				JSONObject output =  requestData.getJSONObject("output");
				try{
					responseMessage = output.getString("response-message");
					responseCode = output.getString("response-code");	
				} catch (Exception ex) {
					msoLogger.debug("Response not in lower hyphen");
				}
				if(responseMessage.equals("")&&responseCode.equals("")){
					try{
						responseMessage = output.getString("ResponseMessage");
						responseCode = output.getString("ResponseCode");	
					} catch (Exception ex) {
						msoLogger.debug("Response does not exist");
					}
				}
				msoLogger.debug("ResponseMessage is: " + responseMessage);
				msoLogger.debug("Response Code is: " + responseCode);
				if(responseMessage.equals("")){
					msg = "Error from SDNC: Response Message is empty.";
					msoLogger.debug(msg);
					throw new IllegalStateException(msg);
				}
				
				if(responseCode.equals("")){
					responseCode = "0";
				}
				
				int code = Integer.parseInt(responseCode);
				if(code >=200 && code <=299 || code ==0){
					msoLogger.debug ("Successful Response from SDNC");
	
				} else {
					msg = "Error from SDNC: Code is not 200-299 or 0.";
					msoLogger.debug(msg);
					throw new IllegalStateException(msg);
				}
			} catch (Exception ex) {
				msg = "Validate SDNC Response has failed.";
				msoLogger.debug(msg);
				throw new IllegalStateException(msg);
			}
		}
	}
}
