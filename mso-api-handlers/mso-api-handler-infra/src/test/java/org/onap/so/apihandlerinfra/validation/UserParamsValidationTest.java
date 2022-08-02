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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.serviceinstancebeans.VfModules;

public class UserParamsValidationTest {

    UserParamsValidation validation = new UserParamsValidation();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public ValidationInformation setupValidationInformation(String path) throws IOException {
        String jsonInput = new String(Files.readAllBytes(Paths.get(path)));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(jsonInput, ServiceInstancesRequest.class);
        ValidationInformation info = new ValidationInformation(sir, null, Action.createInstance, 7, false,
                sir.getRequestDetails().getRequestParameters());
        for (Map<String, Object> params : sir.getRequestDetails().getRequestParameters().getUserParams()) {
            ObjectMapper obj = new ObjectMapper();
            String input = obj.writeValueAsString(params.get("service"));
            Service validate = obj.readValue(input, Service.class);
            info.setUserParams(validate);
            break;
        }
        info.setRequestInfo(sir.getRequestDetails().getRequestInfo());
        return info;
    }

    @Test
    public void validateModelTypeExceptionTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid modelType in userParams service modelInfo is specified");
        validation.validate(setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/ModelInfoNoModelType.json"));
    }

    @Test
    public void validateDuplicateInstanceNameDifferentCustomizationIdVnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName but different modelCustomizationId (instanceName should be unique)  in userParams vnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(0).setInstanceName("UbuntuVNF2");
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameSameCustomizationIdVnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams vnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(2).setInstanceName("UbuntuVNF2");
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameSameCustomizationIdVnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: instanceName is missing or empty with same modelCustomizationId in userParams vnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(1).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateDuplicateNullInstanceNameSameCustomizationIdVnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams vnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(1).setInstanceName(null);
        info.getUserParams().getResources().getVnfs().get(2).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateDifferentInstanceNameSameCustomizationIdVnfTest() throws IOException, ValidationException {
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameDifferentCustomizationIdVnfTest() throws IOException, ValidationException {
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(0).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameDifferentCustomizationIdVfModuleInOneVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName but different modelCustomizationId (instanceName should be unique)  in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(2).getVfModules().get(2).setInstanceName("lcm-demo-ubuntu-3");
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameDifferentCustomizationIdVfModuleInMultipleVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName but different modelCustomizationId (instanceName should be unique)  in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(0).getVfModules().get(0).setInstanceName("lcm-demo-ubuntu-3");
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameSameCustomizationIdVfModuleInOneVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(2).getVfModules().get(1).setInstanceName("lcm-demo-ubuntu-2");
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameSameCustomizationIdVfModuleInMultipleVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        VfModules vfModule1 = info.getUserParams().getResources().getVnfs().get(0).getVfModules().get(0);
        VfModules vfModule2 = info.getUserParams().getResources().getVnfs().get(1).getVfModules().get(0);
        vfModule2.setInstanceName(vfModule1.getInstanceName());
        vfModule2.getModelInfo().setModelCustomizationId(vfModule1.getModelInfo().getModelCustomizationId());
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameSameCustomizationIdVfModuleInOneVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: instanceName is missing or empty with same modelCustomizationId in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        info.getUserParams().getResources().getVnfs().get(2).getVfModules().get(1).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameSameCustomizationIdVfModuleInMultipleVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: instanceName is missing or empty with same modelCustomizationId in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        VfModules vfModule1 = info.getUserParams().getResources().getVnfs().get(0).getVfModules().get(0);
        VfModules vfModule2 = info.getUserParams().getResources().getVnfs().get(1).getVfModules().get(0);
        vfModule2.setInstanceName(null);
        vfModule2.getModelInfo().setModelCustomizationId(vfModule1.getModelInfo().getModelCustomizationId());
        validation.validate(info);
    }

    @Test
    public void validateDuplicateNullInstanceNameSameCustomizationIdVfModuleInMultipleVnfTest()
            throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams vfModule resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        VfModules vfModule1 = info.getUserParams().getResources().getVnfs().get(0).getVfModules().get(0);
        VfModules vfModule2 = info.getUserParams().getResources().getVnfs().get(1).getVfModules().get(0);
        vfModule1.setInstanceName(null);
        vfModule2.setInstanceName(null);
        vfModule2.getModelInfo().setModelCustomizationId(vfModule1.getModelInfo().getModelCustomizationId());
        validation.validate(info);
    }

    @Test
    public void validateInstanceNameSameCustomizationIdVfModuleInVnfTest() throws IOException, ValidationException {
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        VfModules vfModule1 = info.getUserParams().getResources().getVnfs().get(0).getVfModules().get(0);
        VfModules vfModule2 = info.getUserParams().getResources().getVnfs().get(1).getVfModules().get(0);
        vfModule2.getModelInfo().setModelCustomizationId(vfModule1.getModelInfo().getModelCustomizationId());
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameDifferentCustomizationIdVfModuleInVnfTest()
            throws IOException, ValidationException {
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNames.json");
        VfModules vfModule1 = info.getUserParams().getResources().getVnfs().get(0).getVfModules().get(0);
        vfModule1.setInstanceName(null);
        VfModules vfModule2 = info.getUserParams().getResources().getVnfs().get(2).getVfModules().get(2);
        vfModule2.setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameDifferentCustomizationIdPnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName but different modelCustomizationId (instanceName should be unique)  in userParams pnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNamesPnf.json");
        info.getUserParams().getResources().getPnfs().get(0).setInstanceName("ORAN_SIM1_2106_pnf_01");
        validation.validate(info);
    }

    @Test
    public void validateDuplicateInstanceNameSameCustomizationIdPnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams pnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNamesPnf.json");
        info.getUserParams().getResources().getPnfs().get(1).getModelInfo()
                .setModelCustomizationId("88a3096a-af87-4853-99f6-7256a9ab6c3e");
        info.getUserParams().getResources().getPnfs().get(1).setInstanceName("ORAN_SIM1_2106_pnf_01");
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameSameCustomizationIdPnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: instanceName is missing or empty with same modelCustomizationId in userParams pnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNamesPnf.json");
        info.getUserParams().getResources().getPnfs().get(1).getModelInfo()
                .setModelCustomizationId("88a3096a-af87-4853-99f6-7256a9ab6c3e");
        info.getUserParams().getResources().getPnfs().get(1).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateDuplicateNullInstanceNameSameCustomizationIdPnfTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "No valid instanceName: same instanceName with same modelCustomizationId in userParams pnf resources is specified");
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNamesPnf.json");
        info.getUserParams().getResources().getPnfs().get(0).setInstanceName(null);
        info.getUserParams().getResources().getPnfs().get(1).getModelInfo()
                .setModelCustomizationId("88a3096a-af87-4853-99f6-7256a9ab6c3e");
        info.getUserParams().getResources().getPnfs().get(1).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateDifferentInstanceNameSameCustomizationIdPnfTest() throws IOException, ValidationException {
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNamesPnf.json");
        info.getUserParams().getResources().getPnfs().get(1).getModelInfo()
                .setModelCustomizationId("88a3096a-af87-4853-99f6-7256a9ab6c3e");
        info.getUserParams().getResources().getPnfs().get(1).setInstanceName("new-pnf-instance-name");
        validation.validate(info);
    }

    @Test
    public void validateNullInstanceNameDifferentCustomizationIdPnfTest() throws IOException, ValidationException {
        ValidationInformation info = setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/DuplicateInstanceNamesPnf.json");
        info.getUserParams().getResources().getPnfs().get(0).setInstanceName(null);
        validation.validate(info);
    }

    @Test
    public void validateInstanceNameExceptionTest() throws IOException, ValidationException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("instanceName in requestInfo does not match instanceName in userParams service");
        validation.validate(
                setupValidationInformation("src/test/resources/Validation/UserParamsValidation/MacroRequest.json"));
    }

    @Test
    public void validateModelTypeTest() throws ValidationException, IOException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("modelType in modelInfo does not match modelType in userParams service");
        validation.validate(
                setupValidationInformation("src/test/resources/Validation/UserParamsValidation/ModelType.json"));
    }

    @Test
    public void validateModelInvariantIdTest() throws ValidationException, IOException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("modelInvariantId in modelInfo does not match modelInvariantId in userParams service");
        validation.validate(
                setupValidationInformation("src/test/resources/Validation/UserParamsValidation/ModelInvariantId.json"));
    }

    @Test
    public void validateModelVersionIdTest() throws ValidationException, IOException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("modelVersionId in modelInfo does not match modelVersionId in userParams service");
        validation.validate(
                setupValidationInformation("src/test/resources/Validation/UserParamsValidation/ModelVersionId.json"));
    }

    @Test
    public void validateModelNameTest() throws ValidationException, IOException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("modelName in modelInfo does not match modelName in userParams service");
        validation.validate(
                setupValidationInformation("src/test/resources/Validation/UserParamsValidation/ModelName.json"));
    }

    @Test
    public void validateModelVersionTest() throws ValidationException, IOException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("modelVersion in modelInfo does not match modelVersion in userParams service");
        validation.validate(
                setupValidationInformation("src/test/resources/Validation/UserParamsValidation/ModelVersion.json"));
    }

    @Test
    public void validateModelCustomizationIdTest() throws ValidationException, IOException {
        thrown.expect(ValidationException.class);
        thrown.expectMessage(
                "modelCustomizationId in modelInfo does not match modelCustomizationId in userParams service");
        validation.validate(setupValidationInformation(
                "src/test/resources/Validation/UserParamsValidation/ModelCustomizationId.json"));
    }
}
