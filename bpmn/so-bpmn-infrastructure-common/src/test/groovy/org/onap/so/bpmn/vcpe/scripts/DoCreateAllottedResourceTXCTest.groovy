/*
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

package org.onap.so.bpmn.vcpe.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import jakarta.ws.rs.core.UriBuilder
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.*
import org.mockito.MockitoAnnotations
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static org.junit.Assert.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*
import static org.onap.so.bpmn.mock.StubResponseAAI.*

class DoCreateAllottedResourceTXCTest extends GroovyTestBase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT)

    String Prefix = "DCARTXC_"

    @BeforeClass
    public static void setUpBeforeClass() {
        aaiUriPfx = UrnPropertiesReader.getVariable("aai.endpoint")
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    public DoCreateAllottedResourceTXCTest() {
        super("DoCreateAllottedResourceTXC")
    }

    // ***** preProcessRequest *****
    @Test
    public void preProcessRequest() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessRequest(mex)

        verify(mex).setVariable("prefix", Prefix)

        assertTrue(checkMissingPreProcessRequest("mso.workflow.sdncadapter.callback"))
        assertTrue(checkMissingPreProcessRequest("mso.workflow.sdnc.replication.delay"))
        assertTrue(checkMissingPreProcessRequest("serviceInstanceId"))
        assertTrue(checkMissingPreProcessRequest("parentServiceInstanceId"))
        assertTrue(checkMissingPreProcessRequest("allottedResourceModelInfo"))
        assertTrue(checkMissingPreProcessRequest("brgWanMacAddress"))
        assertTrue(checkMissingPreProcessRequest("allottedResourceRole"))
        assertTrue(checkMissingPreProcessRequest("allottedResourceType"))
    }

    // ***** getAaiAR *****

    @Test
    @Ignore
    public void getAaiAR() {
        MockGetAllottedResource(wireMockRule, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")

        ExecutionEntity mex = setupMock()
        initGetAaiAR(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.getAaiAR(mex)

        verify(mex).setVariable("foundActiveAR", true)
    }

    @Test

    public void getAaiAR_Duplicate() {
        MockGetAllottedResource(wireMockRule, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")

        ExecutionEntity mex = setupMock()
        initGetAaiAR(mex)

        // fail if duplicate
        when(mex.getVariable("failExists")).thenReturn("true")

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.getAaiAR(mex) }))
    }

    @Test
    public void getAaiAR_NotActive() {
        MockGetAllottedResource(wireMockRule, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")

        ExecutionEntity mex = setupMock()
        initGetAaiAR(mex)

        // not active
        when(mex.getVariable("aaiAROrchStatus")).thenReturn("not-active")

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.getAaiAR(mex) }))
    }

    @Test
    @Ignore
    public void getAaiAR_NoStatus() {
        MockGetAllottedResource(wireMockRule, CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")

        ExecutionEntity mex = setupMock()
        initGetAaiAR(mex)

        when(mex.getVariable("aaiAROrchStatus")).thenReturn(null)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.getAaiAR(mex)

        verify(mex, never()).setVariable("foundActiveAR", true)
    }

    // ***** createAaiAR *****

    @Test
    public void createAaiAR() {
        ExecutionEntity mex = setupMock()
        initCreateAaiAr(mex)
        when(mex.getVariable("PSI_resourceLink")).thenReturn(AAIUriFactory.createResourceFromExistingURI(Types.SERVICE_INSTANCE, UriBuilder.fromPath("/aai/v9/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST).build()))
        when(mex.getVariable("CSI_resourceLink")).thenReturn("/aai/v9/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST)
        when(mex.getVariable("allottedResourceModelInfo")).thenReturn("{\n" +
                "  \"modelInvariantUuid\":\"modelInvariantUuid\",\n" +
                "  \"modelUuid\" : \"modelUuid\"\n" +
                "}")
        AAIResourcesClient client = mock(AAIResourcesClient.class)
        DoCreateAllottedResourceTXC doCreateAllottedResourceTXC = spy(DoCreateAllottedResourceTXC.class)
        when(doCreateAllottedResourceTXC.getAAIClient()).thenReturn(client)
        doCreateAllottedResourceTXC.createAaiAR(mex)
    }


    @Test
    public void createAaiAR_MissingPsiLink() {
        ExecutionEntity mex = setupMock()
        initCreateAaiAr(mex)

        when(mex.getVariable("PSI_resourceLink")).thenReturn(null)

        MockPutAllottedResource(wireMockRule, CUST, SVC, INST, ARID)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
    }

    @Test
    public void createAaiAR_HttpFailed() {
        ExecutionEntity mex = setupMock()
        initCreateAaiAr(mex)

        MockPutAllottedResource_500(wireMockRule, CUST, SVC, INST, ARID)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
    }

    @Test
    public void createAaiAR_BpmnError() {
        ExecutionEntity mex = setupMock()
        initCreateAaiAr(mex)

        when(mex.getVariable("aai.endpoint")).thenThrow(new BpmnError("expected exception"))

        MockPutAllottedResource(wireMockRule, CUST, SVC, INST, ARID)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
    }

    @Test
    public void createAaiAR_Ex() {
        ExecutionEntity mex = setupMock()
        initCreateAaiAr(mex)

        when(mex.getVariable("aai.endpoint")).thenThrow(new RuntimeException("expected exception"))

        MockPutAllottedResource(wireMockRule, CUST, SVC, INST, ARID)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
    }

    // ***** buildSDNCRequest *****

    @Test
    public void buildSDNCRequest() {
        ExecutionEntity mex = setupMock()
        initBuildSDNCRequest(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        String result = DoCreateAllottedResourceTXC.buildSDNCRequest(mex, "myact", "myreq")

        assertTrue(result.indexOf("<sdncadapter:RequestId>myreq</") >= 0)
        assertTrue(result.indexOf("<sdncadapter:SvcAction>myact</") >= 0)
        assertTrue(result.indexOf("<allotted-resource-id>ari</") >= 0)
        assertTrue(result.indexOf("<sdncadapter:SvcInstanceId>sii</") >= 0)
        assertTrue(result.indexOf("<service-instance-id>sii</") >= 0)
        assertTrue(result.indexOf("<parent-service-instance-id>psii</") >= 0)
        assertTrue(result.indexOf("<subscription-service-type>sst</") >= 0)
        assertTrue(result.indexOf("<global-customer-id>gci</") >= 0)
        assertTrue(result.indexOf("<sdncadapter:CallbackUrl>scu</") >= 0)
        assertTrue(result.indexOf("<request-id>mri</") >= 0)
        assertTrue(result.indexOf("<model-invariant-uuid/>") >= 0)
        assertTrue(result.indexOf("<model-uuid/>") >= 0)
        assertTrue(result.indexOf("<model-customization-uuid/>") >= 0)
        assertTrue(result.indexOf("<model-version/>") >= 0)
        assertTrue(result.indexOf("<model-name/>") >= 0)
    }

    @Test
    public void buildSDNCRequest_Ex() {
        ExecutionEntity mex = setupMock()
        initBuildSDNCRequest(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.buildSDNCRequest(mex, "myact", "myreq") }))
    }

    // ***** preProcessSDNCAssign *****

    @Test
    public void preProcessSDNCAssign() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        def data = initPreProcessSDNC(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessSDNCAssign(mex)

        def req = map.get("sdncAssignRequest")
        assertNotNull(req)

        assertEquals(data, map.get("rollbackData"))

        def rbreq = data.get(Prefix, "sdncAssignRollbackReq")

        assertTrue(req.indexOf("<sdncadapter:SvcAction>assign</") >= 0)
        assertTrue(req.indexOf("<request-action>CreateTunnelXConnInstance</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)

        assertTrue(rbreq.indexOf("<sdncadapter:SvcAction>unassign</") >= 0)
        assertTrue(rbreq.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
        assertTrue(rbreq.indexOf("<sdncadapter:RequestId>") >= 0)
    }

    @Test
    public void preProcessSDNCAssign_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCAssign(mex) }))
    }

    @Test
    public void preProcessSDNCAssign_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCAssign(mex) }))
    }

    // ***** preProcessSDNCCreate *****

    @Test
    public void preProcessSDNCCreate() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        def data = initPreProcessSDNC(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessSDNCCreate(mex)

        def req = map.get("sdncCreateRequest")
        assertNotNull(req)

        assertEquals(data, map.get("rollbackData"))

        def rbreq = data.get(Prefix, "sdncCreateRollbackReq")

        assertTrue(req.indexOf("<sdncadapter:SvcAction>create</") >= 0)
        assertTrue(req.indexOf("<request-action>CreateTunnelXConnInstance</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)

        assertTrue(rbreq.indexOf("<sdncadapter:SvcAction>delete</") >= 0)
        assertTrue(rbreq.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
        assertTrue(rbreq.indexOf("<sdncadapter:RequestId>") >= 0)

    }

    @Test
    public void preProcessSDNCCreate_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCCreate(mex) }))
    }

    @Test
    public void preProcessSDNCCreate_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCCreate(mex) }))
    }

    // ***** preProcessSDNCActivate *****

    @Test
    public void preProcessSDNCActivate() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        def data = initPreProcessSDNC(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessSDNCActivate(mex)

        def req = map.get("sdncActivateRequest")
        assertNotNull(req)

        assertEquals(data, map.get("rollbackData"))

        def rbreq = data.get(Prefix, "sdncActivateRollbackReq")

        assertTrue(req.indexOf("<sdncadapter:SvcAction>activate</") >= 0)
        assertTrue(req.indexOf("<request-action>CreateTunnelXConnInstance</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)

        assertTrue(rbreq.indexOf("<sdncadapter:SvcAction>deactivate</") >= 0)
        assertTrue(rbreq.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
        assertTrue(rbreq.indexOf("<sdncadapter:RequestId>") >= 0)

    }

    @Test
    public void preProcessSDNCActivate_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCActivate(mex) }))
    }

    @Test
    public void preProcessSDNCActivate_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCActivate(mex) }))
    }

    // ***** validateSDNCResp *****

    @Test
    public void validateSDNCResp() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        def data = initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp()

        when(mex.getVariable(Prefix + "sdncResponseSuccess")).thenReturn(true)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create")

        verify(mex).getVariable("WorkflowException")
        verify(mex).getVariable("SDNCA_SuccessIndicator")
        verify(mex).getVariable("rollbackData")

        assertEquals(data, map.get("rollbackData"))

        assertEquals("true", data.get(Prefix, "rollback" + "SDNCcreate"))

    }

    @Test
    public void validateSDNCResp_Get() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp()

        when(mex.getVariable(Prefix + "sdncResponseSuccess")).thenReturn(true)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "get")

        verify(mex).getVariable("WorkflowException")
        verify(mex).getVariable("SDNCA_SuccessIndicator")

        verify(mex, never()).getVariable("rollbackData")
    }

    @Test
    public void validateSDNCResp_Unsuccessful() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp()

        // unsuccessful
        when(mex.getVariable(Prefix + "sdncResponseSuccess")).thenReturn(false)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
    }

    @Test
    public void validateSDNCResp_BpmnError() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp()

        when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
    }

    @Test
    public void validateSDNCResp_Ex() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp()

        when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
    }

    // ***** preProcessSDNCGet *****

    @Test
    public void preProcessSDNCGet_FoundAR() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcessSDNCGet(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessSDNCGet(mex)

        String req = map.get("sdncGetRequest")

        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
        assertTrue(req.indexOf("<sdncadapter:SvcInstanceId>sii</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:SvcOperation>arlink</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:CallbackUrl>myurl</") >= 0)

    }

    @Test
    public void preProcessSDNCGet_NotFoundAR() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcessSDNCGet(mex)

        when(mex.getVariable("foundActiveAR")).thenReturn(false)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessSDNCGet(mex)

        String req = map.get("sdncGetRequest")

        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
        assertTrue(req.indexOf("<sdncadapter:SvcInstanceId>sii</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:SvcOperation>assignlink</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:CallbackUrl>myurl</") >= 0)

    }

    @Test
    public void preProcessSDNCGet_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNCGet(mex)

        when(mex.getVariable("foundActiveAR")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCGet(mex) }))
    }

    // ***** updateAaiAROrchStatus *****

    @Test
    @Ignore
    public void updateAaiAROrchStatus() {
        MockPatchAllottedResource(wireMockRule, CUST, SVC, INST, ARID)

        ExecutionEntity mex = setupMock()
        initUpdateAaiAROrchStatus(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.updateAaiAROrchStatus(mex, "success")
    }

    // ***** generateOutputs *****

    @Test
    public void generateOutputs() {
        ExecutionEntity mex = setupMock()
        def txctop = FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXC/SDNCTopologyQueryCallback.xml")

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("enhancedCallbackRequestData")).thenReturn(txctop)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.generateOutputs(mex)

        verify(mex).setVariable("allotedResourceName", "namefromrequest")
        verify(mex).setVariable("vni", "my-vni")
        verify(mex).setVariable("vgmuxBearerIP", "my-bearer-ip")
        verify(mex).setVariable("vgmuxLanIP", "my-lan-ip")

    }

    @Test
    public void generateOutputs_BadXml() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("enhancedCallbackRequestData")).thenReturn("invalid xml")

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.generateOutputs(mex)

        verify(mex, never()).setVariable(anyString(), anyString())

    }

    @Test
    public void generateOutputs_BpmnError() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("enhancedCallbackRequestData")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.generateOutputs(mex)
        verify(mex, never()).setVariable(anyString(), anyString())

    }

    @Test
    public void generateOutputs_Ex() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("enhancedCallbackRequestData")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.generateOutputs(mex)
        verify(mex, never()).setVariable(anyString(), anyString())

    }

    // ***** preProcessRollback *****

    @Test
    public void preProcessRollback() {
        ExecutionEntity mex = setupMock()
        WorkflowException wfe = mock(WorkflowException.class)

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("WorkflowException")).thenReturn(wfe)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessRollback(mex)

        verify(mex).setVariable("prevWorkflowException", wfe)

    }

    @Test
    public void preProcessRollback_NotWFE() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("WorkflowException")).thenReturn("I'm not a WFE")

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.preProcessRollback(mex)

        verify(mex, never()).setVariable(eq("prevWorkflowException"), any())

    }

    @Test
    public void preProcessRollback_BpmnError() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.preProcessRollback(mex)

    }

    @Test
    public void preProcessRollback_Ex() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.preProcessRollback(mex)

    }

    // ***** postProcessRollback *****

    @Test
    public void postProcessRollback() {
        ExecutionEntity mex = setupMock()
        WorkflowException wfe = mock(WorkflowException.class)

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("prevWorkflowException")).thenReturn(wfe)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.postProcessRollback(mex)

        verify(mex).setVariable("WorkflowException", wfe)
        verify(mex).setVariable("rollbackData", null)

    }

    @Test
    public void postProcessRollback_NotWFE() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("prevWorkflowException")).thenReturn("I'm not a WFE")

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
        DoCreateAllottedResourceTXC.postProcessRollback(mex)

        verify(mex, never()).setVariable(eq("WorkflowException"), any())
        verify(mex).setVariable("rollbackData", null)

    }

    @Test
    public void postProcessRollback_BpmnError() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("prevWorkflowException")).thenThrow(new BpmnError("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.postProcessRollback(mex) }))
        verify(mex, never()).setVariable("rollbackData", null)

    }

    @Test
    public void postProcessRollback_Ex() {
        ExecutionEntity mex = setupMock()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("prevWorkflowException")).thenThrow(new RuntimeException("expected exception"))

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        DoCreateAllottedResourceTXC.postProcessRollback(mex)
        verify(mex, never()).setVariable("rollbackData", null)

    }

    private boolean checkMissingPreProcessRequest(String fieldnm) {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()

        when(mex.getVariable(fieldnm)).thenReturn("")

        return doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessRequest(mex) })
    }

    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("sdncurn")
        when(mex.getVariable("mso.workflow.sdnc.replication.delay")).thenReturn("sdncdelay")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("parentServiceInstanceId")).thenReturn("psii")
        when(mex.getVariable("allottedResourceModelInfo")).thenReturn("armi")
        when(mex.getVariable("brgWanMacAddress")).thenReturn("bwma")
        when(mex.getVariable("allottedResourceRole")).thenReturn("arr")
        when(mex.getVariable("allottedResourceType")).thenReturn("art")
    }

    private void initGetAaiAR(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("allottedResourceType")).thenReturn("TXCt")
        when(mex.getVariable("allottedResourceRole")).thenReturn("TXCr")
        when(mex.getVariable("CSI_service")).thenReturn(FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXC/getAR.xml"))
        when(mex.getVariable("aai.endpoint")).thenReturn(aaiUriPfx)
        when(mex.getVariable("aaiAROrchStatus")).thenReturn("Active")
    }

    private initCreateAaiAr(ExecutionEntity mex) {
        when(mex.getVariable("disableRollback")).thenReturn(45)
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("parentServiceInstanceId")).thenReturn("psii")
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("allottedResourceId")).thenReturn(ARID)
        when(mex.getVariable("aai.endpoint")).thenReturn(aaiUriPfx)
        when(mex.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn(UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.namespace"))
        when(mex.getVariable("PSI_resourceLink")).thenReturn(AAIUriFactory.createResourceFromExistingURI(Types.SERVICE_INSTANCE, UriBuilder.fromPath("/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST).build()))
        when(mex.getVariable("allottedResourceType")).thenReturn("TXCt")
        when(mex.getVariable("allottedResourceRole")).thenReturn("TXCr")
        when(mex.getVariable("CSI_resourceLink")).thenReturn(aaiUriPfx + "/aai/v9/mycsi")
        when(mex.getVariable("allottedResourceModelInfo")).thenReturn("""
                {
                    "modelInvariantUuid":"modelinvuuid",
                    "modelUuid":"modeluuid",
                    "modelCustomizationUuid":"modelcustuuid"
                }
            """)
    }

    private initBuildSDNCRequest(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("allottedResourceId")).thenReturn("ari")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("parentServiceInstanceId")).thenReturn("psii")
        when(mex.getVariable("subscriptionServiceType")).thenReturn("sst")
        when(mex.getVariable("globalCustomerId")).thenReturn("gci")
        when(mex.getVariable("sdncCallbackUrl")).thenReturn("scu")
        when(mex.getVariable("msoRequestId")).thenReturn("mri")
    }

    private RollbackData initPreProcessSDNC(ExecutionEntity mex) {
        def data = new RollbackData()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("rollbackData")).thenReturn(data)

        return data
    }

    private initPreProcessSDNCGet(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("sdncCallbackUrl")).thenReturn("myurl")
        when(mex.getVariable("foundActiveAR")).thenReturn(true)
        when(mex.getVariable("aaiARGetResponse")).thenReturn("<selflink>arlink</selflink>")
        when(mex.getVariable("sdncAssignResponse")).thenReturn("<response-data>&lt;object-path&gt;assignlink&lt;/object-path&gt;</response-data>")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("sdncCallbackUrl")).thenReturn("myurl")
    }

    private RollbackData initValidateSDNCResp(ExecutionEntity mex) {
        def data = new RollbackData()

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("prefix")).thenReturn(Prefix)
        when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
        when(mex.getVariable("rollbackData")).thenReturn(data)

        return data
    }

    private String initValidateSDNCResp_Resp() {
        return "<response-data>&lt;response-code&gt;200&lt;/response-code&gt;</response-data>"
    }

    private initUpdateAaiAROrchStatus(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("aaiARPath")).thenReturn("/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST + "/allotted-resources/allotted-resource/" + ARID)
    }

}
