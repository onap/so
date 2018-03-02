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
package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.openecomp.mso.adapters.sdnc.impl.Constants;
import org.openecomp.mso.adapters.sdncrest.SDNCErrorCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

/**
 * Sends requests to SDNC and processes the responses.
 */
public abstract class SDNCConnector {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
	private static final MsoAlarmLogger ALARMLOGGER = new MsoAlarmLogger();

	public SDNCResponseCommon send(String content, TypedRequestTunables rt) {
		LOGGER.info(MessageEnum.RA_SEND_REQUEST_SDNC, rt.toString(), "SDNC", "");
		LOGGER.debug("SDNC Request Body:\n" + content);

		HttpRequestBase method = null;
		HttpResponse httpResponse = null;

		try {
			int timeout = Integer.parseInt(rt.getTimeout());

			RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.build();

			HttpClient client = HttpClientBuilder.create().build();

			if ("POST".equals(rt.getReqMethod())) {
				HttpPost httpPost = new HttpPost(rt.getSdncUrl());
				httpPost.setConfig(requestConfig);
				httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_XML));
				method =  httpPost;
			} else if ("PUT".equals(rt.getReqMethod())) {
				HttpPut httpPut = new HttpPut(rt.getSdncUrl());
				httpPut.setConfig(requestConfig);
				httpPut.setEntity(new StringEntity(content, ContentType.APPLICATION_XML));
				method =  httpPut;
			} else if ("GET".equals(rt.getReqMethod())) {
				HttpGet httpGet = new HttpGet(rt.getSdncUrl());
				httpGet.setConfig(requestConfig);
				method =  httpGet;
			} else if ("DELETE".equals(rt.getReqMethod())) {
				HttpDelete httpDelete = new HttpDelete(rt.getSdncUrl());
				httpDelete.setConfig(requestConfig);
				method =  httpDelete;
			}

			// AAF Integration, disabled for now due to the constrains from other party
			// String userCredentials = CredentialConstants.getSecurityProperties().getEncryptedProperty(CredentialConstants.DEFAULT_AUTH, "", CredentialConstants.getEncryptionKey());
            // if (userCredentials == null) {
            //		userCredentials = "";
            //}
			String userCredentials = SDNCAdapterProperties.getEncryptedProperty(Constants.SDNC_AUTH_PROP,
				Constants.DEFAULT_SDNC_AUTH, Constants.ENCRYPTION_KEY);
			String authorization = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
			if(null != method) {
			    method.setHeader("Authorization", authorization);
			    method.setHeader("Accept", "application/yang.data+xml");
			}
			else {
			    LOGGER.debug("method is NULL:");
			}

			

			httpResponse = client.execute(method);

			String responseContent = null;
			if (httpResponse.getEntity() != null) {
				responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			}

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			LOGGER.debug("SDNC Response: " + statusCode + " " + statusMessage
				+ (responseContent == null ? "" : System.lineSeparator() + responseContent));

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {
				String errMsg = "SDNC returned " + statusCode + " " + statusMessage;

				String errors = analyzeErrors(responseContent);
				if (errors != null) {
					errMsg += " " + errors;
				}

				logError(errMsg);
				ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, errMsg);
				return createErrorResponse(statusCode, errMsg, rt);
			}

			httpResponse = null;
			
			if(null != method) {
	            method.reset();
			}
            else {
                LOGGER.debug("method is NULL:");
            }

			method = null;

			LOGGER.info(MessageEnum.RA_RESPONSE_FROM_SDNC, responseContent, "SDNC", "");
			return createResponseFromContent(statusCode, statusMessage, responseContent, rt);

		} catch (SocketTimeoutException e) {
			String errMsg = "Request to SDNC timed out";
			logError(errMsg, e);
			return createErrorResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, errMsg, rt);

		} catch (ConnectTimeoutException e) {
			String errMsg = "Request to SDNC timed out";
			logError(errMsg, e);
			return createErrorResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, errMsg, rt);

		} catch (Exception e) {
			String errMsg = "Error processing request to SDNC";
			logError(errMsg, e);
			return createErrorResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, errMsg, rt);

		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
				} catch (Exception e) {
				    LOGGER.debug("Exception:", e);
				}
			}

			if (method != null) {
				try {
					method.reset();
				} catch (Exception e) {
				    LOGGER.debug("Exception:", e);
				}
			}
		}
	}

	protected void logError(String errMsg) {
		LOGGER.error(MessageEnum.RA_EXCEPTION_COMMUNICATE_SDNC, "SDNC", "",
			MsoLogger.ErrorCode.AvailabilityError, errMsg);
		ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, errMsg);
	}

	protected void logError(String errMsg, Throwable t) {
		LOGGER.error(MessageEnum.RA_EXCEPTION_COMMUNICATE_SDNC, "SDNC", "",
			MsoLogger.ErrorCode.AvailabilityError, errMsg, t);
		ALARMLOGGER.sendAlarm("MsoInternalError", MsoAlarmLogger.CRITICAL, errMsg);
	}

	/**
	 * Generates a response object from content received from SDNC.  The response
	 * object may be a success response object or an error response object. This
	 * method must be overridden by the subclass to return the correct object type.
	 * @param statusCode the response status code from SDNC (e.g. 200)
	 * @param statusMessage the response status message from SDNC (e.g. "OK")
	 * @param responseContent the body of the response from SDNC (possibly null)
	 * @param rt request tunables
	 * @return a response object
	 */
	protected abstract SDNCResponseCommon createResponseFromContent(int statusCode,
			String statusMessage, String responseContent, TypedRequestTunables rt);

	/**
	 * Generates an error response object. This method must be overridden by the
	 * subclass to return the correct object type.
	 * @param statusCode the response status code (from SDNC, or internally generated)
	 * @param errMsg the error message (normally a verbose explanation of the error)
	 * @param rt request tunables
	 * @return an error response object
	 */
	protected abstract SDNCErrorCommon createErrorResponse(int statusCode,
			String errMsg, TypedRequestTunables rt);

	/**
	 * Called by the send() method to analyze errors that may be encoded in the
	 * content of non-2XX responses.  By default, this method tries to parse the
	 * content as a restconf error.
	 * <pre>
	 *     xmlns="urn:ietf:params:xml:ns:yang:ietf-restconf"
	 * </pre>
	 * If an error (or errors) can be obtained from the content, then the result
	 * is a string in this format:
	 * <pre>
	 * [error-type:TYPE, error-tag:TAG, error-message:MESSAGE] ...
	 * </pre>
	 * If no error could be obtained from the content, then the result is null.
	 * <p>
	 * The subclass can override this method to provide another implementation.
	 */
	protected String analyzeErrors(String content) {
		if (content == null || content.isEmpty()) {
			return null;
		}

		// Confirmed with Andrew Shen on 11/1/16 that SDNC will send content like
		// this in error responses (non-2XX response codes) to "agnostic" service
		// requests.
		//
		// <errors xmlns="urn:ietf:params:xml:ns:yang:ietf-restconf">
		//   <error>
		//     <error-type>protocol</error-type>
		//     <error-tag>malformed-message</error-tag>
		//     <error-message>Error parsing input: The element type "input" must be terminated by the matching end-tag "&lt;/input&gt;".</error-message>
		//   </error>
		// </errors>

		StringBuilder output = null;

		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			InputSource source = new InputSource(new StringReader(content));
			Document doc = documentBuilderFactory.newDocumentBuilder().parse(source);
			NodeList errors = (NodeList) xpath.evaluate("errors/error", doc, XPathConstants.NODESET);

			for (int i = 0; i < errors.getLength(); i++)
			{
				Element error = (Element) errors.item(i);

				String info = "";

				try {
					String errorType = xpath.evaluate("error-type", error);
					info += "error-type:" + errorType;
				} catch (XPathExpressionException e) {
				    LOGGER.error(MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-type", error.toString(), "SDNC", "",
				    	MsoLogger.ErrorCode.DataError, "XPath Exception", e);
				}

				try {
					String errorTag = xpath.evaluate( "error-tag", error);
					if (!info.isEmpty()) {
						info += ", ";
					}
					info += "error-tag:" + errorTag;
				} catch (XPathExpressionException e) {
					LOGGER.error(MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-tag", error.toString(), "SDNC", "",
						MsoLogger.ErrorCode.DataError, "XPath Exception", e);
				}

				try {
					String errorMessage = xpath.evaluate("error-message", error);
					if (!info.isEmpty()) {
						info += ", ";
					}
					info += "error-message:" + errorMessage;
				} catch (Exception e) {
					LOGGER.error(MessageEnum.RA_EVALUATE_XPATH_ERROR, "error-message", error.toString(), "SDNC", "",
						MsoLogger.ErrorCode.DataError, "XPath Exception", e);
				}

				if (!info.isEmpty()) {
					if (output == null) {
						output = new StringBuilder("[" + info + "]");
					} else {
						output.append(" [").append(info).append("]");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error (MessageEnum.RA_ANALYZE_ERROR_EXC, "SDNC", "",
				MsoLogger.ErrorCode.DataError, "Exception while analyzing errors", e);
		}

		return output.toString();
	}
}
