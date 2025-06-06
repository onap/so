/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CatalogDbAdapterBaseTest {

    @LocalServerPort
    protected int port;

    @Test
    public void testNothing() {}

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        int port = SocketUtils.findAvailableTcpPort();
        registry.add("spring.datasource.url", () -> String.format("jdbc:mariadb://localhost:%s/catalogdb", port));
        registry.add("spring.flyway.url", () -> String.format("jdbc:mariadb://localhost:%s/catalogdb", port));
        registry.add("mariaDB4j.port", () -> port);
    }
}
