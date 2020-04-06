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



import static org.junit.Assert.assertTrue;
import javax.transaction.Transactional;
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
    public void testInitASDCConfiguration() throws Exception {
        assertTrue("msopreist".equals(config.getUser()));
        assertTrue("msoasdc-id-local".equals(config.getConsumerGroup()));
        assertTrue("msoasdc-id-local".equals(config.getConsumerID()));
        assertTrue("Pre-IST".equals(config.getEnvironmentName()));
        assertTrue("localhost:8443".equals(config.getAsdcAddress()));
        assertTrue("msopreist".equals(config.getPassword()));
        assertTrue(config.getPollingInterval() == 30);
        assertTrue(config.getPollingTimeout() == 30);
        assertTrue(config.getRelevantArtifactTypes().size() == config.SUPPORTED_ARTIFACT_TYPES_LIST.size());
        assertTrue(config.getWatchDogTimeout() == 1);
        assertTrue(config.isUseHttpsWithDmaap() == true);
        assertTrue(config.isUseHttpsWithSDC() == true);
    }

}
