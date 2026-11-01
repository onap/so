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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;

/**
 * Unit Test for the DecomposeService Flow
 *
 */

public class DecomposeServiceIT extends BaseIntegrationTest {

    @Before
    public void assertProductionProcessIsDeployed() throws Exception {
        String resourceName = "subprocess/BuildingBlock/DecomposeService.bpmn";
        URL resource = getClass().getClassLoader().getResource(resourceName);
        Assert.assertNotNull(resource);
        String location = resource.toExternalForm();
        Assert.assertTrue(location,
                location.contains("/MSOCommonBPMN/target/classes/")
                        || (location.matches("jar:.*/MSOCommonBPMN-[^/]+\\.jar!/" + resourceName)
                                && !location.contains("-tests.jar!/")));
        var definition = processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey("DecomposeService").latestVersion().singleResult();
        Assert.assertNotNull(definition);
        try (InputStream source = resource.openStream();
                InputStream deployed = processEngine.getRepositoryService()
                        .getResourceAsStream(definition.getDeploymentId(), definition.getResourceName())) {
            Assert.assertArrayEquals(source.readAllBytes(), deployed.readAllBytes());
        }
    }

    public DecomposeServiceIT() throws IOException {

    }

    @Test
    public void testDecomposeService_success() {
        stubCatalog("getCatalogServiceResourcesDataWithConfig.json");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables, "testRequestId123", "ff5256d2-5a33-55df-13ab-12abad84e7ff");
        invokeSubProcess("DecomposeService", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));
        assertDecomposition(businessKey, "5df8b6de-2083-11e7-93ae-92361f002671", 1);

    }

    @Test
    public void testDecomposeService_success_partial() {
        stubCatalog("getCatalogServiceResourcesData.json");


        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables, "testRequestId123", "ff5256d2-5a33-55df-13ab-12abad84e7ff");
        invokeSubProcess("DecomposeService", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));
        assertDecomposition(businessKey, "b73c6911-be64-448a-bb4b-7b7a7006507a", 0);

    }

    private void setVariablesSuccess(Map<String, Object> variables, String requestId, String siId) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", requestId);
        variables.put("msoRequestId", requestId);
        variables.put("serviceInstanceId", siId);

        String serviceModelInfo = "{ " + "\"modelType\": \"service\"," + "\"modelInvariantUuid\": \"cmw-123-456-789\","
                + "\"modelVersionId\": \"ab6478e5-ea33-3346-ac12-ab121484a3fe\"," + "\"modelName\": \"ServicevSAMP12\","
                + "\"modelVersion\": \"1.0\"}";
        variables.put("serviceModelInfo", serviceModelInfo);

    }

    private void stubCatalog(String responseFile) {
        wireMockServer.stubFor(get(urlPathEqualTo("/v2/serviceResources"))
                .withQueryParam("serviceModelInvariantUuid", equalTo("cmw-123-456-789"))
                .withQueryParam("serviceModelVersion", equalTo("1.0")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json").withBodyFile(responseFile)));
    }

    private void assertDecomposition(String businessKey, String modelUuid, int allottedResources) {
        String processId = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey)
                .processDefinitionKey("DecomposeService").singleResult().getId();
        Assert.assertNull(historyService.createHistoricVariableInstanceQuery().processInstanceId(processId)
                .variableName("WorkflowException").singleResult());
        Object result = BPMNUtil.getRawVariable(processEngine, "DecomposeService", "serviceDecomposition", processId);
        Assert.assertTrue(result instanceof ServiceDecomposition);
        ServiceDecomposition service = (ServiceDecomposition) result;
        Assert.assertEquals(modelUuid, service.getModelInfo().getModelUuid());
        Assert.assertEquals(1, service.getVnfResources().size());
        Assert.assertTrue(service.getNetworkResources().isEmpty());
        Assert.assertEquals(allottedResources, service.getAllottedResources().size());
        wireMockServer.verify(1,
                getRequestedFor(urlPathEqualTo("/v2/serviceResources"))
                        .withQueryParam("serviceModelInvariantUuid", equalTo("cmw-123-456-789"))
                        .withQueryParam("serviceModelVersion", equalTo("1.0")));
        Assert.assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

}
