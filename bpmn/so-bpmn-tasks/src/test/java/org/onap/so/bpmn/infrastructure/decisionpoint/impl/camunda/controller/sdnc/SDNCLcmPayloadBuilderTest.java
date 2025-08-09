/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.sdnc;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_PAYLOAD;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.common.SoPropertyConstants;
import org.onap.so.client.sdnc.lcm.beans.payload.ActivateNESwPayload;
import org.onap.so.client.sdnc.lcm.beans.payload.DownloadNESwPayload;
import org.onap.so.client.sdnc.lcm.beans.payload.UpgradePostCheckPayload;
import org.onap.so.client.sdnc.lcm.beans.payload.UpgradePreCheckPayload;

@RunWith(SpringJUnit4ClassRunner.class)
public class SDNCLcmPayloadBuilderTest {
    private static Logger logger = LoggerFactory.getLogger(SDNCLcmPayloadBuilderTest.class);

    protected static String payload = "{" + "\"ipaddressV4Oam\": \"192.168.1.10\"," + "\"oldSwVersion\": \"v1\","
            + "\"preCheckRuleName\": \"r101\"," + "\"preCheckAdditionalData\": \"{}\","
            + "\"preCheckPlaybook\": \"precheck_playbook\"," + "\"swToBeDownloaded\": \"[{"
            + "\\\"swLocation\\\": \\\"http://192.168.1.20/test.zip\\\"," + "\\\"swFileSize\\\": 123456,"
            + "\\\"swFileCompression\\\": \\\"ZIP\\\"," + "\\\"swFileFormat\\\": \\\"binary\\\"}]\","
            + "\"downloadNESwPlaybook\": \"downloadnesw_playbook\","
            + "\"activateNESwPlaybook\": \"activatenesw_playbook\"," + "\"postCheckRuleName\": \"r102\","
            + "\"postCheckAdditionalData\": \"{}\"," + "\"postCheckPlaybook\": \"postcheck_playbook\"" + "}";
    protected String targetSoftwareVersion = "v2";

    @MockBean
    private DelegateExecution execution;

    public static String getRequestPayload() {
        return payload;
    }

    @Before
    public void setUp() {
        when(execution.getVariable(REQUEST_PAYLOAD)).thenReturn(payload);
        when(execution.getVariable(SoPropertyConstants.TARGET_SOFTWARE_VERSION)).thenReturn(targetSoftwareVersion);
    }


    @Test
    public final void testBuildActivateNESwPayload() {
        String expectedPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
                + "\"playbook-name\":\"activatenesw_playbook\"," + "\"swVersionToBeActivated\":\"v2\"" + "}";

        ActivateNESwPayload activateNESwPayload = SDNCLcmPayloadBuilder.buildActivateNESwPayload(execution);
        try {
            String payload = SDNCLcmPayloadBuilder.convertToSting(activateNESwPayload);
            logger.debug("ActivateNESwPayload:\n{}", payload);

            assertEquals(expectedPayload, payload);
        } catch (JsonProcessingException e) {
            fail("Convert ActivateNESwPayload to String error: " + e.toString());
        }
    }

    @Test
    public final void testBuildDownloadNESwPayload() {
        String expectedSwToBeDownloadedElement = "{" + "\"swLocation\":\"http://192.168.1.20/test.zip\","
                + "\"swFileSize\":123456," + "\"swFileCompression\":\"ZIP\"," + "\"swFileFormat\":\"binary\"" + "}";
        String expectedPayload =
                "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\"," + "\"playbook-name\":\"downloadnesw_playbook\","
                        + "\"swToBeDownloaded\":[" + expectedSwToBeDownloadedElement + "]" + "}";

        DownloadNESwPayload downloadNESwPayload = SDNCLcmPayloadBuilder.buildDownloadNESwPayload(execution);
        try {
            String payload = SDNCLcmPayloadBuilder.convertToSting(downloadNESwPayload);
            logger.debug("DownloadNESwPayload:\n{}", payload);

            assertEquals(expectedPayload, payload);
        } catch (JsonProcessingException e) {
            fail("Convert DownloadNESwPayload to String error: " + e.toString());
        }
    }

    @Test
    public final void testBuildUpgradePostCheckPayload() {
        String expectedPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
                + "\"playbook-name\":\"postcheck_playbook\"," + "\"oldSwVersion\":\"v1\","
                + "\"targetSwVersion\":\"v2\"," + "\"ruleName\":\"r102\"," + "\"additionalData\":\"{}\"" + "}";

        UpgradePostCheckPayload upgradePostCheckPayload = SDNCLcmPayloadBuilder.buildUpgradePostCheckPayload(execution);
        try {
            String payload = SDNCLcmPayloadBuilder.convertToSting(upgradePostCheckPayload);
            logger.debug("UpgradePostCheckPayload:\n{}", payload);

            assertEquals(expectedPayload, payload);
        } catch (JsonProcessingException e) {
            fail("Convert UpgradePostCheckPayload to String error: " + e.toString());
        }
    }

    @Test
    public final void testBuildUpgradePreCheckPayload() {
        String expectedPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
                + "\"playbook-name\":\"precheck_playbook\"," + "\"oldSwVersion\":\"v1\","
                + "\"targetSwVersion\":\"v2\"," + "\"ruleName\":\"r101\"," + "\"additionalData\":\"{}\"" + "}";

        UpgradePreCheckPayload upgradePreCheckPayload = SDNCLcmPayloadBuilder.buildUpgradePreCheckPayload(execution);
        try {
            String payload = SDNCLcmPayloadBuilder.convertToSting(upgradePreCheckPayload);
            logger.debug("UpgradePreCheckPayload:\n{}", payload);

            assertEquals(expectedPayload, payload);
        } catch (JsonProcessingException e) {
            fail("Convert UpgradePreCheckPayload to String error: " + e.toString());
        }
    }
}
