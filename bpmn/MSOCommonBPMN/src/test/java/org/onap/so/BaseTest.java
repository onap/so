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
package org.onap.so;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupMapperLayer;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.test.categories.SpringAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureWireMock(port = 0)
@Category(SpringAware.class)
public abstract class BaseTest extends BuildingBlockTestDataSetup {


    protected Map<String, Object> variables = new HashMap<>();

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    protected HttpHeaders headers = new HttpHeaders();


    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    protected WireMockServer wireMockServer;
    /*
     * Mocked for injection via autowiring
     */

    @Value("${mso.catalog.db.spring.endpoint}")
    protected String endpoint;

    @Value("${wiremock.server.port}")
    protected String wireMockPort;

    @MockBean
    protected CatalogDbClient MOCK_catalogDbClient;

    @SpyBean
    protected InjectionHelper MOCK_injectionHelper;

    @SpyBean
    protected ExceptionBuilder exceptionUtil;

    /*
     * Classes that cannot be simply mocked because they are both needed for testing another class, and must be
     * autowired when being tested themselves....or classes with private methods that must be stubbed during testing
     */

    @SpyBean
    protected BBInputSetupMapperLayer SPY_bbInputSetupMapperLayer;
    @SpyBean
    protected BBInputSetupUtils SPY_bbInputSetupUtils;
    @SpyBean
    protected BBInputSetup SPY_bbInputSetup;

    /*
     * Mocked for injection via the IntectionHelper
     */



    @Before
    public void baseTestBefore() {
        wireMockServer.resetAll();
        variables.put("gBuildingBlockExecution", execution);
    }

    @LocalServerPort
    private int port;

    protected String readFile(String path) throws IOException {
        return readFile(path, Charset.defaultCharset());
    }

    protected String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    protected String readJsonFileAsString(String fileLocation) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(new File(fileLocation));
        return jsonNode.asText();
    }

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

}
