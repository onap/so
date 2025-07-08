/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom.
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
package org.onap.so.simulator;

import static org.junit.Assert.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeSuiteSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;

@Test
@ContextConfiguration(classes = org.onap.so.simulator.SimulatorIntegrationTest.EndpointConfig.class)
public class SimulatorIntegrationTest extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("simulatorClient")
    private HttpClient apiClient;

    @CitrusTest
    public void thatRequestCanBeMade() {
        http().client(apiClient).send().get("/sim/v1/tenantOne/stacks/network_dummy_id/stackId");
        http().client(apiClient).receive().response(HttpStatus.OK);
        assertTrue(true);
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public TestRunnerBeforeSuiteSupport startEmbeddedSimulator() {
            return new TestRunnerBeforeSuiteSupport() {
                @Override
                public void beforeSuite(TestRunner runner) {
                    SpringApplication.run(Simulator.class);
                }
            };
        }
    }
}
