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
import org.junit.Ignore;
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
    String subscriber = "";
    String subscriber2 = "";

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

        // Subscriber
        subscriber = "{\"globalSubscriberId\": \"SUB12_0322_DS_1201\",\"subscriberCommonSiteId\": \"DALTX0101\",\"subscriberName\": \"SUB_12_0322_DS_1201\"}";
        subscriber2 = "{\"globalSubscriberId\": \"SUB12_0322_DS_1201\",\"subscriberName\": \"SUB_12_0322_DS_1201\"}";
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
                resourceARHoming.getVnf().getResourceId(),"aic", "dfwtx",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2",
                resourceARHoming2.getVnf().getResourceId(),"aic", "testCloudRegionId2",
                null, null), resourceARHoming2String);
        assertEquals(homingSolutionCloud("cloud","aic", "testCloudRegionId3",
                "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
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
        assertEquals(homingSolutionService("service", "testSIID1", "MDTNJ01",
                resourceARHoming.getVnf().getResourceId(),"aic", "dfwtx",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2",
                resourceARHoming2.getVnf().getResourceId(),
                "aic", "testCloudRegionId2",
                null, null), resourceARHoming2String);
        assertEquals(homingSolutionCloud("cloud","aic",
                "testCloudRegionId3",
                "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(homingSolutionService("service", "testServiceInstanceIdNet",
                "testVnfHostNameNet", resourceNetHoming.getVnf().getResourceId(),"aic",
                "testCloudRegionIdNet",
                null, null), resourceNetHomingString);
        assertEquals(homingSolutionCloud("cloud", "aic",
                "testCloudRegionIdNet2",
                "\"f1d563e8-e714-4393-8f99-cc480144a05n\", \"j1d563e8-e714-4393-8f99-cc480144a05n\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05n\", \"b1d563e8-e714-4393-8f99-cc480144a05n\""),
                resourceNetHoming2String);
        assertEquals(verifyOofRequest(), expectedOofRequest);

    }

    @Test
    @Ignore
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/BuildingBlock/DecomposeService.bpmn",
            "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_success_vnfResourceList() throws Exception {

        // Create a Service Decomposition
        MockGetServiceResourcesCatalogDataByModelUuid("2f7f309d-c842-4644-a2e4-34167be5eeb4",
                "/BuildingBlocks/oofCatalogResp.json");
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

        //Get Variables

        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey,
                "WorkflowException");
        ServiceDecomposition serviceDecompositionExp = (ServiceDecomposition) getVariableFromHistory(businessKey,
                "serviceDecomposition");
        System.out.println("serviceDecompositionExp is: " + serviceDecompositionExp);

        Resource resourceVnf = serviceDecompositionExp.getServiceResource("test-resource-id-000");
        System.out.println("resourceVnf is: " + resourceVnf);
        HomingSolution resourceVnfHoming = resourceVnf.getHomingSolution();

        String resourceVnfHomingString = resourceVnfHoming.toString();
        System.out.println("resourceVnfHomingString is: " + resourceVnfHomingString);
        resourceVnfHomingString = resourceVnfHomingString.replaceAll("\\s+", " ");
        System.out.println("Now resourceVnfHomingString is: " + resourceVnfHomingString);

        assertNull(workflowException);

        //Verify request
        String oofRequest = (String) getVariableFromHistory(businessKey, "oofRequest");
        System.out.println("oofRequest is: " + oofRequest);
        assertEquals(FileUtil.readResourceFile("__files/BuildingBlocks/oofRequest_infravnf").
                replaceAll("\n", "").replaceAll("\r", "").
                replaceAll("\t", ""), oofRequest.replaceAll("\n", "").
                replaceAll("\r", "").replaceAll("\t", ""));

        //System.out.println("resourceVnfHoming.getVnf().getResourceId() is: " + resourceVnfHoming.getVnf().getResourceId());

        assertEquals(homingSolutionService("service", "service-instance-01234",
                "MDTNJ01", "test-resource-id-000","att-aic",
                "mtmnj1a",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\"," +
                        " \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\"," +
                        " \"b1d563e8-e714-4393-8f99-cc480144a05e\""), resourceVnfHomingString);
    }

    @Test
    @Ignore // 1802 merge
    @Deployment(resources = {"subprocess/BuildingBlock/Homing.bpmn", "subprocess/ReceiveWorkflowMessage.bpmn"})
    public void testHoming_success_existingLicense() throws Exception {

        mockOof();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<String, Object>();
        setVariablesExistingLicense(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniro");

        waitForProcessEnd(businessKey, 10000);

        //Get Variables
        WorkflowException workflowException = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp = (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
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
        assertEquals(homingSolutionService("service", "testSIID1", "MDTNJ01",
                "aic", "dfwtx", "KDTNJ01",
                "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2",
                resourceARHoming2.getVnf().getResourceId(),"aic", "testCloudRegionId2",
                null, null), resourceARHoming2String);
        assertEquals(homingSolutionCloud("cloud", "aic",
                "testCloudRegionId3",
                "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(verifyOofRequestExistingLicense(), oofRequest);

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
        variables.put("isDebugLogEnabled", "true");
        //	variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
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
        serviceDecomposition.setServiceNetworks(netList);

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
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
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
        variables.put("isDebugLogEnabled", "true");
        //	variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", null);
        variables.put("subscriberInfo", subscriber2);
    }

    private void setVariablesExistingLicense(Map<String, Object> variables) {
        HomingSolution currentHomingSolution = new HomingSolution();
        serviceDecomposition.getServiceVnfs().get(0).setCurrentHomingSolution(currentHomingSolution);
        serviceDecomposition.getServiceVnfs().get(0).getCurrentHomingSolution().getLicense().addEntitlementPool("testEntitlementPoolId1");
        serviceDecomposition.getServiceVnfs().get(0).getCurrentHomingSolution().getLicense().addEntitlementPool("testEntitlementPoolId2");

        serviceDecomposition.getServiceVnfs().get(0).getCurrentHomingSolution().getLicense().addLicenseKeyGroup("testLicenseKeyGroupId1");
        serviceDecomposition.getServiceVnfs().get(0).getCurrentHomingSolution().getLicense().addLicenseKeyGroup("testLicenseKeyGroupId2");

        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", serviceDecomposition);
        variables.put("subscriberInfo", subscriber2);

    }

    /*private String homingSolutionService(String resourceModuleName, String serviceInstanceId, String vnfHostname, String cloudOwner,
                                         String cloudRegionId, String licenseList) {
        String solution = "";
        if (licenseList == null || licenseList == "") {
            solution = "{\n" +
                    "  \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "  \"serviceResourceId\": \"some_resource_id\",\n" +
                    "  \"solution\": {\n" +
                    "  \"identifierType\": \"serviceInstanceId\",\n" +
                    "  \"identifiers\": [\"" + serviceInstanceId + "\"]\n" +
                    "  }\n" +
                    "  \"assignmentInfo\": [\n" +
                    "    { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "    { \"key\": \"vnfHostName\", \"value\": \"" + vnfHostname + "\" },\n" +
                    "    { \"key\": \"isRehome\", \"value\": \"False\" },\n" +
                    "    { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "    ]\n" +
                    "  }";
        } else {
            solution = "{\n" +
                    "  \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "  \"serviceResourceId\": \"some_resource_id\",\n" +
                    "  \"solution\": {\n" +
                    "    \"identifierType\": \"service_instance_id\",\n" +
                    "    \"identifiers\": [\"" + serviceInstanceId + "\"]\n" +
                    "  }\n" +
                    "  \"assignmentInfo\": [\n" +
                    "    { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "    { \"key\": \"vnfHostName\", \"value\": \"" + vnfHostname + "\" },\n" +
                    "    { \"key\": \"isRehome\", \"value\": \"False\" },\n" +
                    "    { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "    ], " +
                    "  \"licenseSolutions\" : [ {\"licenseKeyGroupUUID\": [" + licenseList + "]} ] " +
                    "}";
        }
        return solution;
    }*/
    private String homingSolutionService(String type, String serviceInstanceId, String vnfHostname,
                                         String vnfResourceId, String cloudOwner,
                                         String cloudRegionId, String enList,
                                         String licenseList){

        String solution = "";
        if(enList == null){
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"serviceInstanceId\" : \"" +
                    serviceInstanceId + "\", \"cloudOwner\" : \"" + cloudOwner + "\", \"cloudRegionId\" : \"" +
                    cloudRegionId + "\", " + "\"vnf\" : { \"resourceId\" : \"" + vnfResourceId +
                    "\", \"resourceType\" : \"VNF\", \"resourceInstance\" : { }, \"homingSolution\" : { \"license\" :" +
                    " { }, \"rehome\" : false }, \"vnfHostname\" : \"" + vnfHostname + "\" }, \"license\" : { }," +
                    " \"rehome\" : false } }";
        }else{
            //language=JSON
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"serviceInstanceId\" : \"" +
                    serviceInstanceId + "\", \"cloudOwner\" : \"" + cloudOwner + "\", \"cloudRegionId\" : \"" +
                    cloudRegionId + "\", \"vnf\" : { \"resourceId\" : \"" + vnfResourceId + "\", \"resourceType\" :" +
                    " \"VNF\", \"resourceInstance\" : { }, \"homingSolution\" : { \"license\" : { }, \"rehome\" :" +
                    " false }, \"vnfHostname\" : \"" + vnfHostname + "\" }, \"license\" : { \"entitlementPoolList\" :" +
                    " [ " + enList + " ], \"licenseKeyGroupList\" : [ " + licenseList + " ] }, \"rehome\" : false } }";
        }
        return solution;
    }

    /*private String homingSolutionCloud(String resourceModuleName, String cloudOwner,
                                       String cloudRegionId, String licenseList) {
        String solution = "";
        if (licenseList == null || licenseList == "") {
            solution = "{\n" +
                    "  \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "  \"serviceResourceId\": \"some_resource_id\",\n" +
                    "  \"solution\": {\n" +
                    "    \"identifierType\": \"cloudRegionId\",\n" +
                    "    \"cloudOwner\": \"" + cloudOwner + "\",\n" +
                    "    \"identifiers\": [\"" + cloudRegionId + "\"]\n" +
                    "  }\n" +
                    "  \"assignmentInfo\": [\n" +
                    "    { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "    { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "    ]\n" +
                    "}";
        } else {
            solution = "{\n" +
                    "  \"resourceModuleName\": \"" + resourceModuleName + "\",\n" +
                    "  \"serviceResourceId\": \"some_resource_id\",\n" +
                    "  \"solution\": {\n" +
                    "    \"identifierType\": \"cloudRegionId\",\n" +
                    "    \"cloudOwner\": \"" + cloudOwner + "\",\n" +
                    "    \"identifiers\": [\"" + cloudRegionId + "\"]\n" +
                    "  }\n" +
                    "  \"assignmentInfo\": [\n" +
                    "    { \"key\": \"cloudOwner\", \"value\": \"" + cloudOwner + "\" },\n" +
                    "    { \"key\": \"cloudRegionId\", \"value\": \"" + cloudRegionId + "\" }\n" +
                    "    ]," +
                    "  \"licenseSolutions\" : [ {\"licenseKeyGroupUUID\": [" + licenseList + "]} ] } " +
                    "}";
        }
        return solution;
    }*/
    private String homingSolutionCloud(String type, String cloudOwner,
                                       String cloudRegionId, String enList,
                                       String licenseList){
        String solution = "";
        if(enList == null){
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" +
                    cloudOwner + "\", \"cloudRegionId\" : \"" + cloudRegionId +
                    "\", \"license\" : { }, \"rehome\" : false } }";
        }else{
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" +
                    cloudOwner + "\", \"cloudRegionId\" : \"" + cloudRegionId +
                    "\", \"license\" : { \"entitlementPoolList\" : [ " + enList +  " ], \"licenseKeyGroupList\" : [ " +
                    licenseList +  " ] }, \"rehome\" : false } }";
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


        String serviceModelInfo = "{\"modelInvariantId\":\"1cc4e2e4-eb6e-404d-a66f-c8733cedcce8\",\"modelUuid\":" +
                "\"2f7f309d-c842-4644-a2e4-34167be5eeb4\",\"modelName\":\"vCPE Service\",\"modelVersion\":\"2.0\",}";
        variables.put("serviceModelInfo", serviceModelInfo);
    }

    private String verifyOofRequest() {
        String request = "{\"requestInfo\":{\"transactionId\":\"testRequestId\",\"requestId\":\"testRequestId\"," +
                "\"callbackUrl\":\"http://localhost:28090/workflows/messages/message/oofResponse/testRequestId\"," +
                "\"sourceId\":\"so\",\"requestType\":\"create\",\"numSolutions\":1,\"optimizers\":[\"placement\"]," +
                "\"timeout\":600},\"placementInfo\":{\"requestParameters\":{\"customerLatitude\":" +
                "\"32.89748\",\"customerLongitude\":\"-97.040443\",\"customerName\":\"xyz\"},\"subscriberInfo\":" +
                "{\"globalSubscriberId\":\"SUB12_0322_DS_1201\",\"subscriberName\":\"SUB_12_0322_DS_1201\"," +
                "\"subscriberCommonSiteId\":\"\"},\"placementDemands\":[{\"resourceModuleName\":\"ALLOTTED_RESOURCE\"" +
                ",\"serviceResourceId\":\"testResourceIdAR\",\"tenantId\":" +
                "\"null\",\"resourceModelInfo\":{\"modelInvariantId\":\"testModelInvariantIdAR\"," +
                "\"modelVersionId\":\"testARModelUuid\",\"modelName\":\"testModelNameAR\",\"modelType\":" +
                "\"testModelTypeAR\",\"modelVersion\":\"testModelVersionAR\",\"modelCustomizationName\":\"\"}}," +
                "{\"resourceModuleName\":\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR2\"," +
                "\"tenantId\":\"null\",\"resourceModelInfo\":{\"modelInvariantId\":\"testModelInvariantIdAR2\"," +
                "\"modelVersionId\":\"testAr2ModelUuid\",\"modelName\":\"testModelNameAR2\"," +
                "\"modelType\":\"testModelTypeAR2\",\"modelVersion\":\"testModelVersionAR2\"," +
                "\"modelCustomizationName\":\"\"}}]},\"serviceInfo\":" +
                "{\"serviceInstanceId\":\"testServiceInstanceId123\"," +
                "\"serviceName\":\"null\",\"modelInfo\":{\"modelType\":\"\",\"modelInvariantId\":" +
                "\"testModelInvariantId\",\"modelVersionId\":\"testModelUuid\",\"modelName\":\"testModelName\"," +
                "\"modelVersion\":\"testModelVersion\",\"modelCustomizationName\":\"" +
                "\"}},\"licenseInfo\":{\"licenseDemands\":[{\"resourceModuleName\":\"VNF\",\"serviceResourceId\":" +
                "\"testResourceIdVNF\",\"resourceInstanceType\":\"VNF\",\"resourceModelInfo\":{\"modelInvariantId\":" +
                "\"testModelInvariantIdVNF\",\"modelVersionId\":\"testVnfModelUuid\",\"modelName\":" +
                "\"testModelNameVNF\",\"modelType\":\"testModelTypeVNF\",\"modelVersion\":\"testModelVersionVNF\"," +
                "\"modelCustomizationName\":\"\"}}]}}";
        return request;
    }

    private String verifyOofRequestExistingLicense(){
        String request = "{\"requestInfo\":{\"transactionId\":\"testRequestId\",\"requestId\":\"testRequestId\"," +
                "\"callbackUrl\":\"http://localhost:28090/workflows/messages/message/SNIROResponse/testRequestId\"," +
                "\"sourceId\":\"mso\",\"requestType\":\"speedchanged\",\"optimizer\":[\"placement\",\"license\"]," +
                "\"numSolutions\":1,\"timeout\":1800},\"placementInfo\":{\"serviceModelInfo\":{\"modelType\":\"\"," +
                "\"modelInvariantId\":\"testModelInvariantId\",\"modelVersionId\":\"testModelUuid\",\"modelName\":" +
                "\"testModelName\",\"modelVersion\":\"testModelVersion\"},\"subscriberInfo\":" +
                "{\"globalSubscriberId\":\"SUB12_0322_DS_1201\",\"subscriberName\":\"SUB_12_0322_DS_1201\"," +
                "\"subscriberCommonSiteId\":\"\"},\"demandInfo\":{\"placementDemand\":[{\"resourceInstanceType\":" +
                "\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR\",\"resourceModuleName\":\"\"," +
                "\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR\"," +
                "\"modelInvariantId\":\"testModelInvariantIdAR\",\"modelName\":\"testModelNameAR\"," +
                "\"modelVersion\":\"testModelVersionAR\",\"modelVersionId\":\"testARModelUuid\",\"modelType\":" +
                "\"testModelTypeAR\"},\"tenantId\":\"\",\"tenantName\":\"\"},{\"resourceInstanceType\":" +
                "\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR2\",\"resourceModuleName\":" +
                "\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR2\"," +
                "\"modelInvariantId\":\"testModelInvariantIdAR2\",\"modelName\":\"testModelNameAR2\"," +
                "\"modelVersion\":\"testModelVersionAR2\",\"modelVersionId\":\"testAr2ModelUuid\"," +
                "\"modelType\":\"testModelTypeAR2\"},\"tenantId\":\"\",\"tenantName\":\"\"}],\"licenseDemand\":" +
                "[{\"resourceInstanceType\":\"VNF\",\"serviceResourceId\":\"testResourceIdVNF\"," +
                "\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":" +
                "\"testModelCustomizationUuidVNF\",\"modelInvariantId\":\"testModelInvariantIdVNF\"," +
                "\"modelName\":\"testModelNameVNF\",\"modelVersion\":\"testModelVersionVNF\"," +
                "\"modelVersionId\":\"testVnfModelUuid\",\"modelType\":\"testModelTypeVNF\"}," +
                "\"existingLicense\":[{\"entitlementPoolUUID\":[\"testEntitlementPoolId1\"," +
                "\"testEntitlementPoolId2\"],\"licenseKeyGroupUUID\":[\"testLicenseKeyGroupId1\"," +
                "\"testLicenseKeyGroupId2\"]}]}]},\"policyId\":[],\"serviceInstanceId\":" +
                "\"testServiceInstanceId123\",\"orderInfo\":\"{\\\"requestParameters\\\":null}\"}}";
        return request;
    }
}
