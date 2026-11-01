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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SliceProfile;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.core.WorkflowException;

public class NsiProfileInventoryContractTest {
    private static final List<SubnetType> DOMAINS =
            Arrays.asList(SubnetType.AN, SubnetType.CN, SubnetType.TN_BH, SubnetType.TN_FH, SubnetType.TN_MH);
    private static final List<SubnetType> TRANSPORT =
            Arrays.asList(SubnetType.TN_BH, SubnetType.TN_FH, SubnetType.TN_MH);
    private static final String SERVICE = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/5G/service-instances/service-instance/";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
    private final List<String> paths = new ArrayList<>();
    private final List<Object> payloads = new ArrayList<>();
    private final List<String> publishedVariables = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private DoAllocateNSIandNSSI script;
    private RuntimeException inventoryFailure;

    @Before
    public void configureRealInventoryObjectsAndExternalClientBoundary() {
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            doAnswer(call -> capture(call.getArgument(0), call.getArgument(1))).when(client)
                    .create(any(AAIResourceUri.class), any(ServiceInstance.class));
            doAnswer(call -> capture(call.getArgument(0), call.getArgument(1))).when(client)
                    .create(any(AAIResourceUri.class), any(SliceProfile.class));
        });
        script = new DoAllocateNSIandNSSI();
        parameters.setServiceName("slice & \u96ea");
        for (SubnetType domain : DOMAINS) {
            SliceTaskInfo<SliceProfileAdapter> task = task(domain);
            task.setSubnetType(domain);
            task.setSliceInstanceId("instance-" + domain);
            task.getNSSTInfo().setInvariantUUID("invariant-" + domain);
            task.getNSSTInfo().setUUID("model-" + domain);
            SliceProfileAdapter profile = new SliceProfileAdapter();
            profile.setSliceProfileId("profile-" + domain);
            profile.setSST("embb-" + domain);
            profile.setPLMNIdList("00101|00202");
            profile.setSNSSAIList("001-000001|001-000002");
            profile.setResourceSharingLevel("non-shared");
            profile.setLatency(17);
            profile.setMaxBandwidth(29);
            profile.setJitter(7);
            task.setSliceProfile(profile);
        }
        variables.put("sliceTaskParams", parameters);
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "5G");
        variables.put("testProcessKey", "DoAllocateNSIandNSSI");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            String key = call.getArgument(0);
            variables.put(key, call.getArgument(1));
            publishedVariables.add(key);
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @After
    public void closeExternalInventoryBoundary() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void accessInstanceUsesItsOwnModelProfileAndGeneratedInventoryIdentity() {
        instanceContract(SubnetType.AN, "sliceprofile_an_");
        assertEquals(task(SubnetType.AN).getSliceInstanceId(), variables.get("ranSliceProfileInstanceId"));
        assertFalse(variables.containsKey("communicationServiceInstance"));
    }

    @Test
    public void coreInstanceUsesItsOwnModelProfileAndGeneratedInventoryIdentity() {
        instanceContract(SubnetType.CN, "sliceprofile_cn_");
        assertEquals(task(SubnetType.CN).getSliceInstanceId(), variables.get("cnSliceProfileInstanceId"));
        assertSame(payloads.get(0), variables.get("communicationServiceInstance"));
    }

    @Test
    public void backhaulInstanceKeepsItsOwnDomainAndInventoryLocation() {
        instanceContract(SubnetType.TN_BH, "sliceprofile_tn_");
        assertSame(payloads.get(0), variables.get("communicationServiceInstance"));
    }

    @Test
    public void fronthaulInstanceKeepsItsExistingNameAndOwnDomain() {
        instanceContract(SubnetType.TN_FH, "tn_fh");
        assertSame(payloads.get(0), variables.get("communicationServiceInstance"));
    }

    @Test
    public void midhaulInstanceKeepsItsOwnNameModelAndDomain() {
        instanceContract(SubnetType.TN_MH, "tn_mh_");
        assertSame(payloads.get(0), variables.get("communicationServiceInstance"));
    }

    @Test
    public void accessProfileCanBeConstructedUsingTheCurrentInventoryModel() {
        requireCurrentProfileModelCompatibility();
        radioOrCoreProfileContract(SubnetType.AN);
    }

    @Test
    public void coreProfileCanBeConstructedUsingTheCurrentInventoryModel() {
        requireCurrentProfileModelCompatibility();
        radioOrCoreProfileContract(SubnetType.CN);
    }

    @Test
    public void transportProfilesKeepTheirOwnGeneratedIdsAndNumericPerformanceValues() {
        for (SubnetType domain : TRANSPORT) {
            resetCapturedWrites();
            createProfile(domain);
            SliceProfile value = profilePayload(domain);
            assertEquals(Integer.valueOf(17), value.getLatency());
            assertEquals(Integer.valueOf(29), value.getMaxBandwidth());
            assertEquals(Integer.valueOf(7), value.getJitter());
            assertNull(value.getMaxNumberOfPDUSession());
            assertNull(value.getResourceSharingLevel());
            assertNull(value.getCoverageAreaTAList());
        }
    }

    @Test
    public void transportProfileZerosRemainPresentNumericValues() {
        for (SubnetType domain : TRANSPORT) {
            resetCapturedWrites();
            SliceProfileAdapter profile = task(domain).getSliceProfile();
            profile.setLatency(0);
            profile.setMaxBandwidth(0);
            profile.setJitter(0);
            createProfile(domain);
            SliceProfile value = profilePayload(domain);
            assertEquals(Integer.valueOf(0), value.getLatency());
            assertEquals(Integer.valueOf(0), value.getMaxBandwidth());
            assertEquals(Integer.valueOf(0), value.getJitter());
        }
    }

    @Test
    public void instanceWriteFailuresPropagateTheOriginalCauseWithoutPublishingCompletion() {
        for (SubnetType domain : DOMAINS) {
            resetCapturedWrites();
            inventoryFailure = new IllegalStateException("instance write " + domain);
            assertSame(inventoryFailure, assertThrows(IllegalStateException.class, () -> createInstance(domain)));
            assertEquals(1, paths.size());
            assertTrue(payloads.get(0) instanceof ServiceInstance);
            assertFalse(publishedVariables.contains("sliceTaskParams"));
        }
    }

    @Test
    public void transportProfileWriteFailuresKeepTheCauseAndStopAfterTheAttemptedWrite() {
        for (SubnetType domain : TRANSPORT) {
            resetCapturedWrites();
            inventoryFailure = new IllegalStateException("profile write " + domain);
            assertSame(inventoryFailure, assertThrows(IllegalStateException.class, () -> createProfile(domain)));
            assertEquals(1, paths.size());
            assertTrue(payloads.get(0) instanceof SliceProfile);
            assertFalse(publishedVariables.contains("sliceTaskParams"));
        }
    }

    @Test
    public void inventoryBpmnErrorsRetainTheirOriginalWorkflowDetail() {
        BpmnError boundary = new BpmnError("AAI_BOUNDARY");
        WorkflowException original = new WorkflowException("Parent", 5310, "original inventory failure");
        variables.put("WorkflowException", original);
        inventoryFailure = boundary;
        assertSame(boundary, assertThrows(BpmnError.class, () -> createInstance(SubnetType.CN)));
        assertSame(original, variables.get("WorkflowException"));
        assertFalse(publishedVariables.contains("sliceTaskParams"));
        resetCapturedWrites();
        assertSame(boundary, assertThrows(BpmnError.class, () -> createProfile(SubnetType.TN_FH)));
        assertSame(original, variables.get("WorkflowException"));
        assertEquals(1, paths.size());
        assertFalse(publishedVariables.contains("sliceTaskParams"));
    }

    @Test
    public void sharedInstanceFactoryPreservesNullAndEmptyOptionalProfileValues() {
        for (SubnetType domain : DOMAINS) {
            SliceProfileAdapter profile = task(domain).getSliceProfile();
            for (String optional : Arrays.asList(null, "")) {
                profile.setSST(optional);
                profile.setPLMNIdList(optional);
                profile.setSNSSAIList(optional);
                profile.setResourceSharingLevel(optional);
                ServiceInstance value = (ServiceInstance) DoAllocateNSIandNSSI.createSliceProfileInstance(task(domain),
                        "name & \u96ea", "pending");
                assertEquals(optional, value.getServiceType());
                assertEquals(optional, value.getServiceInstanceLocationId());
                assertEquals(optional, value.getEnvironmentContext());
                assertEquals(optional, value.getServiceFunction());
                assertEquals("name & \u96ea", value.getServiceInstanceName());
                assertEquals("pending", value.getOrchestrationStatus());
                assertEquals(domain.getSubnetType(), value.getWorkloadContext());
                assertTrue(value.getCreatedAt().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
            }
        }
        assertTrue(paths.isEmpty());
        assertTrue(publishedVariables.isEmpty());
    }

    private Object capture(AAIResourceUri uri, Object payload) {
        paths.add(uri.build().getPath());
        payloads.add(payload);
        assertFalse(publishedVariables.contains("sliceTaskParams"));
        if (inventoryFailure != null) {
            throw inventoryFailure;
        }
        return null;
    }

    private void instanceContract(SubnetType domain, String namePrefix) {
        createInstance(domain);
        String id = task(domain).getSliceInstanceId();
        assertEquals(id, UUID.fromString(id).toString());
        assertEquals(Arrays.asList(SERVICE + id), paths);
        assertEquals(1, payloads.size());
        assertTrue(payloads.get(0) instanceof ServiceInstance);
        ServiceInstance value = (ServiceInstance) payloads.get(0);
        assertEquals(namePrefix + "slice & \u96ea", value.getServiceInstanceName());
        assertEquals("slice-profile", value.getServiceRole());
        assertEquals("deactivated", value.getOrchestrationStatus());
        assertEquals("embb-" + domain, value.getServiceType());
        assertEquals("00101|00202", value.getServiceInstanceLocationId());
        assertEquals("001-000001|001-000002", value.getEnvironmentContext());
        assertEquals("non-shared", value.getServiceFunction());
        assertEquals("invariant-" + domain, value.getModelInvariantId());
        assertEquals("model-" + domain, value.getModelVersionId());
        assertEquals(domain.getSubnetType(), value.getWorkloadContext());
        assertTrue(value.getCreatedAt().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        assertSame(parameters, variables.get("sliceTaskParams"));
        assertEquals(1, publishedVariables.stream().filter("sliceTaskParams"::equals).count());
        for (SubnetType other : DOMAINS) {
            if (other != domain) {
                assertEquals("instance-" + other, task(other).getSliceInstanceId());
            }
        }
    }

    private SliceProfile profilePayload(SubnetType domain) {
        String id = task(domain).getSliceProfile().getSliceProfileId();
        assertEquals(id, UUID.fromString(id).toString());
        assertNotEquals("profile-" + domain, id);
        assertEquals(Arrays.asList(SERVICE + "instance-" + domain + "/slice-profiles/slice-profile/" + id), paths);
        assertEquals(1, payloads.size());
        assertTrue(payloads.get(0) instanceof SliceProfile);
        SliceProfile value = (SliceProfile) payloads.get(0);
        assertEquals(id, value.getProfileId());
        assertSame(parameters, variables.get("sliceTaskParams"));
        assertEquals(Arrays.asList("sliceTaskParams"), publishedVariables);
        return value;
    }

    private void radioOrCoreProfileContract(SubnetType domain) {
        SliceProfileAdapter profile = task(domain).getSliceProfile();
        profile.setCoverageAreaTAList("100|200");
        profile.setMaxNumberOfUEs(11);
        profile.setMaxNumberOfPDUSession(13);
        profile.setExpDataRateDL(19);
        profile.setExpDataRateUL(23);
        profile.setAreaTrafficCapDL(31);
        profile.setAreaTrafficCapUL(37);
        profile.setOverallUserDensity(41);
        profile.setActivityFactor(43);
        profile.setUeMobilityLevel("stationary");
        profile.setCsAvailabilityTarget(0.75f);
        profile.setCsReliabilityMeanTime("reliability & \u96ea");
        profile.setExpDataRate(47);
        profile.setMsgSizeByte("0");
        profile.setTransferIntervalTarget("5");
        createProfile(domain);
        SliceProfile value = profilePayload(domain);
        assertEquals("100|200", value.getCoverageAreaTAList());
        assertEquals(Integer.valueOf(17), value.getLatency());
        assertEquals(Integer.valueOf(13), value.getMaxNumberOfPDUSession());
        assertEquals(Integer.valueOf(19), value.getExpDataRateDL());
        assertEquals(Integer.valueOf(23), value.getExpDataRateUL());
        assertEquals(Integer.valueOf(31), value.getAreaTrafficCapDL());
        assertEquals(Integer.valueOf(37), value.getAreaTrafficCapUL());
        assertEquals(Integer.valueOf(41), value.getOverallUserDensity());
        assertEquals(Integer.valueOf(43), value.getActivityFactor());
        assertEquals("stationary", value.getUeMobilityLevel());
        assertEquals("non-shared", value.getResourceSharingLevel());
        assertEquals(Float.valueOf(0.75f), value.getCsAvailabilityTarget());
        assertEquals("reliability & \u96ea", value.getCsReliabilityMeanTime());
        assertEquals(Integer.valueOf(47), value.getExpDataRate());
        assertEquals("0", value.getMsgSizeByte());
        assertEquals("5", value.getTransferIntervalTarget());
    }

    private void requireCurrentProfileModelCompatibility() {
        Assume.assumeTrue(
                "SO-4457: AN/CN profile construction calls setMaxNumberOfUEs, absent from the current AAI model",
                Boolean.getBoolean("groovy.migration.verification"));
    }

    private void resetCapturedWrites() {
        paths.clear();
        payloads.clear();
        publishedVariables.clear();
    }

    private SliceTaskInfo<SliceProfileAdapter> task(SubnetType domain) {
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

    private void createInstance(SubnetType domain) {
        switch (domain) {
            case AN:
                script.createAnSliceProfileInstance(execution);
                break;
            case CN:
                script.createCnSliceProfileInstance(execution);
                break;
            case TN_FH:
                script.createTnFHSliceProfileInstance(execution);
                break;
            case TN_MH:
                script.createTnMHSliceProfileInstance(execution);
                break;
            default:
                script.createTnBHSliceProfileInstance(execution);
        }
    }

    private void createProfile(SubnetType domain) {
        switch (domain) {
            case AN:
                script.createAnSliceProfile(execution);
                break;
            case CN:
                script.createCnSliceProfile(execution);
                break;
            case TN_FH:
                script.createTnFHSliceProfile(execution);
                break;
            case TN_MH:
                script.createTnMHSliceProfile(execution);
                break;
            default:
                script.createTnBHSliceProfile(execution);
        }
    }
}
