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

package org.onap.so.apihandlerinfra.tenantisolation.dmaap;

import static org.junit.Assert.assertEquals;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;


public class OperationalEnvironmentPublisherTest extends BaseTest {

    @Autowired
    private OperationalEnvironmentPublisher publisher;

    @Test
    public void getProperties() throws FileNotFoundException, IOException {

        assertEquals(
                "B3705D6C2D521257CC2422ACCF03B001811ACC49F564DDB3A2CF2A1378B6D35A23CDCB696F2E1EDFBE6758DFE7C74B94F4A7DF84A0E2BB904935AC4D900D5597DF981ADE6CE1FF3AF993BED0",
                publisher.getAuth());
        assertEquals("07a7159d3bf51a0e53be7a8f89699be7", publisher.getKey());
        assertEquals("test.operationalEnvironmentEvent", publisher.getTopic());
        assertEquals("http://localhost:" + env.getProperty("wiremock.server.port"), publisher.getHost().get());
    }
}
