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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.Test;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.InstanceDirection;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelatedInstancesValidationTest extends BaseTest {

    RelatedInstancesValidation validation = new RelatedInstancesValidation();

    public ValidationInformation setupValidationInformation(String path) throws IOException {
        String jsonInput = new String(Files.readAllBytes(Paths.get(path)));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(jsonInput, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, null, Action.createInstance, 7, false,
                sir.getRequestDetails().getRequestParameters());

        info.setRequestScope("service");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        sir.getRequestDetails().getModelInfo().setModelCustomizationName("name");

        info.setRequestInfo(sir.getRequestDetails().getRequestInfo());
        return info;
    }

    @Test
    public void testCreateVnfNetworkInstanceGroup() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RelatedInstances/v7CreateVnfNetworkInstanceGroup.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 6, false, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("vnf");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        sir.getRequestDetails().getModelInfo().setModelCustomizationName("name");
        RelatedInstancesValidation validation = new RelatedInstancesValidation();
        validation.validate(info);

        assertEquals(info.getVnfType(), "Test/name");
    }

    @Test
    public void testCreateSIVpnBonding() throws IOException, ValidationException {
        String requestJson = new String(Files.readAllBytes(
                Paths.get("src/test/resources/MsoRequestTest/RelatedInstances/ServiceInstanceVpnBondingService.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(requestJson, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, new HashMap<String, String>(),
                Action.createInstance, 7, false, sir.getRequestDetails().getRequestParameters());
        info.setRequestScope("service");
        sir.setServiceInstanceId("0fd90c0c-0e3a-46e2-abb5-4c4820d5985b");
        sir.getRequestDetails().getModelInfo().setModelCustomizationName("name");
        RelatedInstancesValidation validation = new RelatedInstancesValidation();
        validation.validate(info);
        RelatedInstanceList[] instanceList = sir.getRequestDetails().getRelatedInstanceList();

        assertEquals(info.getRequestScope(), "service");
        assertEquals(instanceList[0].getRelatedInstance().getModelInfo().getModelType().toString(), "vpnBinding");
        assertEquals(instanceList[1].getRelatedInstance().getModelInfo().getModelType().toString(), "network");
    }

    @Test
    public void validateModelTypeExceptionTest() throws IOException {
        ValidationException e = assertThrows(ValidationException.class, () -> validation.validate(
                setupValidationInformation("src/test/resources/Validation/VpnBondingValidation/NoModelType.json")));
        assertTrue(e.getMessage().contains("No valid modelType in relatedInstance is specified"));
    }

    @Test
    public void validateInstanceNameVpnBindingExceptionTest() throws IOException {
        ValidationException e =
                assertThrows(ValidationException.class, () -> validation.validate(setupValidationInformation(
                        "src/test/resources/Validation/VpnBondingValidation/NoInstanceNameVpnBinding.json")));
        assertTrue(e.getMessage()
                .contains("No valid instanceName in relatedInstance for vpnBinding modelType is specified"));
    }

    @Test
    public void validateInstanceNameNetworkExceptionTest() throws IOException {
        ValidationException e =
                assertThrows(ValidationException.class, () -> validation.validate(setupValidationInformation(
                        "src/test/resources/Validation/VpnBondingValidation/NoInstanceNameNetwork.json")));
        assertTrue(
                e.getMessage().contains("No valid instanceName in relatedInstance for network modelType is specified"));
    }

    @Test
    public void validateInstanceIdExceptionTest() throws IOException {
        ValidationException e = assertThrows(ValidationException.class, () -> validation.validate(
                setupValidationInformation("src/test/resources/Validation/VpnBondingValidation/NoInstanceId.json")));
        assertTrue(e.getMessage().contains("No valid instanceId in relatedInstance is specified"));
    }

    @Test
    public void validatemodelInvariantIdExceptionTest() throws IOException {
        ValidationException e =
                assertThrows(ValidationException.class, () -> validation.validate(setupValidationInformation(
                        "src/test/resources/Validation/VpnBondingValidation/NoModelInvariantId.json")));
        assertTrue(e.getMessage().contains("No valid modelInvariantId in relatedInstance is specified"));
    }

    @Test
    public void validateRelatedInstanceInstanceIdTest() throws IOException, ValidationException {
        validation.validate(setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/RelatedInstanceInstanceId.json"));
    }

    @Test
    public void validateRelatedInstanceInstanceIdExceptionTest() throws IOException {
        ValidationException e = assertThrows(ValidationException.class, () -> {
            ValidationInformation info = setupValidationInformation(
                    "src/test/resources/Validation/UserParamsValidation/RelatedInstanceInstanceId.json");
            RelatedInstanceList[] instanceList = info.sir.getRequestDetails().getRelatedInstanceList();
            instanceList[0].getRelatedInstance().setInstanceDirection(InstanceDirection.fromValue("destination"));
            validation.validate(info);
        });
        assertTrue(e.getMessage().contains("serviceInstanceId matching the serviceInstanceId in request URI"));
    }
}
