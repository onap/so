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

package org.onap.so.asdc;

import static org.mockito.Mockito.reset;
import org.junit.After;
import org.junit.runner.RunWith;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.github.tomakehurst.wiremock.WireMockServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = SpringContextHelper.class, initializers = ConfigDataApplicationContextInitializer.class)
@AutoConfigureWireMock(port = 0)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseTest {
    @MockBean
    protected VfResourceStructure vfResourceStructure;
    @MockBean
    protected ToscaResourceStructure toscaResourceStruct;
    @SpyBean
    protected WatchdogDistribution watchdogDistributionSpy;
    @SpyBean
    protected ToscaResourceInstaller toscaInstaller;
    @Autowired
    protected WireMockServer wireMockServer;

    @Value("${wiremock.server.port}")
    protected String wireMockPort;

    @After
    public void after() {
        reset(vfResourceStructure);
        reset(toscaResourceStruct);
    }
}
