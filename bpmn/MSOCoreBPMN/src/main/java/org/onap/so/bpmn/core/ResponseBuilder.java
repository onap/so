/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.so.bpmn.core;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used in the output variable mapping configuration of subflow call activity tasks to normalize subflow responses. The
 * output mapping is normally set up as follows. Note that the order of these mappings is important!
 * <p>
 * OUTPUT MAPPING
 * 
 * <pre>
 *   SOURCE EXPRESSION                                      TARGET
 *   ${ResponseBuilder.buildWorkflowException(execution)}   WorkflowException
 *   ${ResponseBuilder.buildWorkflowResponse(execution)}    SomeResponseVariable
 * </pre>
 */
public class ResponseBuilder implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final String WORKFLOWEXCEPTION = "WorkflowException";
    private static final Logger logger = LoggerFactory.getLogger(ResponseBuilder.class);

    /**
     * Creates a WorkflowException using data from the execution variables. If the variables do not indicate that there
     * was an error, null is returned.
     * 
     * @param execution the execution
     */
    public WorkflowException buildWorkflowException(DelegateExecution execution) {

        String method =
                getClass().getSimpleName() + ".buildWorkflowException(" + "execution=" + execution.getId() + ")";

        logger.debug("Entered {}", method);

        String prefix = (String) execution.getVariable("prefix");
        String processKey = getProcessKey(execution);

        logger.debug("processKey={}", processKey);

        // See if there"s already a WorkflowException object in the execution.
        WorkflowException theException = (WorkflowException) execution.getVariable(WORKFLOWEXCEPTION);

        if (theException != null) {
            logger.debug("Exited {} - propagated {}", method, theException);
            return theException;
        }

        // Look in the legacy variables: ErrorResponse and ResponseCode

        String errorResponse = trimString(execution.getVariable(prefix + "ErrorResponse"), null);
        String responseCode = trimString(execution.getVariable(prefix + "ResponseCode"), null);
        logger.debug("errorResponse={}", errorResponse);
        logger.debug("responseCode={}", responseCode);
        if (errorResponse != null || !isOneOf(responseCode, null, "0", "200", "201", "202", "204")) {
            // This is an error condition. We need to return a WorkflowExcpetion

            if (errorResponse == null) {
                // No errorResponse string. See if there"s something in the Response variable
                String response = trimString(execution.getVariable(processKey + "Response"), null);
                if (response == null) {
                    errorResponse = "Received response code " + responseCode + " from " + processKey;
                } else {
                    errorResponse = response;
                }
            }

            // Some subflows may try to return a WorkflowException as XML in the
            // errorResponse. If provided, use the errorCode and errorMessage
            // from the XML

            String maybeXML = removeXMLNamespaces(errorResponse);

            String xmlErrorMessage = trimString(getXMLTextElement(maybeXML, "ErrorMessage"), null);
            String xmlErrorCode = trimString(getXMLTextElement(maybeXML, "ErrorCode"), null);

            if (xmlErrorMessage != null || xmlErrorCode != null) {
                logger.debug("xmlErrorMessage={}", xmlErrorMessage);
                logger.debug("xmlErrorCode={}", xmlErrorCode);

                if (xmlErrorMessage == null) {
                    errorResponse = "Received error code " + xmlErrorCode + " from " + processKey;
                } else {
                    errorResponse = xmlErrorMessage;
                }

                if (xmlErrorCode != null) {
                    responseCode = xmlErrorCode;
                }
            }

            // Convert the responseCode to an integer

            int intResponseCode;

            try {
                intResponseCode = Integer.valueOf(responseCode);
            } catch (NumberFormatException e) {
                // Internal Error
                intResponseCode = 2000;
            }

            // Convert 3-digit HTTP response codes (we should not be using them here)
            // to appropriate 4-digit response codes

            if (intResponseCode < 1000) {
                if (intResponseCode >= 400 && intResponseCode <= 499) {
                    // Invalid Message
                    intResponseCode = 1002;
                } else {
                    // Internal Error
                    intResponseCode = 2000;
                }
            }

            // Create a new WorkflowException object

            theException = new WorkflowException(processKey, intResponseCode, errorResponse);
            execution.setVariable(WORKFLOWEXCEPTION, theException);
            logger.debug("Exited {} - created {}", method, theException);
            return theException;
        }

        logger.debug("Exited {} - no WorkflowException", method);
        return null;
    }

    /**
     * Returns the "Response" variable, unless the execution variables indicate there was an error. In that case, null
     * is returned.
     * 
     * @param execution the execution
     */
    public Object buildWorkflowResponse(DelegateExecution execution) {

        String method = getClass().getSimpleName() + ".buildWorkflowResponse(" + "execution=" + execution.getId() + ")";
        logger.debug("Entered {}", method);

        String prefix = (String) execution.getVariable("prefix");
        String processKey = getProcessKey(execution);

        Object theResponse = null;

        WorkflowException theException = (WorkflowException) execution.getVariable(WORKFLOWEXCEPTION);
        String errorResponse = trimString(execution.getVariable(prefix + "ErrorResponse"), null);
        String responseCode = trimString(execution.getVariable(prefix + "ResponseCode"), null);

        if (theException == null && errorResponse == null
                && isOneOf(responseCode, null, "0", "200", "201", "202", "204")) {

            theResponse = execution.getVariable("WorkflowResponse");

            if (theResponse == null) {
                theResponse = execution.getVariable(processKey + "Response");
            }
        }

        logger.debug("Exited {}", method);
        return theResponse;
    }

    /**
     * Checks if the specified item is one of the specified values.
     * 
     * @param item the item
     * @param values the list of values
     * @return true if the item is in the list of values
     */
    private boolean isOneOf(Object item, Object... values) {
        if (values == null) {
            return item == null;
        }

        for (Object value : values) {
            if (value == null) {
                if (item == null) {
                    return true;
                }
            } else {
                if (value.equals(item)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates a string value of the specified object, trimming whitespace in the process. If the result is null or
     * empty, the specified empty string value is returned. Otherwise the trimmed value is returned. This method helps
     * ensure consistent treatment of empty and null strings.
     * 
     * @param object the object to convert (possibly null)
     * @param emptyStringValue the desired value for empty results
     */
    private String trimString(Object object, String emptyStringValue) {
        if (object == null) {
            return emptyStringValue;
        }

        String s = String.valueOf(object).trim();
        return "".equals(s) ? emptyStringValue : s;
    }

    /**
     * Returns the process definition key (i.e. the process name) from the execution.
     * 
     * @param execution the execution
     */
    private String getProcessKey(DelegateExecution execution) {
        Object testKey = execution.getVariable("testProcessKey");

        if (testKey instanceof String) {
            return (String) testKey;
        }

        return execution.getProcessEngineServices().getRepositoryService()
                .getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }

    /**
     * Removes namespace definitions and prefixes from XML, if any.
     */
    private String removeXMLNamespaces(String xml) {
        // remove xmlns declaration
        xml = xml.replaceAll("xmlns.*?(\"|\').*?(\"|\')", "");

        // remove opening tag prefix
        xml = xml.replaceAll("(<)(\\w+:)(.*?>)", "$1$3");

        // remove closing tags prefix
        xml = xml.replaceAll("(</)(\\w+:)(.*?>)", "$1$3");

        // remove extra spaces left when xmlns declarations are removed
        xml = xml.replaceAll("\\s+>", ">");

        return xml;
    }

    /**
     * Extracts text from an XML element. This method is not namespace aware (namespaces are ignored). The first
     * matching element is selected.
     * 
     * @param xml the XML document or fragment
     * @param tag the desired element, e.g. "<name>"
     * @return the element text, or null if the element was not found
     */
    private String getXMLTextElement(String xml, String tag) {
        xml = removeXMLNamespaces(xml);

        if (!tag.startsWith("<")) {
            tag = "<" + tag + ">";
        }

        int start = xml.indexOf(tag);

        if (start == -1) {
            return null;
        }

        int end = xml.indexOf('<', start + tag.length());

        if (end == -1) {
            return null;
        }

        return xml.substring(start + tag.length(), end);
    }
}
