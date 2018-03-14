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

/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
package org.openecomp.mso.bpmn.common;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.core.domain.AllottedResource;
import org.openecomp.mso.bpmn.core.domain.HomingSolution;
import org.openecomp.mso.bpmn.core.domain.ModelInfo;
import org.openecomp.mso.bpmn.core.domain.NetworkResource;
import org.openecomp.mso.bpmn.core.domain.Resource;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.domain.ServiceInstance;
import org.openecomp.mso.bpmn.core.domain.VnfResource;
import org.openecomp.mso.bpmn.mock.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogDataByModelUuid;
import static org.openecomp.mso.bpmn.mock.StubResponseOof.mockOof;
import static org.openecomp.mso.bpmn.mock.StubResponseOof.mockOof_500;


/**
 * Test the OOF Homing subflow building block.
 */
public class OofHomingTest extends WorkflowTest {

    ServiceDecomposition serviceDecomposition = new ServiceDecomposition();

    private final CallbackSet callbacks = new CallbackSet();

    public OofHomingTest() throws IOException {
        String oofCallback = FileUtil.readResourceFile("__files/BuildingBlocks/oofCallbackInfraVnf");
        String oofCallback2 = FileUtil.readResourceFile("__files/BuildingBlocks/oofCallback2AR1Vnf");
        String oofCallback3 = FileUtil.readResourceFile("__files/BuildingBlocks/oofCallback2AR1Vnf2Net");

        String oofCallbackNoSolution = FileUtil.
                readResourceFile("__files/BuildingBlocks/oofCallbackNoSolutionFound");
        String oofCallbackPolicyException = FileUtil.
                readResourceFile("__files/BuildingBlocks/oofCallbackPolicyException");
        String oofCallbackServiceException = FileUtil.
                readResourceFile("__files/BuildingBlocks/oofCallbackServiceException");

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
        ar.setResourceInstanceName("testARInstanceName");
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
        ar2.setResourceInstanceName("testAR2InstanceName");
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
        vnf.setResourceInstanceName("testVnfInstanceName");
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
        serviceDecomposition.setServiceAllottedResources(arList);
        serviceDecomposition.setServiceVnfs(vnfList);
        serviceDecomposition.setServiceInstance(si);
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_success_2AR1Vnf() throws Exception {

        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oof2");

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");
        ServiceDecomposition serviceDecompositionExp = (ServiceDecomposition) getVariableFromHistory(businessKey,
                "serviceDecomposition");
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
        assertEquals(homingSolutionService("service", "testSIID1", "MDTNJ01",
                "aic", "dfwtx",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"," +
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2",
                "aic", "testCloudRegionId2", null), resourceARHoming2String);
        assertEquals(homingSolutionCloud("cloud", "aic",
                "testCloudRegionId3",
                "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"," +
                "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(verifyOofRequest(), expectedOofRequest);

    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_success_2AR1Vnf2Net() throws Exception {

        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables2(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oof3");

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");
        ServiceDecomposition serviceDecompositionExp = (ServiceDecomposition) getVariableFromHistory(businessKey,
                "serviceDecomposition");
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
        assertEquals(homingSolutionService("service", "testSIID1",
                "MDTNJ01",
                "aic", "dfwtx",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"," +
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2",
                "testVnfHostname2",
                "aic", "testCloudRegionId2", null),
                resourceARHoming2String);
        assertEquals(homingSolutionCloud("cloud", "aic",
                "testCloudRegionId3",
                "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"," +
                "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(homingSolutionService("service", "testServiceInstanceIdNet",
                "testVnfHostNameNet", "aic", "testCloudRegionIdNet",
                null), resourceNetHomingString);
        assertEquals(homingSolutionCloud("cloud", "aic",
                "testCloudRegionIdNet2",
                "\"f1d563e8-e714-4393-8f99-cc480144a05n\"," +
                          " \"j1d563e8-e714-4393-8f99-cc480144a05n\"," +
                        "\"s1d563e8-e714-4393-8f99-cc480144a05n\", " +
                        "\"b1d563e8-e714-4393-8f99-cc480144a05n\""),
                resourceNetHoming2String);
        assertEquals(verifyOofRequest(), expectedOofRequest);
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_success_vnfResourceList() throws Exception {

        // Create a Service Decomposition
        //System.out.println("At start of testHoming_success_vnfResourceList");
        MockGetServiceResourcesCatalogDataByModelUuid("2f7f309d-c842-4644-a2e4-34167be5eeb4",
                "/BuildingBlocks/oofCatalogResp.json");
        //MockGetServiceResourcesCatalogData("1cc4e2e4-eb6e-404d-a66f-c8733cedcce8",
        // "5.0", "/BuildingBlocks/catalogResp.json");
        String busKey = UUID.randomUUID().toString();
        Map<String, Object> vars = new HashMap<>();
        setVariablesForServiceDecomposition(vars, "testRequestId123",
                "ff5256d2-5a33-55df-13ab-12abad84e7ff");
        invokeSubProcess("DecomposeService", busKey, vars);

        ServiceDecomposition sd = (ServiceDecomposition) getVariableFromHistory(busKey,
                "serviceDecomposition");
        System.out.println("In testHoming_success_vnfResourceList, ServiceDecomposition = " + sd);
        List<VnfResource> vnfResourceList = sd.getServiceVnfs();
//System.out.println(" vnfResourceList = " + vnfResourceList);
        vnfResourceList.get(0).setResourceId("test-resource-id-000");

        // Invoke Homing

        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("homingSolution", "oof");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", sd);
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

        //Get Variables

        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");
        ServiceDecomposition serviceDecompositionExp = (ServiceDecomposition) getVariableFromHistory(businessKey,
                "serviceDecomposition");

        Resource resourceVnf = serviceDecompositionExp.getServiceResource("test-resource-id-000");
        HomingSolution resourceVnfHoming = resourceVnf.getHomingSolution();

        String resourceVnfHomingString = resourceVnfHoming.toString();
        resourceVnfHomingString = resourceVnfHomingString.replaceAll("\\s+", " ");

        assertNull(workflowException);

        //Verify request
        String oofRequest = (String) getVariableFromHistory(businessKey, "oofRequest");
        assertEquals(FileUtil.readResourceFile("__files/BuildingBlocks/oofRequest_infravnf").
                replaceAll("\n", "").replaceAll("\r", "").
                replaceAll("\t", ""), oofRequest.replaceAll("\n", "").
                replaceAll("\r", "").replaceAll("\t", ""));

        assertEquals(homingSolutionService("ALLOTTED_RESOURCE", "testSIID1",
                "MDTNJ01", "att-aic", "dfwtx",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\"," +
                        " \"j1d563e8-e714-4393-8f99-cc480144a05e\"," +
                        " \"s1d563e8-e714-4393-8f99-cc480144a05e\", " +
                        "\"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVnfHomingString);
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_error_inputVariable() throws Exception {

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables3(variables);

        invokeSubProcess("Homing", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=4000,errorMessage=A required " +
                "input variable is missing or null]", workflowException.toString());
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_error_badResponse() throws Exception {
        mockOof_500();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=500,errorMessage=Received a " +
                "Bad Sync Response from Sniro/OOF.]", workflowException.toString());
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_error_oofNoSolution() throws Exception {
        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oofNoSol");

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=400,errorMessage=No solution found " +
                "for plan 08e1b8cf-144a-4bac-b293-d5e2eedc97e8]", workflowException.toString());
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_error_oofPolicyException() throws Exception {
        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oofPolicyEx");

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=400,errorMessage=OOF Async Callback " +
                "Response contains a Request Error Policy Exception: Message content size exceeds the allowable " +
                "limit]", workflowException.toString());
    }

    @Test
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_error_oofServiceException() throws Exception {
        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "oofServiceEx");

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");

        assertEquals("WorkflowException[processKey=Homing,errorCode=400,errorMessage=OOF Async Callback " +
                        "Response contains a Request Error Service Exception: OOF PlacementError: " +
                        "requests.exceptions.HTTPError: 404 Client Error: Not Found for " +
                        "url: http://135.21.171.200:8091/v1/plans/97b4e303-5f75-492c-8fb2-21098281c8b8]",
                workflowException.toString());
    }


    private void setVariables(Map<String, Object> variables) {
        variables.put("homingSolution", "oof");
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
        variables.put("isDebugLogEnabled", "true");
        //	variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", serviceDecomposition);
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
        serviceDecomposition.setServiceNetworks(netList);

        variables.put("homingSolution", "oof");
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
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", serviceDecomposition);
    }

    private void setVariables3(Map<String, Object> variables) {
        variables.put("homingSolution", "oof");
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
        variables.put("isDebugLogEnabled", "true");
        //	variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", null);
    }

    private String homingSolutionService(String resourceModuleName, String serviceInstanceId, String vnfHostname, String cloudOwner,
                                         String cloudRegionId, String licenseList) {
        String solution = "";
        if (licenseList == null || licenseList == "") {
            solution = "{\n" +
                    "        \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "        \"serviceResourceId\": \"some_resource_id\",\n" +
                    "        \"solution\": {\n" +
                    "            \"identifierType\": \"serviceInstanceId\",\n" +
                    "            \"identifiers\": [\"" + serviceInstanceId + "\"]\n" +
                    "        }\n" +
                    "        \"assignmentInfo\": [\n" +
                    "          { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "          { \"key\": \"vnfHostName\", \"value\": \"" + vnfHostname + "\" },\n" +
                    "          { \"key\": \"isRehome\", \"value\": \"False\" },\n" +
                    "          { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "        ]\n" +
                    "      }";
        } else {
            solution = "{\n" +
                    "        \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "        \"serviceResourceId\": \"some_resource_id\",\n" +
                    "        \"solution\": {\n" +
                    "            \"identifierType\": \"service_instance_id\",\n" +
                    "            \"identifiers\": [\"" + serviceInstanceId + "\"]\n" +
                    "        }\n" +
                    "        \"assignmentInfo\": [\n" +
                    "          { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "          { \"key\": \"vnfHostName\", \"value\": \"" + vnfHostname + "\" },\n" +
                    "          { \"key\": \"isRehome\", \"value\": \"False\" },\n" +
                    "          { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "        ], " +
                    "        \"licenseSolutions\" : [ {\"licenseKeyGroupUUID\": [" + licenseList + "]} ] " +
                    "    }";
        }
        return solution;
    }

    private String homingSolutionCloud(String resourceModuleName, String cloudOwner,
                                       String cloudRegionId, String licenseList) {
        String solution = "";
        if (licenseList == null || licenseList == "") {
            solution = "{\n" +
                    "        \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "        \"serviceResourceId\": \"some_resource_id\",\n" +
                    "        \"solution\": {\n" +
                    "            \"identifierType\": \"cloudRegionId\",\n" +
                    "            \"cloudOwner\": \"" + cloudOwner + "\",\n" +
                    "            \"identifiers\": [\"" + cloudRegionId + "\"]\n" +
                    "        }\n" +
                    "        \"assignmentInfo\": [\n" +
                    "          { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "          { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "        ]\n" +
                    "    }";
        } else {
            solution = "{\n" +
                    "        \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "        \"serviceResourceId\": \"some_resource_id\",\n" +
                    "        \"solution\": {\n" +
                    "            \"identifierType\": \"cloudRegionId\",\n" +
                    "            \"cloudOwner\": \"" + cloudOwner + "\",\n" +
                    "            \"identifiers\": [\"" + cloudRegionId + "\"]\n" +
                    "        }\n" +
                    "        \"assignmentInfo\": [\n" +
                    "          { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "          { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "        ]," +
                    "        \"licenseSolutions\" : [ {\"licenseKeyGroupUUID\": [" + licenseList + "]} ] } " +
                    "    }";
        }
        return solution;
    }

    private void setVariablesForServiceDecomposition(Map<String, Object> variables, String requestId, String siId) {
        variables.put("homingSolution", "oof");
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


        String serviceModelInfo = "{\n" +
                "      \"modelInvariantId\": \"1cc4e2e4-eb6e-404d-a66f-c8733cedcce8\",\n" +
                "      \"modelUuid\": \"2f7f309d-c842-4644-a2e4-34167be5eeb4\",\n" +
                "      \"modelName\": \"vCPE Service\",\n" +
                "      \"modelVersion\": \"2.0\",\n" +
                "    }";
        variables.put("serviceModelInfo", serviceModelInfo);
    }

    private String verifyOofRequest() {
        String request = "{\n" +
                "  \"requestInfo\": {\n" +
                "    \"transactionId\": \"testRequestId\",\n" +
                "    \"requestId\": \"testRequestId\",\n" +
                "    \"callbackUrl\": \"https://so:5000/callbackUrl\",\n" +
                "    \"sourceId\": \"so\",\n" +
                "    \"requestType\": \"create\",\n" +
                "    \"numSolutions\": 1,\n" +
                "    \"optimizers\": [\"placement\"],\n" +
                "    \"timeout\": 600\n" +
                "  },\n" +
                "  \"placementInfo\": {\n" +
                "    \"requestParameters\": { \"customerLatitude\": 32.89748, \"customerLongitude\": -97.040443, " +
                "\"customerName\": \"xyz\" },\n" +
                "    \"placementDemands\": [\n" +
                "      {\n" +
                "        \"resourceModuleName\": \"vGMuxInfra\",\n" +
                "        \"serviceResourceId\": \"vGMuxInfra-xx\",\n" +
                "        \"tenantId\": \"vGMuxInfra-tenant\",\n" +
                "        \"resourceModelInfo\": {\n" +
                "          \"modelInvariantId\": \"vGMuxInfra-modelInvariantId\",\n" +
                "          \"modelVersionId\": \"vGMuxInfra-versionId\",\n" +
                "          \"modelName\": \"vGMuxInfra-model\",\n" +
                "          \"modelType\": \"resource\",\n" +
                "          \"modelVersion\": \"1.0\",\n" +
                "          \"modelCustomizationName\": \"vGMuxInfra-customeModelName\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"resourceModuleName\": \"vG\",\n" +
                "        \"serviceResourceId\": \"71d563e8-e714-4393-8f99-cc480144a05e\",\n" +
                "        \"tenantId\": \"vG-tenant\",\n" +
                "        \"resourceModelInfo\": {\n" +
                "          \"modelInvariantId\": \"vG-modelInvariantId\",\n" +
                "          \"modelVersionId\": \"vG-versionId\",\n" +
                "          \"modelName\": \"vG-model\",\n" +
                "          \"modelType\": \"resource\",\n" +
                "          \"modelVersion\": \"1.0\",\n" +
                "          \"modelCustomizationName\": \"vG-customeModelName\"\n" +
                "        },\n" +
                "        \"existingCandidates\": [\n" +
                "          {\n" +
                "            \"identifierType\": \"serviceInstanceId\",\n" +
                "            \"cloudOwner\": \"\",\n" +
                "            \"identifiers\": [\"gjhd-098-fhd-987\"]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"excludedCandidates\": [\n" +
                "          {\n" +
                "            \"identifierType\": \"serviceInstanceId\",\n" +
                "            \"cloudOwner\": \"\",\n" +
                "            \"identifiers\": [\"gjhd-098-fhd-987\"]\n" +
                "          },\n" +
                "          {\n" +
                "            \"identifierType\": \"vimId\",\n" +
                "            \"cloudOwner\": \"vmware\",\n" +
                "            \"identifiers\": [\"NYMDT67\"]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"requiredCandidates\": [\n" +
                "          {\n" +
                "            \"identifierType\": \"vimId\",\n" +
                "            \"cloudOwner\": \"amazon\",\n" +
                "            \"identifiers\": [\"TXAUS219\"]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"serviceInfo\": {\n" +
                "    \"serviceInstanceId\": \"d61b2543-5914-4b8f-8e81-81e38575b8ec\",\n" +
                "    \"serviceName\": \"vCPE\",\n" +
                "    \"modelInfo\": {\n" +
                "      \"modelInvariantId\": \"vCPE-invariantId\",\n" +
                "      \"modelVersionId\": \"vCPE-versionId\",\n" +
                "      \"modelName\": \"vCPE-model\",\n" +
                "      \"modelType\": \"service\",\n" +
                "      \"modelVersion\": \"1.0\",\n" +
                "      \"modelCustomizationName\": \"vCPE-customeModelName\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"licenseInfo\": {\n" +
                "    \"licenseDemands\": [\n" +
                "      {\n" +
                "        \"resourceModuleName\": \"vGMuxInfra\",\n" +
                "        \"serviceResourceId\": \"vGMuxInfra-xx\",\n" +
                "        \"resourceModelInfo\": {\n" +
                "          \"modelInvariantId\": \"vGMuxInfra-modelInvariantId\",\n" +
                "          \"modelVersionId\": \"vGMuxInfra-versionId\",\n" +
                "          \"modelName\": \"vGMuxInfra-model\",\n" +
                "          \"modelType\": \"resource\",\n" +
                "          \"modelVersion\": \"1.0\",\n" +
                "          \"modelCustomizationName\": \"vGMuxInfra-customeModelName\"\n" +
                "        },\n" +
                "        \"existingLicenses\": {\n" +
                "          \"entitlementPoolUUID\": [\"87257b49-9602-4ca1-9817-094e52bc873b\", " +
                "\"43257b49-9602-4fe5-9337-094e52bc9435\"],\n" +
                "          \"licenseKeyGroupUUID\": [\"87257b49-9602-4ca1-9817-094e52bc873b\", " +
                "\"43257b49-9602-4fe5-9337-094e52bc9435\"]\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        return request;
    }

}
