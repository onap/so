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

package org.onap.so.apihandlerinfra;

import java.nio.file.Files;
import java.nio.file.Paths;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.github.tomakehurst.wiremock.WireMockServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiHandlerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Transactional
@AutoConfigureWireMock(port = 0)
public abstract class BaseTest {
    protected Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    @SpyBean
    protected RequestsDbClient requestsDbClient;

    @Autowired
    protected Environment env;

    @LocalServerPort
    private int port;

    @Autowired
    protected WireMockServer wireMockServer;

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    protected String createURLWithPort(String uri, String orchestrationPath) {
        return "http://localhost:" + port + orchestrationPath + uri;
    }

    protected String createURLWithPort(String uri, int iPort) {
        return "http://localhost:" + iPort + uri;
    }

    @After
    public void tearDown() {
        wireMockServer.resetAll();
    }

    public static String getResponseTemplate;
    public static String getResponseTemplateNoBody;
    public static String infraActivePost;

    @BeforeClass
    public static void setupTest() throws Exception {
        getResponseTemplate = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/InfraActiveRequests/getInfraActiveRequest.json")));
        getResponseTemplateNoBody = new String(Files.readAllBytes(
                Paths.get("src/test/resources/__files/InfraActiveRequests/getInfraActiveRequestNoBody.json")));
        infraActivePost = new String(Files.readAllBytes(
                Paths.get("src/test/resources/__files/InfraActiveRequests/createInfraActiveRequests.json")));
    }

    public String getTestUrl(String requestId) {
        return "/infraActiveRequests/" + requestId;
    }
}
