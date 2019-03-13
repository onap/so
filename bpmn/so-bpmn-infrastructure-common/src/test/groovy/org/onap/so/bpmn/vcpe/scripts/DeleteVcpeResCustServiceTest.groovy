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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static org.junit.Assert.*
import static org.mockito.Mockito.*

class DeleteVcpeResCustServiceTest extends GroovyTestBase {

    private static String request

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT)

    String Prefix = "DVRCS_"

    @BeforeClass
    public static void setUpBeforeClass() {
        request = FileUtil.readResourceFile("__files/VCPE/DeleteVcpeResCustService/request.json")
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    public DeleteVcpeResCustServiceTest() {
        super("DeleteVcpeResCustService")
    }

    // ***** preProcessRequest *****

    @Test
    public void preProcessRequest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.preProcessRequest(mex)

        verify(mex).getVariable(DBGFLAG)

        assertEquals(Prefix, map.get("prefix"))
        assertEquals(request, map.get("DeleteVcpeResCustServiceRequest"))
        assertEquals("mri", map.get("msoRequestId"))
        assertEquals("ra", map.get("requestAction"))
        assertEquals("VID", map.get("source"))
        assertEquals(CUST, map.get("globalSubscriberId"))
        assertEquals(CUST, map.get("globalCustomerId"))
        assertEquals("false", map.get("disableRollback"))
        assertEquals("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb", map.get("productFamilyId"))
        assertEquals(SVC, map.get("subscriptionServiceType"))

        assertEquals("mdt1", map.get("lcpCloudRegionId"))
        assertEquals("8b1df54faa3b49078e3416e21370a3ba", map.get("tenantId"))
        assertEquals("1707", map.get("sdncVersion"))
        assertEquals(
                """{"cloudOwner":"CloudOwner","tenantId":"8b1df54faa3b49078e3416e21370a3ba","lcpCloudRegionId":"mdt1"}""",
                map.get("cloudConfiguration")
        )
        assertTrue(map.containsKey(Prefix + "requestInfo"))

        def reqinfo = map.get(Prefix + "requestInfo")
        assertTrue(reqinfo.indexOf("<request-id>mri</") >= 0)
        assertTrue(reqinfo.indexOf("<source>VID</") >= 0)
    }

    @Test
    public void preProcessRequest_EmptyParts() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)

        def req = request
                .replace('"source"', '"sourceXXX"')

        when(mex.getVariable("bpmnRequest")).thenReturn(req)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.preProcessRequest(mex)

        verify(mex).getVariable(DBGFLAG)
        verify(mex).setVariable("prefix", Prefix)
        verify(mex).setVariable("DeleteVcpeResCustServiceRequest", req)
        verify(mex).setVariable("msoRequestId", "mri")
        verify(mex).setVariable("requestAction", "ra")
        verify(mex).setVariable("source", "VID")
        verify(mex).setVariable("globalSubscriberId", CUST)
        verify(mex).setVariable("globalCustomerId", CUST)
        verify(mex).setVariable("disableRollback", "false")
        verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
        verify(mex).setVariable("subscriptionServiceType", SVC)

        verify(mex).setVariable("lcpCloudRegionId", "mdt1")
        verify(mex).setVariable("cloudOwner", "CloudOwner")
        verify(mex).setVariable("tenantId", "8b1df54faa3b49078e3416e21370a3ba")
        assertEquals(
                """{"cloudOwner":"CloudOwner","tenantId":"8b1df54faa3b49078e3416e21370a3ba","lcpCloudRegionId":"mdt1"}""",
                map.get("cloudConfiguration")
        )
        verify(mex).setVariable("sdncVersion", "1707")
        assertTrue(map.containsKey(Prefix + "requestInfo"))

        def reqinfo = map.get(Prefix + "requestInfo")
        println reqinfo
        assertTrue(reqinfo.indexOf("<request-id>mri</") >= 0)
        assertTrue(reqinfo.indexOf("<source>VID</") >= 0)
    }

    @Test
    public void preProcessRequest_MissingServiceInstanceId() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        when(mex.getVariable("serviceInstanceId")).thenReturn(null)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.preProcessRequest(mex) }))
    }

    @Test
    public void preProcessRequest_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        when(mex.getVariable("bpmnRequest")).thenThrow(new BpmnError("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.preProcessRequest(mex) }))
    }

    @Test
    public void preProcessRequest_Ex() {
        ExecutionEntity mex = setupMock()
        initPreProcess(mex)

        when(mex.getVariable("bpmnRequest")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.preProcessRequest(mex) }))
    }

    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("bpmnRequest")).thenReturn(request)
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("requestAction")).thenReturn("ra")
    }

    // ***** sendSyncResponse *****

    @Test
    public void sendSyncResponse() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initSendSyncResponse(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.sendSyncResponse(mex)

        verify(mex, times(2)).getVariable(DBGFLAG)

        verify(mex).setVariable(processName + "WorkflowResponseSent", "true")

        assertEquals("202", map.get(processName + "ResponseCode"))
        assertEquals("Success", map.get(processName + "Status"))

        def resp = map.get(processName + "Response")

        assertTrue(resp.indexOf('"instanceId":"sii"') >= 0)
        assertTrue(resp.indexOf('"requestId":"mri"') >= 0)
    }

    @Test
    public void sendSyncResponse_Ex() {
        ExecutionEntity mex = setupMock()
        initSendSyncResponse(mex)

        when(mex.getVariable("serviceInstanceId")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.sendSyncResponse(mex) }))
    }

    private initSendSyncResponse(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
    }

    // ***** prepareVnfAndModulesDelete *****

    @Test
    public void prepareVnfAndModulesDelete() {
        ExecutionEntity mex = setupMock()
        initPrepareVnfAndModulesDelete(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.prepareVnfAndModulesDelete(mex)

        verify(mex).getVariable(DBGFLAG)

        verify(mex).setVariable("vnfId", "vnfB")
    }

    @Test
    public void prepareVnfAndModulesDelete_Empty() {
        ExecutionEntity mex = setupMock()
        initPrepareVnfAndModulesDelete(mex)

        when(mex.getVariable(Prefix + "relatedVnfIdList")).thenReturn(new LinkedList())

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.prepareVnfAndModulesDelete(mex)

        verify(mex).getVariable(DBGFLAG)

        verify(mex).setVariable("vnfId", "")
    }

    @Test
    public void prepareVnfAndModulesDelete_Ex() {
        ExecutionEntity mex = setupMock()
        initPrepareVnfAndModulesDelete(mex)

        when(mex.getVariable(Prefix + "relatedVnfIdList")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.prepareVnfAndModulesDelete(mex) }))
    }

    private initPrepareVnfAndModulesDelete(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable(Prefix + "relatedVnfIdList")).thenReturn(Arrays.asList("vnfA", "vnfB", "vnfC"))
        when(mex.getVariable(Prefix + "vnfsDeletedCount")).thenReturn(1)
    }

    // ***** validateVnfDelete *****

    @Test
    public void validateVnfDelete() {
        ExecutionEntity mex = setupMock()
        initValidateVnfDelete(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.validateVnfDelete(mex)

        verify(mex).getVariable(DBGFLAG)

        verify(mex).setVariable(Prefix + "vnfsDeletedCount", 3)
    }

    @Test
    public void validateVnfDelete_Ex() {
        ExecutionEntity mex = setupMock()
        initValidateVnfDelete(mex)

        when(mex.getVariable(Prefix + "vnfsDeletedCount")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.validateVnfDelete(mex) }))
    }

    private initValidateVnfDelete(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable(Prefix + "vnfsDeletedCount")).thenReturn(2)
    }

    // ***** postProcessResponse *****

    @Test
    public void postProcessResponse() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPostProcessResponse(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.postProcessResponse(mex)

        verify(mex).getVariable(DBGFLAG)

        verify(mex).setVariable(Prefix + "Success", true)

        def req = map.get(Prefix + "CompleteMsoProcessRequest")

        assertTrue(req.indexOf("<request-id>mri</") >= 0)
        assertTrue(req.indexOf("<source>mysrc</") >= 0)
    }

    @Test
    public void postProcessResponse_BpmnError() {
        ExecutionEntity mex = setupMock()
        initPostProcessResponse(mex)

        when(mex.getVariable("source")).thenThrow(new BpmnError("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.postProcessResponse(mex) }))
    }

    @Test
    public void postProcessResponse_Ex() {
        ExecutionEntity mex = setupMock()
        initPostProcessResponse(mex)

        when(mex.getVariable("source")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.postProcessResponse(mex) }))
    }

    private initPostProcessResponse(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("source")).thenReturn("mysrc")
        when(mex.getVariable("msoRequestId")).thenReturn("mri")
    }

    // ***** prepareFalloutRequest *****

    @Test
    public void prepareFalloutRequest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        WorkflowException wfe = initPrepareFalloutRequest(mex)
        when(wfe.getErrorMessage()).thenReturn("mymsg")
        when(wfe.getErrorCode()).thenReturn(999)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.prepareFalloutRequest(mex)

        verify(mex).getVariable(DBGFLAG)
        verify(mex, times(4)).getVariable("WorkflowException")
        verify(mex).getVariable(Prefix + "requestInfo")

        def fo = map.get(Prefix + "falloutRequest")

        assertTrue(fo.indexOf("<hello>world</") >= 0)
        assertTrue(fo.indexOf("ErrorMessage>mymsg</") >= 0)
        assertTrue(fo.indexOf("ErrorCode>999</") >= 0)
    }

    @Test
    public void prepareFalloutRequest_Ex() {
        ExecutionEntity mex = setupMock()
        setupMap(mex)
        initPrepareFalloutRequest(mex)

        when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.prepareFalloutRequest(mex) }))
    }

    private initPrepareFalloutRequest(ExecutionEntity mex) {
        WorkflowException wfe = mock(WorkflowException.class)

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("WorkflowException")).thenReturn(wfe)
        when(mex.getVariable(Prefix + "requestInfo")).thenReturn("<hello>world</hello>")

        return wfe
    }

    // ***** sendSyncError *****

    @Test
    public void sendSyncError() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initSendSyncError(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.sendSyncError(mex)

        verify(mex, times(2)).getVariable(DBGFLAG)

        verify(mex).setVariable(processName + "WorkflowResponseSent", "true")

        assertEquals("500", map.get(processName + "ResponseCode"))
        assertEquals("Fail", map.get(processName + "Status"))

        def resp = map.get(processName + "Response")

        assertTrue(resp.indexOf("ErrorMessage>mymsg</") >= 0)

        verify(mex).setVariable("WorkflowResponse", resp)
    }

    @Test
    public void sendSyncError_NotWfe() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initSendSyncError(mex)

        when(mex.getVariable("WorkflowException")).thenReturn("not a WFE")

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.sendSyncError(mex)

        verify(mex, times(2)).getVariable(DBGFLAG)
        verify(mex, times(2)).getProcessEngineServices()
        verify(mex, times(2)).getProcessDefinitionId()

        verify(mex).setVariable(processName + "WorkflowResponseSent", "true")

        assertEquals("500", map.get(processName + "ResponseCode"))
        assertEquals("Fail", map.get(processName + "Status"))

        def resp = map.get(processName + "Response")

        assertTrue(resp.indexOf("ErrorMessage>Sending Sync Error.</") >= 0)

        verify(mex).setVariable("WorkflowResponse", resp)
    }

    @Test
    public void sendSyncError_NullWfe() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initSendSyncError(mex)

        when(mex.getVariable("WorkflowException")).thenReturn(null)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
        DeleteVcpeResCustService.sendSyncError(mex)

        verify(mex, times(2)).getVariable(DBGFLAG)

        verify(mex).setVariable(processName + "WorkflowResponseSent", "true")

        assertEquals("500", map.get(processName + "ResponseCode"))
        assertEquals("Fail", map.get(processName + "Status"))

        def resp = map.get(processName + "Response")

        assertTrue(resp.indexOf("ErrorMessage>Sending Sync Error.</") >= 0)

        verify(mex).setVariable("WorkflowResponse", resp)
    }

    @Test
    public void sendSyncError_Ex() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initSendSyncError(mex)

        when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        DeleteVcpeResCustService.sendSyncError(mex)

        assertFalse(map.containsKey(processName + "ResponseCode"))
    }

    private initSendSyncError(ExecutionEntity mex) {
        WorkflowException wfe = mock(WorkflowException.class)

        when(mex.getVariable(DBGFLAG)).thenReturn("true")
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("WorkflowException")).thenReturn(wfe)

        when(wfe.getErrorMessage()).thenReturn("mymsg")
    }

    // ***** processJavaException *****

    @Test
    public void processJavaException() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initProcessJavaException(mex)

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.processJavaException(mex) }))

        verify(mex).getVariable(DBGFLAG)

        verify(mex).setVariable("prefix", Prefix)

        def wfe = map.get("WorkflowException")

        assertEquals("Caught a Java Lang Exception", wfe.getErrorMessage())
    }

    @Test
    public void processJavaException_BpmnError() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initProcessJavaException(mex)

        when(mex.getVariables()).thenThrow(new BpmnError("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.processJavaException(mex) }))

        assertFalse(map.containsKey("WorkflowException"))
    }

    @Test
    public void processJavaException_Ex() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initProcessJavaException(mex)

        when(mex.getVariables()).thenThrow(new RuntimeException("expected exception"))

        DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()

        assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.processJavaException(mex) }))

        def wfe = map.get("WorkflowException")

        assertEquals("Exception in processJavaException method", wfe.getErrorMessage())
    }

    private initProcessJavaException(ExecutionEntity mex) {
        when(mex.getVariable(DBGFLAG)).thenReturn("true")
    }
}