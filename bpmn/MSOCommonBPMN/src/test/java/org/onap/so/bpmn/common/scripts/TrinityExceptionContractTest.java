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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.onap.so.bpmn.common.scripts.TrinityExceptionUtil.Error;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TrinityExceptionContractTest {
    private static final String NAMESPACE = "http://org.onap/so/request/types/v1";
    private final TrinityExceptionUtil utility = new TrinityExceptionUtil();

    @Test
    public void unavailableAndUnrecognizedResponsesUseTheResourceError() {
        for (String code : Arrays.asList(null, "0", "500", "408", "502", "unexpected")) {
            assertSame(String.valueOf(code), Error.SVC_NO_SERVER_RESOURCES,
                    utility.mapErrorCodetoError(code, "downstream detail"));
        }
    }

    @Test
    public void badRequestDistinguishesAbsentFromEmptyDetails() {
        assertSame(Error.SVC_GENERAL_SERVICE_ERROR, utility.mapErrorCodetoError("400", null));
        assertSame(Error.SVC_DETAILED_SERVICE_ERROR, utility.mapErrorCodetoError("400", ""));
        assertSame(Error.SVC_DETAILED_SERVICE_ERROR, utility.mapErrorCodetoError("400", "invalid region"));
    }

    @Test
    public void passThroughCodesDoNotCreateACommonException() {
        for (String code : Arrays.asList("401", "405", "409", "503")) {
            Map<String, Object> state = state();
            state.put("test_ResponseCode", code);
            DelegateExecution execution = execution(state);
            assertNull(code, utility.mapErrorCodetoError(code, "detail"));
            assertEquals(code, "", utility.buildException("detail", execution));
            verify(execution, never()).setVariable(anyString(), any());
        }
    }

    @Test
    public void everyExplicitErrorKeepsItsTypedIdentityAndNamespace() throws Exception {
        for (Error error : Error.values()) {
            Map<String, Object> state = state();
            state.put("test_err", error);
            ArrayList<String> values = new ArrayList<>(Arrays.asList("München & <region>", "雪 \"quoted\""));
            state.put("test_errVariables", values);
            String response = utility.buildException("unused", execution(state));
            Document xml = xml(response);
            assertEquals("requestError", xml.getDocumentElement().getLocalName());
            assertEquals(NAMESPACE, xml.getDocumentElement().getNamespaceURI());
            assertEquals(error.getMsgId(), text(xml, "messageId"));
            assertEquals(error.getMsgTxt(), text(xml, "text"));
            String kind = error.getMsgId().startsWith("SVC") ? "serviceException" : "policyException";
            assertEquals(1, xml.getElementsByTagNameNS(NAMESPACE, kind).getLength());
            NodeList variables = xml.getElementsByTagNameNS(NAMESPACE, "variables");
            assertEquals(2, variables.getLength());
            assertEquals(values.get(0), variables.item(0).getTextContent());
            assertEquals(values.get(1), variables.item(1).getTextContent());
            assertSame(error, state.get("test_err"));
            assertSame(values, state.get("test_errVariables"));
            assertEquals("org.codehaus.groovy.runtime.GStringImpl",
                    state.get("test_ErrorResponse").getClass().getName());
            assertEquals(response, state.get("test_ErrorResponse").toString());
            assertEquals(error.getMsgTxt(), state.get("test_errTxt"));
        }
    }

    @Test
    public void detailedResponseGeneratesAnOrderedListOfStringVariables() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", "400");
        String response = utility.buildException("Bad <input> & 雪", execution(state));
        assertSame(Error.SVC_DETAILED_SERVICE_ERROR, state.get("test_err"));
        assertEquals(ArrayList.class, state.get("test_errVariables").getClass());
        assertEquals(Arrays.asList("Bad <input> & 雪", "400"), state.get("test_errVariables"));
        assertEquals("Bad <input> & 雪",
                xml(response).getElementsByTagNameNS(NAMESPACE, "variables").item(0).getTextContent());
    }

    @Test
    public void anExplicitEmptyVariableListSuppressesGeneratedDetails() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", "400");
        ArrayList<String> empty = new ArrayList<>();
        state.put("test_errVariables", empty);
        String response = utility.buildException("detail", execution(state));
        assertSame(empty, state.get("test_errVariables"));
        assertEquals(0, xml(response).getElementsByTagNameNS(NAMESPACE, "variables").getLength());
    }

    @Test
    public void explicitTextIsEscapedAndAnEmptyOverrideRemainsEmpty() throws Exception {
        for (String text : Arrays.asList("München & <snow> 雪", "")) {
            Map<String, Object> state = state();
            state.put("test_err", Error.POL_GENERAL_POLICY_ERROR);
            state.put("test_errTxt", text);
            String response = utility.buildException("unused", execution(state));
            assertEquals(text, text(xml(response), "text"));
            assertEquals(text, state.get("test_errTxt"));
        }
    }

    @Test
    public void numericResponseCodesAreConvertedBeforeDefaultErrorSelection() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", Integer.valueOf(500));
        String response = utility.buildException("transport unavailable", execution(state));
        assertEquals("SVC1000", text(xml(response), "messageId"));
        assertSame(Error.SVC_NO_SERVER_RESOURCES, state.get("test_err"));
        assertEquals(Integer.valueOf(500), state.get("test_ResponseCode"));
        assertNull(state.get("test_errVariables"));
    }

    @Test
    public void parsingUsesPrefixScopedTextAndLeavesStoredTemplateUnchanged() {
        Map<String, Object> state = state();
        state.put("test_errTxt", "Rejected %1 with code %2: %1");
        state.put("test_errVariables", Arrays.asList("München", "400"));
        state.put("errTxt", "wrong scope");
        assertEquals("Rejected München with code 400: %1", utility.parseError(execution(state)));
        assertEquals("Rejected %1 with code %2: %1", state.get("test_errTxt"));
    }

    @Test
    public void absentTextAndAbsentOrEmptyVariablesHaveDistinctFallbacks() {
        Map<String, Object> state = state();
        assertEquals("failed", utility.parseError(execution(state)));
        state.put("test_errTxt", "unexpanded %1");
        assertEquals("unexpanded %1", utility.parseError(execution(state)));
        state.put("test_errVariables", new ArrayList<>());
        assertEquals("unexpanded %1", utility.parseError(execution(state)));
        state.put("test_errTxt", "");
        assertEquals("", utility.parseError(execution(state)));
    }

    @Test
    public void sdncRequestDataRetainsItsMessageAndSeparateControllerCode() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", "400");
        String callback = "<callback><RequestData><response-message>Region &amp; 雪</response-message>"
                + "<response-code>7310</response-code></RequestData></callback>";
        String response = utility.mapSDNCAdapterExceptionToErrorResponse(callback, execution(state));
        assertSame(Error.SVC_DETAILED_SERVICE_ERROR, state.get("test_err"));
        assertEquals(Arrays.asList("Region & 雪", "7310"), state.get("test_errVariables"));
        assertEquals("400", state.get("test_ResponseCode"));
        Document xml = xml(response);
        assertEquals("SVC2000", text(xml, "messageId"));
        assertEquals("Region & 雪", xml.getElementsByTagNameNS(NAMESPACE, "variables").item(0).getTextContent());
        assertEquals("7310", xml.getElementsByTagNameNS(NAMESPACE, "variables").item(1).getTextContent());
    }

    @Test
    public void flatSdncCallbacksUseTheExecutionResponseCode() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", "400");
        String response = utility.mapSDNCAdapterExceptionToErrorResponse(
                "<callback><ResponseMessage>Invalid &lt;region&gt;</ResponseMessage></callback>", execution(state));
        assertEquals(Arrays.asList("Invalid <region>", "400"), state.get("test_errVariables"));
        assertEquals("SVC2000", text(xml(response), "messageId"));
    }

    @Test
    public void structuredAaiErrorsRetainTheirOriginalBusinessIdentity() throws Exception {
        Map<String, Object> state = state();
        String input = "<fault xmlns:a=\"urn:aai:error\"><a:requestError><a:serviceException>"
                + "<a:messageId>SVC3002</a:messageId><a:text>Cannot update %1</a:text>"
                + "<a:variables>雪 &amp; region</a:variables></a:serviceException></a:requestError></fault>";
        String response = utility.mapAAIExceptionTCommonException(input, execution(state));
        Document xml = xml(response);
        assertEquals("requestError", xml.getDocumentElement().getLocalName());
        assertEquals("SVC3002", xml.getElementsByTagNameNS("*", "messageId").item(0).getTextContent());
        assertEquals("Cannot update %1", xml.getElementsByTagNameNS("*", "text").item(0).getTextContent());
        assertEquals("雪 & region", xml.getElementsByTagNameNS("*", "variables").item(0).getTextContent());
        assertEquals(response, state.get("test_ErrorResponse").toString());
        assertNull(state.get("test_err"));
    }

    @Test
    public void malformedAaiErrorsUseAnEscapedCommonFallback() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", "400");
        String response = utility.mapAAIExceptionTCommonException("not XML", execution(state));
        Document xml = xml(response);
        assertEquals("SVC2000", text(xml, "messageId"));
        assertEquals("Received error from A&AI, unable to parse",
                xml.getElementsByTagNameNS(NAMESPACE, "variables").item(0).getTextContent());
        assertEquals(Arrays.asList("Received error from A&AI, unable to parse", "400"), state.get("test_errVariables"));
    }

    @Test
    public void responseFreeOverloadSelectsTheGeneralBadRequestError() throws Exception {
        Map<String, Object> state = state();
        state.put("test_ResponseCode", "400");
        String response = utility.buildException(execution(state));
        assertSame(Error.SVC_GENERAL_SERVICE_ERROR, state.get("test_err"));
        assertEquals("SVC0001", text(xml(response), "messageId"));
        assertEquals(0, xml(response).getElementsByTagNameNS(NAMESPACE, "variables").getLength());
        assertNull(state.get("test_errVariables"));
    }

    private static Map<String, Object> state() {
        Map<String, Object> values = new HashMap<>();
        values.put("prefix", "test_");
        return values;
    }

    private static DelegateExecution execution(Map<String, Object> values) {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(call -> values.get(call.getArgument(0)));
        doAnswer(call -> {
            values.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        return execution;
    }

    private static Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static String text(Document document, String localName) {
        return document.getElementsByTagNameNS(NAMESPACE, localName).item(0).getTextContent();
    }
}
