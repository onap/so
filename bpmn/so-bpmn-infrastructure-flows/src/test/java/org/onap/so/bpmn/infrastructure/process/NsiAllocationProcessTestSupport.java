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
package org.onap.so.bpmn.infrastructure.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.onap.aai.domain.yang.NetworkRoute;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.UrnPropertiesReader;

public abstract class NsiAllocationProcessTestSupport extends WorkflowPersistenceSupport {
    private static final String PROCESS = "DoAllocateNSIandNSSI";
    private static final String ALLOCATION = "/api/rest/provMns/v1/NSS/SliceProfiles";
    private static final List<SubnetType> TRANSPORT =
            Arrays.asList(SubnetType.TN_FH, SubnetType.TN_MH, SubnetType.TN_BH);
    private final Map<String, Map<String, Object>> outcomes = new HashMap<>();
    private final List<String> owned = new ArrayList<>();
    private final List<String> inventory = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private HistoryService historyService;
    private WireMockServer wireMockServer;
    private int wireMockPort;
    private MockedStatic<UrnPropertiesReader> configuration;


    @Before
    public void configureShippedProcessesAndExternalBoundaries() throws Exception {
        runtimeService = engine.getRuntimeService();
        repositoryService = engine.getRepositoryService();
        historyService = engine.getHistoryService();
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        wireMockPort = wireMockServer.port();

        for (String process : Arrays.asList(PROCESS, "DoAllocateNSSI", "HandleOrchestrationTask")) {
            String path = "subprocess/" + process + ".bpmn";
            URL original = getClass().getClassLoader().getResource(path);
            assertNotNull(original);
            assertTrue(original.toExternalForm(),
                    original.toExternalForm().contains("/target/classes/")
                            || (original.toExternalForm().contains("so-bpmn-infrastructure-flows-")
                                    && !original.toExternalForm().contains("-tests.jar!/")));
            try (InputStream source = original.openStream()) {
                repositoryService.createDeployment().addInputStream(path, source).deploy();
            }
            var definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(process)
                    .latestVersion().singleResult();
            assertNotNull(definition);
            try (InputStream source = original.openStream();
                    InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                            definition.getResourceName())) {
                assertArrayEquals(source.readAllBytes(), deployed.readAllBytes());
            }
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put("so.auth.username", "test");
        properties.put("so.auth.password", "test");
        properties.put("mso.adapters.nssmf.endpoint", "http://localhost:" + wireMockPort);
        properties.put("mso.adapters.requestDb.endpoint", "http://localhost:" + wireMockPort);
        configuration = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS);
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            configuration
                    .when(() -> UrnPropertiesReader.getVariable(eq(property.getKey()), any(DelegateExecution.class)))
                    .thenReturn(property.getValue().toString());
            configuration.when(() -> UrnPropertiesReader.getVariable(property.getKey()))
                    .thenReturn(property.getValue().toString());
        }
        wireMockServer.resetAll();
        for (SubnetType domain : domains()) {
            wireMockServer.stubFor(post(urlEqualTo(ALLOCATION))
                    .withRequestBody(matchingJsonPath("$.serviceInfo.serviceUuid", equalTo("model-" + domain)))
                    .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")
                            .withBody("{\"jobId\":\"job-" + domain + "\",\"nssiId\":\"nssi-" + domain + "\"}")));
            wireMockServer.stubFor(post(urlEqualTo(job(domain)))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("{\"responseDescriptor\":{\"progress\":100,\"status\":\"FINISHED\","
                                    + "\"nssiId\":\"nssi-" + domain + "\",\"statusDescription\":\"complete " + domain
                                    + "\",\"errorCode\":\"0\"}}")));
        }
        wireMockServer.stubFor(put(urlEqualTo("/orchestrationTask/task-1"))
                .willReturn(aResponse().withStatus(200).withBody("{\"updated\":true}")));
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            doAnswer(call -> record(call.getArgument(0), call.getArgument(1))).when(client)
                    .create(any(AAIResourceUri.class), any(NetworkRoute.class));
            doAnswer(call -> record(call.getArgument(0), call.getArgument(1))).when(client)
                    .create(any(AAIResourceUri.class), any(ServiceInstance.class));
            doAnswer(call -> record(call.getArgument(0), call.getArgument(1))).when(client)
                    .create(any(AAIResourceUri.class), any(SliceProfile.class));
            doAnswer(call -> record(call.getArgument(0), call.getArgument(1))).when(client)
                    .create(any(AAIResourceUri.class), any(Relationship.class));
        });
    }

    @After
    public void closeOwnedProcessesAndInventoryBoundary() {
        for (String id : owned) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "NSI slice test cleanup");
            }
        }
        try {
            if (clients != null) {
                clients.close();
            }
        } finally {
            try {
                if (configuration != null) {
                    configuration.close();
                }
            } finally {
                if (wireMockServer != null) {
                    wireMockServer.stop();
                }
            }
        }
    }

    protected ProcessInstance start(boolean frontAndMidhaul, String... entries) {
        Map<String, Object> input = new HashMap<>();
        SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
        parameters.setServiceId("service-profile");
        parameters.setServiceName("slice & \u96ea");
        parameters.setSuggestNsiId("nsi-1");
        parameters.setSuggestNsiName("nsi & \u96ea");
        parameters.getServiceProfile().put("sST", "embb");
        parameters.getServiceProfile().put("shared", false);
        parameters.getServiceProfile().put("count", 0);
        parameters.getServiceProfile().put("optional", null);
        List<SubnetType> required = entries.length == 2
                ? (frontAndMidhaul ? domains() : Arrays.asList(SubnetType.AN, SubnetType.CN, SubnetType.TN_BH))
                : (entries[0].equals("Activity_0z1ympd") ? TRANSPORT : Arrays.asList(SubnetType.TN_BH));
        for (SubnetType domain : domains()) {
            SliceTaskInfo<SliceProfileAdapter> task = task(parameters, domain);
            task.setEndPointId("endpoint-" + domain);
            if (!required.contains(domain)) {
                continue;
            }
            task.setSubnetType(domain);
            task.setNetworkType(domain.getNetworkType());
            task.setVendor("vendor-" + domain);
            task.setSliceInstanceId("profile-instance-" + domain);
            task.getNSSTInfo().setUUID("model-" + domain);
            task.getNSSTInfo().setInvariantUUID("invariant-" + domain);
            SliceProfileAdapter profile = new SliceProfileAdapter();
            profile.setSliceProfileId("profile-" + domain);
            profile.setSNSSAIList("001-000001|001-000002");
            profile.setPLMNIdList("00101|00202");
            profile.setCoverageAreaTAList("100|200");
            profile.setResourceSharingLevel("shared");
            profile.setIpAddress(domain == SubnetType.AN ? "192.0.2.10" : "198.51.100.20");
            profile.setNextHopInfo("192.0.2.1");
            profile.setLogicInterfaceId("interface-" + domain);
            task.setSliceProfile(profile);
        }
        input.put("sliceTaskParams", parameters);
        input.put("sliceServiceInstanceName", "slice & \u96ea");
        input.put("globalSubscriberId", "customer-1");
        input.put("subscriptionServiceType", "5G");
        input.put("isNSIOptionAvailable", false);
        input.put("processFHandMH", frontAndMidhaul);
        input.put("msoRequestId", "request-1");
        input.put("mso-request-id", "wire-request-1");
        input.put("taskId", "task-1");
        input.put("taskName", "allocate & \u96ea");
        input.put("taskStatus", "running");
        input.put("isManual", "false");
        var builder = runtimeService.createProcessInstanceByKey(PROCESS).businessKey(UUID.randomUUID().toString())
                .setVariables(input);
        for (String entry : entries) {
            builder.startBeforeActivity(entry);
        }
        ProcessInstanceWithVariables instance = builder.executeWithVariablesInReturn();
        outcomes.put(instance.getId(), instance.getVariables());
        owned.add(instance.getId());
        return instance;
    }

    protected void completed(ProcessInstance process, List<SubnetType> expected, int inventoryWrites) {
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertNotNull(historyService.createHistoricProcessInstanceQuery().processInstanceId(process.getId())
                .singleResult().getEndTime());
        assertNull(value(process, "WorkflowException"));
        Object restored = value(process, "sliceTaskParams");
        assertTrue(restored instanceof SliceTaskParamsAdapter);
        SliceTaskParamsAdapter parameters = (SliceTaskParamsAdapter) restored;
        assertEquals(Boolean.FALSE, parameters.getServiceProfile().get("shared"));
        assertEquals(Integer.valueOf(0), parameters.getServiceProfile().get("count"));
        assertTrue(parameters.getServiceProfile().containsKey("optional"));
        assertNull(parameters.getServiceProfile().get("optional"));
        for (SubnetType domain : expected) {
            assertEquals("nssi-" + domain, task(parameters, domain).getSuggestNssiId());
            String variable = domain == SubnetType.AN ? "An"
                    : domain == SubnetType.CN ? "Cn"
                            : domain == SubnetType.TN_FH ? "TnFH" : domain == SubnetType.TN_MH ? "TnMH" : "TnBH";
            assertTrue(value(process, variable + "AllocateNssiNbiRequest") instanceof NssmfAdapterNBIRequest);
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(ALLOCATION))
                    .withRequestBody(matchingJsonPath("$.serviceInfo.serviceUuid", equalTo("model-" + domain))));
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(job(domain))));
        }
        wireMockServer.verify(expected.size(), putRequestedFor(urlEqualTo("/orchestrationTask/task-1")));
        assertEquals(expected.size() * 3, wireMockServer.getAllServeEvents().size());
        assertEquals(inventoryWrites, inventory.size());
        assertEquals(expected.size(), historyService.createHistoricProcessInstanceQuery()
                .superProcessInstanceId(process.getId()).processDefinitionKey("DoAllocateNSSI").finished().count());
        activity(process, "EndEvent_0elqlhv", 1);
    }

    private Object record(AAIResourceUri uri, Object payload) {
        inventory.add(payload.getClass().getSimpleName() + " " + uri.build().getPath());
        return null;
    }

    private Object value(ProcessInstance instance, String name) {
        return outcomes.get(instance.getId()).get(name);
    }

    protected void activity(ProcessInstance instance, String id, long count) {
        assertEquals(id, count, historyService.createHistoricActivityInstanceQuery().processInstanceId(instance.getId())
                .activityId(id).finished().count());
    }

    protected List<SubnetType> domains() {
        return Arrays.asList(SubnetType.AN, SubnetType.CN, SubnetType.TN_FH, SubnetType.TN_MH, SubnetType.TN_BH);
    }

    private SliceTaskInfo<SliceProfileAdapter> task(SliceTaskParamsAdapter parameters, SubnetType domain) {
        switch (domain) {
            case AN:
                return parameters.getAnSliceTaskInfo();
            case CN:
                return parameters.getCnSliceTaskInfo();
            case TN_FH:
                return parameters.getTnFHSliceTaskInfo();
            case TN_MH:
                return parameters.getTnMHSliceTaskInfo();
            default:
                return parameters.getTnBHSliceTaskInfo();
        }
    }

    private String job(SubnetType domain) {
        return "/api/rest/provMns/v1/NSS/jobs/job-" + domain;
    }
}
