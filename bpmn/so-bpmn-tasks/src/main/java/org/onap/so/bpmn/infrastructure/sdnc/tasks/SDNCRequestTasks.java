/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.infrastructure.sdnc.exceptions.SDNCErrorResponseException;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.xpath.XPathFactoryImpl;

@Component
@RequiredArgsConstructor
public class SDNCRequestTasks {

    private static final Logger logger = LoggerFactory.getLogger(SDNCRequestTasks.class);
    private static final String NET_SF_SAXON_XPATH_IMPL = "net.sf.saxon.xpath.XPathFactoryImpl";
    private static final String XPATH_FACTORY_PROPERTY_NAME =
            "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON;
    private static final JsonPath path = JsonPath.compile("$.GENERIC-RESOURCE-API:output.ack-final-indicator");
    private static final String SDNC_REQUEST = "SDNCRequest";
    private static final String MESSAGE = "_MESSAGE";
    private static final String CORRELATOR = "_CORRELATOR";
    protected static final String IS_CALLBACK_COMPLETED = "isCallbackCompleted";
    protected static final String SDNC_SUCCESS = "200";

    private final ExceptionBuilder exceptionBuilder;
    private final SDNCClient sdncClient;

    public void createCorrelationVariables(DelegateExecution execution) {
        SDNCRequest request = (SDNCRequest) execution.getVariable(SDNC_REQUEST);
        execution.setVariable(request.getCorrelationName() + CORRELATOR, request.getCorrelationValue());
        execution.setVariable("sdncTimeout", request.getTimeOut());
    }

    public void callSDNC(DelegateExecution execution) {
        SDNCRequest request = (SDNCRequest) execution.getVariable(SDNC_REQUEST);
        try {
            String response = sdncClient.post(request.getSDNCPayload(), request.getTopology());
            // SDNC Response with RFC-8040 prefixes GENERIC-RESOURCE-API
            String finalMessageIndicator = path.read(response);
            execution.setVariable("isSDNCCompleted", convertIndicatorToBoolean(finalMessageIndicator));
        } catch (PathNotFoundException e) {
            logger.error("Error Parsing SDNC Response. Could not find read final ack indicator from JSON.", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    "Recieved invalid response from SDNC, unable to read message content.", ONAPComponents.SO);
        } catch (MapperException e) {
            logger.error("Failed to map SDNC object to JSON prior to POST.", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    "Failed to map SDNC object to JSON prior to POST.", ONAPComponents.SO);
        } catch (BadResponseException e) {
            logger.error("Did not receive a successful response from SDNC.", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e.getLocalizedMessage(),
                    ONAPComponents.SDNC);
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientErrorException: 404 Not Found, Failed to contact SDNC", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "SDNC cannot be contacted.",
                    ONAPComponents.SO);
        }
    }

    public void processCallback(DelegateExecution execution) {
        try {
            SDNCRequest request = (SDNCRequest) execution.getVariable(SDNC_REQUEST);
            String asyncRequest = (String) execution.getVariable(request.getCorrelationName() + MESSAGE);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(asyncRequest)));

            String finalMessageIndicator = getXmlElement(doc, "/input/ack-final-indicator");

            boolean isCallbackCompleted = convertIndicatorToBoolean(finalMessageIndicator);
            execution.setVariable(IS_CALLBACK_COMPLETED, isCallbackCompleted);
            if (isCallbackCompleted) {
                String responseCode = getXmlElement(doc, "/input/response-code");
                if (!SDNC_SUCCESS.equalsIgnoreCase(responseCode)) {
                    String responseMessage;
                    try {
                        responseMessage = getXmlElement(doc, "/input/response-message");
                    } catch (Exception e) {
                        responseMessage = "Unknown Error in SDNC";
                    }
                    throw new SDNCErrorResponseException(responseMessage);
                }
            }
        } catch (SDNCErrorResponseException e) {
            logger.error("SDNC error response - {}", e.getMessage());
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e.getMessage(), ONAPComponents.SDNC);
        } catch (Exception e) {
            logger.error("Error processing SDNC callback", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "Error processing SDNC callback",
                    ONAPComponents.SO);
        }
    }

    public void handleTimeOutException(DelegateExecution execution) {
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                "Error timed out waiting on SDNC Async-Response", ONAPComponents.SO);
    }

    protected boolean convertIndicatorToBoolean(String finalMessageIndicator) {
        return "Y".equals(finalMessageIndicator);
    }

    protected static String getXmlElement(final Document doc, final String exp) throws Exception {
        final TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, StringUtils.EMPTY);

        final Transformer transformer = factory.newTransformer();
        final StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        logger.debug(writer.getBuffer().toString());

        System.setProperty(XPATH_FACTORY_PROPERTY_NAME, NET_SF_SAXON_XPATH_IMPL);
        final XPathFactory xPathFactory = XPathFactoryImpl.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        final XPath xPath = xPathFactory.newXPath();
        final String result = xPath.evaluate(exp, doc);
        if (result == null || result.isEmpty()) {
            throw new Exception("XPath Failed to find element expression: " + exp);
        }
        return result;
    }
}
