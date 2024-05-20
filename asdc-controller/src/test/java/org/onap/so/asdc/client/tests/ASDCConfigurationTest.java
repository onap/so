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

package org.onap.so.asdc.client.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class tests the ASDC Controller by using the ASDC Mock CLient
 * 
 *
 */
@Transactional
public class ASDCConfigurationTest extends BaseTest {

    @Autowired
    private ASDCConfiguration config;

    private final List<String> msgBusAddressList = new ArrayList<>();

    private static final String MSO_PRE_IST = "msopreist";
    private static final String MSO_ASDC_ID_LOCAL = "msoasdc-id-local";
    private static final String PRE_IST = "Pre-IST";
    private static final String ASDC_ADDRESS = "localhost:8443";

    @Test
    public void isConsumeProduceStatusTopicTest() {
        assertTrue(config.isConsumeProduceStatusTopic());
    }

    @Test
    public void getUserTest() {
        assertEquals(MSO_PRE_IST, config.getUser());
    }

    @Test
    public void getConsumerGroupTest() {
        assertEquals(MSO_ASDC_ID_LOCAL, config.getConsumerGroup());
    }

    @Test
    public void getConsumerIDTest() {
        assertEquals(MSO_ASDC_ID_LOCAL, config.getConsumerID());
    }

    @Test
    public void getEnvironmentNameTest() {
        assertEquals(PRE_IST, config.getEnvironmentName());
    }

    @Test
    public void getAsdcAddress() {
        assertEquals(ASDC_ADDRESS, config.getSdcAddress());
    }

    @Test
    public void getPasswordTest() {
        assertEquals(MSO_PRE_IST, config.getPassword());
    }

    @Test
    public void getPollingIntervalTest() {
        assertEquals(30, config.getPollingInterval());
    }

    @Test
    public void getPollingTimeoutTest() {
        assertEquals(30, config.getPollingTimeout());
    }

    @Test
    public void getRelevantArtifactTypesTest() {
        assertEquals(config.getRelevantArtifactTypes().size(), ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST.size());
    }

    @Test
    public void getWatchDogTimeoutTest() {
        assertEquals(1, config.getWatchDogTimeout());
    }

    @Test
    public void activateServerTLSAuthTest() {
        assertFalse(config.activateServerTLSAuth());
    }

    @Test
    public void getKeyStorePasswordTest() {
        assertNull(config.getKeyStorePassword());
    }

    @Test
    public void getKeyStorePathTest() {
        assertNull(config.getKeyStorePath());
    }

    @Test
    public void isFilterInEmptyResourcesTest() {
        assertTrue(config.isFilterInEmptyResources());
    }

    @Test
    public void setGetAsdcControllerNameTest() {
        String asdcControllerName = "testAsdcControllerName";
        config.setAsdcControllerName(asdcControllerName);
        String actualAsdcControllerName = config.getAsdcControllerName();
        assertEquals(asdcControllerName, actualAsdcControllerName);
    }
}
