/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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

package org.onap.so.bpmn.common.scripts

import joptsimple.internal.Strings
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrnPropertiesReader.class)
class CreateAAIVfModuleTest extends MsoGroovyTest {

    private static final String VNF_ID = "vnfIdTest"
    private static final String VNF_TYPE = "vnfTypeTest"
    private static final String VNF_NAME = "testVnf"
    private static final String SERVICE_ID = "123"
    private static final String PERSONAL_MODEL_ID = "modelTest"
    private static final String PERSONAL_MODEL_VERSION = "12"
    private static final String MODEL_CUST_ID = "modelCustIdTest"
    private static final String VNF_PERSONAL_MODEL_ID = "perModIdTest"
    private static final String VNF_PERSONAL_MODEL_VER = "14"
    private static final String VF_MODULE_NAME = "modTestName"
    private static final String VF_MODULE_MODEL_NAME = "modModelNameTest"

    private static final String DEFAULT_AAI_VERSION = "9"
    private static final String DEFAULT_AAI_NAMESPACE = "defaultTestNamespace"

    private CreateAAIVfModule testedObject
    private ExecutionEntity mockExecution

    @Before
    void setupTest() {
        mockExecution = setupMock("CreateAAIVfModule")
        testedObject = new CreateAAIVfModule()
    }

    @Test
    void preProcessRequest_successful() {
        // given
        when(mockExecution.getVariable("vnfId")).thenReturn(VNF_ID)
        when(mockExecution.getVariable("vnfType")).thenReturn(VNF_TYPE)
        when(mockExecution.getVariable("mso.workflow.default.aai.v3.generic-vnf.uri")).thenReturn('uriTest')
        when(mockExecution.getVariable("vnfName")).thenReturn(VNF_NAME)
        when(mockExecution.getVariable("serviceId")).thenReturn(SERVICE_ID)
        when(mockExecution.getVariable("personaModelId")).thenReturn(PERSONAL_MODEL_ID)
        when(mockExecution.getVariable("personaModelVersion")).thenReturn(PERSONAL_MODEL_VERSION)
        when(mockExecution.getVariable("modelCustomizationId")).thenReturn(MODEL_CUST_ID)
        when(mockExecution.getVariable("vnfPersonaModelId")).thenReturn(VNF_PERSONAL_MODEL_ID)
        when(mockExecution.getVariable("vnfPersonaModelVersion")).thenReturn(VNF_PERSONAL_MODEL_VER)
        when(mockExecution.getVariable("isBaseVfModule")).thenReturn("true")
        when(mockExecution.getVariable("vfModuleName")).thenReturn(VF_MODULE_NAME)
        when(mockExecution.getVariable("vfModuleModelName")).thenReturn(VF_MODULE_MODEL_NAME)
        PowerMockito.mockStatic(UrnPropertiesReader.class)
        PowerMockito.when(UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.version"))
                .thenReturn(DEFAULT_AAI_VERSION)
        PowerMockito.when(UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.namespace"))
                .thenReturn(DEFAULT_AAI_NAMESPACE)

        //when
        testedObject.preProcessRequest(mockExecution)

        //then
        verify(mockExecution).setVariable("CAAIVfMod_vnfId", VNF_ID)
        verify(mockExecution).setVariable("CAAIVfMod_vnfName", VNF_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_vnfType", VNF_TYPE)
        verify(mockExecution).setVariable("CAAIVfMod_serviceId", SERVICE_ID)
        verify(mockExecution).setVariable("CAAIVfMod_personaId", PERSONAL_MODEL_ID)
        verify(mockExecution).setVariable("CAAIVfMod_personaVer", PERSONAL_MODEL_VERSION)
        verify(mockExecution).setVariable("CAAIVfMod_modelCustomizationId", MODEL_CUST_ID)
        verify(mockExecution).setVariable("CAAIVfMod_vnfPersonaId", VNF_PERSONAL_MODEL_ID)
        verify(mockExecution).setVariable("CAAIVfMod_vnfPersonaVer", VNF_PERSONAL_MODEL_VER)
        verify(mockExecution).setVariable("CAAIVfMod_isBaseVfModule", true)
        verify(mockExecution).setVariable("CAAIVfMod_moduleName", VF_MODULE_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_moduleModelName", VF_MODULE_MODEL_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_aaiNamespace", DEFAULT_AAI_NAMESPACE + "v" + DEFAULT_AAI_VERSION)
    }

    @Test
    void processAAIGenericVnfQuery_setVnfResponse() {
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponseCode")).thenReturn(200)
        when(mockExecution.getVariable("CAAIVfMod_vnfId")).thenReturn(Strings.EMPTY)
        when(mockExecution.getVariable("CAAIVfMod_vnfName")).thenReturn(VNF_NAME)

        testedObject.processAAIGenericVnfQuery(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_queryGenericVnfResponse", "Invalid request for new Generic VNF which already exists, Vnf Name=" + VNF_NAME)
    }

    @Test
    void processAAIGenericVnfQuery_setVfModuleResponse() {
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponseCode")).thenReturn(500)
        when(mockExecution.getVariable("CAAIVfMod_vnfId")).thenReturn(VNF_ID)

        testedObject.processAAIGenericVnfQuery(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_createVfModuleResponse", "Invalid request for Add-on Module requested for non-existant Generic VNF, VNF Id=" + VNF_ID)
    }

    @Test
    void parseForAddOnModule_moduleNameFound() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>"+VNF_NAME+"</vnf-name>" +
                "<vf-module-name>"+VF_MODULE_NAME+"</vf-module-name></CAAIVfMod_queryGenericVnfResponse>"

        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(xml)
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn(VF_MODULE_NAME)

        testedObject.parseForAddOnModule(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_vnfNameFromAAI", VNF_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_moduleExists", true)
        verify(mockExecution).setVariable("CAAIVfMod_parseModuleResponse", "VF Module "+VF_MODULE_NAME+
                " already exists for Generic VNF "+VNF_NAME)
    }

    @Test
    void parseForAddOnModule_moduleNameNotFound() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>"+VNF_NAME+"</vnf-name>" +
                "</CAAIVfMod_queryGenericVnfResponse>"
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(xml)
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn(VF_MODULE_NAME)
        when(mockExecution.getVariable("CAAIVfMod_moduleExists")).thenReturn(false)

        testedObject.parseForAddOnModule(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_vnfNameFromAAI", VNF_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
        verify(mockExecution).setVariable("CAAIVfMod_parseModuleResponse", "VF Module "+VF_MODULE_NAME+
                " does not exist for Generic VNF "+VNF_NAME)
    }

    @Test
    void parseForBaseModule_moduleNameFound() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>"+VNF_NAME+"</vnf-name>" +
                "<vf-module-name>"+VF_MODULE_NAME+"</vf-module-name></CAAIVfMod_queryGenericVnfResponse>"
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(xml)
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn(VF_MODULE_NAME)

        testedObject.parseForBaseModule(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_vnfNameFromAAI", VNF_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
        verify(mockExecution).setVariable("CAAIVfMod_baseModuleConflict", true)
        verify(mockExecution).setVariable("CAAIVfMod_parseModuleResponse", "VF Module "+VF_MODULE_NAME+
                " already exists for Generic VNF "+VNF_NAME)
    }

    @Test
    void parseForBaseModule_isBaseVfModule() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>"+VNF_NAME+"</vnf-name>" +
                "<is-base-vf-module>true</is-base-vf-module></CAAIVfMod_queryGenericVnfResponse>"
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(xml)
        when(mockExecution.getVariable("CAAIVfMod_baseModuleConflict")).thenReturn(false)

        testedObject.parseForBaseModule(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_vnfNameFromAAI", VNF_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
        verify(mockExecution).setVariable("CAAIVfMod_baseModuleConflict", true)
        verify(mockExecution).setVariable("CAAIVfMod_parseModuleResponse",
                "Base VF Module already exists for Generic VNF "+VNF_NAME)
    }

    @Test
    void parseForBaseModule_baseModuleConflictIsFalse() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>"+VNF_NAME+"</vnf-name></CAAIVfMod_queryGenericVnfResponse>"
        when(mockExecution.getVariable("CAAIVfMod_queryGenericVnfResponse")).thenReturn(xml)
        when(mockExecution.getVariable("CAAIVfMod_baseModuleConflict")).thenReturn(false)
        when(mockExecution.getVariable("CAAIVfMod_moduleName")).thenReturn(VF_MODULE_NAME)

        testedObject.parseForBaseModule(mockExecution)

        verify(mockExecution).setVariable("CAAIVfMod_vnfNameFromAAI", VNF_NAME)
        verify(mockExecution).setVariable("CAAIVfMod_moduleExists", false)
        verify(mockExecution).setVariable("CAAIVfMod_parseModuleResponse", "VF Module "+VF_MODULE_NAME+
                " does not exist for Generic VNF "+VNF_NAME)
    }

}
