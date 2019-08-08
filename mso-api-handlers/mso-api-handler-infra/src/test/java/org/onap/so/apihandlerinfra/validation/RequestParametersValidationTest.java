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

package org.onap.so.apihandlerinfra.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequestParametersValidationTest extends BaseTest {

    private static final String SERVICE = "service";

    @Test
    public void testVfModuleWithFalseALaCarte() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleModelVersionId.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        sir.getRequestDetails().getRequestParameters().setUsePreload(null);

        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, false, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("vfModule");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);

        assertFalse(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertFalse(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testVfModuleWithNoALaCarte() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(Paths
                .get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleRequestParametersNoALaCarte.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, false, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("vfModule");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);

        assertTrue(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertTrue(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testVfModuleWithTrueALaCarte() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(Paths
                .get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleRequestParametersIsALaCarte.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        sir.getRequestDetails().getRequestParameters().setUsePreload(null);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, true, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("vfModule");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);

        assertTrue(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertTrue(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testVfModuleWithReqVersionBelow4() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleModelVersionId.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        sir.getRequestDetails().getRequestParameters().setUsePreload(null);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 3, false, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("vfModule");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);
        assertTrue(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertTrue(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testServiceWithFalseALaCarte() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleModelVersionId.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        sir.getRequestDetails().getRequestParameters().setUsePreload(null);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, false, sir.getRequestDetails().getRequestParameters());
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        info.setRequestScope("service");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);
        assertFalse(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertFalse(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testServiceWithNoALaCarte() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(Paths
                .get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleRequestParametersNoALaCarte.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, false, sir.getRequestDetails().getRequestParameters());
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        sir.getRequestDetails().getRequestParameters().setSubscriptionServiceType("subscriptionServiceType");
        info.setRequestScope("service");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);
        assertFalse(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertFalse(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testServiceWithTrueALaCarte() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(Paths
                .get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleRequestParametersIsALaCarte.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        sir.getRequestDetails().getRequestParameters().setUsePreload(null);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, true, sir.getRequestDetails().getRequestParameters());
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        info.setRequestScope("service");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);
        assertTrue(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertTrue(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testServiceWithReqVersionBelow4() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleModelVersionId.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        sir.getRequestDetails().getRequestParameters().setUsePreload(null);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 3, false, sir.getRequestDetails().getRequestParameters());
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        info.setRequestScope("service");
        RequestParametersValidation validation = new RequestParametersValidation();
        validation.validate(info);
        assertTrue(sir.getRequestDetails().getRequestParameters().getUsePreload());
        assertTrue(info.getReqParameters().getUsePreload());
    }

    @Test
    public void testServiceWithVnfInstanceParams() throws IOException {
        List<Map<String, String>> expectedListOfUserParameters = new ArrayList<>();
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RequestParameters/VfModuleModelVersionId.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);

        Optional<Map<String, Object>> requestDetails = sir.getRequestDetails()
                .getRequestParameters()
                .getUserParams()
                .stream()
                .filter(x -> x.containsKey(SERVICE))
                .findFirst();

        ObjectMapper objectMapper = new ObjectMapper();
        String input = objectMapper.writeValueAsString(requestDetails.get().get(SERVICE));
        Service service = mapper.readValue(input, Service.class);

        Map<String, String> expectedMapOfUserParameters = new HashMap<>();

        expectedMapOfUserParameters.put("instanceName", "someVnfInstanceName");

        expectedListOfUserParameters.add(expectedMapOfUserParameters);

        List<Map<String, String>> actualListOfUserParameters = service.getResources().getVnfs().stream().findFirst().get().getInstanceParams();

        assertEquals(actualListOfUserParameters, expectedListOfUserParameters);
    }
}
