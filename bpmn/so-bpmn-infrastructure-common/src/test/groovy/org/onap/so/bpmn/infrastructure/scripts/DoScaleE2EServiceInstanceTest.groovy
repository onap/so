/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC All rights reserved. *
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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.vcpe.scripts.GroovyTestBase

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class DoScaleE2EServiceInstanceTest extends GroovyTestBase {

    private static String request

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(GroovyTestBase.PORT)

    String Prefix = "CVRCS_"

    @BeforeClass
    public static void setUpBeforeClass() {
        request = FileUtil.readResourceFile("__files/InfrastructureFlows/DoScaleE2EServiceInstance.json")
    }

    @Before
    public void init()
    {
        MockitoAnnotations.initMocks(this)
    }
    public DoScaleE2EServiceInstanceTest(){
        super("DoScaleE2EServiceInstance")
    }

    @Test
    @Ignore
    // Ignored because of the bug https://jira.onap.org/browse/SO-1681
    public void preProcessRequestTest(){

        ExecutionEntity mex = setupMock()
        setupMap(mex)
        initPreProcess(mex)

        DoScaleE2EServiceInstance instance = new DoScaleE2EServiceInstance()
        instance.preProcessRequest(mex)
        verify(mex).setVariable("resourceTemplateUUIDs", "ns111:ns333:")
    }

    @Test
    public void preInitResourcesOperStatusTest(){
        ExecutionEntity mex = setupMock()
        setupMap(mex)
        initPreProcess(mex)
        DoScaleE2EServiceInstance instance = new DoScaleE2EServiceInstance()
        instance.preInitResourcesOperStatus(mex)

        verify(mex).setVariable("serviceInstanceId","e151059a-d924-4629-845f-264db19e50b4")
        verify(mex).setVariable("operationId", "59960003992")
        verify(mex).setVariable("operationType", "SCALE")
        verify(mex).setVariable("URN_mso_openecomp_adapters_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

        String payload =
                """<soapenv:Envelope xmlns:ns="http://org.onap.so/requestsdb"
                  xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:initResourceOperationStatus>
         <serviceId>e151059a-d924-4629-845f-264db19e50b4</serviceId>
         <operationId>59960003992</operationId>
         <operationType>SCALE</operationType>
         <resourceTemplateUUIDs>ns111:ns333:</resourceTemplateUUIDs>
      </ns:initResourceOperationStatus>
   </soapenv:Body>
</soapenv:Envelope>"""
        verify(mex).setVariable("CVFMI_initResOperStatusRequest", payload)
    }

    private void initPreProcess(ExecutionEntity mex) {
        when(mex.getVariable(GroovyTestBase.DBGFLAG)).thenReturn("true")
        when(mex.getVariable("bpmnRequest")).thenReturn(request)
        when(mex.getVariable("msoRequestId")).thenReturn("mri")
        when(mex.getVariable("serviceType")).thenReturn("VoLTE")
        when(mex.getVariable("serviceInstanceId")).thenReturn("e151059a-d924-4629-845f-264db19e50b4")
        when(mex.getVariable("serviceInstanceName")).thenReturn("ra")
        when(mex.getVariable("operationId")).thenReturn("59960003992")
        when(mex.getVariable("globalSubscriberId")).thenReturn("4993921112123")
        when(mex.getVariable("resourceTemplateUUIDs")).thenReturn("ns111:ns333:")
    }
}