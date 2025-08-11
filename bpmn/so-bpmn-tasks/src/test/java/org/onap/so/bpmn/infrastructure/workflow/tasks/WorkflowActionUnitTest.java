/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowActionUnitTest {

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/Macro/";

    @Mock
    private CatalogDbClient catalogDbClient;
    @Mock
    private BBInputSetup bbInputSetup;
    @Mock
    private BBInputSetupUtils bbInputSetupUtils;
    @Mock
    private ExceptionBuilder exceptionBuilder;
    @Mock
    private AAIConfigurationResources aaiConfigurationResources;

    @InjectMocks
    @Spy
    private WorkflowAction workflowAction;

    @Test
    @Ignore
    // TODO: Fix this test
    public void traverseCatalogDbForConfigurationTest() {

        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
        CvnfcConfigurationCustomization vfModuleCustomization = new CvnfcConfigurationCustomization();
        ConfigurationResource configuration = new ConfigurationResource();
        configuration.setToscaNodeType("FabricConfiguration");
        configuration.setModelUUID("my-uuid");
        vfModuleCustomization.setConfigurationResource(configuration);
        cvnfcCustomization.setCvnfcConfigurationCustomization(Collections.singletonList(vfModuleCustomization));
        List<CvnfcCustomization> cvnfcCustomizations = Arrays.asList(cvnfcCustomization);
        // when(catalogDbClient.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID(any(String.class),
        // any(String.class)))
        // .thenReturn(cvnfcCustomizations);

        // List<CvnfcConfigurationCustomization> results =
        // workflowAction.traverseCatalogDbForConfiguration("myVnfCustomizationId", "myVfModuleCustomizationId");

        // assertThat(results, is(Arrays.asList(vfModuleCustomization)));

    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + filename)));
    }

    private List<OrchestrationFlow> createFlowList(String... myList) {

        List<OrchestrationFlow> result = new ArrayList<>();
        for (String name : myList) {
            OrchestrationFlow flow = new OrchestrationFlow();
            flow.setFlowName(name);
            result.add(flow);
        }

        return result;

    }
}
