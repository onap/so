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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.beans.nsmf.AllocateAnNssi;
import org.onap.so.beans.nsmf.AllocateCnNssi;
import org.onap.so.beans.nsmf.AllocateTnNssi;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.beans.nsmf.ServiceInfo;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;

public class NsiSubnetAllocationRequestContractTest {
    private final DoAllocateNSIandNSSI script = new DoAllocateNSIandNSSI();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void configureRealProfilesAndExecutionState() {
        parameters.setSuggestNsiId("nsi-1");
        parameters.setSuggestNsiName("nsi & \u96ea");
        parameters.getServiceProfile().put("sST", "fallback-sst");
        parameters.getServiceProfile().put("resourceSharingLevel", "shared");
        configure(parameters.getAnSliceTaskInfo(), SubnetType.AN);
        configure(parameters.getCnSliceTaskInfo(), SubnetType.CN);
        configure(parameters.getTnBHSliceTaskInfo(), SubnetType.TN_BH);
        configure(parameters.getTnFHSliceTaskInfo(), SubnetType.TN_FH);
        configure(parameters.getTnMHSliceTaskInfo(), SubnetType.TN_MH);
        variables.put("sliceTaskParams", parameters);
        variables.put("sliceServiceInstanceName", "slice \"\u96ea\"");
        variables.put("globalSubscriberId", "customer <one>");
        variables.put("subscriptionServiceType", "5G & private");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void accessRequestKeepsTypedProfileIdentityEndpointsAndZeroCapacity() {
        NssmfAdapterNBIRequest request = access();
        AllocateAnNssi allocation = request.getAllocateAnNssi();
        service(request.getServiceInfo(), parameters.getAnSliceTaskInfo(), "nssi_anslice \"\u96ea\"");
        assertEquals("model-AN", allocation.getNsstId());
        assertEquals("instance-AN", allocation.getSliceProfile().getSliceProfileId());
        assertEquals("script-AN.groovy", allocation.getScriptName());
        assertEquals("nsi-1", allocation.getNsiInfo().getNsiId());
        assertEquals("nsi & \u96ea", allocation.getNsiInfo().getNsiName());
        assertEquals("2001:db8::10", allocation.getEndPoint().getIpAddress());
        assertEquals("interface-AN", allocation.getEndPoint().getLogicInterfaceId());
        assertEquals("2001:db8::1", allocation.getEndPoint().getNextHopInfo());
        assertEquals(NetworkType.ACCESS, request.getEsrInfo().getNetworkType());
        assertEquals("vendor & \u96ea", request.getEsrInfo().getVendor());
        assertSame(SubnetType.AN, variables.get("anSubnetType"));
        assertSame(parameters.getAnSliceTaskInfo(), variables.get("anSliceTaskInfo"));
        JsonNode json = mapper.valueToTree(request);
        assertTrue(json.path("allocateAnNssi").path("sliceProfile").path("maxNumberofPDUSession").isInt());
        assertEquals(0, json.path("allocateAnNssi").path("sliceProfile").path("maxNumberofPDUSession").intValue());
        assertFalse(json.path("allocateAnNssi").path("sliceProfile").has("latency"));
        assertFalse(json.has("allocateCnNssi"));
        assertFalse(json.has("allocateTnNssi"));
    }

    @Test
    public void coreRequestKeepsItsOwnTypedProfileAndAdapterIdentity() {
        NssmfAdapterNBIRequest request = core();
        AllocateCnNssi allocation = request.getAllocateCnNssi();
        service(request.getServiceInfo(), parameters.getCnSliceTaskInfo(), "nssi_cnslice \"\u96ea\"");
        assertEquals("model-CN", allocation.getNsstId());
        assertEquals("instance-CN", allocation.getSliceProfile().getSliceProfileId());
        assertEquals("script-CN.groovy", allocation.getScriptName());
        assertEquals("nsi-1", allocation.getNsiInfo().getNsiId());
        assertEquals("interface-CN", allocation.getEndPoint().getLogicInterfaceId());
        assertEquals(NetworkType.CORE, request.getEsrInfo().getNetworkType());
        assertEquals("vendor & \u96ea", request.getEsrInfo().getVendor());
        assertSame(SubnetType.CN, variables.get("cnSubnetType"));
        assertSame(parameters.getCnSliceTaskInfo(), variables.get("cnSliceTaskInfo"));
        JsonNode json = mapper.valueToTree(request);
        assertTrue(json.path("allocateCnNssi").path("sliceProfile").path("latency").isInt());
        assertEquals(0, json.path("allocateCnNssi").path("sliceProfile").path("latency").intValue());
        assertFalse(json.has("allocateAnNssi"));
        assertFalse(json.has("allocateTnNssi"));
    }

    @Test
    public void sharedAccessSelectionKeepsItsNssiIdentityAndModelName() {
        SliceTaskInfo<SliceProfileAdapter> task = parameters.getAnSliceTaskInfo();
        task.setSuggestNssiId("existing-an");
        task.setSuggestNssiName("suggestion-name");
        NssmfAdapterNBIRequest request = access();
        assertEquals("existing-an", request.getAllocateAnNssi().getNssiId());
        assertEquals("existing-an", request.getServiceInfo().getNssiId());
        assertEquals("model name AN", request.getServiceInfo().getNssiName());
        assertEquals("nssi_anslice \"\u96ea\"", request.getAllocateAnNssi().getNssiName());
    }

    @Test
    public void sharedCoreSelectionRetainsItsIdentityAndCoreRequestName() {
        parameters.getCnSliceTaskInfo().setSuggestNssiId("existing-cn");
        NssmfAdapterNBIRequest request = core();
        assertEquals("existing-cn", request.getAllocateCnNssi().getNssiId());
        assertEquals("existing-cn", request.getServiceInfo().getNssiId());
        assertEquals("nssi_cnslice \"\u96ea\"", request.getServiceInfo().getNssiName());
    }

    @Test
    public void accessFallsBackToTheServiceSstForNullAndEmptyProfileValues() {
        for (String sst : Arrays.asList(null, "")) {
            parameters.getAnSliceTaskInfo().getSliceProfile().setSST(sst);
            assertEquals("fallback-sst", access().getServiceInfo().getSST());
        }
    }

    @Test
    public void coreFallsBackToTheServiceSstForNullAndEmptyProfileValues() {
        for (String sst : Arrays.asList(null, "")) {
            parameters.getCnSliceTaskInfo().getSliceProfile().setSST(sst);
            assertEquals("fallback-sst", core().getServiceInfo().getSST());
        }
    }

    @Test
    public void explicitUrllcProfilesOverrideTheServiceSstAndSelectTheirOwnPerformanceRequests() {
        parameters.getAnSliceTaskInfo().getSliceProfile().setSST("urllc");
        parameters.getCnSliceTaskInfo().getSliceProfile().setSST("urllc");
        NssmfAdapterNBIRequest an = access();
        NssmfAdapterNBIRequest cn = core();
        assertEquals("urllc", an.getServiceInfo().getSST());
        assertEquals("urllc", cn.getServiceInfo().getSST());
        assertEquals(1, an.getAllocateAnNssi().getSliceProfile().getPerfReq().getPerfReqUrllcList().size());
        assertEquals(1, cn.getAllocateCnNssi().getSliceProfile().getPerfReq().getPerfReqUrllcList().size());
        assertNull(an.getAllocateAnNssi().getSliceProfile().getPerfReq().getPerfReqEmbbList());
        assertNull(cn.getAllocateCnNssi().getSliceProfile().getPerfReq().getPerfReqEmbbList());
    }

    @Test
    public void requestProfilesAndEndpointsAreIndependentWithoutMutatingTheInputAdapters() {
        NssmfAdapterNBIRequest an = access();
        NssmfAdapterNBIRequest cn = core();
        assertNotSame(an, cn);
        assertNotSame(an.getServiceInfo(), cn.getServiceInfo());
        assertNotSame(an.getAllocateAnNssi().getEndPoint(), cn.getAllocateCnNssi().getEndPoint());
        an.getAllocateAnNssi().getSliceProfile().setLatency(99);
        cn.getAllocateCnNssi().getSliceProfile().setMaxNumberOfUEs(42);
        assertEquals(0, parameters.getAnSliceTaskInfo().getSliceProfile().getLatency());
        assertEquals(0, parameters.getCnSliceTaskInfo().getSliceProfile().getMaxNumberOfUEs());
        assertEquals("source-AN", parameters.getAnSliceTaskInfo().getSliceProfile().getSliceProfileId());
        assertEquals("source-CN", parameters.getCnSliceTaskInfo().getSliceProfile().getSliceProfileId());
        assertSame(an, variables.get("AnAllocateNssiNbiRequest"));
        assertSame(cn, variables.get("CnAllocateNssiNbiRequest"));
    }

    @Test
    public void singletonAndRepeatedNetworkListsRemainArraysInTheirInputOrder() {
        for (String values : Arrays.asList("first", "first|second")) {
            parameters.getAnSliceTaskInfo().getSliceProfile().setSNSSAIList(values);
            parameters.getCnSliceTaskInfo().getSliceProfile().setSNSSAIList(values);
            parameters.getAnSliceTaskInfo().getSliceProfile().setPLMNIdList(values);
            parameters.getCnSliceTaskInfo().getSliceProfile().setPLMNIdList(values);
            JsonNode expected = mapper.valueToTree(Arrays.asList(values.split("\\|")));
            JsonNode an = mapper.valueToTree(access().getAllocateAnNssi().getSliceProfile());
            JsonNode cn = mapper.valueToTree(core().getAllocateCnNssi().getSliceProfile());
            assertEquals(expected, an.path("snssaiList"));
            assertEquals(expected, cn.path("snssaiList"));
            assertEquals(expected, an.path("pLMNIdList"));
            assertEquals(expected, cn.path("pLMNIdList"));
        }
    }

    @Test
    public void nullAndEmptyOptionalNssiIdentifiersRemainDistinctInSerializedRequests() {
        for (String id : Arrays.asList(null, "")) {
            parameters.getAnSliceTaskInfo().setSuggestNssiId(id);
            parameters.getCnSliceTaskInfo().setSuggestNssiId(id);
            JsonNode an = mapper.valueToTree(access());
            JsonNode cn = mapper.valueToTree(core());
            assertEquals(id != null, an.path("allocateAnNssi").has("nssiId"));
            assertEquals(id != null, cn.path("allocateCnNssi").has("nssiId"));
            if (id != null) {
                assertEquals("", an.path("allocateAnNssi").path("nssiId").textValue());
                assertEquals("", cn.path("allocateCnNssi").path("nssiId").textValue());
            }
        }
    }

    @Test
    public void backhaulRequestConnectsAccessToCoreWithoutReversingTheEndpoints() {
        NssmfAdapterNBIRequest request = backhaul();
        service(request.getServiceInfo(), parameters.getTnBHSliceTaskInfo(), "nssi_tnslice \"\u96ea\"");
        AllocateTnNssi allocation = request.getAllocateTnNssi();
        assertEquals(1, allocation.getTransportSliceNetworks().size());
        assertEquals(1, allocation.getTransportSliceNetworks().get(0).getConnectionLinks().size());
        assertEquals("endpoint-AN",
                allocation.getTransportSliceNetworks().get(0).getConnectionLinks().get(0).getTransportEndpointA());
        assertEquals("endpoint-CN",
                allocation.getTransportSliceNetworks().get(0).getConnectionLinks().get(0).getTransportEndpointB());
        assertEquals("nsi-1", allocation.getNsiInfo().getNsiId());
        assertEquals("script-TN_BH.groovy", allocation.getScriptName());
        assertEquals(NetworkType.TRANSPORT, request.getEsrInfo().getNetworkType());
        assertEquals("vendor & \u96ea", request.getEsrInfo().getVendor());
        assertSame(SubnetType.TN_BH, variables.get("tnBHSubnetType"));
        assertSame(parameters.getTnBHSliceTaskInfo(), variables.get("tnBHSliceTaskInfo"));
    }

    @Test
    public void backhaulProfileRetainsItsDomainAndNumericZeroValues() {
        NssmfAdapterNBIRequest request = backhaul();
        JsonNode profile = mapper.valueToTree(request.getAllocateTnNssi().getSliceProfile());
        assertEquals("TN_BH", profile.path("domainType").textValue());
        assertTrue(profile.path("maxBandwidth").isInt());
        assertEquals(0, profile.path("maxBandwidth").intValue());
        assertTrue(profile.path("jitter").isInt());
        assertEquals(0, profile.path("jitter").intValue());
        assertFalse(profile.has("latency"));
        assertEquals("shared", profile.path("resourceSharingLevel").textValue());
        assertEquals("nssi_tnslice \"\u96ea\"", request.getServiceInfo().getNssiName());
    }

    @Test
    public void fronthaulRequestRetainsItsOwnServiceModelProfileAndTypedState() {
        transportRequest(SubnetType.TN_FH);
    }

    @Test
    public void midhaulRequestRetainsItsOwnServiceModelProfileAndTypedState() {
        transportRequest(SubnetType.TN_MH);
    }

    @Test
    public void fronthaulAndMidhaulKeepNullEmptyIdentifiersAndSstFallbackDistinct() {
        for (SubnetType domain : Arrays.asList(SubnetType.TN_FH, SubnetType.TN_MH)) {
            for (String value : Arrays.asList(null, "")) {
                SliceTaskInfo<SliceProfileAdapter> task = transportTask(domain);
                task.setSuggestNssiId(value);
                task.getSliceProfile().setSST(value);
                NssmfAdapterNBIRequest request = transport(domain);
                assertEquals(value, request.getAllocateTnNssi().getNssiId());
                assertEquals(value, request.getServiceInfo().getNssiId());
                assertEquals("fallback-sst", request.getServiceInfo().getSST());
                JsonNode json = mapper.valueToTree(request);
                assertEquals(value != null, json.path("serviceInfo").has("nssiId"));
                assertTrue(json.path("allocateTnNssi").has("nssiId"));
                if (value == null) {
                    assertTrue(json.path("allocateTnNssi").path("nssiId").isNull());
                } else {
                    assertEquals("", json.path("allocateTnNssi").path("nssiId").textValue());
                }
            }
        }
    }

    @Test
    public void fronthaulAndMidhaulRequestsDoNotShareMutableProfilesOrOverwriteOtherDomains() {
        NssmfAdapterNBIRequest fh = transport(SubnetType.TN_FH);
        NssmfAdapterNBIRequest mh = transport(SubnetType.TN_MH);
        assertNotSame(fh, mh);
        assertNotSame(fh.getServiceInfo(), mh.getServiceInfo());
        assertNotSame(fh.getAllocateTnNssi().getSliceProfile(), mh.getAllocateTnNssi().getSliceProfile());
        fh.getAllocateTnNssi().getSliceProfile().setJitter(81);
        assertEquals(0, mh.getAllocateTnNssi().getSliceProfile().getJitter());
        assertEquals(0, transportTask(SubnetType.TN_FH).getSliceProfile().getJitter());
        assertEquals("source-TN_FH", transportTask(SubnetType.TN_FH).getSliceProfile().getSliceProfileId());
        assertSame(fh, variables.get("TnFHAllocateNssiNbiRequest"));
        assertSame(mh, variables.get("TnMHAllocateNssiNbiRequest"));
        assertFalse(variables.containsKey("TnBHAllocateNssiNbiRequest"));
        assertFalse(variables.containsKey("AnAllocateNssiNbiRequest"));
        assertFalse(variables.containsKey("CnAllocateNssiNbiRequest"));
    }

    private void transportRequest(SubnetType domain) {
        SliceTaskInfo<SliceProfileAdapter> task = transportTask(domain);
        task.setSuggestNssiId("selected-" + domain);
        NssmfAdapterNBIRequest request = transport(domain);
        ServiceInfo service = request.getServiceInfo();
        assertEquals("customer <one>", service.getGlobalSubscriberId());
        assertEquals("5G & private", service.getSubscriptionServiceType());
        assertEquals("nsi-1", service.getNsiId());
        assertEquals("selected-" + domain, service.getNssiId());
        assertEquals("model-" + domain, service.getServiceUuid());
        assertEquals("invariant-" + domain, service.getServiceInvariantUuid());
        assertEquals("embb", service.getSST());
        String suffix = domain == SubnetType.TN_FH ? "fh" : "mh";
        assertEquals("nssi_tn_" + suffix + "_" + variables.get("sliceServiceInstanceName"), service.getNssiName());
        assertEquals("vendor & \u96ea", request.getEsrInfo().getVendor());
        assertSame(NetworkType.TRANSPORT, request.getEsrInfo().getNetworkType());
        AllocateTnNssi allocation = request.getAllocateTnNssi();
        assertEquals("selected-" + domain, allocation.getNssiId());
        assertEquals("nsi-1", allocation.getNsiInfo().getNsiId());
        assertEquals("nsi & \u96ea", allocation.getNsiInfo().getNsiName());
        JsonNode profile = mapper.valueToTree(allocation.getSliceProfile());
        assertFalse(profile.has("domainType"));
        assertEquals("source-" + domain, profile.path("sliceProfileId").textValue());
        assertTrue(profile.path("maxBandwidth").isInt());
        assertEquals(0, profile.path("maxBandwidth").intValue());
        assertTrue(profile.path("jitter").isInt());
        assertEquals(0, profile.path("jitter").intValue());
        assertEquals("shared", profile.path("resourceSharingLevel").textValue());
        assertFalse(profile.has("latency"));
        String prefix = domain == SubnetType.TN_FH ? "tnFH" : "tnMH";
        assertSame(task, variables.get(prefix + "SliceTaskInfo"));
        assertSame(domain, variables.get(prefix + "SubnetType"));
        JsonNode json = mapper.valueToTree(request);
        assertFalse(json.has("allocateAnNssi"));
        assertFalse(json.has("allocateCnNssi"));
    }

    private SliceTaskInfo<SliceProfileAdapter> transportTask(SubnetType domain) {
        return domain == SubnetType.TN_FH ? parameters.getTnFHSliceTaskInfo() : parameters.getTnMHSliceTaskInfo();
    }

    private NssmfAdapterNBIRequest transport(SubnetType domain) {
        if (domain == SubnetType.TN_FH) {
            script.prepareAllocateTnFHNssi(execution);
            return (NssmfAdapterNBIRequest) variables.get("TnFHAllocateNssiNbiRequest");
        }
        script.prepareAllocateTnMHNssi(execution);
        return (NssmfAdapterNBIRequest) variables.get("TnMHAllocateNssiNbiRequest");
    }

    private NssmfAdapterNBIRequest access() {
        script.prepareAllocateAnNssi(execution);
        assertTrue(variables.get("AnAllocateNssiNbiRequest") instanceof NssmfAdapterNBIRequest);
        return (NssmfAdapterNBIRequest) variables.get("AnAllocateNssiNbiRequest");
    }

    private NssmfAdapterNBIRequest core() {
        script.prepareAllocateCnNssi(execution);
        assertTrue(variables.get("CnAllocateNssiNbiRequest") instanceof NssmfAdapterNBIRequest);
        return (NssmfAdapterNBIRequest) variables.get("CnAllocateNssiNbiRequest");
    }

    private NssmfAdapterNBIRequest backhaul() {
        script.prepareAllocateTnBHNssi(execution);
        assertTrue(variables.get("TnBHAllocateNssiNbiRequest") instanceof NssmfAdapterNBIRequest);
        return (NssmfAdapterNBIRequest) variables.get("TnBHAllocateNssiNbiRequest");
    }

    private void service(ServiceInfo service, SliceTaskInfo<SliceProfileAdapter> task, String name) {
        assertEquals("customer <one>", service.getGlobalSubscriberId());
        assertEquals("5G & private", service.getSubscriptionServiceType());
        assertEquals("nsi-1", service.getNsiId());
        assertEquals(task.getNSSTInfo().getUUID(), service.getServiceUuid());
        assertEquals(task.getNSSTInfo().getInvariantUUID(), service.getServiceInvariantUuid());
        assertEquals("embb", service.getSST());
        assertEquals("shared", service.getResourceSharingLevel());
        assertEquals(name, service.getNssiName());
    }

    private void configure(SliceTaskInfo<SliceProfileAdapter> task, SubnetType domain) {
        task.setSubnetType(domain);
        task.setNetworkType(domain.getNetworkType());
        task.setVendor("vendor & \u96ea");
        task.setScriptName("script-" + domain + ".groovy");
        task.setSliceInstanceId("instance-" + domain);
        task.setEndPointId("endpoint-" + domain);
        task.getNSSTInfo().setUUID("model-" + domain);
        task.getNSSTInfo().setInvariantUUID("invariant-" + domain);
        task.getNSSTInfo().setName("model name " + domain);
        SliceProfileAdapter profile = new SliceProfileAdapter();
        profile.setSliceProfileId("source-" + domain);
        profile.setSNSSAIList("001-000001|001-000002");
        profile.setPLMNIdList("00101|00202");
        profile.setCoverageAreaTAList("100|200");
        profile.setResourceSharingLevel("shared");
        profile.setUeMobilityLevel("stationary");
        profile.setIpAddress("2001:db8::10");
        profile.setLogicInterfaceId("interface-" + domain);
        profile.setNextHopInfo("2001:db8::1");
        task.setSliceProfile(profile);
    }
}
