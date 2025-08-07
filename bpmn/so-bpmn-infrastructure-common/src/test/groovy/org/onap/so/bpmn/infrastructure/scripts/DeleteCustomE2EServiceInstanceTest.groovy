/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.vcpe.scripts.GroovyTestBase

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class DeleteCustomE2EServiceInstanceTest extends GroovyTestBase {

    private static String request

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(GroovyTestBase.PORT)

    String Prefix = "CVRCS_"
    String RbType = "DCRENI_"

    @BeforeClass
    public static void setUpBeforeClass() {
        request = FileUtil.readResourceFile("__files/InfrastructureFlows/DeleteCustomE2EService.json")
    }

    @Before
    public void init()
    {
        MockitoAnnotations.initMocks(this)
    }

    public DeleteCustomE2EServiceInstanceTest(){
        super("DeleteCustomE2EServiceInstance")
    }
    @Test
    public void preProcessRequestTest () {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        mex.setVariable("isDebugLogEnabled","true")
        instance.preProcessRequest(mex);

        verify(mex).getVariable(GroovyTestBase.DBGFLAG)

        Map<String,String> userParams = new HashMap<>()
        userParams.put("someUserParam","someValue")

        verify(mex).setVariable("prefix", "DELSI_")
        verify(mex).setVariable("msoRequestId", "mri")
        verify(mex).setVariable("source", "CCD")
        verify(mex).setVariable("operationType", "DELETE")
        verify(mex).setVariable("globalSubscriberId", "38829939920000")
        verify(mex).setVariable("serviceInputParams",userParams)
    }

    @Test
    public void sendSyncResponseTest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.sendSyncResponse(mex)
        verify(mex).setVariable("DeleteCustomE2EServiceInstanceWorkflowResponseSent", "true")
    }

    @Test
    public void prepareCompletionRequestTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.prepareCompletionRequest(mex)
        String msoComplitionRequest = FileUtil.readResourceFile("__files/GenericFlows/MsoCompletionRequest.xml")
        //verify(mex).setVariable("completionRequest", msoComplitionRequest)
    }

    @Test
    public void sendSyncErrorTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.sendSyncError(mex)

    }

    @Test
    public void prepareFalloutRequest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.prepareFalloutRequest(mex)
        //verify(mex).setVariable("falloutRequest", requestInfo)
    }

    @Test
    public void processJavaExceptionTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        DeleteCustomE2EServiceInstance instance = new DeleteCustomE2EServiceInstance()
        instance.processJavaException()
    }


    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(GroovyTestBase.DBGFLAG)).thenReturn("true")
        when(mex.getVariable("bpmnRequest")).thenReturn(request)
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("serviceType")).thenReturn("VoLTE")
        when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
        when(mex.getVariable("requestAction")).thenReturn("ra")
        when(mex.getVariable("operationId")).thenReturn("59960003992")
    }
}
