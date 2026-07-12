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

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(classes = org.onap.so.simulator.SimulatorIntegrationTest.EndpointConfig.class)
public class SimulatorIntegrationTest extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("simulatorClient")
    private HttpClient apiClient;

    @CitrusTest
    public void thatRequestCanBeMade() {
        $(http().client(apiClient).send().get("/sim/v1/tenantOne/stacks/network_dummy_id/stackId"));
        $(http().client(apiClient).receive().response(HttpStatus.OK));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public String startEmbeddedSimulator() {
            SpringApplication.run(Simulator.class);
            return "simulator";
        }
    }
}
