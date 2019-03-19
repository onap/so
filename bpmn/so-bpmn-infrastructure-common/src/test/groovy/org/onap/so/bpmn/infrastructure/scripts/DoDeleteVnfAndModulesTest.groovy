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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.GenericVnf
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.graphinventory.entities.uri.Depth
import org.onap.so.client.graphinventory.exceptions.GraphInventoryUriComputationException
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
public class DoDeleteVnfAndModulesTest extends MsoGroovyTest{

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Spy
    DoDeleteVnfAndModules doDeleteVnfAndModules

    String cloudConfiguration = "{ " +
            "\"lcpCloudRegionId\": \"mdt1\"," +
            "\"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\"" + "}";

    @Before
    public void init() throws IOException {
        super.init("DoDeleteVnfAndModules")
        MockitoAnnotations.initMocks(this);
        when(doDeleteVnfAndModules.getAAIClient()).thenReturn(client)
    }

    @Test
    public void testPreProcessRequestTest() {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("cloudConfiguration")).thenReturn(cloudConfiguration)
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
        when(mockExecution.getVariable("productFamilyId")).thenReturn("productFamilyId")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("mdt1")
        when(mockExecution.getVariable("tenantId")).thenReturn("19123c2924c648eb8e42a3c1f14b7682")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("MSO_test")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")

        DoDeleteVnfAndModules obj = new DoDeleteVnfAndModules()
        obj.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", "DDVAM_")
        Mockito.verify(mockExecution).setVariable("requestId", "12345")
        Mockito.verify(mockExecution).setVariable("mso-request-id", "12345")
        Mockito.verify(mockExecution).setVariable("DDVAM_source", "VID")
        Mockito.verify(mockExecution).setVariable("DDVAM_isVidRequest", "true")
        Mockito.verify(mockExecution).setVariable("DDVAM_sdncVersion", "1702")
        Mockito.verify(mockExecution).setVariable("DDVAM_isVidRequest", "true")
        Mockito.verify(mockExecution).setVariable("sdncCallbackUrl", "http://localhost:8090/SDNCAdapterCallback")
    }

  

    @Test
    public void testQueryAAIVfModuleNullEndPoint() {
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "12345").depth(Depth.ONE)
        doThrow(new GraphInventoryUriComputationException("Error in AAI")).when(client).get(GenericVnf.class,uri)
        try {
            doDeleteVnfAndModules.queryAAIVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution,atLeastOnce()).setVariable(captor.capture(),captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals("AAI GET Failed:Error in AAI", workflowException.getErrorMessage())
        Assert.assertEquals(1002, workflowException.getErrorCode())
    }

    @Test
    public void testQueryAAIVfModule() {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        mockAAIGenericVnf("12345","__files/AAI/GenericVnfVfModule.json")
        doDeleteVnfAndModules.queryAAIVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DCVFM_queryAAIVfModuleResponseCode", 200)
    }
}
