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

package org.onap.so.apihandlerinfra.tenantisolation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.exceptions.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TenantIsolationRequestTest extends BaseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testParseCloudResourceECOMP() throws Exception {
        try {
            String requestJSON = new String(Files.readAllBytes(
                    Paths.get("src/test/resources/TenantIsolation/ECOMPOperationEnvironmentCreate.json")));
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> instanceIdMap = new HashMap<String, String>();
            CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
            TenantIsolationRequest request = new TenantIsolationRequest("1234");
            request.parse(cor, instanceIdMap, Action.create);
            assertNotNull(request.getRequestId());
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testParseCloudResourceVNF() throws Exception {
        try {
            String requestJSON = new String(Files
                    .readAllBytes(Paths.get("src/test/resources/TenantIsolation/VNFOperationEnvironmentCreate.json")));
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> instanceIdMap = new HashMap<String, String>();
            CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
            TenantIsolationRequest request = new TenantIsolationRequest("1234");
            request.parse(cor, instanceIdMap, Action.create);
            assertNotNull(request.getRequestId());
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testParseCloudResourceVNFInvalid() throws Exception {
        expectedException.expect(ValidationException.class);

        String requestJSON = new String(Files.readAllBytes(
                Paths.get("src/test/resources/TenantIsolation/VNFOperationEnvironmentCreateInvalid.json")));
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> instanceIdMap = new HashMap<String, String>();
        CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
        TenantIsolationRequest request = new TenantIsolationRequest("1234");
        request.parse(cor, instanceIdMap, Action.create);
        assertNotNull(request.getRequestId());
    }

    @Test
    public void testParseActivateCloudResource() throws Exception {
        try {
            String requestJSON = new String(Files
                    .readAllBytes(Paths.get("src/test/resources/TenantIsolation/ActivateOperationEnvironment.json")));
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> instanceIdMap = new HashMap<String, String>();
            CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
            TenantIsolationRequest request = new TenantIsolationRequest("1234");
            request.parse(cor, instanceIdMap, Action.activate);
            assertNotNull(request.getRequestId());
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testParseActivateCloudResourceInvalid() throws Exception {
        expectedException.expect(ValidationException.class);

        String requestJSON = new String(Files.readAllBytes(
                Paths.get("src/test/resources/TenantIsolation/ActivateOperationEnvironmentInvalid.json")));
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> instanceIdMap = new HashMap<String, String>();
        CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
        TenantIsolationRequest request = new TenantIsolationRequest("1234");
        request.parse(cor, instanceIdMap, Action.activate);
        assertNotNull(request.getRequestId());
    }

    @Test
    public void testParseDeactivateCloudResource() throws Exception {
        try {
            String requestJSON = new String(Files
                    .readAllBytes(Paths.get("src/test/resources/TenantIsolation/DeactivateOperationEnvironment.json")));
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> instanceIdMap = new HashMap<String, String>();
            CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
            TenantIsolationRequest request = new TenantIsolationRequest("1234");
            request.parse(cor, instanceIdMap, Action.deactivate);
            assertNotNull(request.getRequestId());
        } catch (ValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testParseDeactivateCloudResourceInvalid() throws Exception {
        expectedException.expect(ValidationException.class);

        String requestJSON = new String(Files.readAllBytes(
                Paths.get("src/test/resources/TenantIsolation/DeactivateOperationEnvironmentInvalid.json")));
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> instanceIdMap = new HashMap<String, String>();
        CloudOrchestrationRequest cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
        TenantIsolationRequest request = new TenantIsolationRequest("1234");
        request.parse(cor, instanceIdMap, Action.deactivate);
        assertNotNull(request.getRequestId());
    }
}
