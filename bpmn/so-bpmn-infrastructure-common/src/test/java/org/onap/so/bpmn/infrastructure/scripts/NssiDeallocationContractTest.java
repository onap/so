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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
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
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.beans.nsmf.DeAllocateNssi;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class NssiDeallocationContractTest {
    private static final String PATH = "/api/rest/provMns/v1/NSS/SliceProfiles/profile-1";
    @Rule
    public WireMockRule adapter = new WireMockRule(0);
    private final DoDeallocateNSSI script = new DoDeallocateNSSI();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Object> current = new HashMap<>();

    @Before
    public void configureExecution() {
        current.put("modelInvariantId", "invariant-1");
        current.put("modelVersionId", "model-2");
        current.put("nssiServiceInstanceId", "nssi-1");
        current.put("nsiServiceInstanceId", "nsi-1");
        current.put("snssai", "001-000001");
        current.put("profileId", "profile-1");
        current.put("globalSubscriberId", "customer-1");
        current.put("serviceType", "5G");
        current.put("vendor", "vendor-1");
        current.put("domainType", NetworkType.CORE);
        current.put("jobId", "previous-job");
        current.put("jobProgress", Integer.valueOf(0));
        current.put("proportion", Integer.valueOf(30));
        current.put("e2eServiceInstanceId", "service-1");
        current.put("operationId", "operation-1");
        current.put("status", "processing");
        variables.put("currentNSSI", current);
        variables.put("testProcessKey", "DoDeallocateNSSI");
        variables.put("scriptName", "deallocate-script");
        variables.put("mso.adapters.nssmf.endpoint", "http://localhost:" + adapter.port());
        variables.put("mso.adapters.db.auth", "test:test");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost/status");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingAcceptsTheCurrentNssiWithoutReplacingItsTypedState() {
        script.preProcessRequest(execution);
        assertSame(current, variables.get("currentNSSI"));
        assertFalse(variables.containsKey("WorkflowException"));
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    @Test
    public void absentOrEmptyCurrentNssiUsesTheBusinessValidationError() {
        for (Object missing : new Object[] {null, Collections.emptyMap()}) {
            variables.put("currentNSSI", missing);
            assertWorkflowFailure(() -> script.preProcessRequest(execution), 500);
            WorkflowException error = (WorkflowException) variables.get("WorkflowException");
            assertTrue(error.getErrorMessage().contains("currentNSSI"));
        }
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    @Test
    public void decompositionRequestRetainsBothModelIdentifiersAndTheEmptyVersion() {
        script.prepareDecomposeService(execution);
        assertTrue(variables.get("serviceModelInfo") instanceof String);
        JSONObject model = new JSONObject((String) variables.get("serviceModelInfo"));
        assertEquals("invariant-1", model.getString("modelInvariantUuid"));
        assertEquals("model-2", model.getString("modelUuid"));
        assertEquals("", model.getString("modelVersion"));
        assertEquals(3, model.length());
    }

    @Test
    public void decompositionMapsEverySupportedCategoryToATypedDomainAndRetainsProfileState() {
        for (SubnetType domain : new SubnetType[] {SubnetType.CN, SubnetType.AN, SubnetType.TN_BH, SubnetType.TN_MH,
                SubnetType.TN_FH}) {
            ServiceDecomposition decomposition = new ServiceDecomposition();
            decomposition.setServiceCategory(domain.name());
            decomposition.setServiceRole("vendor-" + domain.name());
            variables.put("serviceDecomposition", decomposition);
            script.processDecomposition(execution);
            assertSame(domain.getNetworkType(), current.get("domainType"));
            assertEquals("vendor-" + domain.name(), current.get("vendor"));
            assertEquals("profile-1", current.get("profileId"));
            assertEquals(Integer.valueOf(0), current.get("jobProgress"));
        }
    }

    @Test
    public void pollingHandoffRetainsTheExactJobIdentifierAsAString() {
        current.put("jobId", "job-001");
        script.prepareJobStatusRequest(execution);
        assertEquals("job-001", variables.get("jobId"));
        assertTrue(variables.get("jobId") instanceof String);
    }

    @Test
    public void changedProgressRequiresADatabaseUpdateWithoutClaimingCompletion() {
        progress("processing", "20");
        assertSame(Boolean.FALSE, variables.get("isNSSIDeAllocated"));
        assertSame(Boolean.TRUE, variables.get("isNeedUpdateDB"));
        assertEquals(Integer.valueOf(20), current.get("jobProgress"));
        assertEquals("processing", current.get("status"));
        assertEquals("deallocation pending", current.get("statusDescription"));
    }

    @Test
    public void unchangedProgressDoesNotRequestAnotherDatabaseUpdate() {
        current.put("jobProgress", Integer.valueOf(20));
        progress("processing", "20");
        assertSame(Boolean.FALSE, variables.get("isNSSIDeAllocated"));
        assertSame(Boolean.FALSE, variables.get("isNeedUpdateDB"));
        assertEquals(Integer.valueOf(20), current.get("jobProgress"));
    }

    @Test
    public void completedProgressSetsBooleanCompletionAndIntegerProgress() {
        progress("finished", "100");
        assertSame(Boolean.TRUE, variables.get("isNSSIDeAllocated"));
        assertSame(Boolean.TRUE, variables.get("isNeedUpdateDB"));
        assertEquals(Integer.valueOf(100), current.get("jobProgress"));
    }

    @Test
    public void weightedDatabaseProgressPreservesOperationIdentityAndDomain() throws Exception {
        for (int progress : new int[] {0, 50, 100}) {
            current.put("jobProgress", Integer.valueOf(progress));
            script.prepareUpdateOperationStatus(execution);
            assertEquals("http://localhost/status", variables.get("dbAdapterEndpoint"));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader((String) variables.get("updateOperationStatus"))));
            assertEquals(1,
                    document.getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateServiceOperationStatus")
                            .getLength());
            assertEquals("service-1", text(document, "serviceId"));
            assertEquals("operation-1", text(document, "operationId"));
            assertEquals("DELETE", text(document, "operationType"));
            assertEquals("processing", text(document, "result"));
            assertEquals("CORE processing", text(document, "operationContent"));
            assertEquals(String.valueOf(progress * 30 / 100), text(document, "progress"));
        }
    }

    @Test
    public void acceptedDeallocationSendsTypedContractsOnceAndResetsProgressForTheReturnedJob() {
        adapter.stubFor(post(urlEqualTo(PATH))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")
                        .withBody("{\"jobId\":\"new-job\",\"nssiId\":\"nssi-1\"}")));
        current.put("jobProgress", Integer.valueOf(60));
        script.sendRequestToNSSMF(execution);
        adapter.verify(1, postRequestedFor(urlEqualTo(PATH)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                .withHeader("Content-Type", containing("application/json")));
        JSONObject payload =
                new JSONObject(adapter.findAll(postRequestedFor(urlEqualTo(PATH))).get(0).getBodyAsString());
        assertEquals(3, payload.length());
        JSONObject request = payload.getJSONObject("deAllocateNssi");
        assertEquals("nsi-1", request.getString("nsiId"));
        assertEquals("nssi-1", request.getString("nssiId"));
        assertEquals("profile-1", request.getString("sliceProfileId"));
        assertEquals("deallocate-script", request.getString("scriptName"));
        assertEquals(1, request.getJSONArray("snssaiList").length());
        assertEquals("001-000001", request.getJSONArray("snssaiList").getString(0));
        assertFalse(request.has("terminateNssiOption"));
        assertEquals("vendor-1", payload.getJSONObject("esrInfo").getString("vendor"));
        assertEquals("cn", payload.getJSONObject("esrInfo").getString("networkType"));
        JSONObject service = payload.getJSONObject("serviceInfo");
        assertEquals("invariant-1", service.getString("serviceInvariantUuid"));
        assertEquals("model-2", service.getString("serviceUuid"));
        assertEquals("nsi-1", service.getString("nsiId"));
        assertEquals("nssi-1", service.getString("nssiId"));
        assertEquals("customer-1", service.getString("globalSubscriberId"));
        assertEquals("5G", service.getString("subscriptionServiceType"));
        assertTrue(variables.get("deAllocateNssi") instanceof DeAllocateNssi);
        assertEquals(0, ((DeAllocateNssi) variables.get("deAllocateNssi")).getTerminateNssiOption());
        assertTrue(variables.get("esrInfo") instanceof String);
        assertTrue(variables.get("serviceInfo") instanceof String);
        assertEquals(payload.getJSONObject("esrInfo").toMap(),
                new JSONObject((String) variables.get("esrInfo")).toMap());
        assertEquals(service.toMap(), new JSONObject((String) variables.get("serviceInfo")).toMap());
        assertEquals("new-job", current.get("jobId"));
        assertEquals(Integer.valueOf(0), current.get("jobProgress"));
        assertSame(current, variables.get("currentNSSI"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void rejectedDeallocationDoesNotReplaceAnExistingJobOrItsProgress() {
        for (int status : new int[] {400, 500}) {
            adapter.resetAll();
            adapter.stubFor(post(urlEqualTo(PATH)).willReturn(
                    aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody("{}")));
            current.put("jobProgress", Integer.valueOf(60));
            assertWorkflowFailure(() -> script.sendRequestToNSSMF(execution), 7000);
            assertEquals("previous-job", current.get("jobId"));
            assertEquals(Integer.valueOf(60), current.get("jobProgress"));
            adapter.verify(1, postRequestedFor(urlEqualTo(PATH)));
        }
    }

    @Test
    public void anEmptySuccessfulDeallocationResponseCannotResetThePreviousJob() {
        adapter.stubFor(post(urlEqualTo(PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("")));
        current.put("jobProgress", Integer.valueOf(60));
        assertWorkflowFailure(() -> script.sendRequestToNSSMF(execution), 7000);
        assertEquals("previous-job", current.get("jobId"));
        assertEquals(Integer.valueOf(60), current.get("jobProgress"));
        adapter.verify(1, postRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    public void failedPollingRetainsProgressAndRequestsFailurePersistenceWithTheExistingStringFlag() {
        current.put("jobProgress", Integer.valueOf(60));
        assertWorkflowFailure(() -> progress("failed", "100"), 7000);
        assertEquals(Integer.valueOf(60), current.get("jobProgress"));
        assertEquals("true", variables.get("isNeedUpdateDB"));
    }

    @Test
    public void absentOrInvalidPollingProgressFailsBeforeChangingTheCurrentProgress() {
        for (String invalid : new String[] {null, "", "invalid"}) {
            current.put("jobProgress", Integer.valueOf(60));
            JSONObject descriptor = new JSONObject().put("status", "processing");
            if (invalid != null) {
                descriptor.put("progress", invalid);
            }
            variables.put("responseDescriptor", descriptor.toString());
            assertWorkflowFailure(() -> script.handleJobStatus(execution), 7000);
            assertEquals(Integer.valueOf(60), current.get("jobProgress"));
        }
    }

    private void progress(String status, String progress) {
        variables.put("responseDescriptor", new JSONObject().put("status", status)
                .put("statusDescription", "deallocation pending").put("progress", progress).toString());
        script.handleJobStatus(execution);
    }

    private void assertWorkflowFailure(Runnable action, int code) {
        try {
            action.run();
            fail("Expected workflow error " + code);
        } catch (BpmnError error) {
            assertEquals(String.valueOf(code), error.getErrorCode());
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            assertEquals(code, ((WorkflowException) variables.get("WorkflowException")).getErrorCode());
        }
    }

    private String text(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagNameNS("", name).getLength());
        return document.getElementsByTagNameNS("", name).item(0).getTextContent();
    }
}
