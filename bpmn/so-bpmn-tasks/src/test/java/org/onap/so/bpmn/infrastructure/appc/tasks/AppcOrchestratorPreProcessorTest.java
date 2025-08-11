/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.appc.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.Vserver;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerVnf;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.db.catalog.client.CatalogDbClient;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AppcOrchestratorPreProcessorTest extends TestDataSetup {

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";
    @Mock
    protected ExtractPojosForBB extractPojosForBB;
    @Mock
    protected AAIVnfResources aaiVnfResources;
    @Mock
    protected CatalogDbClient catalogDbClient;
    @InjectMocks
    private AppcOrchestratorPreProcessor appcOrchestratorPreProcessor = new AppcOrchestratorPreProcessor();

    private ObjectMapper mapper = new JettisonStyleMapperProvider().getMapper();

    @Test
    public void buildAppcTaskRequestTest() throws Exception {
        final String expectedRequestJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "appcTaskRequest.json")));
        ApplicationControllerTaskRequest expectedTaskRequest =
                mapper.readValue(expectedRequestJson, ApplicationControllerTaskRequest.class);
        execution.getLookupMap().put(ResourceKey.GENERIC_VNF_ID, "-TEST");
        fillRequiredAppcExecutionFields();
        GenericVnf genericVnf = getTestGenericVnf();
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(genericVnf);
        mockReferenceResponse();
        execution.getLookupMap().put(ResourceKey.VF_MODULE_ID, "VF-MODULE-ID-TEST");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("VF-MODULE-ID");
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
        appcOrchestratorPreProcessor.buildAppcTaskRequest(execution, "Lock");
        ApplicationControllerTaskRequest actualTaskRequest = execution.getVariable("appcOrchestratorRequest");
        assertThat(actualTaskRequest, sameBeanAs(expectedTaskRequest));
    }

    @Test
    public void getVserversForAppcTest() throws Exception {

        GenericVnf genericVnf = getTestGenericVnf();

        final String aaiVnfJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiGenericVnfWithVservers.json")));
        final String aaiVserverJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiVserverQueryResponse.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiVnfJson);
        ObjectMapper mapper = new ObjectMapper();
        Vserver vserver = mapper.readValue(aaiVserverJson, Vserver.class);
        doReturn(aaiResultWrapper).when(aaiVnfResources).queryVnfWrapperById(genericVnf);
        doReturn(Optional.of(vserver)).when(aaiVnfResources).getVserver(ArgumentMatchers.any(AAIResourceUri.class));
        appcOrchestratorPreProcessor.getVserversForAppc(execution, genericVnf);
        ArrayList<String> vserverIdList = execution.getVariable("vserverIdList");
        ArrayList<String> expectedVserverIdList = new ArrayList<String>();
        expectedVserverIdList.add("1b3f44e5-d96d-4aac-bd9a-310e8cfb0af5");
        expectedVserverIdList.add("14551849-1e70-45cd-bc5d-a256d49548a2");
        expectedVserverIdList.add("48bd7f11-408f-417c-b834-b41c1b98f7d7");
        ArrayList<String> vmIdList = execution.getVariable("vmIdList");
        ArrayList<String> expectedVmIdList = new ArrayList<String>();
        expectedVmIdList.add("http://VSERVER-link.com");
        expectedVmIdList.add("http://VSERVER-link.com");
        expectedVmIdList.add("http://VSERVER-link.com");
        assertEquals(vserverIdList, expectedVserverIdList);
        assertEquals(vmIdList, expectedVmIdList);
    }

    @Test
    public void addVmInfoToAppcTaskRequestTest() {
        ApplicationControllerTaskRequest appcTaskRequest = new ApplicationControllerTaskRequest();
        ApplicationControllerVnf applicationControllerVnf = new ApplicationControllerVnf();
        appcTaskRequest.setApplicationControllerVnf(applicationControllerVnf);
        execution.setVariable("appcOrchestratorRequest", appcTaskRequest);
        ArrayList<String> vmIdList = new ArrayList<String>();
        vmIdList.add("http://VSERVER-link.com");
        vmIdList.add("http://VSERVER-link.com");
        vmIdList.add("http://VSERVER-link.com");
        execution.setVariable("vmIdList", vmIdList);
        ArrayList<String> vserverIdList = new ArrayList<String>();
        vserverIdList.add("1b3f44e5-d96d-4aac-bd9a-310e8cfb0af5");
        vserverIdList.add("14551849-1e70-45cd-bc5d-a256d49548a2");
        vserverIdList.add("48bd7f11-408f-417c-b834-b41c1b98f7d7");
        execution.setVariable("vserverIdList", vserverIdList);
        execution.setVariable("vmIndex", 1);
        appcOrchestratorPreProcessor.addVmInfoToAppcTaskRequest(execution);
        Integer nextVmIndex = execution.getVariable("vmIndex");
        assertThat(nextVmIndex).isEqualTo(2);
        Integer vmIdListSize = execution.getVariable("vmIdListSize");
        assertThat(vmIdListSize).isEqualTo(3);
        appcTaskRequest = execution.getVariable("appcOrchestratorRequest");
        assertEquals(appcTaskRequest.getApplicationControllerVnf().getApplicationControllerVm().getVserverId(),
                "14551849-1e70-45cd-bc5d-a256d49548a2");
        assertEquals(appcTaskRequest.getApplicationControllerVnf().getApplicationControllerVm().getVmId(),
                "http://VSERVER-link.com");
    }

    private void mockReferenceResponse() {
        ControllerSelectionReference reference = new ControllerSelectionReference();
        reference.setControllerName("TEST-CONTROLLER-NAME");
        when(catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(eq("TEST-VNF-TYPE"),
                eq(Action.Lock.toString()))).thenReturn(reference);
    }

    private void fillRequiredAppcExecutionFields() {
        RequestContext context = new RequestContext();
        context.setMsoRequestId("TEST-MSO-ID");
        context.setRequestorId("testRequestorId");
        execution.setVariable("aicIdentity", "AIC-TEST");
        execution.setVariable("vmIdList", "VM-ID-LIST-TEST");
        execution.setVariable("vserverIdList", "VSERVER-ID-LIST");
        execution.setVariable("identityUrl", "IDENTITY-URL-TEST");
        execution.getGeneralBuildingBlock().setRequestContext(context);
    }

    private GenericVnf getTestGenericVnf() {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("TEST-VNF-ID");
        genericVnf.setVnfType("TEST-VNF-TYPE");
        genericVnf.setVnfName("TEST-VNF-NAME");
        genericVnf.setIpv4OamAddress("127.0.0.1");
        return genericVnf;
    }

    @Test
    public void buildAppcTaskRequestConfigModifyTest() throws Exception {
        final String expectedRequestJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "appcTaskRequestConfigModify.json")));
        ApplicationControllerTaskRequest expectedTaskRequest =
                mapper.readValue(expectedRequestJson, ApplicationControllerTaskRequest.class);
        execution.getLookupMap().put(ResourceKey.GENERIC_VNF_ID, "-TEST");
        fillRequiredAppcExecutionFieldsConfigModify();
        GenericVnf genericVnf = getTestGenericVnf();
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(genericVnf);
        mockReferenceResponseForConfigModify();
        execution.getLookupMap().put(ResourceKey.VF_MODULE_ID, "VF-MODULE-ID-TEST");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("VF-MODULE-ID");
        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
        appcOrchestratorPreProcessor.buildAppcTaskRequest(execution, "ConfigModify");
        ApplicationControllerTaskRequest actualTaskRequest = execution.getVariable("appcOrchestratorRequest");
        assertThat(actualTaskRequest, sameBeanAs(expectedTaskRequest));
    }

    private void fillRequiredAppcExecutionFieldsConfigModify() {
        RequestContext context = new RequestContext();
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setPayload(
                "{\"request_parameters\":{\"host_ip_address\":\"10.10.10.10\"},\"configuration_parameters\":{\"name1\":\"value1\",\"name2\":\"value2\"}}");
        context.setRequestParameters(requestParameters);
        context.setMsoRequestId("TEST-MSO-ID");
        context.setRequestorId("testRequestorId");
        execution.setVariable("aicIdentity", "AIC-TEST");
        execution.setVariable("vmIdList", "VM-ID-LIST-TEST");
        execution.setVariable("vserverIdList", "VSERVER-ID-LIST");
        execution.setVariable("identityUrl", "IDENTITY-URL-TEST");
        execution.getGeneralBuildingBlock().setRequestContext(context);
    }

    private void mockReferenceResponseForConfigModify() {
        ControllerSelectionReference reference = new ControllerSelectionReference();
        reference.setControllerName("APPC");
        when(catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(eq("TEST-VNF-TYPE"),
                eq(Action.ConfigModify.toString()))).thenReturn(reference);
    }

}
