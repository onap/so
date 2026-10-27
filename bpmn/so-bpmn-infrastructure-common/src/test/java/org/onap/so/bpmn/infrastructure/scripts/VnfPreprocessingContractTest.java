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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.w3c.dom.Document;

public class VnfPreprocessingContractTest {
    @Rule
    public WireMockRule catalog = new WireMockRule(0);
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final DoCreateVnf script = new DoCreateVnf();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "DoCreateVnf");
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceId", "service-1");
        variables.put("vnfName", "vnf-1");
        variables.put("vnfId", "existing-vnf-id");
        variables.put("vnfType", "router");
        variables.put("lcpCloudRegionId", "request-region");
        variables.put("tenantId", "tenant-1");
        variables.put("vnfResourceDecomposition", new VnfResource());
        variables.put("mso.workflow.sdncadapter.callback", "http://localhost/callback");
        variables.put("mso.catalog.db.spring.endpoint", "http://localhost:" + catalog.port());
        variables.put("so.auth.username", "test");
        variables.put("so.auth.password", "test");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        catalog.stubFor(get(urlEqualTo("/homingInstance/service-1")).willReturn(aResponse().withStatus(404)));
    }

    @Test
    public void defaultsKeepTheirNamesAndRuntimeTypes() {
        script.preProcessRequest(execution);
        assertEquals("DoCVNF_", variables.get("prefix"));
        assertEquals("request-1", variables.get("DoCVNF_requestId"));
        assertEquals("request-1", variables.get("mso-request-id"));
        assertEquals("service-1", variables.get("DoCVNF_serviceInstanceId"));
        assertSame(Boolean.FALSE, variables.get("SDNCInteractionEnabled"));
        assertEquals("1702", variables.get("DoCVNF_sdncVersion"));
        assertEquals("", variables.get("DoCVNF_modelVersionId"));
        assertEquals("", variables.get("DoCVNF_modelCustomizationId"));
        assertEquals("", variables.get("DoCVNF_globalSubscriberId"));
        assertEquals("existing-vnf-id", variables.get("DoCVNF_vnfId"));
        assertEquals("http://localhost/callback", variables.get("DoCVNF_sdncCallbackUrl"));
        catalog.verify(1, getRequestedFor(urlEqualTo("/homingInstance/service-1")));
    }

    @Test
    public void rollbackDataRetainsExistingEntriesAndStringEligibilityFlags() {
        RollbackData original = new RollbackData();
        original.put("OTHER", "resource", "keep");
        original.put("VNF", "custom", "keep-too");
        variables.put("rollbackData", original);
        script.preProcessRequest(execution);
        assertSame(original, variables.get("rollbackData"));
        assertEquals("keep", original.get("OTHER", "resource"));
        assertEquals("keep-too", original.get("VNF", "custom"));
        assertEquals("service-1", original.get("VNF", "serviceInstanceId"));
        assertEquals("tenant-1", original.get("VNF", "tenantId"));
        assertEquals("VID", original.get("VNF", "source"));
        for (String flag : new String[] {"rollbackSDNCAssign", "rollbackSDNCActivate", "rollbackVnfCreate"}) {
            assertEquals("false", original.get("VNF", flag));
        }
    }

    @Test
    public void modelIdentifiersAndExplicitSdncVersionArePreserved() {
        variables.put("vnfModelInfo", "{\"modelInvariantUuid\":\"invariant-1\",\"modelUuid\":\"version-1\","
                + "\"modelVersion\":\"2.0\",\"modelName\":\"Grüße\",\"modelCustomizationUuid\":\"custom-1\"}");
        variables.put("sdncVersion", "1806");
        variables.put("disableRollback", "true");
        script.preProcessRequest(execution);
        assertEquals("invariant-1", variables.get("DoCVNF_modelInvariantId"));
        assertEquals("version-1", variables.get("DoCVNF_modelVersionId"));
        assertEquals("2.0", variables.get("DoCVNF_modelVersion"));
        assertEquals("Grüße", variables.get("DoCVNF_modelName"));
        assertEquals("custom-1", variables.get("DoCVNF_modelCustomizationId"));
        assertEquals("1806", variables.get("DoCVNF_sdncVersion"));
        assertEquals("true", variables.get("DoCVNF_suppressRollback"));
    }

    @Test
    public void generatedResourceIdIsAValidUuidWhenNoIdWasProvided() {
        variables.remove("vnfId");
        script.preProcessRequest(execution);
        Object generated = variables.get("DoCVNF_vnfId");
        assertTrue(generated instanceof String);
        assertEquals(generated, UUID.fromString((String) generated).toString());
    }

    @Test
    public void requestInformationEscapesXmlAndRetainsItsNamespace() throws Exception {
        variables.put("msoRequestId", "Grüße & <request>");
        script.preProcessRequest(execution);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(
                ((String) variables.get("DoCVNF_requestInfo")).getBytes(StandardCharsets.UTF_8)));
        String namespace = "http://org.onap/so/infra/vnf-request/v1";
        assertEquals(namespace, document.getDocumentElement().getNamespaceURI());
        assertEquals("Grüße & <request>",
                document.getElementsByTagNameNS(namespace, "request-id").item(0).getTextContent());
        assertEquals("CREATE", document.getElementsByTagNameNS(namespace, "action").item(0).getTextContent());
        assertEquals("VID", document.getElementsByTagNameNS(namespace, "source").item(0).getTextContent());
    }

    @Test
    public void homingResultUpdatesTheRegionInBothExecutionAndRollbackData() {
        catalog.stubFor(get(urlEqualTo("/homingInstance/service-1"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"serviceInstanceId\":\"service-1\"," + "\"cloud_region_id\":\"homed-region\"}")));
        script.preProcessRequest(execution);
        assertEquals("homed-region", variables.get("DoCVNF_cloudSiteId"));
        assertEquals("homed-region", ((RollbackData) variables.get("rollbackData")).get("VNF", "cloudSiteId"));
        catalog.verify(1, getRequestedFor(urlEqualTo("/homingInstance/service-1")));
    }

    @Test
    public void unavailableHomingKeepsTheRequestedRegion() {
        catalog.stubFor(get(urlEqualTo("/homingInstance/service-1")).willReturn(aResponse().withStatus(503)));
        script.preProcessRequest(execution);
        assertEquals("request-region", variables.get("DoCVNF_cloudSiteId"));
        assertEquals("request-region", ((RollbackData) variables.get("rollbackData")).get("VNF", "cloudSiteId"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void blankCallbackFailsBeforeHomingOrRollbackEligibilityIsStored() {
        for (String callback : new String[] {"", " "}) {
            variables.put("mso.workflow.sdncadapter.callback", callback);
            try {
                script.preProcessRequest(execution);
                fail("Expected callback validation error");
            } catch (BpmnError error) {
                assertEquals("2000", error.getErrorCode());
                WorkflowException stored = (WorkflowException) variables.get("WorkflowException");
                assertEquals(2000, stored.getErrorCode());
                assertEquals("Required variable 'mso.workflow.sdncadapter.callback' is missing",
                        stored.getErrorMessage());
            }
            assertFalse(variables.containsKey("rollbackData"));
            assertTrue(catalog.getAllServeEvents().isEmpty());
        }
    }
}
