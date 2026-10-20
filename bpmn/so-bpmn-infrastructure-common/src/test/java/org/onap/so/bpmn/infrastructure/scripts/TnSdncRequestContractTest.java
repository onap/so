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
package org.onap.so.bpmn.infrastructure.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.google.gson.JsonObject;

@RunWith(Parameterized.class)
public class TnSdncRequestContractTest {
    private static final String WORKFLOW_NS = "http://org.onap/so/workflow/schema/v1";
    private static final String ADAPTER_NS = "http://org.onap/workflow/sdnc/adapter/schema/v1";
    private final String action;
    private final String requestAction;
    private final TnNssmfUtils script = new TnNssmfUtils();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private JsonObject model;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> actions() {
        return Arrays.asList(new Object[][] {{"create", "AllocateTransportSliceInstance"},
                {"delete", "DeleteTransportSliceInstance"}, {"activate", "ActivateTransportSliceInstance"},
                {"deactivate", "DeactivateTransportSliceInstance"}, {"update", "ModifyTransportSliceInstance"},
                {"vendor-extension", "vendor-extension"}});
    }

    public TnSdncRequestContractTest(String action, String requestAction) {
        this.action = action;
        this.requestAction = requestAction;
    }

    @Before
    public void configureExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        variables.put("testReqId", "transaction Gr\u00fc\u00dfe & <1>");
        variables.put("msoRequestId", "request & <1>");
        variables.put("sdncCallbackUrl", "http://callback.invalid/sdnc?first=1&second=2");
        variables.put("sliceServiceInstanceId", "service Gr\u00fc\u00dfe & <1>");
        variables.put("subscriptionServiceType", "transport & <shared>");
        variables.put("serviceType", "transport-slice");
        variables.put("sliceServiceInstanceName", "slice Gr\u00fc\u00dfe & <west>");
        variables.put("globalSubscriberId", "customer & <1>");
        variables.put("vnfParamsExistFlag", Boolean.FALSE);
        model = new JsonObject();
        model.addProperty("modelInvariantUuid", "invariant & <1>");
        model.addProperty("modelUuid", "model & <1>");
        model.addProperty("modelVersion", "1.0");
        model.addProperty("modelName", "model Gr\u00fc\u00dfe & <1>");
        model.addProperty("modelCustomizationUuid", "customization & <1>");
        variables.put("serviceModelInfo", model.toString());
    }

    @Test
    public void lifecycleActionRetainsItsWireContractAndEscapedIdentity() throws Exception {
        Document request = xml(script.buildSDNCRequest(execution, "nssi & <1>", action));

        assertRequest(request, requestAction, true);
        assertDefaultParameters(request);
    }

    @Test
    public void explicitRequestActionOverridesOnlyTheRequestInformation() throws Exception {
        model.remove("modelCustomizationUuid");
        variables.put("serviceModelInfo", model.toString());

        Document request = xml(script.buildSDNCRequest(execution, "nssi & <1>", action, "custom & <request>"));

        assertRequest(request, "custom & <request>", false);
    }

    @Test
    public void structuredInputParametersRemainXmlWithEmptyFalseAndZeroValues() throws Exception {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("vnf-input-parameters",
                "<param><name>label</name><value>Gr\u00fc\u00dfe &amp; SO</value></param>"
                        + "<param><name>optional</name><value/></param>");
        parameters.put("tunnel-details", "<active>false</active><count>0</count>");
        variables.put("vnfParamsExistFlag", Boolean.TRUE);
        variables.put("TNNSSMF_vnfParamsMap", parameters);

        Document request = xml(script.buildSDNCRequest(execution, "nssi & <1>", action));

        assertRequest(request, requestAction, true);
        Element input = element(request.getDocumentElement(), "", "vnf-request-input");
        Element parameterBlock = element(input, "", "vnf-input-parameters");
        NodeList values = parameterBlock.getElementsByTagNameNS("", "value");
        assertEquals(2, values.getLength());
        assertEquals("Gr\u00fc\u00dfe & SO", values.item(0).getTextContent());
        assertEquals("", values.item(1).getTextContent());
        Element details = element(input, "", "tunnel-details");
        assertEquals("false", text(details, "", "active"));
        assertEquals("0", text(details, "", "count"));
    }

    @Test
    public void absentParameterFlagUsesTheDefaultInputParameterContract() throws Exception {
        variables.remove("vnfParamsExistFlag");

        Document request = xml(script.buildSDNCRequest(execution, "nssi & <1>", action));

        assertRequest(request, requestAction, true);
        assertDefaultParameters(request);
    }

    private void assertRequest(Document request, String expectedAction, boolean customization) {
        Element root = request.getDocumentElement();
        assertEquals("SDNCAdapterWorkflowRequest", root.getLocalName());
        assertEquals(WORKFLOW_NS, root.getNamespaceURI());
        Element header = element(root, ADAPTER_NS, "RequestHeader");
        assertEquals("transaction Gr\u00fc\u00dfe & <1>", text(header, ADAPTER_NS, "RequestId"));
        assertEquals("nssi & <1>", text(header, ADAPTER_NS, "SvcInstanceId"));
        assertEquals(action, text(header, ADAPTER_NS, "SvcAction"));
        assertEquals("vnf-topology-operation", text(header, ADAPTER_NS, "SvcOperation"));
        assertEquals(variables.get("sdncCallbackUrl"), text(header, ADAPTER_NS, "CallbackUrl"));
        assertEquals("generic-resource", text(header, ADAPTER_NS, "MsoAction"));
        Element data = element(root, WORKFLOW_NS, "SDNCRequestData");
        Element information = element(data, "", "request-information");
        assertEquals("request & <1>", text(information, "", "request-id"));
        assertEquals(expectedAction, text(information, "", "request-action"));
        assertEquals(variables.get("sliceServiceInstanceId"), text(information, "", "source"));
        Element service = element(data, "", "service-information");
        assertEquals(variables.get("sliceServiceInstanceId"), text(service, "", "service-id"));
        assertEquals("nssi & <1>", text(service, "", "service-instance-id"));
        assertEquals(variables.get("subscriptionServiceType"), text(service, "", "subscription-service-type"));
        assertEquals(variables.get("globalSubscriberId"), text(service, "", "global-customer-id"));
        Element vnf = element(data, "", "vnf-information");
        assertEquals(variables.get("sliceServiceInstanceId"), text(vnf, "", "vnf-id"));
        assertEquals("transport-slice", text(vnf, "", "vnf-type"));
        for (Element owner : Arrays.asList(service, vnf)) {
            Element details = element(owner, "", "onap-model-information");
            assertEquals("invariant & <1>", text(details, "", "model-invariant-uuid"));
            assertEquals("model & <1>", text(details, "", "model-uuid"));
            assertEquals("1.0", text(details, "", "model-version"));
            assertEquals("model Gr\u00fc\u00dfe & <1>", text(details, "", "model-name"));
            assertEquals(customization ? 1 : 0,
                    details.getElementsByTagNameNS("", "model-customization-uuid").getLength());
            if (customization) {
                assertEquals("customization & <1>", text(details, "", "model-customization-uuid"));
            }
        }
        Element input = element(data, "", "vnf-request-input");
        assertEquals(variables.get("sliceServiceInstanceName"), text(input, "", "vnf-name"));
        assertEquals(variables.get("sliceServiceInstanceId"), text(input, "", "tenant"));
        assertEquals(variables.get("sliceServiceInstanceId"), text(input, "", "aic-cloud-region"));
    }

    private void assertDefaultParameters(Document request) {
        Element parameters = element(request.getDocumentElement(), "", "vnf-input-parameters");
        Element parameter = element(parameters, "", "param");
        assertEquals(variables.get("sliceServiceInstanceId"), text(parameter, "", "name"));
        assertEquals(0, parameter.getElementsByTagNameNS("", "value").getLength());
    }

    private Element element(Element parent, String namespace, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
        assertEquals(localName, 1, nodes.getLength());
        return (Element) nodes.item(0);
    }

    private String text(Element parent, String namespace, String localName) {
        Element element = element(parent, namespace, localName);
        if (namespace.isEmpty()) {
            assertNull(element.getNamespaceURI());
        }
        return element.getTextContent();
    }

    private Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
