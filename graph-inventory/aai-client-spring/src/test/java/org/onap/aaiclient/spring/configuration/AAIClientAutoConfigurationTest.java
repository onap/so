/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.aaiclient.spring.configuration;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.spring.AAIResourcesClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

public class AAIClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(RestTemplateAutoConfiguration.class, AAIClientAutoConfiguration.class));

    @Test
    void clientExists() {
        this.contextRunner.withPropertyValues("aai.endpoint=http://localhost:1234")
                .run((context) -> assertThat(context).hasSingleBean(AAIResourcesClient.class));
    }

    @Test
    void settingsAdded() {
        String aaiEndpoint = "http://example.com:1234";
        String aaiAuth =
                "2A11B07DB6214A839394AA1EC5844695F5114FC407FF5422625FB00175A3DCB8A1FF745F22867EFA72D5369D599BBD88DA8BED4233CF5586";
        String msoKey = "07a7159d3bf51a0e53be7a8f89699be7";
        this.contextRunner.withPropertyValues("aai.endpoint=" + aaiEndpoint, "aai.auth=" + aaiAuth, "aai.key=" + msoKey)
                .run((context) -> {
                    AAIConfigurationProperties bean = context.getBean(AAIConfigurationProperties.class);
                    assertEquals(aaiEndpoint, bean.getEndpoint().toString());
                    assertEquals(aaiAuth, bean.getAuth());
                });
    }

}
