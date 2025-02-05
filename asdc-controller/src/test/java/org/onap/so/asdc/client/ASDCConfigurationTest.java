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

package org.onap.so.asdc.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * THis class tests the ASDC Controller by using the ASDC Mock CLient
 *
 *
 */
public class ASDCConfigurationTest extends BaseTest {

    @Autowired
    private ASDCConfiguration config;

    @Test
    @Transactional
    public void testInitASDCConfiguration() {
        assertEquals("msopreist", config.getUser());
        assertEquals("msoasdc-id-local", config.getConsumerGroup());
        assertEquals("msoasdc-id-local", config.getConsumerID());
        assertEquals("Pre-IST", config.getEnvironmentName());
        assertEquals("localhost:8443", config.getSdcAddress());
        assertEquals("msopreist", config.getPassword());
        assertEquals(30, config.getPollingInterval());
        assertEquals(30, config.getPollingTimeout());
        assertEquals(config.getRelevantArtifactTypes().size(), ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST.size());
        assertEquals(1, config.getWatchDogTimeout());
        assertTrue(config.isUseHttpsWithSDC());
    }

}
