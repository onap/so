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

import org.openecomp.mso.adapters.sdncrest.SDNCErrorCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceError;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.openecomp.mso.logger.MsoLogger;

/**
 * SDNCConnector for "agnostic" API services.
 */
public class SDNCServiceRequestConnector extends SDNCConnector {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	@Override
	protected SDNCResponseCommon createResponseFromContent(int statusCode, String statusMessage,
			String responseContent, TypedRequestTunables rt) {
		try {
			return parseResponseContent(responseContent);
		} catch (ParseException e) {
			logError(e.getMessage());
			return createErrorResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage(), rt);
		}
	}

	@Override
	protected SDNCErrorCommon createErrorResponse(int statusCode, String errMsg,
			TypedRequestTunables rt) {
		return new SDNCServiceError(rt.getReqId(), String.valueOf(statusCode), errMsg, "Y");
	}

	/**
	 * Parses SDNC synchronous service response content or service notification content.
	 * If the content can be parsed and contains all required elements, then an object
	 * is returned.  The type of the returned object depends on the response code
	 * contained in the content.  For 2XX response codes, an SDNCServiceResponse is
	 * returned.  Otherwise, an SDNCServiceError is returned.  If the content cannot
	 * be parsed, or if the content does not contain all required elements, a parse
	 * exception is thrown.  This method performs no logging or alarming.
	 * @throws ParseException on error
	 */
	public static SDNCResponseCommon parseResponseContent(String responseContent)
			throws ParseException {
		try {
			// Note: this document builder is not namespace-aware, so namespaces are ignored.
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			InputSource source = new InputSource(new StringReader(responseContent));
			Document doc = documentBuilderFactory.newDocumentBuilder().parse(source);

			// Find the configuration-response-common child under the root element.
			// The root element is expected to be an "output" element, but we don't really care.

			Element root = doc.getDocumentElement();
			Element configurationResponseCommon = null;

			for (Element child : SDNCAdapterUtils.childElements(root)) {
				if ("configuration-response-common".equals(child.getNodeName())) {
					configurationResponseCommon = child;
					break;
				}
			}

			if (configurationResponseCommon == null) {
				throw new ParseException("No configuration-response-common element in SDNC response", 0);
			}

			// Process the children of configuration-response-common.

			String responseCode = null;
			String responseMessage = null;
			String svcRequestId = null;
			String ackFinalIndicator = null;
			List<Element> responseParameters = new ArrayList<>();

			for (Element child : SDNCAdapterUtils.childElements(configurationResponseCommon)) {
				if ("response-code".equals(child.getNodeName())) {
					responseCode = child.getTextContent();
				} else if ("response-message".equals(child.getNodeName())) {
					responseMessage = child.getTextContent();
				} else if ("svc-request-id".equals(child.getNodeName())) {
					svcRequestId = child.getTextContent();
				} else if ("ack-final-indicator".equals(child.getNodeName())) {
					ackFinalIndicator = child.getTextContent();
				} else if ("response-parameters".equals(child.getNodeName())) {
                    responseParameters.add(child);
				}
			}

			// svc-request-id is mandatory.

			if (svcRequestId == null || svcRequestId.isEmpty()) {
				throw new ParseException("No svc-request-id in SDNC response", 0);
			}

			// response-code is mandatory.

			if (responseCode == null || responseCode.isEmpty()) {
				throw new ParseException("No response-code in SDNC response", 0);
			}

			// ack-final-indicator is optional: default to "Y".

			if (ackFinalIndicator == null || ackFinalIndicator.trim().isEmpty()) {
				ackFinalIndicator = "Y";
			}

			if (!ackFinalIndicator.equals("Y") && !"N".equals(ackFinalIndicator)) {
				throw new ParseException("Invalid ack-final-indicator in SDNC response: '" + ackFinalIndicator + "'", 0);
			}

			// response-message is optional.  If the value is empty, omit it from the response object.

			if (responseMessage != null && responseMessage.isEmpty()) {
				responseMessage = null;
			}

			// If the response code in the message from SDNC was not 2XX, return SDNCServiceError.

			if (!responseCode.matches("2[0-9][0-9]") && !responseCode.equals("0")) {
				// Not a 2XX response.  Return SDNCServiceError.
				return new SDNCServiceError(svcRequestId, responseCode, responseMessage, ackFinalIndicator);
			}

			// Create a success response object.

			SDNCServiceResponse response = new SDNCServiceResponse(svcRequestId,
				responseCode, responseMessage, ackFinalIndicator);

            // Process any response-parameters that might be present.

            for (Element element : responseParameters) {
                String tagName = null;
                String tagValue = null;

                for (Element child : SDNCAdapterUtils.childElements(element)) {
                    if ("tag-name".equals(child.getNodeName())) {
                        tagName = child.getTextContent();
                    } else if ("tag-value".equals(child.getNodeName())) {
                        tagValue = child.getTextContent();
                    }
                }

                // tag-name is mandatory

                if (tagName == null) {
                    throw new ParseException("Missing tag-name in SDNC response parameter", 0);
                }

                // tag-value is optional.  If absent, make it an empty string so we don't
                // end up with null values in the parameter map.

                if (tagValue == null) {
                    tagValue = "";
                }

                response.addParam(tagName, tagValue);
            }

			return response;
		} catch (ParseException e) {
			throw e;
		} catch (Exception e) {
		    LOGGER.debug("Exception:", e);
			throw new ParseException("Failed to parse SDNC response", 0);
		}
	}
}
