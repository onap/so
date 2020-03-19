/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.sdnc.sdncrest;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.onap.so.adapters.sdncrest.SDNCErrorCommon;
import org.onap.so.adapters.sdncrest.SDNCResponseCommon;
import org.onap.so.adapters.sdncrest.SDNCServiceError;
import org.onap.so.adapters.sdncrest.SDNCServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * SDNCConnector for "agnostic" API services.
 */

@Component
public class SDNCServiceRequestConnector extends SDNCConnector {

    private static final Logger logger = LoggerFactory.getLogger(SDNCServiceRequestConnector.class);

    @Override
    protected SDNCResponseCommon createResponseFromContent(int statusCode, String statusMessage, String responseContent,
            TypedRequestTunables rt) {
        try {
            return parseResponseContent(responseContent);
        } catch (Exception e) {
            logger.error("Error occured:", e);
            return createErrorResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage(), rt);
        }
    }

    @Override
    protected SDNCErrorCommon createErrorResponse(int statusCode, String errMsg, TypedRequestTunables rt) {
        return new SDNCServiceError(rt.getReqId(), String.valueOf(statusCode), errMsg, "Y");
    }

    /**
     * Parses SDNC synchronous service response content or service notification content. If the content can be parsed
     * and contains all required elements, then an object is returned. The type of the returned object depends on the
     * response code contained in the content. For 2XX response codes, an SDNCServiceResponse is returned. Otherwise, an
     * SDNCServiceError is returned. If the content cannot be parsed, or if the content does not contain all required
     * elements, a parse exception is thrown. This method performs no logging or alarming.
     *
     * @throws ParseException on error
     */
    public static SDNCResponseCommon parseResponseContent(String responseContent)
            throws ParseException, ParserConfigurationException, SAXException, IOException {

        // Note: this document builder is not namespace-aware, so namespaces are ignored.
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
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

            switch (child.getNodeName()) {
                case "response-code":
                    responseCode = child.getTextContent();
                    break;
                case "response-message":
                    responseMessage = child.getTextContent();
                    break;
                case "svc-request-id":
                    svcRequestId = child.getTextContent();
                    break;
                case "ack-final-indicator":
                    ackFinalIndicator = child.getTextContent();
                    break;
                case "response-parameters":
                    responseParameters.add(child);
                    break;
                default:
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

        if (!"Y".equals(ackFinalIndicator) && !"N".equals(ackFinalIndicator)) {
            throw new ParseException("Invalid ack-final-indicator in SDNC response: '" + ackFinalIndicator + "'", 0);
        }

        // response-message is optional. If the value is empty, omit it from the response object.

        if (responseMessage != null && responseMessage.isEmpty()) {
            responseMessage = null;
        }

        // If the response code in the message from SDNC was not 2XX, return SDNCServiceError.

        if (!responseCode.matches("2[0-9][0-9]") && !("0").equals(responseCode)) {
            // Not a 2XX response. Return SDNCServiceError.
            return new SDNCServiceError(svcRequestId, responseCode, responseMessage, ackFinalIndicator);
        }

        return createSDNCServiceResponse(responseCode, responseMessage, svcRequestId, ackFinalIndicator,
                responseParameters);

    }

    /**
     * @param responseCode
     * @param responseMessage
     * @param svcRequestId
     * @param ackFinalIndicator
     * @param responseParameters
     * @return
     * @throws ParseException
     */
    protected static SDNCServiceResponse createSDNCServiceResponse(String responseCode, String responseMessage,
            String svcRequestId, String ackFinalIndicator, List<Element> responseParameters) throws ParseException {

        // Create a success response object.
        SDNCServiceResponse response =
                new SDNCServiceResponse(svcRequestId, responseCode, responseMessage, ackFinalIndicator);

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

            // tag-value is optional. If absent, make it an empty string so we don't
            // end up with null values in the parameter map.

            if (tagValue == null) {
                tagValue = "";
            }

            response.addParam(tagName, tagValue);
        }
        return response;
    }
}
