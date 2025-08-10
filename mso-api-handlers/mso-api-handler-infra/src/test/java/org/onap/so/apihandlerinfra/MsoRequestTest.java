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

package org.onap.so.apihandlerinfra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.Service;
import org.onap.aai.domain.yang.Tenant;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.infra.rest.AAIDataRetrieval;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class MsoRequestTest extends BaseTest {
    private ObjectMapper mapper = new ObjectMapper();
    private HashMap<String, String> instanceIdMapTest = new HashMap<String, String>();
    private ServiceInstancesRequest sir;
    private MsoRequest msoRequest;
    private Action action;
    private String version;
    private int reqVersion;
    private String originalRequestJSON;
    private String requestJSON;
    private boolean expected;
    private String expectedException;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private AAIDataRetrieval aaiDataRet;

    @InjectMocks
    private MsoRequest msoRequestMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/MsoRequestTest" + JsonInput;
        String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
        return input;
    }

    public String inputStreamVpnBonding(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/Validation" + JsonInput;
        String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
        return input;
    }

    // Tests for successful validation of incoming JSON requests through the parse method
    @Test
    @Parameters(method = "successParameters")
    public void successTest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest, Action action,
            int reqVersion) throws ValidationException, IOException {
        this.sir = sir;
        this.instanceIdMapTest = instanceIdMapTest;
        this.action = action;
        this.reqVersion = reqVersion;
        this.version = "v" + reqVersion;
        this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("vfModuleInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("volumeGroupInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("networkInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("configurationInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Parameters
    private Collection<Object[]> successParameters() throws JsonMappingException, IOException {
        return Arrays.asList(new Object[][] {
                {mapper.readValue(inputStream("/CloudConfiguration/EmptyCloudConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "3"},
                {mapper.readValue(inputStream("/ProjectAndOwningEntity/EmptyOwningEntityName.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "5"},
                {mapper.readValue(inputStream("/PlatformAndLineOfBusiness/OptionalLineOfBusiness.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "6"},
                {mapper.readValue(inputStream("/ProjectAndOwningEntity/OptionalProject.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "6"},
                {mapper.readValue(inputStream("/PlatformAndLineOfBusiness/PlatformTest.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "5"},
                {mapper.readValue(inputStream("/RequestInfo/EmptyRequestorId.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, "3"},
                {mapper.readValue(inputStream("/RequestInfo/ServiceProductFamilyIdFlag.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "5"},
                {mapper.readValue(inputStream("/RequestInfo/ServiceProductFamilyIdUpdate.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "5"},
                {mapper.readValue(inputStream("/ModelInfo/ModelCustomizationIdUsingPreload.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "5"},
                {mapper.readValue(inputStream("/ModelInfo/ModelInvariantIdConfigurationDelete.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "3"},
                {mapper.readValue(inputStream("/ModelInfo/ModelInvariantIdService.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "4"},
                {mapper.readValue(inputStream("/ModelInfo/ModelVersionNetwork.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deleteInstance, "3"},
                {mapper.readValue(inputStream("/ModelInfo/VnfModelCustomizationIdEmpty.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "5"},
                {mapper.readValue(inputStream("/ModelInfo/VnfModelCustomizationNameNull.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "5"},
                {mapper.readValue(inputStream("/ModelInfo/VnfModelCustomizationTest.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "5"},
                {mapper.readValue(inputStream("/ModelInfo/ServiceModelNameEmptyOnActivate.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "2"},
                {mapper.readValue(inputStream("/ModelInfo/ServiceModelNameEmptyOnDelete.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "2"},
                {mapper.readValue(inputStream("/ModelInfo/ValidModelCustomizationId.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "4"},
                {mapper.readValue(inputStream("/ModelInfo/ValidModelCustomizationId.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "5"},
                {mapper.readValue(inputStream("/ModelInfo/ValidModelCustomizationIdService.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "5"},
                {mapper.readValue(inputStream("/RelatedInstances/RelatedInstances.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "5"},
                {mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelInvariantId.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "5"},
                {mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesVfModule.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/ServiceInPlaceSoftwareUpdate.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "6"},
                {mapper.readValue(inputStream("/SuccessfulValidation/ServiceInPlaceSoftwareUpdate.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.applyUpdatedConfig, "6"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v5EnableService.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/VnfActivate.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, "6"},
                {mapper.readValue(inputStream("/RequestParameters/VnfRequestParameters.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "6"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v3DeleteNetwork.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "3"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v3UpdateNetwork.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "3"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v5CreateConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v6CreateConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "6"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v5EnableConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v5EnableConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.disablePort, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v5ActivateConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "5"},
                {mapper.readValue(inputStream("/RelatedInstances/v5ActivateNoRelatedInstance.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v5DeactivateConfiguration.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "5"},
                {mapper.readValue(inputStream("/RelatedInstances/v5DeactivateNoRelatedInstance.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/v6AddRelationships.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.addRelationships, "6"},
                {mapper.readValue(inputStream("/SuccessfulValidation/UserParams.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addRelationships, "5"},
                {mapper.readValue(inputStream("/SuccessfulValidation/ServiceAssign.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.assignInstance, "7"},
                {mapper.readValue(inputStream("/RelatedInstances/ServiceInstanceVpnBondingService.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "7"},
                {mapper.readValue(inputStream("/SuccessfulValidation/ServiceUnassign.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.unassignInstance, "7"}});
    }

    @Test
    @Parameters(method = "customWorkflowSuccessParameters")
    public void customWorkflowSuccessTest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest,
            Action action, int reqVersion) throws ValidationException, IOException {
        this.sir = sir;
        this.instanceIdMapTest = instanceIdMapTest;
        this.action = action;
        this.reqVersion = reqVersion;
        this.version = "v" + reqVersion;
        this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("workflowUuid", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Parameters
    private Collection<Object[]> customWorkflowSuccessParameters() throws JsonMappingException, IOException {
        return Arrays.asList(
                new Object[][] {{mapper.readValue(inputStream("/SuccessfulValidation/v1ExecuteCustomWorkflow.json"),
                        ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "1"}

                });
    }

    @Test
    @Parameters(method = "aLaCarteParameters")
    public void aLaCarteFlagTest(boolean expected, ServiceInstancesRequest sir) {
        this.expected = expected;
        this.sir = sir;
        this.msoRequest = new MsoRequest();
        assertEquals(expected, msoRequest.getAlacarteFlag(sir));
    }

    @Parameters
    private Collection<Object[]> aLaCarteParameters() throws IOException {
        return Arrays.asList(new Object[][] {
                {false, mapper.readValue(inputStream("/RequestParameters/RequestParametersNull.json"),
                        ServiceInstancesRequest.class)},
                {true, mapper.readValue(inputStream("/RequestParameters/RequestParametersALaCarteTrue.json"),
                        ServiceInstancesRequest.class)}});
    }

    // Tests various scenarios ensuring that requests containing missing or invalid information will throw a
    // ValidationException
    @Test
    @Parameters(method = "validationParameters")
    public void validationFailureTest(String expectedException, ServiceInstancesRequest sir,
            HashMap<String, String> instanceIdMapTest, Action action, int reqVersion)
            throws IOException, ValidationException {
        this.expectedException = expectedException;
        this.sir = sir;
        this.instanceIdMapTest = instanceIdMapTest;
        this.action = action;
        this.reqVersion = reqVersion;
        this.version = "v" + reqVersion;
        this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        this.instanceIdMapTest.put("instanceGroupId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
        thrown.expect(ValidationException.class);
        thrown.expectMessage(expectedException);
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Parameters
    private Collection<Object[]> validationParameters() throws IOException {
        return Arrays.asList(new Object[][] {
                // ValidationException for CloudConfiguration
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/CloudConfiguration.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 4},
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/CloudConfigurationNetwork.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/CloudConfigurationConfig.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.enablePort, 5},
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(
                                inputStream("/CloudConfiguration/InPlaceSoftwareUpdateCloudConfiguration.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.inPlaceSoftwareUpdate, 6},
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(
                                inputStream("/CloudConfiguration/DeactivateAndCloudDeleteCloudConfiguration.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deactivateAndCloudDelete, 7},
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/ScaleOutNoCloudConfig.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid cloudConfiguration is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/VnfRecreateCloudConfig.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.recreateInstance, 7},
                {"No valid lcpCloudRegionId is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/EmptyLcpCloudConfiguration.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid lcpCloudRegionId is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/InPlaceSoftwareUpdateCloudRegionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.inPlaceSoftwareUpdate, 6},
                {"No valid tenantId is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/EmptyTenantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid tenantId is specified",
                        mapper.readValue(inputStream("/CloudConfiguration/InPlaceSoftwareUpdateTenantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.inPlaceSoftwareUpdate, 6},
                // ValidationException for ModelInfo
                {"No valid modelCustomizationId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.replaceInstance, 5},
                {"No valid modelCustomizationId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v5CreateConfigurationModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelCustomizationId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v4CreateVfModuleMissingModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 4},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfModelCustomizationIdPreload.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.replaceInstance, 5},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelCustomizationName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelCustomization.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.replaceInstance, 6},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelCustomization.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 6},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.replaceInstance, 5},
                {"No valid modelCustomizationId or modelCustomizationName is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfRecreateNoModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.recreateInstance, 7},
                {"No valid modelCustomizationId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ScaleOutNoModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid model-info is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelInfoNull.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelInvariantId format is specified",
                        mapper.readValue(inputStream("/ModelInfo/InvalidModelInvariantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 2},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelInvariantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deactivateInstance, 4},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v5ModelInvariantIdNetwork.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.enablePort, 5},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v5ModelInvariantIdDisablePort.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.disablePort, 5},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelInvariantIdVnf.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 3},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelInvariantIdConfiguration.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 3},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v5DeactivateModelInvariantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deactivateInstance, 5},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v3UpdateNetworkBad.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 4},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ScaleOutNoModelInvariantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid modelInvariantId is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfRecreateModelInvariantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.recreateInstance, 7},
                {"No valid modelName is specified",
                        mapper.readValue(inputStream("/ModelInfo/VfModuleModelName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deleteInstance, 4},
                {"No valid modelName is specified",
                        mapper.readValue(inputStream("/ModelInfo/ScaleOutNoModelName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid modelName is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfRecreateModelName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.recreateInstance, 7},
                {"No valid modelType is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelTypeNull.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelVersion is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelVersionService.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 3},
                {"No valid modelVersion is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelVersionVfModule.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 3},
                {"No valid modelVersion is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelVersionServiceCreate.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 3},
                {"No valid modelVersion is specified",
                        mapper.readValue(inputStream("/ModelInfo/ScaleOutNoModelVersion.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid modelVersion is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfRecreateModelVersion.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.recreateInstance, 7},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelVersionId.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ConfigurationModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v2ModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 4},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v2ModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deactivateInstance, 4},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v2ModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v2ModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.enablePort, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v2ModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.disablePort, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ModelVersionIdCreate.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/v5ActivateModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/ScaleOutNoModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/VnfRecreateModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.recreateInstance, 7},
                {"No valid modelVersionId is specified",
                        mapper.readValue(inputStream("/ModelInfo/CreateInstanceGroupNoModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                // ValidationException for Platform and LineOfBusiness
                {"No valid lineOfBusinessName is specified",
                        mapper.readValue(inputStream("/PlatformAndLineOfBusiness/EmptyLineOfBusiness.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid platform is specified",
                        mapper.readValue(inputStream("/PlatformAndLineOfBusiness/Platform.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 6},
                {"No valid platformName is specified",
                        mapper.readValue(inputStream("/PlatformAndLineOfBusiness/EmptyPlatformName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                // ValidationException for Project and OwningEntity
                {"No valid owningEntity is specified",
                        mapper.readValue(inputStream("/ProjectAndOwningEntity/OwningEntity.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 6},
                {"No valid owningEntityId is specified",
                        mapper.readValue(inputStream("/ProjectAndOwningEntity/EmptyOwningEntityId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid projectName is specified",
                        mapper.readValue(inputStream("/ProjectAndOwningEntity/EmptyProjectName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid owningEntity is specified",
                        mapper.readValue(inputStream("/ProjectAndOwningEntity/ServiceAssignNoOE.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                // ValidationException for RelatedInstances
                {"No valid connectionPoint relatedInstance for Port Configuration is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5EnablePortNoConnectionPoint.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.disablePort, 5},
                {"No valid connectionPoint relatedInstance for Port Configuration is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5EnablePortNoConnectionPoint.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.enablePort, 5},
                {"No valid destination vnf relatedInstance for Port Configuration is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5CreateNoDestinationRelatedInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid instanceId format in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesIdFormat.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid instanceId in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid instanceName format in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesNameFormat.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid instanceName in relatedInstance for pnf modelType is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v6AddRelationshipsInstanceName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.removeRelationships, 6},
                {"No valid instanceName in relatedInstance for pnf modelType is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v6AddRelationshipsInstanceName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addRelationships, 6},
                {"No valid modelType in relatedInstance is specified",
                        mapper.readValue(inputStreamVpnBonding("/VpnBondingValidation/NoModelType.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                {"No valid instanceId in relatedInstance is specified",
                        mapper.readValue(inputStreamVpnBonding("/VpnBondingValidation/NoInstanceId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                {"No valid instanceName in relatedInstance for vpnBinding modelType is specified",
                        mapper.readValue(inputStreamVpnBonding("/VpnBondingValidation/NoInstanceNameVpnBinding.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                {"No valid instanceName in relatedInstance for network modelType is specified",
                        mapper.readValue(inputStreamVpnBonding("/VpnBondingValidation/NoInstanceNameNetwork.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                {"No valid modelCustomizationName or modelCustomizationId in relatedInstance of vnf is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid modelInfo in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelInvariantId format in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelInvariantIdFormat.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelInvariantId in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelInvariantId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelName in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelType in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelType.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelVersion in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelVersion.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid modelVersionId in relatedInstance is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/ServiceNoRelatedInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addRelationships, 2},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/ServiceNoRelatedInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.removeRelationships, 2},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5EnablePortNoRelatedInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.disablePort, 2},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5EnablePortNoRelatedInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.enablePort, 5},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5CreateNoRelatedInstances.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v4RelatedInstancesNull.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 4},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/RelatedInstances/ScaleOutNoRelatedInstances.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid related service instance for vfModule request is specified",
                        mapper.readValue(inputStream("/RelatedInstances/VfModuleRelatedInstancesService.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid related service instance for vnf request is specified",
                        mapper.readValue(inputStream("/RelatedInstances/VnfRelatedInstancesService.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid related service instance for volumeGroup request is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesServiceInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid related vnf instance for vfModule request is specified",
                        mapper.readValue(inputStream("/RelatedInstances/VfModuleRelatedInstancesVnf.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid related vnf instance for volumeGroup request is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesVnfInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid serviceInstanceId matching the serviceInstanceId in request URI is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesInstanceId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid serviceInstanceId matching the serviceInstanceId in request URI is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v6VnfDeleteInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.deleteInstance, 6},
                {"No valid source vnf relatedInstance for Port Configuration is specified",
                        mapper.readValue(inputStream("/RelatedInstances/v5CreateNoSourceRelatedInstance.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid vnfInstanceId matching the vnfInstanceId in request URI is specified",
                        mapper.readValue(inputStream("/RelatedInstances/RelatedInstancesVnfInstanceId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.activateInstance, 5},
                {"No valid instanceName format is specified",
                        mapper.readValue(inputStream("/RelatedInstances/InvalidInstanceName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 2},
                {"No valid relatedInstanceList is specified",
                        mapper.readValue(inputStream("/RelatedInstances/CreateInstanceGroupNoRelatedInstances.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                {"No valid related service instance for instanceGroup request is specified",
                        mapper.readValue(inputStream("/RelatedInstances/CreateInstanceGroupService.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                {"No valid modelVersionId in relatedInstance is specified",
                        mapper.readValue(
                                inputStream("/RelatedInstances/CreateInstanceGroupRelatedInstanceModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 7},
                // ValidationException for RequestInfo
                {"No valid requestInfo is specified",
                        mapper.readValue(inputStream("/RequestInfo/RequestInfoNull.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid requestInfo is specified",
                        mapper.readValue(inputStream("/RequestInfo/RequestInfo.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.applyUpdatedConfig, 6},
                {"No valid requestInfo is specified",
                        mapper.readValue(inputStream("/RequestInfo/RequestInfo.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.inPlaceSoftwareUpdate, 6},
                {"No valid requestInfo is specified",
                        mapper.readValue(inputStream("/RequestInfo/ScaleOutNoRequestInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                {"No valid productFamilyId is specified",
                        mapper.readValue(inputStream("/RequestInfo/VnfProductFamilyId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 3},
                {"No valid productFamilyId is specified",
                        mapper.readValue(inputStream("/RequestInfo/NetworkProductFamilyId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 3},
                {"No valid productFamilyId is specified",
                        mapper.readValue(inputStream("/RequestInfo/NetworkProductFamilyId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.updateInstance, 3},
                {"No valid productFamilyId is specified",
                        mapper.readValue(inputStream("/RequestInfo/ServiceProductFamilyId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid requestorId is specified",
                        mapper.readValue(inputStream("/RequestInfo/EmptyRequestorId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid requestorId is specified",
                        mapper.readValue(inputStream("/RequestInfo/RequestorId.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.applyUpdatedConfig, 6},
                {"No valid requestorId is specified",
                        mapper.readValue(inputStream("/RequestInfo/RequestorId.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.inPlaceSoftwareUpdate, 6},
                // ValidationException for RequestParameters
                {"No valid aLaCarte in requestParameters",
                        mapper.readValue(inputStream("/RequestParameters/RequestParametersNull.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addRelationships, 4},
                {"No valid requestParameters is specified",
                        mapper.readValue(inputStream("/RequestParameters/RequestParametersNull.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid requestParameters is specified",
                        mapper.readValue(inputStream("/RequestParameters/RequestParameters.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.applyUpdatedConfig, 6},
                {"No valid requestParameters is specified",
                        mapper.readValue(inputStream("/RequestParameters/RequestParameters.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.inPlaceSoftwareUpdate, 6},
                {"No valid requestParameters is specified",
                        mapper.readValue(inputStream("/RequestParameters/AssignEmptyReqParameters.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                // ValidationException for SubscriberInfo
                {"No valid globalSubscriberId is specified",
                        mapper.readValue(inputStream("/SubscriberInfo/EmptyGlobalSubscriberId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid subscriberInfo is specified",
                        mapper.readValue(inputStream("/SubscriberInfo/EmptySubscriberInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid subscriptionServiceType is specified",
                        mapper.readValue(inputStream("/SubscriberInfo/EmptySubscriptionServiceType.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.createInstance, 5},
                {"No valid subscriberInfo is specified",
                        mapper.readValue(inputStream("/SubscriberInfo/AssignEmptySubscriberInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                // Validation for UserParams
                {"No valid cloudConfiguration in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/AssignCloudConfigVnf.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid cloudConfiguration in userParams network resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/NetworkCloudConfig.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelInfo in userParams is specified",
                        mapper.readValue(inputStream("/RequestParameters/UserParamsModelInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelInfo in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/UserParamsVnfModelInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelVersionId in userParams service modelInfo is specified",
                        mapper.readValue(inputStream("/RequestParameters/UserParamsModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelVersionId in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/VnfModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelVersionId in userParams vfModule resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/VfModuleModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelVersionId in userParams network resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/NetworkModelVersionId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid platform in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/UserParamsPlatform.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid platformName in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/UserParamsPlatformName.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid productFamilyId in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/ProductFamilyId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid vfModules in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/VfModules.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelInfo in userParams vfModules resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/VfModulesModelInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelInfo in userParams network resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/Network.json"), ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelCustomizationId in userParams vfModule resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/VfModuleModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelCustomizationId in userParams vnf resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/VnfModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                {"No valid modelCustomizationId in userParams network resources is specified",
                        mapper.readValue(inputStream("/RequestParameters/NetworkModelCustomizationId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.assignInstance, 7},
                // Validation for ConfigurationParameters
                {"No valid configuration parameters is specified",
                        mapper.readValue(inputStream("/ConfigurationParameters/NoConfigurationParameters.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.scaleOut, 7},
                // Validation for Add and Remove Members
                {"No valid vnf relatedInstance is specified",
                        mapper.readValue(inputStream("/MembersValidation/RelatedInstancesVnf.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addMembers, 7},
                {"No valid related instances is specified",
                        mapper.readValue(inputStream("/MembersValidation/RelatedInstances.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addMembers, 7},
                {"No valid requestInfo is specified",
                        mapper.readValue(inputStream("/MembersValidation/MembersRequestInfo.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addMembers, 7},
                {"No valid requestorId is specified",
                        mapper.readValue(inputStream("/MembersValidation/MembersRequestorId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addMembers, 7},
                {"No valid source is specified",
                        mapper.readValue(inputStream("/MembersValidation/AddMembersSource.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addMembers, 7},
                {"No valid instanceId in relatedInstances is specified",
                        mapper.readValue(inputStream("/MembersValidation/AddMembersInstanceId.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.addMembers, 7},
                {"No valid modelType in relatedInstance is specified",
                        mapper.readValue(inputStream("/MembersValidation/DeleteMembersModelType.json"),
                                ServiceInstancesRequest.class),
                        instanceIdMapTest, Action.removeMembers, 7}});
    }

    @Test
    public void nullInstanceIdMapTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/RequestParameters/RequestParametersNull.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest = null;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(NullPointerException.class);
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void serviceInstanceIdHashMapFailureTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("serviceInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid serviceInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void vnfInstanceIdHashMapFailureTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("vnfInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid vnfInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void vfModuleInstanceIdHashMapFailureTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("vfModuleInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid vfModuleInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void volumeGroupInstanceIdHashMapFailureTest()
            throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("volumeGroupInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid volumeGroupInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void networkInstanceIdHashMapFailureTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("networkInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid networkInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void configurationInstanceIdHashMapFailureTest()
            throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("configurationInstanceId", "test");
        this.action = Action.createInstance;
        thrown.expect(ValidationException.class);
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expectMessage("No valid configurationInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void instanceGroupIdHashMapFailureTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("instanceGroupInstanceId", "test");
        this.action = Action.createInstance;
        thrown.expect(ValidationException.class);
        this.reqVersion = 7;
        this.version = "v" + reqVersion;
        thrown.expectMessage("No valid instanceGroupInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
    }

    @Test
    public void testVfModuleV4UsePreLoad() throws JsonMappingException, IOException, ValidationException {
        this.requestJSON = inputStream("/SuccessfulValidation/v4CreateVfModule.json");
        this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
        this.instanceIdMapTest.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
        this.reqVersion = 4;
        this.version = "v" + reqVersion;
        this.sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
        this.msoRequest = new MsoRequest();
        msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, version, originalRequestJSON, reqVersion,
                false);

        this.requestJSON = inputStream("/ModelInfo/v4CreateVfModuleNoCustomizationId.json");
        this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
        this.instanceIdMapTest.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
        this.sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
        msoRequest = new MsoRequest();
        msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, version, originalRequestJSON, reqVersion,
                false);
    }

    @Test
    public void buildServiceErrorResponseTest() throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("serviceInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid serviceInstanceId is specified");
        this.msoRequest = new MsoRequest();
        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
        Response response =
                msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
                        "Mapping of request to JSON object failed.  ", ErrorNumbers.SVC_BAD_PARAMETER, null, version);
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

    @Test
    public void buildServiceErrorPolicyExceptionResponseTest()
            throws JsonMappingException, IOException, ValidationException {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/InstanceIdHashMap.json"),
                ServiceInstancesRequest.class);
        this.instanceIdMapTest.put("serviceInstanceId", "test");
        this.action = Action.createInstance;
        this.reqVersion = 5;
        this.version = "v" + reqVersion;
        thrown.expect(ValidationException.class);
        thrown.expectMessage("No valid serviceInstanceId is specified");
        this.msoRequest = new MsoRequest();


        this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON, reqVersion, false);
        Response response =
                msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.PolicyException,
                        "Mapping of request to JSON object failed. ", ErrorNumbers.SVC_BAD_PARAMETER, null, version);
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

    @Test
    public void domToStrTest() throws Exception {
        String xmlStr = "<dummy><service-instance-id>1234</service-instance-id></dummy>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlStr)));
        String result = MsoRequest.domToStr(document);
        assertNotNull(result);
    }

    @Test
    public void getTenantNameFromAAITest() throws Exception {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/ServiceAssign.json"),
                ServiceInstancesRequest.class);
        String tenantId = "88a6ca3ee0394ade9403f075db23167e";
        String tenantNameFromAAI = "testTenantName";
        String cloudRegion = "mdt1";
        String cloudOwner = "cloudOwner";
        this.sir.getRequestDetails().getCloudConfiguration().setCloudOwner(cloudOwner);
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantName(tenantNameFromAAI);
        doReturn(tenant).when(aaiDataRet).getTenant(cloudOwner, cloudRegion, tenantId);
        String tenantName = msoRequestMock.getTenantNameFromAAI(this.sir);
        assertEquals(tenantNameFromAAI, tenantName);
    }


    @Test
    public void getProductFamilyNameFromAAITest() throws Exception {
        this.sir = mapper.readValue(inputStream("/SuccessfulValidation/ServiceAssign.json"),
                ServiceInstancesRequest.class);
        String serviceId = "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb";
        String serviceDescription = "testServiceDescription";
        Service service = new Service();
        service.setServiceId(serviceId);
        service.setServiceDescription(serviceDescription);
        doReturn(service).when(aaiDataRet).getService(serviceId);
        String productFamilyName = msoRequestMock.getProductFamilyNameFromAAI(this.sir);
        assertEquals(serviceDescription, productFamilyName);
    }


}
