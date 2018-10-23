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
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.Before
import org.junit.Test
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.springframework.core.env.Environment

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CreateAAIVfModuleTest {

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
    private DelegateExecutionFake executionFake

    @Before
    void setupTest() {
        testedObject = new CreateAAIVfModule()
        executionFake = new DelegateExecutionFake()
    }

    @Test
    void preProcessRequest_successful() {
        //given
        prepareUrnPropertiesReader()
        executionFake.setVariable("vnfId", VNF_ID)
        executionFake.setVariable("vnfType", VNF_TYPE)
        executionFake.setVariable("vnfName", VNF_NAME)
        executionFake.setVariable("serviceId", SERVICE_ID)
        executionFake.setVariable("personaModelId", PERSONAL_MODEL_ID)
        executionFake.setVariable("personaModelVersion", PERSONAL_MODEL_VERSION)
        executionFake.setVariable("modelCustomizationId", MODEL_CUST_ID)
        executionFake.setVariable("vnfPersonaModelId", VNF_PERSONAL_MODEL_ID)
        executionFake.setVariable("vnfPersonaModelVersion", VNF_PERSONAL_MODEL_VER)
        executionFake.setVariable("isBaseVfModule", "true")
        executionFake.setVariable("vfModuleName", VF_MODULE_NAME)
        executionFake.setVariable("vfModuleModelName", VF_MODULE_MODEL_NAME)
        //when
        testedObject.preProcessRequest(executionFake)
        //then
        assertThat(executionFake.getVariable("CAAIVfMod_vnfId")).isEqualTo(VNF_ID)
        assertThat(executionFake.getVariable("CAAIVfMod_vnfName")).isEqualTo(VNF_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_vnfType")).isEqualTo(VNF_TYPE)
        assertThat(executionFake.getVariable("CAAIVfMod_serviceId")).isEqualTo(SERVICE_ID)
        assertThat(executionFake.getVariable("CAAIVfMod_personaId")).isEqualTo(PERSONAL_MODEL_ID)
        assertThat(executionFake.getVariable("CAAIVfMod_personaVer")).isEqualTo(PERSONAL_MODEL_VERSION)
        assertThat(executionFake.getVariable("CAAIVfMod_modelCustomizationId")).isEqualTo(MODEL_CUST_ID)
        assertThat(executionFake.getVariable("CAAIVfMod_vnfPersonaId")).isEqualTo(VNF_PERSONAL_MODEL_ID)
        assertThat(executionFake.getVariable("CAAIVfMod_vnfPersonaVer")).isEqualTo(VNF_PERSONAL_MODEL_VER)
        assertThat(executionFake.getVariable("CAAIVfMod_isBaseVfModule")).isEqualTo(true)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleName")).isEqualTo(VF_MODULE_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleModelName")).isEqualTo(VF_MODULE_MODEL_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_aaiNamespace"))
                .isEqualTo(DEFAULT_AAI_NAMESPACE + "v" + DEFAULT_AAI_VERSION)
    }

    @Test
    void processAAIGenericVnfQuery_setVnfResponse() {
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponseCode", 200)
        executionFake.setVariable("CAAIVfMod_vnfId", Strings.EMPTY)
        executionFake.setVariable("CAAIVfMod_vnfName", VNF_NAME)

        testedObject.processAAIGenericVnfQuery(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_queryGenericVnfResponse"))
                .isEqualTo("Invalid request for new Generic VNF which already exists, Vnf Name=" + VNF_NAME)
    }

    @Test
    void processAAIGenericVnfQuery_setVfModuleResponse() {
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponseCode", 500)
        executionFake.setVariable("CAAIVfMod_vnfId", VNF_ID)

        testedObject.processAAIGenericVnfQuery(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_createVfModuleResponse"))
                .isEqualTo("Invalid request for Add-on Module requested for non-existant Generic VNF, VNF Id=" + VNF_ID)
    }

    @Test
    void parseForAddOnModule_moduleNameFound() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>" + VNF_NAME + "</vnf-name>" +
                "<vf-module-name>" + VF_MODULE_NAME + "</vf-module-name></CAAIVfMod_queryGenericVnfResponse>"
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponse", xml)
        executionFake.setVariable("CAAIVfMod_moduleName", VF_MODULE_NAME)

        testedObject.parseForAddOnModule(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_vnfNameFromAAI")).isEqualTo(VNF_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleExists")).isEqualTo(true)
        assertThat(executionFake.getVariable("CAAIVfMod_parseModuleResponse"))
                .isEqualTo("VF Module " + VF_MODULE_NAME + " already exists for Generic VNF " + VNF_NAME)
    }

    @Test
    void parseForAddOnModule_moduleNameNotFound() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>" + VNF_NAME + "</vnf-name>" +
                "</CAAIVfMod_queryGenericVnfResponse>"
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponse", xml)
        executionFake.setVariable("CAAIVfMod_moduleName", VF_MODULE_NAME)
        executionFake.setVariable("CAAIVfMod_moduleExists", false)

        testedObject.parseForAddOnModule(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_vnfNameFromAAI")).isEqualTo(VNF_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleExists")).isEqualTo(false)
        assertThat(executionFake.getVariable("CAAIVfMod_parseModuleResponse"))
                .isEqualTo("VF Module " + VF_MODULE_NAME + " does not exist for Generic VNF " + VNF_NAME)
    }

    @Test
    void parseForBaseModule_moduleNameFound() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>" + VNF_NAME + "</vnf-name>" +
                "<vf-module-name>" + VF_MODULE_NAME + "</vf-module-name></CAAIVfMod_queryGenericVnfResponse>"
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponse", xml)
        executionFake.setVariable("CAAIVfMod_moduleName", VF_MODULE_NAME)

        testedObject.parseForBaseModule(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_vnfNameFromAAI")).isEqualTo(VNF_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleExists")).isEqualTo(false)
        assertThat(executionFake.getVariable("CAAIVfMod_baseModuleConflict")).isEqualTo(true)
        assertThat(executionFake.getVariable("CAAIVfMod_parseModuleResponse"))
                .isEqualTo("VF Module " + VF_MODULE_NAME + " already exists for Generic VNF " + VNF_NAME)
    }

    @Test
    void parseForBaseModule_isBaseVfModule() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>" + VNF_NAME + "</vnf-name>" +
                "<is-base-vf-module>true</is-base-vf-module></CAAIVfMod_queryGenericVnfResponse>"
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponse", xml)
        executionFake.setVariable("CAAIVfMod_baseModuleConflict", false)

        testedObject.parseForBaseModule(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_vnfNameFromAAI")).isEqualTo(VNF_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleExists")).isEqualTo(false)
        assertThat(executionFake.getVariable("CAAIVfMod_baseModuleConflict")).isEqualTo(true)
        assertThat(executionFake.getVariable("CAAIVfMod_parseModuleResponse"))
                .isEqualTo("Base VF Module already exists for Generic VNF " + VNF_NAME)
    }

    @Test
    void parseForBaseModule_baseModuleConflictIsFalse() {
        String xml = "<CAAIVfMod_queryGenericVnfResponse><vnf-name>" + VNF_NAME + "</vnf-name></CAAIVfMod_queryGenericVnfResponse>"
        executionFake.setVariable("CAAIVfMod_queryGenericVnfResponse", xml)
        executionFake.setVariable("CAAIVfMod_baseModuleConflict", false)
        executionFake.setVariable("CAAIVfMod_moduleName", VF_MODULE_NAME)

        testedObject.parseForBaseModule(executionFake)

        assertThat(executionFake.getVariable("CAAIVfMod_vnfNameFromAAI")).isEqualTo(VNF_NAME)
        assertThat(executionFake.getVariable("CAAIVfMod_moduleExists")).isEqualTo(false)
        assertThat(executionFake.getVariable("CAAIVfMod_parseModuleResponse"))
                .isEqualTo("VF Module " + VF_MODULE_NAME + " does not exist for Generic VNF " + VNF_NAME)
    }

    private void prepareUrnPropertiesReader() {
        Environment mockEnvironment = mock(Environment.class)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.version")).thenReturn(DEFAULT_AAI_VERSION)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.namespace")).thenReturn(DEFAULT_AAI_NAMESPACE)
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
    }

}
