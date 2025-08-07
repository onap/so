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

package org.onap.so.bpmn.common;

import static org.junit.Assert.assertNotNull;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetGenericVnfById;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;

/**
 * Unit test for GenerateVfModuleName.bpmn.
 */

public class GenerateVfModuleNameIT extends BaseIntegrationTest {
    private static final String EOL = "\n";

    @Test
    public void TestGenerateVfModuleNameSuvvess() {

        //
        MockGetGenericVnfById(wireMockServer, ".*", "GenericFlows/getGenericVnfByNameResponse.xml");
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("vnfName", "STMTN5MMSC20");
        variables.put("vfModuleLabel", "ModuleLabel1");
        variables.put("personaModelId", "extrovert");
        variables.put("vnfId", "12345678-f41f-4822-9323-b75962763d74");

        runtimeService.startProcessInstanceByKey("GenerateVfModuleName", variables);
        String response = BPMNUtil.getVariable(processEngine, "GenerateVfModuleName", "vfModuleName");

        assertNotNull(response);
    }
}
