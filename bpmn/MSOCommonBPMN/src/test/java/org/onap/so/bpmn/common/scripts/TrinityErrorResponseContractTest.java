/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.common.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TrinityErrorResponseContractTest {
    private static final String NS = "http://org.onap/so/request/types/v1";
    private final TrinityExceptionUtil utility = new TrinityExceptionUtil();
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);

    @Before
    public void configureExecution() {
        variables.put("prefix", "T_");
        variables.put("T_ResponseCode", "400");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void supportedErrorsRetainTheirPublicCodesTextsAndElementKinds() throws Exception {
        String[][] errors = {{"SVC_GENERAL_SERVICE_ERROR", "SVC0001", "Internal Error"},
                {"SVC_BAD_PARAMETER", "SVC0002", "Invalid input value for message part %1"},
                {"SVC_NO_SERVER_RESOURCES", "SVC1000", "No server resources available to process the request"},
                {"SVC_DETAILED_SERVICE_ERROR", "SVC2000",
                        "The following service error occurred: %1. Error code is %2."},
                {"POL_GENERAL_POLICY_ERROR", "POL0001", "A policy error occurred."},
                {"POL_USER_NOT_PROVISIONED", "POL1009", "User has not been provisioned for service"},
                {"POL_USER_SUSPENDED", "POL1010", "User has been suspended from service"},
                {"POL_DETAILED_POLICY_ERROR", "POL2000", "The following policy error occurred: %1. Error code is %2."},
                {"POL_MSG_SIZE_EXCEEDS_LIMIT", "POL9003", "Message content size exceeds the allowable limit"}};
        for (String[] expected : errors) {
            variables.put("T_err", TrinityExceptionUtil.Error.valueOf(expected[0]));
            variables.remove("T_errTxt");
            variables.put("T_errVariables", Collections.emptyList());
            Document document = xml(utility.buildException(execution));
            assertEquals(NS, document.getDocumentElement().getNamespaceURI());
            assertEquals("requestError", document.getDocumentElement().getLocalName());
            assertEquals(expected[1], text(document, "messageId"));
            assertEquals(expected[2], text(document, "text"));
            boolean service = expected[1].startsWith("SVC");
            assertEquals(service ? 1 : 0, document.getElementsByTagNameNS(NS, "serviceException").getLength());
            assertEquals(service ? 0 : 1, document.getElementsByTagNameNS(NS, "policyException").getLength());
            assertEquals(0, document.getElementsByTagNameNS(NS, "variables").getLength());
        }
    }

    @Test
    public void customTextAndVariablesAreEscapedWithoutLosingEmptyOrFalseValues() throws Exception {
        variables.put("T_err", TrinityExceptionUtil.Error.SVC_BAD_PARAMETER);
        String message = "Grüße & <field> \"quoted\"";
        variables.put("T_errTxt", message);
        List<Object> substitutions = Arrays.asList("Grüße & <SO>", 0, false, "");
        variables.put("T_errVariables", substitutions);
        String response = utility.buildException(execution);
        Document document = xml(response);
        assertEquals(message, text(document, "text"));
        NodeList values = document.getElementsByTagNameNS(NS, "variables");
        assertEquals(4, values.getLength());
        for (int index = 0; index < substitutions.size(); index++) {
            assertEquals(String.valueOf(substitutions.get(index)), values.item(index).getTextContent());
        }
        Object stored = variables.get("T_ErrorResponse");
        assertEquals("org.codehaus.groovy.runtime.GStringImpl", stored.getClass().getName());
        assertEquals(response, stored.toString());
        assertSame(TrinityExceptionUtil.Error.SVC_BAD_PARAMETER, variables.get("T_err"));
        assertSame(substitutions, variables.get("T_errVariables"));
        assertEquals(message, variables.get("T_errTxt"));
    }

    @Test
    public void detailedServiceErrorKeepsTheTemplateAndOrderedSubstitutions() throws Exception {
        String detail = "Controller rejected Grüße & <value>";
        String response = utility.buildException(detail, execution);
        Document document = xml(response);
        assertEquals("SVC2000", text(document, "messageId"));
        assertEquals("The following service error occurred: %1. Error code is %2.", text(document, "text"));
        assertEquals(Arrays.asList(detail, "400"), variables.get("T_errVariables"));
        assertEquals(detail, document.getElementsByTagNameNS(NS, "variables").item(0).getTextContent());
        assertEquals("400", document.getElementsByTagNameNS(NS, "variables").item(1).getTextContent());
        assertEquals("The following service error occurred: " + detail + ". Error code is 400.",
                utility.parseError(execution));
    }

    @Test
    public void missingDescriptionUsesTheGeneralServiceError() throws Exception {
        Document document = xml(utility.buildException(execution));
        assertEquals("SVC0001", text(document, "messageId"));
        assertEquals("Internal Error", text(document, "text"));
        assertEquals(0, document.getElementsByTagNameNS(NS, "variables").getLength());
    }

    @Test
    public void absentHumanReadableTextHasTheDefinedFallback() {
        assertEquals("failed", utility.parseError(execution));
        variables.put("T_errTxt", "");
        variables.put("T_errVariables", Collections.emptyList());
        assertEquals("", utility.parseError(execution));
        variables.put("T_errTxt", "No substitutions");
        assertEquals("No substitutions", utility.parseError(execution));
    }

    private static String text(Document document, String name) {
        NodeList matches = document.getElementsByTagNameNS(NS, name);
        assertEquals(1, matches.getLength());
        return matches.item(0).getTextContent();
    }

    private static Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
