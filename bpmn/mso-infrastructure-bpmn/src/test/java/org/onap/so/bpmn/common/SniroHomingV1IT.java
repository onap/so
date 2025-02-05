/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.*;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onap.so.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogDataByModelUuid;
import static org.onap.so.bpmn.mock.StubResponseSNIRO.mockSNIRO;
import static org.onap.so.bpmn.mock.StubResponseSNIRO.mockSNIRO_500;



/**
 * Test the SNIRO Homing subflow building block.
 */
@Ignore
public class SniroHomingV1IT extends BaseIntegrationTest {

    Logger logger = LoggerFactory.getLogger(SniroHomingV1IT.class);


    ServiceDecomposition serviceDecomposition = new ServiceDecomposition();
    String subscriber = "";
    String subscriber2 = "";

    private final CallbackSet callbacks = new CallbackSet();

    public SniroHomingV1IT() throws IOException {
        String sniroCallback = FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallback2AR1Vnf");
        String sniroCallback2 = FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallback2AR1Vnf2Net");
        String sniroCallback3 = FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackInfraVnf");
        String sniroCallbackNoSolution =
                FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackNoSolutionFound");
        String sniroCallbackPolicyException =
                FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackPolicyException");
        String sniroCallbackServiceException =
                FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackServiceException");
        callbacks.put("sniro", JSON, "SNIROResponse", sniroCallback);
        callbacks.put("sniro2", JSON, "SNIROResponse", sniroCallback2);
        callbacks.put("sniro3", JSON, "SNIROResponse", sniroCallback3);
        callbacks.put("sniroNoSol", JSON, "SNIROResponse", sniroCallbackNoSolution);
        callbacks.put("sniroPolicyEx", JSON, "SNIROResponse", sniroCallbackPolicyException);
        callbacks.put("sniroServiceEx", JSON, "SNIROResponse", sniroCallbackServiceException);

        // Service Model
        ModelInfo sModel = new ModelInfo();
        sModel.setModelCustomizationUuid("testModelCustomizationUuid");
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
        logger.debug("SERVICE DECOMP: {}", serviceDecomposition.getServiceResourcesJsonString());
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
    // 1802 merge

    public void testHoming_success_2AR1Vnf() throws Exception {

        mockSNIRO(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniro");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
        String expectedSniroRequest = (String) getVariableFromHistory(businessKey, "sniroRequest");

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
        expectedSniroRequest = expectedSniroRequest.replaceAll("\\s+", "");

        assertNull(workflowException);
        assertEquals(
                homingSolutionService("service", "testSIID1", "MDTNJ01", "aic", "dfwtx", "KDTNJ01", "3.0",
                        "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2", "aic", "testCloudRegionId2",
                "testAicClli2", "3.0", null, null), resourceARHoming2String);
        assertEquals(
                homingSolutionCloud("cloud", "", "", "aic", "testCloudRegionId3", "testAicClli3", "3.0",
                        "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(verifySniroRequest(), expectedSniroRequest);

    }

    @Test
    // 1802 merge

    public void testHoming_success_2AR1Vnf2Net() throws Exception {

        mockSNIRO(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables2(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniro2");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");
        String expectedSniroRequest = (String) getVariableFromHistory(businessKey, "sniroRequest");

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
        expectedSniroRequest = expectedSniroRequest.replaceAll("\\s+", "");

        assertNull(workflowException);
        assertEquals(
                homingSolutionService("service", "testSIID1", "MDTNJ01", "aic", "dfwtx", "KDTNJ01", "3.0",
                        "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2", "aic", "testCloudRegionId2",
                "testAicClli2", "3.0", null, null), resourceARHoming2String);
        assertEquals(
                homingSolutionCloud("cloud", "", "", "aic", "testCloudRegionId3", "testAicClli3", "3.0",
                        "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(homingSolutionService("service", "testServiceInstanceIdNet", "testVnfHostNameNet", "aic",
                "testCloudRegionIdNet", "testAicClliNet", "3.0", null, null), resourceNetHomingString);
        assertEquals(
                homingSolutionCloud("cloud", "", "", "aic", "testCloudRegionIdNet2", "testAicClliNet2", "3.0",
                        "\"f1d563e8-e714-4393-8f99-cc480144a05n\", \"j1d563e8-e714-4393-8f99-cc480144a05n\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05n\", \"b1d563e8-e714-4393-8f99-cc480144a05n\""),
                resourceNetHoming2String);
        assertEquals(verifySniroRequest(), expectedSniroRequest);
    }

    @Test
    // 1802 merge

    public void testHoming_success_vnfResourceList() throws Exception {

        // Create a Service Decomposition
        MockGetServiceResourcesCatalogDataByModelUuid(wireMockServer, "2f7f309d-c842-4644-a2e4-34167be5eeb4",
                "/BuildingBlocks/catalogResp.json");
        String busKey = UUID.randomUUID().toString();
        Map<String, Object> vars = new HashMap<>();
        setVariablesForServiceDecomposition(vars, "testRequestId123", "ff5256d2-5a33-55df-13ab-12abad84e7ff");
        invokeSubProcess("DecomposeService", busKey, vars);

        ServiceDecomposition sd = (ServiceDecomposition) getVariableFromHistory(busKey, "serviceDecomposition");
        List<VnfResource> vnfResourceList = sd.getVnfResources();
        vnfResourceList.get(0).setResourceId("test-resource-id-000");

        // Invoke Homing

        mockSNIRO(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("homingService", "sniro");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", sd);
        variables.put("subscriberInfo", subscriber2);

        invokeSubProcess("Homing", businessKey, variables);
        injectWorkflowMessages(callbacks, "sniro3");
        waitForProcessEnd(businessKey, 10000);

        // Get Variables

        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        ServiceDecomposition serviceDecompositionExp =
                (ServiceDecomposition) getVariableFromHistory(businessKey, "serviceDecomposition");

        Resource resourceVnf = serviceDecompositionExp.getServiceResource("test-resource-id-000");
        HomingSolution resourceVnfHoming = resourceVnf.getHomingSolution();

        String resourceVnfHomingString = resourceVnfHoming.toString();
        resourceVnfHomingString = resourceVnfHomingString.replaceAll("\\s+", " ");

        assertNull(workflowException);

        // Verify request
        String sniroRequest = (String) getVariableFromHistory(businessKey, "sniroRequest");
        assertEquals(
                FileUtil.readResourceFile("__files/BuildingBlocks/sniroRequest_infravnf").replaceAll("\n", "")
                        .replaceAll("\r", "").replaceAll("\t", ""),
                sniroRequest.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", ""));

        assertEquals(homingSolutionService("service", "service-instance-01234", "MDTNJ01", "CloudOwner", "mtmnj1a",
                "KDTNJ01", "3.0", "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVnfHomingString);
    }

    @Test

    public void testHoming_success_existingLicense() throws Exception {

        mockSNIRO(wireMockServer);

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
        String sniroRequest = (String) getVariableFromHistory(businessKey, "sniroRequest");

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
        sniroRequest = sniroRequest.replaceAll("\\s+", "");

        assertNull(workflowException);
        assertEquals(
                homingSolutionService("service", "testSIID1", "MDTNJ01", "aic", "dfwtx", "KDTNJ01", "3.0",
                        "\"f1d563e8-e714-4393-8f99-cc480144a05e\", \"j1d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"s1d563e8-e714-4393-8f99-cc480144a05e\", \"b1d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceARHomingString);
        assertEquals(homingSolutionService("service", "testSIID2", "testVnfHostname2", "aic", "testCloudRegionId2",
                "testAicClli2", "3.0", null, null), resourceARHoming2String);
        assertEquals(
                homingSolutionCloud("cloud", "", "", "aic", "testCloudRegionId3", "testAicClli3", "3.0",
                        "\"91d563e8-e714-4393-8f99-cc480144a05e\", \"21d563e8-e714-4393-8f99-cc480144a05e\"",
                        "\"31d563e8-e714-4393-8f99-cc480144a05e\", \"71d563e8-e714-4393-8f99-cc480144a05e\""),
                resourceVNFHomingString);
        assertEquals(verifySniroRequest_existingLicense(), sniroRequest);

    }


    @Test

    public void testHoming_error_inputVariable() throws Exception {

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables3(variables);

        invokeSubProcess("Homing", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=4000,errorMessage=A required input variable is missing or null]",
                workflowException.toString());
    }

    @Test

    public void testHoming_error_badResponse() throws Exception {
        mockSNIRO_500(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=500,errorMessage=Received a Bad Sync Response from Sniro/OOF.]",
                workflowException.toString());
    }

    @Test
    // 1802 merge

    public void testHoming_error_sniroNoSolution() throws Exception {
        mockSNIRO(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniroNoSol");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=400,errorMessage=No solution found for plan 08e1b8cf-144a-4bac-b293-d5e2eedc97e8]",
                workflowException.toString());
    }

    @Test

    public void testHoming_error_sniroPolicyException() throws Exception {
        mockSNIRO(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniroPolicyEx");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=400,errorMessage=Sniro Async Callback Response contains a Request Error Policy Exception: Message content size exceeds the allowable limit]",
                workflowException.toString());
    }

    @Test

    public void testHoming_error_sniroServiceException() throws Exception {
        mockSNIRO(wireMockServer);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        setVariables(variables);

        invokeSubProcess("Homing", businessKey, variables);

        injectWorkflowMessages(callbacks, "sniroServiceEx");

        waitForProcessEnd(businessKey, 10000);

        // Get Variables
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");

        assertEquals(
                "WorkflowException[processKey=Homing,errorCode=400,errorMessage=Sniro Async Callback Response contains a Request Error Service Exception: SNIROPlacementError: requests.exceptions.HTTPError: 404 Client Error: Not Found for url: http://135.21.171.200:8091/v1/plans/97b4e303-5f75-492c-8fb2-21098281c8b8]",
                workflowException.toString());
    }



    private void setVariables(Map<String, Object> variables) {
        variables.put("homingService", "sniro");
        variables.put("isDebugLogEnabled", "true");
        // variables.put("mso-request-id", "testRequestId");
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
        netModel2.setModelInvariantUuid("testModelInvariantIdNet2");
        netModel2.setModelName("testModelNameNet2");
        netModel2.setModelVersion("testModelVersionNet2");
        net2.setModelInfo(netModel2);
        netList.add(net2);
        serviceDecomposition.setNetworkResources(netList);

        variables.put("homingService", "sniro");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", serviceDecomposition);
        variables.put("subscriberInfo", subscriber2);
    }

    private void setVariables3(Map<String, Object> variables) {
        variables.put("homingService", "sniro");
        variables.put("isDebugLogEnabled", "true");
        // variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
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
        // variables.put("mso-request-id", "testRequestId");
        variables.put("msoRequestId", "testRequestId");
        variables.put("serviceInstanceId", "testServiceInstanceId");
        variables.put("serviceDecomposition", serviceDecomposition);
        variables.put("subscriberInfo", subscriber2);

    }

    private String homingSolutionService(String type, String serviceInstanceId, String vnfHostname, String cloudOwner,
            String cloudRegionId, String aicClli, String aicVersion, String enList, String licenseList) {
        String solution = "";
        if (enList == null) {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"serviceInstanceId\" : \""
                    + serviceInstanceId + "\", \"vnfHostname\" : \"" + vnfHostname + "\", \"cloudOwner\" : \""
                    + cloudOwner + "\", \"cloudRegionId\" : \"" + cloudRegionId + "\", \"aicClli\" : \"" + aicClli
                    + "\", \"aicVersion\" : \"" + aicVersion + "\", \"license\" : { }, \"rehome\" : false } }";
        } else {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"serviceInstanceId\" : \""
                    + serviceInstanceId + "\", \"vnfHostname\" : \"" + vnfHostname + "\", \"cloudOwner\" : \""
                    + cloudOwner + "\", \"cloudRegionId\" : \"" + cloudRegionId + "\", \"aicClli\" : \"" + aicClli
                    + "\", \"aicVersion\" : \"" + aicVersion + "\", \"license\" : { \"entitlementPoolList\" : [ "
                    + enList + " ], \"licenseKeyGroupList\" : [ " + licenseList + " ] }, \"rehome\" : false } }";
        }
        return solution;
    }

    private String homingSolutionCloud(String type, String serviceInstanceId, String vnfHostname, String cloudOwner,
            String cloudRegionId, String aicClli, String aicVersion, String enList, String licenseList) {
        String solution = "";
        if (enList == null) {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" + cloudOwner
                    + "\", \"cloudRegionId\" : \"" + cloudRegionId + "\", \"aicClli\" : \"" + aicClli
                    + "\", \"aicVersion\" : \"" + aicVersion + "\", \"license\" : { }, \"rehome\" : false } }";
        } else {
            solution = "{ \"homingSolution\" : { \"inventoryType\" : \"" + type + "\", \"cloudOwner\" : \"" + cloudOwner
                    + "\", \"cloudRegionId\" : \"" + cloudRegionId + "\", \"aicClli\" : \"" + aicClli
                    + "\", \"aicVersion\" : \"" + aicVersion + "\", \"license\" : { \"entitlementPoolList\" : [ "
                    + enList + " ], \"licenseKeyGroupList\" : [ " + licenseList + " ] }, \"rehome\" : false } }";
        }
        return solution;
    }

    private void setVariablesForServiceDecomposition(Map<String, Object> variables, String requestId, String siId) {
        variables.put("homingService", "sniro");
        variables.put("isDebugLogEnabled", "true");
        variables.put("mso-request-id", requestId);
        variables.put("msoRequestId", requestId);
        variables.put("serviceInstanceId", siId);

        String serviceModelInfo = "{ " + "\"modelType\": \"service\","
                + "\"modelInvariantUuid\": \"1cc4e2e4-eb6e-404d-a66f-c8733cedcce8\","
                + "\"modelUuid\": \"2f7f309d-c842-4644-a2e4-34167be5eeb4\","
                + "\"modelName\": \"ADIOD vRouter vCE 011017 Service\"," + "\"modelVersion\": \"5.0\"," + "}";
        variables.put("serviceModelInfo", serviceModelInfo);
    }

    private String verifySniroRequest() {
        String request =
                "{\"requestInfo\":{\"transactionId\":\"testRequestId\",\"requestId\":\"testRequestId\",\"callbackUrl\":\"http://localhost:28090/workflows/messages/message/SNIROResponse/testRequestId\",\"sourceId\":\"mso\",\"requestType\":\"initial\",\"optimizer\":[\"placement\",\"license\"],\"numSolutions\":1,\"timeout\":1800},\"placementInfo\":{\"serviceModelInfo\":{\"modelType\":\"\",\"modelInvariantId\":\"testModelInvariantId\",\"modelVersionId\":\"testModelUuid\",\"modelName\":\"testModelName\",\"modelVersion\":\"testModelVersion\"},\"subscriberInfo\":{\"globalSubscriberId\":\"SUB12_0322_DS_1201\",\"subscriberName\":\"SUB_12_0322_DS_1201\",\"subscriberCommonSiteId\":\"\"},\"demandInfo\":{\"placementDemand\":[{\"resourceInstanceType\":\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR\",\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR\",\"modelInvariantId\":\"testModelInvariantIdAR\",\"modelName\":\"testModelNameAR\",\"modelVersion\":\"testModelVersionAR\",\"modelVersionId\":\"testARModelUuid\",\"modelType\":\"testModelTypeAR\"},\"tenantId\":\"\",\"tenantName\":\"\"},{\"resourceInstanceType\":\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR2\",\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR2\",\"modelInvariantId\":\"testModelInvariantIdAR2\",\"modelName\":\"testModelNameAR2\",\"modelVersion\":\"testModelVersionAR2\",\"modelVersionId\":\"testAr2ModelUuid\",\"modelType\":\"testModelTypeAR2\"},\"tenantId\":\"\",\"tenantName\":\"\"}],\"licenseDemand\":[{\"resourceInstanceType\":\"VNF\",\"serviceResourceId\":\"testResourceIdVNF\",\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidVNF\",\"modelInvariantId\":\"testModelInvariantIdVNF\",\"modelName\":\"testModelNameVNF\",\"modelVersion\":\"testModelVersionVNF\",\"modelVersionId\":\"testVnfModelUuid\",\"modelType\":\"testModelTypeVNF\"}}]},\"policyId\":[],\"serviceInstanceId\":\"testServiceInstanceId123\",\"orderInfo\":\"{\\\"requestParameters\\\":null}\"}}";
        return request;
    }

    private String verifySniroRequest_existingLicense() {
        String request =
                "{\"requestInfo\":{\"transactionId\":\"testRequestId\",\"requestId\":\"testRequestId\",\"callbackUrl\":\"http://localhost:28090/workflows/messages/message/SNIROResponse/testRequestId\",\"sourceId\":\"mso\",\"requestType\":\"speedchanged\",\"optimizer\":[\"placement\",\"license\"],\"numSolutions\":1,\"timeout\":1800},\"placementInfo\":{\"serviceModelInfo\":{\"modelType\":\"\",\"modelInvariantId\":\"testModelInvariantId\",\"modelVersionId\":\"testModelUuid\",\"modelName\":\"testModelName\",\"modelVersion\":\"testModelVersion\"},\"subscriberInfo\":{\"globalSubscriberId\":\"SUB12_0322_DS_1201\",\"subscriberName\":\"SUB_12_0322_DS_1201\",\"subscriberCommonSiteId\":\"\"},\"demandInfo\":{\"placementDemand\":[{\"resourceInstanceType\":\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR\",\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR\",\"modelInvariantId\":\"testModelInvariantIdAR\",\"modelName\":\"testModelNameAR\",\"modelVersion\":\"testModelVersionAR\",\"modelVersionId\":\"testARModelUuid\",\"modelType\":\"testModelTypeAR\"},\"tenantId\":\"\",\"tenantName\":\"\"},{\"resourceInstanceType\":\"ALLOTTED_RESOURCE\",\"serviceResourceId\":\"testResourceIdAR2\",\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidAR2\",\"modelInvariantId\":\"testModelInvariantIdAR2\",\"modelName\":\"testModelNameAR2\",\"modelVersion\":\"testModelVersionAR2\",\"modelVersionId\":\"testAr2ModelUuid\",\"modelType\":\"testModelTypeAR2\"},\"tenantId\":\"\",\"tenantName\":\"\"}],\"licenseDemand\":[{\"resourceInstanceType\":\"VNF\",\"serviceResourceId\":\"testResourceIdVNF\",\"resourceModuleName\":\"\",\"resourceModelInfo\":{\"modelCustomizationId\":\"testModelCustomizationUuidVNF\",\"modelInvariantId\":\"testModelInvariantIdVNF\",\"modelName\":\"testModelNameVNF\",\"modelVersion\":\"testModelVersionVNF\",\"modelVersionId\":\"testVnfModelUuid\",\"modelType\":\"testModelTypeVNF\"},\"existingLicense\":[{\"entitlementPoolUUID\":[\"testEntitlementPoolId1\",\"testEntitlementPoolId2\"],\"licenseKeyGroupUUID\":[\"testLicenseKeyGroupId1\",\"testLicenseKeyGroupId2\"]}]}]},\"policyId\":[],\"serviceInstanceId\":\"testServiceInstanceId123\",\"orderInfo\":\"{\\\"requestParameters\\\":null}\"}}";
        return request;
    }

}
