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

package org.onap.so.bpmn.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onap.so.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogDataByModelUuid;
import static org.onap.so.bpmn.mock.StubResponseOof.mockOof;
import static org.onap.so.bpmn.mock.StubResponseOof.mockOof_500;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.AllottedResource;
import org.onap.so.bpmn.core.domain.HomingSolution;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.onap.so.bpmn.core.domain.NetworkResource;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.onap.so.bpmn.core.domain.ServiceInstance;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.onap.so.bpmn.mock.FileUtil;


/**
 * Test the OOF Homing subflow building block.
 */
@Ignore
public class OofHomingIT extends BaseIntegrationTest {

    ServiceDecomposition serviceDecomposition = new ServiceDecomposition();
    String subscriber = "";
    String subscriber2 = "";

    private final CallbackSet callbacks = new CallbackSet();

    public OofHomingIT() throws IOException {
        String oofCallback = FileUtil.readResourceFile("__files/BuildingBlocks/oofCallbackInfraVnf.json");
        String oofCallback2 = FileUtil.readResourceFile("__files/BuildingBlocks/oofCallback2AR1Vnf.json");
        String oofCallback3 = FileUtil.readResourceFile("__files/BuildingBlocks/oofCallback2AR1Vnf2Net.json");

        String oofCallbackNoSolution =
                FileUtil.readResourceFile("__files/BuildingBlocks/oofCallbackNoSolutionFound.json");
        String oofCallbackPolicyException =
                FileUtil.readResourceFile("__files/BuildingBlocks/oofCallbackPolicyException.json");
        String oofCallbackServiceException =
                FileUtil.readResourceFile("__files/BuildingBlocks/oofCallbackServiceException.json");

        callbacks.put("oof", JSON, "oofResponse", oofCallback);
        callbacks.put("oof2", JSON, "oofResponse", oofCallback2);
        callbacks.put("oof3", JSON, "oofResponse", oofCallback3);
        callbacks.put("oofNoSol", JSON, "oofResponse", oofCallbackNoSolution);
        callbacks.put("oofPolicyEx", JSON, "oofResponse", oofCallbackPolicyException);
        callbacks.put("oofServiceEx", JSON, "oofResponse", oofCallbackServiceException);

        // Service Model
        ModelInfo sModel = new ModelInfo();
        sModel.setModelCustomizationName("testModelCustomizationName");
        sModel.setModelInstanceName("testModelInstanceName");
        sModel.setModelInvariantUuid("testModelInvariantId");
        sModel.setModelName("testModelName");
        sModel.setModelUuid("testModelUuid");
        sModel.setModelVersion("testModelVersion");
        // Service Instance
        ServiceInstance si = new ServiceInstance();
        si.setInstanceId("testServiceInstanceId123");
        // Allotted Resources
        List<AllottedResource> arList = new ArrayList<AllottedResource>();
        AllottedResource ar = new AllottedResource();
        ar.setResourceId("testResourceIdAR");
        ar.setNfFunction("testARFunctionName");
        ModelInfo arModel = new ModelInfo();
        arModel.setModelCustomizationUuid("testModelCustomizationUuidAR");
        arModel.setModelInvariantUuid("testModelInvariantIdAR");
        arModel.setModelName("testModelNameAR");
        arModel.setModelVersion("testModelVersionAR");
        arModel.setModelUuid("testARModelUuid");
        arModel.setModelType("testModelTypeAR");
        ar.setModelInfo(arModel);
        AllottedResource ar2 = new AllottedResource();
        ar2.setResourceId("testResourceIdAR2");
        ar2.setNfFunction("testAR2FunctionName");
        ModelInfo arModel2 = new ModelInfo();
        arModel2.setModelCustomizationUuid("testModelCustomizationUuidAR2");
        arModel2.setModelInvariantUuid("testModelInvariantIdAR2");
        arModel2.setModelName("testModelNameAR2");
        arModel2.setModelVersion("testModelVersionAR2");
        arModel2.setModelUuid("testAr2ModelUuid");
        arModel2.setModelType("testModelTypeAR2");
        ar2.setModelInfo(arModel2);
        arList.add(ar);
        arList.add(ar2);
        // Vnfs
        List<VnfResource> vnfList = new ArrayList<VnfResource>();
        VnfResource vnf = new VnfResource();
        vnf.setResourceId("testResourceIdVNF");
        vnf.setNfFunction("testVnfFunctionName");
        vnf.getHomingSolution().setOofDirectives("{ \n" + "      \"directives\":[ \n" + "         { \n"
                + "            \"vnfc_directives\":[ \n" + "               { \n"
                + "                  \"vnfc_id\":\"<ID of VNFC>\",\n" + "                  \"directives\":[ \n"
                + "                     { \n"
                + "                        \"directive_name\":\"<Name of directive,example flavor_directive>\",\n"
                + "                        \"attributes\":[ \n" + "                           { \n"
                + "                              \"attribute_name\":\"<name of attribute, such as flavor label>\",\n"
                + "                              \"attribute_value\":\"<value such as cloud specific flavor>\"\n"
                + "                           }\n" + "                        ]\n" + "                     },\n"
                + "                     { \n"
                + "                        \"directive_name\":\"<Name of directive,example vnic-info>\",\n"
                + "                        \"attributes\":[ \n" + "                           { \n"
                + "                              \"attribute_name\":\"<name of attribute, such as vnic-type>\",\n"
                + "                              \"attribute_value\":\"<value such as direct/normal>\"\n"
                + "                           },\n" + "                           { \n"
                + "                              \"attribute_name\":\"<name of attribute, such as provider netweork>\",\n"
                + "                              \"attribute_value\":\"<value such as physnet>\"\n"
                + "                           }\n" + "                        ]\n" + "                     }\n"
                + "                  ]\n" + "               }\n" + "            ]\n" + "         },\n" + "         { \n"
                + "            \"vnf_directives\":{ \n" + "               \"directives\":[ \n"
                + "                  { \n" + "                     \"directive_name\":\"<Name of directive>\",\n"
                + "                     \"attributes\":[ \n" + "                        { \n"
                + "                           \"attribute_name\":\"<name of attribute>\",\n"
                + "                           \"attribute_value\":\"<value>\"\n" + "                        }\n"
                + "                     ]\n" + "                  },\n" + "                  { \n"
                + "                     \"directive_name\":\"<Name of directive>\",\n"
                + "                     \"attributes\":[ \n" + "                        { \n"
                + "                           \"attribute_name\":\"<name of attribute>\",\n"
                + "                           \"attribute_value\":\"<value >\"\n" + "                        },\n"
                + "                        { \n"
                + "                           \"attribute_name\":\"<name of attribute>\",\n"
                + "                           \"attribute_value\":\"<value >\"\n" + "                        }\n"
                + "                     ]\n" + "                  }\n" + "               ]\n" + "            }\n"
                + "         }\n" + "      ]\n" + "   },\n" + "   \"sdnc_directives\":{ \n" + "      \"directives\":[ \n"
                + "         { \n" + "            \"vnfc_directives\":[ \n" + "               { \n"
                + "                  \"vnfc_id\":\"<ID of VNFC>\",\n" + "                  \"directives\":[ \n"
                + "                     { \n"
                + "                        \"directive_name\":\"<Name of directive,example flavor_directive>\",\n"
                + "                        \"attributes\":[ \n" + "                           { \n"
                + "                              \"attribute_name\":\"<name of attribute, such as flavor label>\",\n"
                + "                              \"attribute_value\":\"<value such as cloud specific flavor>\"\n"
                + "                           }\n" + "                        ]\n" + "                     },\n"
                + "                     { \n"
                + "                        \"directive_name\":\"<Name of directive,example vnic-info>\",\n"
                + "                        \"attributes\":[ \n" + "                           { \n"
                + "                              \"attribute_name\":\"<name of attribute, such as vnic-type>\",\n"
                + "                              \"attribute_value\":\"<value such as direct/normal>\"\n"
                + "                           },\n" + "                           { \n"
                + "                              \"attribute_name\":\"<name of attribute, such as provider netweork>\",\n"
                + "                              \"attribute_value\":\"<value such as physnet>\"\n"
                + "                           }\n" + "                        ]\n" + "                     }\n"
                + "                  ]\n" + "               }\n" + "            ]\n" + "         },\n" + "         { \n"
                + "            \"vnf_directives\":{ \n" + "               \"directives\":[ \n"
                + "                  { \n" + "                     \"directive_name\":\"<Name of directive>\",\n"
                + "                     \"attributes\":[ \n" + "                        { \n"
                + "                           \"attribute_name\":\"<name of attribute>\",\n"
                + "                           \"attribute_value\":\"<value>\"\n" + "                        }\n"
                + "                     ]\n" + "                  },\n" + "                  { \n"
                + "                     \"directive_name\":\"<Name of directive>\",\n"
                + "                     \"attributes\":[ \n" + "                        { \n"
                + "                           \"attribute_name\":\"<name of attribute>\",\n"
                + "                           \"attribute_value\":\"<value >\"\n" + "                        },\n"
                + "                        { \n"
                + "                           \"attribute_name\":\"<name of attribute>\",\n"
                + "                           \"attribute_value\":\"<value >\"\n" + "                        }\n"
                + "                     ]\n" + "                  }\n" + "               ]\n" + "            }\n"
                + "         }\n" + "      ]\n" + "   }");
        ModelInfo vnfModel = new ModelInfo();
        vnfModel.setModelCustomizationUuid("testModelCustomizationUuidVNF");
        vnfModel.setModelInvariantUuid("testModelInvariantIdVNF");
        vnfModel.setModelName("testModelNameVNF");
        vnfModel.setModelVersion("testModelVersionVNF");
        vnfModel.setModelUuid("testVnfModelUuid");
        vnfModel.setModelType("testModelTypeVNF");
        vnf.setModelInfo(vnfModel);
        vnfList.add(vnf);
        System.out.println("SERVICE DECOMP: " + serviceDecomposition.getServiceResourcesJsonString());
        serviceDecomposition.setModelInfo(sModel);
        serviceDecomposition.setAllottedResources(arList);
        serviceDecomposition.setVnfResources(vnfList);
        serviceDecomposition.setServiceInstance(si);

        // Subscriber
        subscriber =
                "{\"globalSubscriberId\": \"SUB12_0322_DS_1201\",\"subscriberCommonSiteId\": \"DALTX0101\",\"subscriberName\": \"SUB_12_0322_DS_1201\"}";
        subscriber2 = "{\"globalSubscriberId\": \"SUB12_0322_DS_1201\",\"subscriberName\": \"SUB_12_0322_DS_1201\"}";
    }

    @Test
    public void testHoming_success_2AR1Vnf() {

        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oof2");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
        String expectedOofRequest = (String) getVariableFromHistory(businessKey, "oofRequest");

        Resource resourceAR = serviceDecompositionExp.getServiceResource("testResourceIdAR");
        HomingSolution resourceARHoming = resourceAR.getHomingSolution();
        Resource resourceAR2 = serviceDecompositionExp.getServiceResource("testResourceIdAR2");
        HomingSolution resourceARHoming2 = resourceAR2.getHomingSolution();
        Resource resourceVNF = serviceDecompositionExp.getServiceResource("testResourceIdVNF");
        HomingSolution resourceVNFHoming = resourceVNF.getHomingSolution();
        String resourceARHomingString = resourceARHoming.toString();
        resourceARHomingString = resourceARHomingString.replaceAll("\\s+", " ");
        String resourceARHoming2String = resourceARHoming2.toString();
        resourceARHoming2String = resourceARHoming2String.replaceAll("\\s+", " ");
        String resourceVNFHomingString = resourceVNFHoming.toString();
        resourceVNFHomingString = resourceVNFHomingString.replaceAll("\\s+", " ");
        expectedOofRequest = expectedOofRequest.replaceAll("\\s+", "");

        assertNull(workflowException);
        assertEquals(homingSolutionService("service", "testSIID1", "MDTNJ01", resourceARHoming.getVnf().getResourceId(),
                "aic", "dfwtx", "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(
                homingSolutionService("service", "testSIID2", "testVnfHostname2",
                        resourceARHoming2.getVnf().getResourceId(), "aic", "testCloudRegionId2", null, null),
                resourceARHoming2String);
        assertEquals(
                homingSolutionCloud("cloud", "aic", "testCloudRegionId3", true,
                        "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(verifyOofRequest(), expectedOofRequest);
    }

    @Test
    public void testHoming_success_2AR1Vnf2Net() {

        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables2(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oof3");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
        String expectedOofRequest = (String) getVariableFromHistory(businessKey, "oofRequest");

        Resource resourceAR = serviceDecompositionExp.getServiceResource("testResourceIdAR");
        HomingSolution resourceARHoming = resourceAR.getHomingSolution();
        Resource resourceAR2 = serviceDecompositionExp.getServiceResource("testResourceIdAR2");
        HomingSolution resourceARHoming2 = resourceAR2.getHomingSolution();
        Resource resourceVNF = serviceDecompositionExp.getServiceResource("testResourceIdVNF");
        HomingSolution resourceVNFHoming = resourceVNF.getHomingSolution();
        Resource resourceNet = serviceDecompositionExp.getServiceResource("testResourceIdNet");
        HomingSolution resourceNetHoming = resourceNet.getHomingSolution();
        Resource resourceNet2 = serviceDecompositionExp.getServiceResource("testResourceIdNet2");
        HomingSolution resourceNetHoming2 = resourceNet2.getHomingSolution();

        String resourceARHomingString = resourceARHoming.toString();
        resourceARHomingString = resourceARHomingString.replaceAll("\\s+", " ");
        String resourceARHoming2String = resourceARHoming2.toString();
        resourceARHoming2String = resourceARHoming2String.replaceAll("\\s+", " ");
        String resourceVNFHomingString = resourceVNFHoming.toString();
        resourceVNFHomingString = resourceVNFHomingString.replaceAll("\\s+", " ");
        String resourceNetHomingString = resourceNetHoming.toString();
        resourceNetHomingString = resourceNetHomingString.replaceAll("\\s+", " ");
        String resourceNetHoming2String = resourceNetHoming2.toString();
        resourceNetHoming2String = resourceNetHoming2String.replaceAll("\\s+", " ");
        expectedOofRequest = expectedOofRequest.replaceAll("\\s+", "");


        assertNull(workflowException);
        assertEquals(homingSolutionService("service", "testSIID1", "MDTNJ01", resourceARHoming.getVnf().getResourceId(),
                "aic", "dfwtx", "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(
                homingSolutionService("service", "testSIID2", "testVnfHostname2",
                        resourceARHoming2.getVnf().getResourceId(), "aic", "testCloudRegionId2", null, null),
                resourceARHoming2String);
        assertEquals(
                homingSolutionCloud("cloud", "aic", "testCloudRegionId3", true,
                        "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(
                homingSolutionService("service", "testServiceInstanceIdNet", "testVnfHostNameNet",
                        resourceNetHoming.getVnf().getResourceId(), "aic", "testCloudRegionIdNet", null, null),
                resourceNetHomingString);
        assertEquals(
                homingSolutionCloud("cloud", "aic", "testCloudRegionIdNet2", false,
                        "\"f1d563e8-e714-4393-8f99-cc480144a05n\", \"j1d563e8-e714-4393-8f99-cc480144a05n\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05n\", \"b1d563e8-e714-4393-8f99-cc480144a05n\""),
                resourceNetHoming2String);
        assertEquals(verifyOofRequest(), expectedOofRequest);

    }

    @Test
    public void testHoming_success_vnfResourceList() {

        // Create a Service Decomposition
        MockGetServiceResourcesCatalogDataByModelUuid(wireMockServer, "2f7f309d-c842-4644-a2e4-34167be5eeb4",
                "/BuildingBlocks/oofCatalogResp.json");
        String busKey = UUID.randomUUID().toString();
        Map<String, Object> vars = new HashMap<>();
        setVariablesForServiceDecomposition(vars, "testRequestId123", "ff5256d2-5a33-55df-13ab-12abad84e7ff");
        invokeSubProcess("DecomposeService", busKey, vars);

        ServiceDecomposition sd = (ServiceDecomposition) getVariableFromHistory(busKey, "serviceDecomposition");
        System.out.println("In testHoming_success_vnfResourceList, ServiceDecomposition = " + sd);
        List<VnfResource> vnfResourceList = sd.getVnfResources();
        vnfResourceList.get(0).setResourceId("test-resource-id-000");

        // Invoke Homing

        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("homingService", "oof");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", sd);
        variables.put("subscriberInfo", subscriber2);
        HashMap customerLocation = new HashMap<String, Object>();
        customerLocation.put("customerLatitude", "32.89748");
        customerLocation.put("customerLongitude", "-97.040443");
        customerLocation.put("customerName", "xyz");
        variables.put("customerLatitude", "32.89748");
        variables.put("customerLongitude", "-97.040443");
        variables.put("customerName", "xyz");
        variables.put("customerLocation", customerLocation);
        variables.put("cloudOwner", "amazon");
        variables.put("cloudRegionId", "TNZED");

        invokeSubProcess("Homing", businessKey, variables);
        injectWorkflowMessages(callbacks, "oof3");
        waitForProcessEnd(businessKey, 10000);

        // Get Variables

        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
        System.out.println("serviceDecompositionExp is: " + serviceDecompositionExp);

        Resource resourceVnf = serviceDecompositionExp.getServiceResource("test-resource-id-000");
        System.out.println("resourceVnf is: " + resourceVnf);
        HomingSolution resourceVnfHoming = resourceVnf.getHomingSolution();

        String resourceVnfHomingString = resourceVnfHoming.toString();
        System.out.println("resourceVnfHomingString is: " + resourceVnfHomingString);
        resourceVnfHomingString = resourceVnfHomingString.replaceAll("\\s+", " ");
        System.out.println("Now resourceVnfHomingString is: " + resourceVnfHomingString);

        assertNull(workflowException);

        // Verify request
        String oofRequest = (String) getVariableFromHistory(businessKey, "oofRequest");
        System.out.println("oofRequest is: " + oofRequest);
        assertEquals(
                FileUtil.readResourceFile("__files/BuildingBlocks/oofRequest_infravnf").replaceAll("\n", "")
                        .replaceAll("\r", "").replaceAll("\t", ""),
                oofRequest.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", ""));

        // System.out.println("resourceVnfHoming.getVnf().getResourceId() is: " +
        // resourceVnfHoming.getVnf().getResourceId());

        assertEquals(
                homingSolutionService("service", "service-instance-01234", "MDTNJ01", "test-resource-id-000",
                        "CloudOwner", "mtmnj1a",
                        "\"f1d563e8-e714-4393-8f99-cc480144a05e\"," + " \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05e\"," + " \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVnfHomingString);
    }

    @Test
    public void testHoming_success_existingLicense() {

        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<String, Object>();
        setVariablesExistingLicense(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniro");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
        String oofRequest = (String) getVariableFromHistory(businessKey, "sniroRequest");

        Resource resourceAR = serviceDecompositionExp.getServiceResource("testResourceIdAR");
        HomingSolution resourceARHoming = (HomingSolution) resourceAR.getHomingSolution();
        Resource resourceAR2 = serviceDecompositionExp.getServiceResource("testResourceIdAR2");
        HomingSolution resourceARHoming2 = (HomingSolution) resourceAR2.getHomingSolution();
        Resource resourceVNF = serviceDecompositionExp.getServiceResource("testResourceIdVNF");
        HomingSolution resourceVNFHoming = (HomingSolution) resourceVNF.getHomingSolution();
        String resourceARHomingString = resourceARHoming.toString();
        resourceARHomingString = resourceARHomingString.replaceAll("\\s+", " ");
        String resourceARHoming2String = resourceARHoming2.toString();
        resourceARHoming2String = resourceARHoming2String.replaceAll("\\s+", " ");
        String resourceVNFHomingString = resourceVNFHoming.toString();
        resourceVNFHomingString = resourceVNFHomingString.replaceAll("\\s+", " ");
        oofRequest = oofRequest.replaceAll("\\s+", "");

        assertNull(workflowException);
        assertEquals(
                homingSolutionService("service", "testSIID1", "MDTNJ01", "aic", "dfwtx", "KDTNJ01",
                        "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(
                homingSolutionService("service", "testSIID2", "testVnfHostname2",
                        resourceARHoming2.getVnf().getResourceId(), "aic", "testCloudRegionId2", null, null),
                resourceARHoming2String);
        assertEquals(
                homingSolutionCloud("cloud", "aic", "testCloudRegionId3", false,
                        "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(verifyOofRequestExistingLicense(), oofRequest);

    }

    @Test
    public void testHoming_error_inputVariable() {

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables3(variables);

        invokeSubProcess("Homing", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=4000,errorMessage=A required "
                + "input variable is missing or null,workStep=*]", workflowException.toString());
    }

    @Test
    public void testHoming_error_badResponse() {
        mockOof_500(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=401,errorMessage=Internal Error - BasicAuth value null,workStep=*]",
                workflowException.toString());
    }

    @Test
    public void testHoming_error_oofNoSolution() {
        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oofNoSol");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        Boolean errorMatch = workflowException.toString()
                .contains("WorkflowException[processKey=Homing,errorCode=400,errorMessage=OOF Async Callback "
                        + "Response contains error: Unable to find any candidate for demand *** Response:");
        assert (errorMatch);
        assertNotNull(businessKey);
    }

    @Test
    public void testHoming_error_oofPolicyException() {
        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oofPolicyEx");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=400,errorMessage=OOF Async Callback "
                + "Response contains a Request Error Policy Exception: Message content size exceeds the allowable "
                + "limit]", workflowException.toString());
    }

    @Test
    public void testHoming_error_oofServiceException() {
        mockOof(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);
        injectWorkflowMessages(callbacks, "oofServiceEx");
        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=400,errorMessage=OOF Async Callback "
                        + "Response contains a Request Error Service Exception: OOF PlacementError: "
                        + "requests.exceptions.HTTPError: 404 Client Error: Not Found for "
                        + "url: http://192.168.171.200:8091/v1/plans/97b4e303-5f75-492c-8fb2-21098281c8b8]",
                workflowException.toString());
    }


    private void setVariables(Map<String, Object> variables) {
        variables.put("homingService", "oof");
        HashMap customerLocation = new HashMap<String, Object>();
        customerLocation.put("customerLatitude", "32.89748");
        customerLocation.put("customerLongitude", "-97.040443");
        customerLocation.put("customerName", "xyz");
        variables.put("customerLatitude", "32.89748");
        variables.put("customerLongitude", "-97.040443");
        variables.put("customerName", "xyz");
        variables.put("customerLocation", customerLocation);
        variables.put("cloudOwner", "amazon");
        variables.put("cloudRegionId", "TNZED");
        variables.put("vgMuxInfraModelInvariantId", "testModelInvariantIdAR");
        variables.put("vgMuxInfraModelId", "testArModelUuid");
        // variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId123");
        variables.put("serviceInstanceName", "testServiceName");
        variables.put("serviceDecomposition", serviceDecomposition);
        variables.put("subscriberInfo", subscriber2);
    }

    private void setVariables2(Map<String, Object> variables) {
        List<NetworkResource> netList = new ArrayList<NetworkResource>();
        NetworkResource net = new NetworkResource();
        net.setResourceId("testResourceIdNet");
        ModelInfo netModel = new ModelInfo();
        netModel.setModelCustomizationUuid("testModelCustomizationUuidNet");
        netModel.setModelInvariantUuid("testModelInvariantIdNet");
        netModel.setModelName("testModelNameNet");
        netModel.setModelVersion("testModelVersionNet");
        net.setModelInfo(netModel);
        netList.add(net);
        NetworkResource net2 = new NetworkResource();
        net2.setResourceId("testResourceIdNet2");
        ModelInfo netModel2 = new ModelInfo();
        netModel2.setModelCustomizationUuid("testModelCustomizationUuidNet2");
        netModel2.setModelCustomizationName("testModelCustomizationNameNet2");
        netModel2.setModelInvariantUuid("testModelInvariantIdNet2");
        netModel2.setModelName("testModelNameNet2");
        netModel2.setModelVersion("testModelVersionNet2");
        net2.setModelInfo(netModel2);
        netList.add(net2);
        serviceDecomposition.setNetworkResources(netList);

        variables.put("homingService", "oof");
        HashMap customerLocation = new HashMap<String, Object>();
        customerLocation.put("customerLatitude", "32.89748");
        customerLocation.put("customerLongitude", "-97.040443");
        customerLocation.put("customerName", "xyz");
        variables.put("customerLatitude", "32.89748");
        variables.put("customerLongitude", "-97.040443");
        variables.put("customerName", "xyz");
        variables.put("customerLocation", customerLocation);
        variables.put("cloudOwner", "amazon");
        variables.put("cloudRegionId", "TNZED");
        variables.put("vgMuxInfraModelInvariantId", "testModelInvariantIdAR");
        variables.put("vgMuxInfraModelId", "testArModelUuid");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId123");
        variables.put("serviceInstanceName", "testServiceName");
        variables.put("serviceDecomposition", serviceDecomposition);
        variables.put("subscriberInfo", subscriber2);
    }

    private void setVariables3(Map<String, Object> variables) {
        variables.put("homingService", "oof");
        HashMap customerLocation = new HashMap<String, Object>();
        customerLocation.put("customerLatitude", "32.89748");
        customerLocation.put("customerLongitude", "-97.040443");
        customerLocation.put("customerName", "xyz");
        variables.put("customerLatitude", "32.89748");
        variables.put("customerLongitude", "-97.040443");
        variables.put("customerName", "xyz");
        variables.put("customerLocation", customerLocation);
        variables.put("cloudOwner", "amazon");
        variables.put("cloudRegionId", "TNZED");
        variables.put("vgMuxInfraModelInvariantId", "testModelInvariantIdAR");
        variables.put("vgMuxInfraModelId", "testArModelUuid");
        variables.put("isDebugLogEnabled", "true");
        // variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId123");
        variables.put("serviceInstanceName", "testServiceName");
        variables.put("serviceDecomposition", null);
        variables.put("subscriberInfo", subscriber2);
    }

    private void setVariablesExistingLicense(Map<String, Object> variables) {
        HomingSolution currentHomingSolution = new HomingSolution();
        serviceDecomposition.getVnfResources().get(0).setCurrentHomingSolution(currentHomingSolution);
        serviceDecomposition.getVnfResources().get(0).getCurrentHomingSolution().getLicense()
                .addEntitlementPool("testEntitlementPoolId1");
        serviceDecomposition.getVnfResources().get(0).getCurrentHomingSolution().getLicense()
                .addEntitlementPool("testEntitlementPoolId2");

        serviceDecomposition.getVnfResources().get(0).getCurrentHomingSolution().getLicense()
                .addLicenseKeyGroup("testLicenseKeyGroupId1");
        serviceDecomposition.getVnfResources().get(0).getCurrentHomingSolution().getLicense()
                .addLicenseKeyGroup("testLicenseKeyGroupId2");

        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", serviceDecomposition);
        variables.put("subscriberInfo", subscriber2);

    }

    private String homingSolutionService(String type, String serviceInstanceId, String vnfHostname,
            String vnfResourceId, String cloudOwner, String cloudRegionId, String enList, String licenseList) {

        String solution = "";
        if (enList == null) {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"serviceInstanceId\" : \""
                    + serviceInstanceId + "\", \"cloudOwner\" : \"" + cloudOwner + "\", \"cloudRegionId\" : \""
                    + cloudRegionId + "\", " + "\"vnf\" : { \"resourceId\" : \"" + vnfResourceId
                    + "\", \"resourceType\" : \"VNF\", \"resourceInstance\" : { }, \"homingSolution\" : { \"license\" :"
                    + " { }, \"rehome\" : false }, \"vnfHostname\" : \"" + vnfHostname + "\" }, \"license\" : { },"
                    + " \"rehome\" : false } }";
        } else {
            // language=JSON
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"serviceInstanceId\" : \""
                    + serviceInstanceId + "\", \"cloudOwner\" : \"" + cloudOwner + "\", \"cloudRegionId\" : \""
                    + cloudRegionId + "\", \"vnf\" : { \"resourceId\" : \"" + vnfResourceId + "\", \"resourceType\" :"
                    + " \"VNF\", \"resourceInstance\" : { }, \"homingSolution\" : { \"license\" : { }, \"rehome\" :"
                    + " false }, \"vnfHostname\" : \"" + vnfHostname + "\" }, \"license\" : { \"entitlementPoolList\" :"
                    + " [ " + enList + " ], \"licenseKeyGroupList\" : [ " + licenseList
                    + " ] }, \"rehome\" : false } }";
        }
        return solution;
    }

    private String homingSolutionCloud(String type, String cloudOwner, String cloudRegionId, Boolean flavors,
            String enList, String licenseList) {
        String solution = "";
        if (enList == null) {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" + cloudOwner
                    + "\", \"cloudRegionId\" : \"" + cloudRegionId + "\", \"license\" : { }, \"rehome\" : false } }";
        } else if (flavors && enList == null) {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" + cloudOwner
                    + "\", \"cloudRegionId\" : \"" + cloudRegionId
                    + "\", \"flavors\" :  [ { \"flavorLabel\" : \"flavorLabel2xxx\", \"flavor\" : \"vimFlavorxxx\" }, "
                    + "{ \"flavorLabel\" : \"flavorLabel1xxx\", \"flavor\" : \"vimFlavorxxx\" } ], "
                    + "\"license\" : { }, \"rehome\" : false } }";
        } else if (flavors) {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" + cloudOwner
                    + "\", \"cloudRegionId\" : \"" + cloudRegionId
                    + "\", \"flavors\" : [ { \"flavorLabel\" : \"flavorLabel2xxx\", \"flavor\" : \"vimFlavorxxx\" }, "
                    + "{ \"flavorLabel\" : \"flavorLabel1xxx\", \"flavor\" : \"vimFlavorxxx\" } ], "
                    + "\"license\" : { \"entitlementPoolList\" : [ " + enList + " ], \"licenseKeyGroupList\" : [ "
                    + licenseList + " ] }, \"rehome\" : false } }";
        } else {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" + cloudOwner
                    + "\", \"cloudRegionId\" : \"" + cloudRegionId + "\", \"license\" : { \"entitlementPoolList\" : [ "
                    + enList + " ], \"licenseKeyGroupList\" : [ " + licenseList + " ] }, \"rehome\" : false } }";
        }
        return solution;
    }

    private void setVariablesForServiceDecomposition(Map<String, Object> variables, String requestId, String siId) {
        variables.put("homingService", "oof");
        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", requestId);
        variables.put("msoRequestId", requestId);
        variables.put("serviceInstanceId", siId);
        HashMap customerLocation = new HashMap<String, Object>();
        customerLocation.put("customerLatitude", "32.89748");
        customerLocation.put("customerLongitude", "-97.040443");
        customerLocation.put("customerName", "xyz");
        variables.put("customerLatitude", "32.89748");
        variables.put("customerLongitude", "-97.040443");
        variables.put("customerName", "xyz");
        variables.put("customerLocation", customerLocation);
        variables.put("cloudOwner", "amazon");
        variables.put("cloudRegionId", "TNZED");


        String serviceModelInfo = "{\"modelInvariantId\":\"1cc4e2e4-eb6e-404d-a66f-c8733cedcce8\",\"modelUuid\":"
                + "\"2f7f309d-c842-4644-a2e4-34167be5eeb4\",\"modelName\":\"vCPE Service\",\"modelVersion\":\"2.0\",}";
        variables.put("serviceModelInfo", serviceModelInfo);
    }

    private String verifyOofRequest() {
        String request = "{\"requestInfo\":{\"transactionId\":\"testRequestId\",\"requestId\":\"testRequestId\","
                + "\"callbackUrl\":\"http://localhost:28090/workflows/messages/message/oofResponse/testRequestId\","
                + "\"sourceId\":\"so\",\"requestType\":\"create\",\"numSolutions\":1,\"optimizers\":[\"placement\"],"
                + "\"timeout\":600},\"placementInfo\":{\"requestParameters\":{\"customerLatitude\":"
                + "\"32.89748\",\"customerLongitude\":\"-97.040443\",\"customerName\":\"xyz\"},\"subscriberInfo\":"
                + "{\"globalSubscriberId\":\"SUB12_0322_DS_1201\",\"subscriberName\":\"SUB_12_0322_DS_1201\","
                + "\"subscriberCommonSiteId\":\"\"},\"placementDemands\":[{\"resourceModuleName\":\"testARFunctionName\""
                + ",\"serviceResourceId\":\"testResourceIdAR\",\"tenantId\":"
                + "\"\",\"resourceModelInfo\":{\"modelInvariantId\":\"no-resourceModelInvariantId\","
                + "\"modelVersionId\":\"no-resourceModelVersionId\",\"modelName\":\"\",\"modelType\":"
                + "\"\",\"modelVersion\":\"\",\"modelCustomizationName\":\"\"}},"
                + "{\"resourceModuleName\":\"testAR2FunctionName\",\"serviceResourceId\":\"testResourceIdAR2\","
                + "\"tenantId\":\"\",\"resourceModelInfo\":{\"modelInvariantId\":\"no-resourceModelInvariantId\","
                + "\"modelVersionId\":\"no-resourceModelVersionId\",\"modelName\":\"\","
                + "\"modelType\":\"\",\"modelVersion\":\"\","
                + "\"modelCustomizationName\":\"\"}},{\"resourceModuleName\":\"testVnfFunctionName\",\"serviceResourceId\":\""
                + "testResourceIdVNF\",\"tenantId\":\"\",\"resourceModelInfo\":{\"modelInvariantId\""
                + ":\"testModelInvariantIdVNF\",\"modelVersionId\":\"testVnfModelUuid\",\"modelName\":\""
                + "testModelNameVNF\",\"modelType\":\"testModelTypeVNF\",\"modelVersion\":\"testModelVersionVNF\""
                + ",\"modelCustomizationName\":\"\"}}]},\"serviceInfo\":"
                + "{\"serviceInstanceId\":\"testServiceInstanceId123\","
                + "\"serviceName\":\"testServiceName\",\"modelInfo\":{\"modelType\":\"\",\"modelInvariantId\":"
                + "\"testModelInvariantId\",\"modelVersionId\":\"testModelUuid\",\"modelName\":\"testModelName\","
                + "\"modelVersion\":\"testModelVersion\",\"modelCustomizationName\":\"" + "\"}}}";
        return request;
    }

    private String verifyOofRequestExistingLicense() {
        String request = "{\"requestInfo\":{\"transactionId\":\"testRequestId\",\"requestId\":\"testRequestId\","
                + "\"callbackUrl\":\"http://localhost:28090/workflows/messages/message/SNIROResponse/testRequestId\","
                + "\"sourceId\":\"mso\",\"requestType\":\"speedchanged\",\"optimizer\":[\"placement\",\"license\"],"
                + "\"numSolutions\":1,\"timeout\":1800},\"placementInfo\":{\"serviceModelInfo\":{\"modelType\":\"\","
                + "\"modelInvariantId\":\"testModelInvariantId\",\"modelVersionId\":\"testModelUuid\",\"modelName\":"
                + "\"testModelName\",\"modelVersion\":\"testModelVersion\"},\"subscriberInfo\":"
                + "{\"globalSubscriberId\":\"SUB12_0322_DS_1201\",\"subscriberName\":\"SUB_12_0322_DS_1201\","
                + "\"subscriberCommonSiteId\":\"\"},\"demandInfo\":{\"placementDemand\":[{\"resourceInstanceType\":"
                + "\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR\",\"resourceModuleName\":\"\","
                + "\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR\","
                + "\"modelInvariantId\":\"testModelInvariantIdAR\",\"modelName\":\"testModelNameAR\","
                + "\"modelVersion\":\"testModelVersionAR\",\"modelVersionId\":\"testARModelUuid\",\"modelType\":"
                + "\"testModelTypeAR\"},\"tenantId\":\"\",\"tenantName\":\"\"},{\"resourceInstanceType\":"
                + "\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR2\",\"resourceModuleName\":"
                + "\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR2\","
                + "\"modelInvariantId\":\"testModelInvariantIdAR2\",\"modelName\":\"testModelNameAR2\","
                + "\"modelVersion\":\"testModelVersionAR2\",\"modelVersionId\":\"testAr2ModelUuid\","
                + "\"modelType\":\"testModelTypeAR2\"},\"tenantId\":\"\",\"tenantName\":\"\"}],\"licenseDemand\":"
                + "[{\"resourceInstanceType\":\"VNF\",\"serviceResourceId\":\"testResourceIdVNF\","
                + "\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":"
                + "\"testModelCustomizationUuidVNF\",\"modelInvariantId\":\"testModelInvariantIdVNF\","
                + "\"modelName\":\"testModelNameVNF\",\"modelVersion\":\"testModelVersionVNF\","
                + "\"modelVersionId\":\"testVnfModelUuid\",\"modelType\":\"testModelTypeVNF\"},"
                + "\"existingLicense\":[{\"entitlementPoolUUID\":[\"testEntitlementPoolId1\","
                + "\"testEntitlementPoolId2\"],\"licenseKeyGroupUUID\":[\"testLicenseKeyGroupId1\","
                + "\"testLicenseKeyGroupId2\"]}]}]},\"policyId\":[],\"serviceInstanceId\":"
                + "\"testServiceInstanceId123\",\"orderInfo\":\"{\\\"requestParameters\\\":null}\"}}";
        return request;
    }
}
