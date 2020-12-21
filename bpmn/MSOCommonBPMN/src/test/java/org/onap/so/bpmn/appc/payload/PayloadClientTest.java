/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.appc.payload;

import static org.junit.Assert.assertEquals;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.Test;

public class PayloadClientTest {

    @Test
    public void upgradeFormatTest() throws Exception {
        String payloadResult =
                "{\"configuration-parameters\":{\"vnf_name\":\"vnfName1\",\"existing_software_version\":\"existingVersion\",\"new_software_version\":\"newVersion\"}}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("existing_software_version", "existingVersion");
        jsonObject.put("new_software_version", "newVersion");
        Optional<String> payload = Optional.of(jsonObject.toString());
        Optional<String> payloadClient = PayloadClient.upgradeFormat(payload, "vnfName1");
        assertEquals(payloadResult, payloadClient.get());
    }

    @Test
    public void resumeTrafficFormatTest() throws Exception {
        String payloadResult = "{\"configuration-parameters\":{\"vnf_name\":\"vnfName1\"}}";
        Optional<String> payloadClient = PayloadClient.resumeTrafficFormat("vnfName1");
        assertEquals(payloadResult, payloadClient.get());
    }

    @Test
    public void quiesceTrafficFormatTest() throws Exception {
        String payloadResult =
                "{\"configuration-parameters\":{\"vnf_name\":\"vnfName1\",\"operations_timeout\":\"operationTimeout\"}}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operations_timeout", "operationTimeout");
        Optional<String> payload = Optional.of(jsonObject.toString());
        Optional<String> payloadClient = PayloadClient.quiesceTrafficFormat(payload, "vnfName1");
        assertEquals(payloadResult, payloadClient.get());
    }

    @Test
    public void startStopFormatTest() throws Exception {
        String payloadResult = "{\" AICIdentity \":\"aicIdentity1\"}";
        Optional<String> payloadClient = PayloadClient.startStopFormat("aicIdentity1");
        assertEquals(payloadResult, payloadClient.get());
    }

    @Test
    public void healthCheckFormatTest() throws Exception {
        String payloadResult =
                "{\"request-parameters\":{\"host-ip-address\":\"hostIpAddress1\"},\"configuration-parameters\":{\"vnf_name\":\"vnfName1\"}}";
        Optional<String> payloadClient = PayloadClient.healthCheckFormat("vnfName1", "hostIpAddress1");
        assertEquals(payloadResult, payloadClient.get());
    }

    @Test
    public void snapshotFormatTest() throws Exception {
        String payloadResult = "{\"vm-id\":\"vmId1\",\"identity-url\":\"identityUrl1\"}";
        Optional<String> payloadClient = PayloadClient.snapshotFormat("vmId1", "identityUrl1");
        assertEquals(payloadResult, payloadClient.get());
    }

}
