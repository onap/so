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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.*
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.so.bpmn.core.UrnPropertiesReader

import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*
import static org.onap.so.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetAllottedResource
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPatchAllottedResource
import com.github.tomakehurst.wiremock.junit.WireMockRule

class DoDeleteAllottedResourceBRGTest extends GroovyTestBase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT)

    String Prefix = "DDARBRG_"

    @BeforeClass
    public static void setUpBeforeClass() {
        GroovyTestBase.setUpBeforeClass()
    }

    @Spy
    DoDeleteAllottedResourceBRG doDeleteAllottedResourceBRG

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
        initAllottedResourceMock()
    }

    public DoDeleteAllottedResourceBRGTest() {
        super("DoDeleteAllottedResourceBRG")
    }

    // ***** preProcessRequest *****

    @Test
    public void preProcessRequest() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()
        DoDeleteAllottedResourceBRG.preProcessRequest(mex)

        verify(mex).setVariable("prefix", Prefix)
        verify(mex).setVariable("sdncCallbackUrl", "sdncurn")

        assertTrue(checkMissingPreProcessRequest("mso.workflow.sdncadapter.callback"))
        assertTrue(checkMissingPreProcessRequest("serviceInstanceId"))
        assertTrue(checkMissingPreProcessRequest("allottedResourceId"))
    }

    @Test
    public void preProcessRequest_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        when(mex.getVariable("serviceInstanceId")).thenThrow(new BpmnError("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessRequest(mex) }))
    }

    @Test
    public void preProcessRequest_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        when(mex.getVariable("serviceInstanceId")).thenThrow(new RuntimeException("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessRequest(mex) }))
    }

    // ***** getAaiAR *****

    @Test
    public void getAaiAR() {
        ExecutionEntity mex = setupMock()
        initGetAaiAR(mex)
        when(doDeleteAllottedResourceBRG.getAllottedResourceUtils()).thenReturn(allottedResourceUtils_MOCK)
        doReturn(true).when(allottedResourceUtils_MOCK).ifExistsAR(eq(mex), eq(ARID))
        doDeleteAllottedResourceBRG.getAaiAR(mex)
        verify(mex).setVariable("parentServiceInstanceId", INST)
    }

    @Test
    public void getAaiAR_EmptyResponse() {
        ExecutionEntity mex = setupMock()
        initGetAaiAR(mex)
        doReturn(false).when(allottedResourceUtils_MOCK).ifExistsAR(eq(mex), eq(ARID))
        assertTrue(doBpmnError({ _ -> doDeleteAllottedResourceBRG.getAaiAR(mex) }))
    }

    // ***** updateAaiAROrchStatus *****

    @Test
    @Ignore
    public void updateAaiAROrchStatus() {
        ExecutionEntity mex = setupMock()
        initUpdateAaiAROrchStatus(mex)

        MockPatchAllottedResource(CUST, SVC, INST, ARID)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()
        DoDeleteAllottedResourceBRG.updateAaiAROrchStatus(mex, "success")
    }

    // ***** buildSDNCRequest *****

    @Test
    public void buildSDNCRequest() {
        ExecutionEntity mex = setupMock()
        initBuildSDNCRequest(mex)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        String result = DoDeleteAllottedResourceBRG.buildSDNCRequest(mex, "myact", "myreq")

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

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.buildSDNCRequest(mex, "myact", "myreq") }))
    }

    // ***** preProcessSDNCUnassign *****

    @Test
    public void preProcessSDNCUnassign() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcessSDNC(mex)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()
        DoDeleteAllottedResourceBRG.preProcessSDNCUnassign(mex)

        def req = map.get("sdncUnassignRequest")

        assertTrue(req.indexOf("<sdncadapter:SvcAction>unassign</") >= 0)
        assertTrue(req.indexOf("<request-action>DeleteBRGInstance</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
    }

    @Test
    public void preProcessSDNCUnassign_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new BpmnError("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessSDNCUnassign(mex) }))
    }

    @Test
    public void preProcessSDNCUnassign_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessSDNCUnassign(mex) }))
    }

    // ***** preProcessSDNCDelete *****

    @Test
    public void preProcessSDNCDelete() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcessSDNC(mex)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()
        DoDeleteAllottedResourceBRG.preProcessSDNCDelete(mex)

        def req = map.get("sdncDeleteRequest")

        assertTrue(req.indexOf("<sdncadapter:SvcAction>delete</") >= 0)
        assertTrue(req.indexOf("<request-action>DeleteBRGInstance</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
    }

    @Test
    public void preProcessSDNCDelete_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new BpmnError("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessSDNCDelete(mex) }))
    }

    @Test
    public void preProcessSDNCDelete_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessSDNCDelete(mex) }))
    }

    // ***** preProcessSDNCDeactivate *****

    @Test
    public void preProcessSDNCDeactivate() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcessSDNC(mex)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()
        DoDeleteAllottedResourceBRG.preProcessSDNCDeactivate(mex)

        def req = map.get("sdncDeactivateRequest")

        assertTrue(req.indexOf("<sdncadapter:SvcAction>deactivate</") >= 0)
        assertTrue(req.indexOf("<request-action>DeleteBRGInstance</") >= 0)
        assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
    }

    @Test
    public void preProcessSDNCDeactivate_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new BpmnError("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessSDNCDeactivate(mex) }))
    }

    @Test
    public void preProcessSDNCDeactivate_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcessSDNC(mex)

        when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessSDNCDeactivate(mex) }))
    }

    // ***** validateSDNCResp *****

    @Test
    public void validateSDNCResp() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp(200)

        when(mex.getVariable(Prefix + "sdncResponseSuccess")).thenReturn(true)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "create")

        verify(mex).getVariable("WorkflowException")
        verify(mex).getVariable("SDNCA_SuccessIndicator")
        verify(mex).getVariable(Prefix + "sdncResponseSuccess")

        verify(mex, never()).getVariable(Prefix + "sdncRequestDataResponseCode")
        verify(mex, never()).setVariable("wasDeleted", false)
    }

    @Test
    public void validateSDNCResp_Fail404_Deactivate_FailNotFound() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)

        def resp = initValidateSDNCResp_Resp(404)
        when(mex.getVariable(Prefix + "sdncRequestDataResponseCode")).thenReturn("404")
        when(mex.getVariable("failNotFound")).thenReturn("true")

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "deactivate") }))
    }

    @Test
    public void validateSDNCResp_Fail404_Deactivate() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)

        def resp = initValidateSDNCResp_Resp(404)
        when(mex.getVariable(Prefix + "sdncRequestDataResponseCode")).thenReturn("404")

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "deactivate")

        verify(mex).setVariable("ARNotFoundInSDNC", true)
        verify(mex).setVariable("wasDeleted", false)
    }

    @Test
    public void validateSDNCResp_Fail404() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)

        def resp = initValidateSDNCResp_Resp(404)
        when(mex.getVariable(Prefix + "sdncRequestDataResponseCode")).thenReturn("404")

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "create") }))
    }

    @Test
    public void validateSDNCResp_Deactivate() {
        ExecutionEntity mex = setupMock()
        def data = initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp(200)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "deactivate") }))
    }

    @Test
    public void validateSDNCResp_BpmnError() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp(200)

        when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "create") }))
    }

    @Test
    public void validateSDNCResp_Ex() {
        ExecutionEntity mex = setupMock()
        initValidateSDNCResp(mex)
        def resp = initValidateSDNCResp_Resp(200)

        when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.validateSDNCResp(mex, resp, "create") }))
    }

    @Test
    @Ignore
    public void deleteAaiAR() {
        ExecutionEntity mex = setupMock()
        initDeleteAaiAR(mex)

        MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceBRG/arGetById.xml")
        MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()
        DoDeleteAllottedResourceBRG.deleteAaiAR(mex)
    }

    @Test
    public void deleteAaiAR_NoArPath() {
        ExecutionEntity mex = setupMock()
        initDeleteAaiAR(mex)

        MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceBRG/arGetById.xml")
        MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)

        when(mex.getVariable("aaiARPath")).thenReturn("")

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.deleteAaiAR(mex) }))
    }

    @Test
    public void deleteAaiAR_BpmnError() {
        ExecutionEntity mex = setupMock()
        initDeleteAaiAR(mex)

        MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceBRG/arGetById.xml")
        MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)

        when(mex.getVariable("aaiARPath")).thenThrow(new BpmnError("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.deleteAaiAR(mex) }))
    }

    @Test
    public void deleteAaiAR_Ex() {
        ExecutionEntity mex = setupMock()
        initDeleteAaiAR(mex)

        MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceBRG/arGetById.xml")
        MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)

        when(mex.getVariable("aaiARPath")).thenThrow(new RuntimeException("expected exception"))

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceBRG.deleteAaiAR(mex) }))
    }

    private boolean checkMissingPreProcessRequest(String fieldnm) {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        DoDeleteAllottedResourceBRG DoDeleteAllottedResourceBRG = new DoDeleteAllottedResourceBRG()

        when(mex.getVariable(fieldnm)).thenReturn("")

        return doBpmnError({ _ -> DoDeleteAllottedResourceBRG.preProcessRequest(mex) })
    }

    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("sdncurn")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("allottedResourceId")).thenReturn("ari")
    }

    private void initGetAaiAR(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("allottedResourceType")).thenReturn("BRG")
        when(mex.getVariable("allottedResourceRole")).thenReturn("BRG")
        when(mex.getVariable("allottedResourceId")).thenReturn(ARID)
        when(mex.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn(UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.namespace"))
        when(mex.getVariable("mso.workflow.global.default.aai.version")).thenReturn(UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.version"))
        when(mex.getVariable("mso.workflow.default.aai.v8.nodes.query.uri")).thenReturn(UrnPropertiesReader.getVariable("mso.workflow.default.aai.v8.nodes-query.uri"))
        when(mex.getVariable("aai.endpoint")).thenReturn(aaiUriPfx)
        when(mex.getVariable("aaiARPath")).thenReturn("/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST + "/allotted-resources/allotted-resource/" + ARID)
        when(mex.getVariable("aaiAROrchStatus")).thenReturn("Active")
    }

    private initUpdateAaiAROrchStatus(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("aaiARPath")).thenReturn("/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST + "/allotted-resources/allotted-resource/" + ARID)
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

    private initPreProcessSDNC(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
    }

    private initValidateSDNCResp(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("prefix")).thenReturn(Prefix)
        when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
    }

    private String initValidateSDNCResp_Resp(int code) {
        return "<response-data>&lt;response-code&gt;${code}&lt;/response-code&gt;</response-data>"
    }

    private initDeleteAaiAR(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("aaiARPath")).thenReturn("/business/customers/customer/" + CUST + "/service-subscriptions/service-subscription/" + SVC + "/service-instances/service-instance/" + INST + "/allotted-resources/allotted-resource/" + ARID)
        when(mex.getVariable("aaiARResourceVersion")).thenReturn("myvers")
        when(mex.getVariable("aai.endpoint")).thenReturn(aaiUriPfx)
    }

}
