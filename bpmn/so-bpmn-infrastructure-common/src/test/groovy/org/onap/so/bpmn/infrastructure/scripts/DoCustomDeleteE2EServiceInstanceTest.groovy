/*-
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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.infrastructure.scripts.DoCustomDeleteE2EServiceInstance
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.vcpe.scripts.GroovyTestBase

import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.eq

class DoCustomDeleteE2EServiceInstanceTest extends GroovyTestBase {

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
    public DoCustomDeleteE2EServiceInstanceTest(){
        super("DoCustomDeleteE2EServiceInstance")
    }

    @Test
    public void preProcessRequestTest(){

        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)

        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.preProcessRequest(mex)
        verify(mex).setVariable("sdncCallbackUrl", "/mso/sdncadapter/")
        verify(mex).setVariable("siParamsXml", "")
    }

    @Test
    public void postProcessAAIGETSuccessTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("GENGS_SuccessIndicator")).thenReturn(true)

        String aaiGetResponse = FileUtil.readResourceFile("__files/GenericFlows/aaiGetResponse.xml")
        when(mex.getVariable("GENGS_service")).thenReturn(aaiGetResponse)
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.postProcessAAIGET(mex)

        verify(mex).setVariable(eq("serviceRelationShip"), anyString())
    }

    @Test
    public void postProcessAAIGETFailureTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("GENGS_FoundIndicator")).thenReturn(false)
        when(mex.getVariable("GENGS_SuccessIndicator")).thenReturn(false)

        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        assertThatThrownBy { instance.postProcessAAIGET(mex) } isInstanceOf BpmnError.class
    }

    @Test
    public void preInitResourcesOperStatusTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("serviceRelationShip")).thenReturn("[{\"resourceInstanceId\":\"3333\",\"resourceType\":\"overlay\"},{\"resourceInstanceId\":\"4444\",\"resourceType\":\"underlay\"},{\"resourceInstanceId\":\"1111\",\"resourceType\":\"vIMS\"},{\"resourceInstanceId\":\"222\",\"resourceType\":\"vEPC\"}]")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.preInitResourcesOperStatus(mex)

        verify(mex).setVariable(eq("CVFMI_initResOperStatusRequest"), anyString())
    }

    @Test
    public void preResourceDeleteTest() {
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("serviceRelationShip")).thenReturn("[{\"resourceInstanceId\":\"3333\",\"resourceType\":\"overlay\"},{\"resourceInstanceId\":\"4444\",\"resourceType\":\"underlay\"},{\"resourceInstanceId\":\"1111\",\"resourceType\":\"vIMS\"},{\"resourceInstanceId\":\"222\",\"resourceType\":\"vEPC\"}]")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        instance.preResourceDelete(mex,"overlay")
        verify(mex).setVariable("resourceType", "overlay")
    }

    @Test
    public void postProcessSDNCDeleteTest(){
        ExecutionEntity mex = setupMock()
        def map = setupMap(mex)
        initPreProcess(mex)
        when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn("true")
        when(mex.getVariable("DDELSI_sdncResponseSuccess")).thenReturn("true")
        when(mex.getVariable("prefix")).thenReturn("DDELSI_")
        DoCustomDeleteE2EServiceInstance instance = new DoCustomDeleteE2EServiceInstance()
        String response = FileUtil.readResourceFile("__files/GenericFlows/SDNCDeleteResponse.xml")
        String method = "deleteE2E";
        instance.postProcessSDNCDelete(mex, response, method)
		// following method doesn't do anything currently -> nothing to check
    }

    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(GroovyTestBase.DBGFLAG)).thenReturn("true")
        when(mex.getVariable("bpmnRequest")).thenReturn(request)
        when(mex.getVariable("mso-request-id")).thenReturn("mri")
        when(mex.getVariable("serviceType")).thenReturn("VoLTE")
        when(mex.getVariable("serviceInstanceId")).thenReturn("e151059a-d924-4629-845f-264db19e50b4")
        when(mex.getVariable("requestAction")).thenReturn("ra")
        when(mex.getVariable("operationId")).thenReturn("59960003992")
        when(mex.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("/mso/sdncadapter/")
        when(mex.getVariable("GENGS_FoundIndicator")).thenReturn("true")
        when(mex.getVariable("GENGS_siResourceLink")).thenReturn("/service-subscription/e2eserviceInstance/delete/service-instances/")
        when(mex.getVariable("globalSubscriberId")).thenReturn("4993921112123")
        when(mex.getVariable("GENGS_service")).thenReturn("test3434")
        when(mex.getVariable("mso.adapters.openecomp.db.endpoint")).thenReturn("http://localhost:8080/mso")
    }
}
