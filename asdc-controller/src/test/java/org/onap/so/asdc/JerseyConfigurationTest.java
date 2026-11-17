/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.Test;
import org.onap.so.asdc.client.ASDCController;
import org.onap.so.asdc.client.test.rest.ASDCRestInterface;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class JerseyConfigurationTest {

    @Test
    public void testEndpointIsNotRegisteredWithoutTestProfile() throws Exception {
        assertThat(postStatusData("basic")).isEqualTo(404);
    }

    @Test
    public void testEndpointIsRegisteredWithTestProfile() throws Exception {
        assertThat(postStatusData("test")).isEqualTo(204);
    }

    private int postStatusData(final String profile) throws Exception {
        try (ConfigurableApplicationContext context =
                new SpringApplicationBuilder(JerseyTestConfig.class).web(WebApplicationType.SERVLET).profiles(profile)
                        .properties("server.port=0").initializers(JerseyConfigurationTest::registerMocks).run()) {
            final String port = context.getEnvironment().getProperty("local.server.port");
            final HttpRequest request =
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/test/statusData/v1"))
                            .POST(HttpRequest.BodyPublishers.ofString("{}")).build();
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        }
    }

    // registerSingleton skips bean post-processing, so the @Autowired fields of the mocked classes stay unresolved
    private static void registerMocks(final ConfigurableApplicationContext context) {
        context.getBeanFactory().registerSingleton("asdcController", mock(ASDCController.class));
        context.getBeanFactory().registerSingleton("toscaResourceInstaller", mock(ToscaResourceInstaller.class));
    }

    @Configuration
    @ImportAutoConfiguration({ServletWebServerFactoryAutoConfiguration.class, JerseyAutoConfiguration.class})
    @Import({JerseyConfiguration.class, ASDCRestInterface.class})
    static class JerseyTestConfig {
    }
}
