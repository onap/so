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

import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.buildingblock.OofHomingV2;
import org.onap.so.bpmn.buildingblock.SniroHomingV2;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.oof.OofClient;
import org.onap.so.client.orchestration.SDNOHealthCheckResources;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sniro.SniroClient;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.github.tomakehurst.wiremock.WireMockServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureWireMock(port = 0)
public abstract class BaseIntegrationTest extends TestDataSetup {

    @Value("${wiremock.server.port}")
    protected String wireMockPort;

    @SpyBean
    protected SDNCClient SPY_sdncClient;

    @SpyBean
    protected SDNOHealthCheckResources MOCK_sdnoHealthCheckResources;

    @SpyBean
    protected SniroHomingV2 sniroHoming;

    @SpyBean
    protected SniroClient sniroClient;

    @SpyBean
    protected OofHomingV2 oofHoming;

    @SpyBean
    protected OofClient oofClient;

    @MockBean
    protected ApplicationControllerAction appCClient;

    @MockBean
    protected CatalogDbClient catalogDbClient;

    @Autowired
    protected WireMockServer wireMockServer;

    @Before
    public void baseTestBefore() {
        wireMockServer.resetAll();
    }
<<<<<<< HEAD

=======
>>>>>>> Refactor OofHomingV2
    public String readResourceFile(String fileName) {
        InputStream stream;
        try {
            stream = getResourceAsStream(fileName);
            byte[] bytes;
            bytes = new byte[stream.available()];
<<<<<<< HEAD
            if (stream.read(bytes) > 0) {
=======
            if(stream.read(bytes) > 0) {
>>>>>>> Refactor OofHomingV2
                stream.close();
                return new String(bytes);
            } else {
                stream.close();
                return "";
            }
        } catch (IOException e) {
            return "";
        }
    }

<<<<<<< HEAD
    private InputStream getResourceAsStream(String resourceName) throws IOException {
        InputStream stream = FileUtil.class.getClassLoader().getResourceAsStream(resourceName);
=======
    private  InputStream getResourceAsStream(String resourceName) throws IOException {
        InputStream stream =
                FileUtil.class.getClassLoader().getResourceAsStream(resourceName);
>>>>>>> Refactor OofHomingV2
        if (stream == null) {
            throw new IOException("Can't access resource '" + resourceName + "'");
        }
        return stream;
    }
}


