/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.CloudApiRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.openstack.beans.CreateStackRequest;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.onap.so.openstack.exceptions.MsoStackAlreadyExists;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.StackResource;
import com.woorea.openstack.heat.StackResource.CreateStack;
import com.woorea.openstack.heat.StackResource.DeleteStack;
import com.woorea.openstack.heat.model.CreateStackParam;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.heat.model.Stack;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class MsoHeatUtilsTest extends MsoHeatUtils {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Spy
    @InjectMocks
    private MsoHeatUtils heatUtils;

    @Mock
    private RequestsDbClient requestDbClient;

    @Mock
    private Heat heatClient;

    @Mock
    private StackStatusHandler stackStatusHandler;

    @Mock
    private Environment env;

    @Mock
    private StackResource stackResource;

    @Mock
    private NovaClientImpl novaClient;

    @Mock
    private DeleteStack mockDeleteStack;

    @Mock
    private Resources mockResources;

    @Mock
    private CreateStack mockCreateStack;

    @Mock
    private CatalogDbClient catalogDbClient;

    private String cloudSiteId = "cloudSiteId";
    private String tenantId = "tenantId";

    private String getRequestId() {
        return MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
    }

    @Before
    public void setup() {
        doReturn("15").when(env).getProperty("org.onap.so.adapters.po.pollInterval", "15");
        String requestId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
    }

    @Test
    public final void pollStackForStatus_Create_Complete_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason("Stack Finished");

        String requestId = getRequestId();
        Stack latestStack = new Stack();
        latestStack.setId("id");
        latestStack.setStackName("stackName");
        latestStack.setStackStatus("CREATE_COMPLETE");
        latestStack.setStackStatusReason("Stack Finished");
        doReturn(latestStack).when(heatUtils).queryHeatStack(isA(Heat.class), eq("stackName/id"));
        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        Stack actual = heatUtils.pollStackForStatus(1, stack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId, false);
        Mockito.verify(stackStatusHandler, times(1)).updateStackStatus(latestStack, requestId);
        Mockito.verify(heatUtils, times(1)).queryHeatStack(isA(Heat.class), eq("stackName/id"));
        assertEquals(true, actual != null);
    }

    @Test
    public final void pollStackForStatus_No_Polling_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason("Stack Finished");
        String requestId = getRequestId();
        doNothing().when(stackStatusHandler).updateStackStatus(stack, requestId);
        doReturn(stack).when(heatUtils).queryHeatStack(isA(Heat.class), eq("stackName/id"));
        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        doReturn("61").when(env).getProperty("org.onap.so.adapters.po.pollInterval", "15");
        Stack actual = heatUtils.pollStackForStatus(1, stack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId, false);
        Mockito.verify(stackStatusHandler, times(1)).updateStackStatus(stack, requestId);
        Mockito.verify(heatUtils, times(1)).queryHeatStack(isA(Heat.class), eq("stackName/id"));
        assertEquals(true, actual != null);
    }

    @Test
    public final void pollStackForStatus_Polling_Exhausted_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason("Stack Finished");
        String requestId = getRequestId();
        doNothing().when(stackStatusHandler).updateStackStatus(stack, requestId);
        doReturn(stack).when(heatUtils).queryHeatStack(isA(Heat.class), eq("stackName/id"));
        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        Stack actual = heatUtils.pollStackForStatus(1, stack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId, false);
        Mockito.verify(stackStatusHandler, times(5)).updateStackStatus(stack, requestId);
        Mockito.verify(heatUtils, times(5)).queryHeatStack(isA(Heat.class), eq("stackName/id"));
        assertEquals(true, actual != null);
    }

    @Test
    public final void postProcessStackCreate_CREATE_IN_PROGRESS_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason("Stack In Progress");
        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        exceptionRule.expect(StackCreationException.class);
        exceptionRule.expectMessage(
                "Stack Creation Failed Openstack Status: CREATE_IN_PROGRESS Status Reason: Stack In Progress. Stack rollback suppressed, stack not deleted");
        heatUtils.postProcessStackCreate(stack, false, 120, false, cloudSiteId, tenantId, createStackParam);
    }

    @Test
    public final void postProcessStackCreate_Backout_True_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason("Stack Finished");

        Stack deletedStack = new Stack();
        deletedStack.setId("id");
        deletedStack.setStackName("stackName");
        deletedStack.setStackStatus("DELETE_COMPLETE");
        deletedStack.setStackStatusReason("Stack Deleted");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");
        doReturn(deletedStack).when(heatUtils).handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
        exceptionRule.expect(StackCreationException.class);
        exceptionRule.expectMessage(
                "Stack Creation Failed Openstack Status: CREATE_IN_PROGRESS Status Reason: Stack Finished , Rollback of Stack Creation completed with status: DELETE_COMPLETE Status Reason: Stack Deleted");
        heatUtils.postProcessStackCreate(stack, true, 120, false, cloudSiteId, tenantId, createStackParam);
        Mockito.verify(heatUtils, times(1)).handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
    }

    @Test
    public final void postProcessStackCreate_Backout_True_Delete_Fail_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason("Stack Finished");

        Stack deletedStack = new Stack();
        deletedStack.setId("id");
        deletedStack.setStackName("stackName");
        deletedStack.setStackStatus("DELETE_COMPLETE");
        deletedStack.setStackStatusReason("Stack Deleted");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");
        doThrow(new MsoOpenstackException(500, "Error In Delete", "Error In Delete")).when(heatUtils)
                .handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
        exceptionRule.expect(MsoException.class);
        exceptionRule.expectMessage(
                "Stack Creation Failed Openstack Status: CREATE_IN_PROGRESS Status Reason: Stack Finished , Rollback of Stack Creation failed with sync error: Error In Delete");
        heatUtils.postProcessStackCreate(stack, true, 120, false, cloudSiteId, tenantId, createStackParam);
        Mockito.verify(heatUtils, times(1)).handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
    }

    @Test
    public final void postProcessStackCreate_Keypair_True_Test() throws MsoException, IOException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_IN_PROGRESS");
        stack.setStackStatusReason(
                "Resource CREATE failed: Conflict: resources.my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID: req-941b0af6-63ae-4d6a-afbc-90b728bacf82");

        Stack createdStack = new Stack();
        createdStack.setId("id");
        createdStack.setStackName("stackName");
        createdStack.setStackStatus("CREATE_COMPLETE");
        createdStack.setStackStatusReason("Stack Created");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");
        doReturn(createdStack).when(heatUtils).handleKeyPairConflict(cloudSiteId, tenantId, createStackParam, 120, true,
                stack);
        heatUtils.postProcessStackCreate(stack, true, 120, true, cloudSiteId, tenantId, createStackParam);
        Mockito.verify(heatUtils, times(1)).handleKeyPairConflict(cloudSiteId, tenantId, createStackParam, 120, true,
                stack);
    }

    @Test
    public final void handleUnknownCreateStackFailure_Test() throws MsoException, IOException {

        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_FAILED");
        stack.setStackStatusReason(
                "Resource CREATE failed: Conflict: resources.my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID: req-941b0af6-63ae-4d6a-afbc-90b728bacf82");

        Stack deletedStack = new Stack();
        deletedStack.setId("id");
        deletedStack.setStackName("stackName");
        deletedStack.setStackStatus("DELETE_COMPLETE");
        deletedStack.setStackStatusReason("Stack Deleted");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");
        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        doReturn(null).when(heatUtils).executeAndRecordOpenstackRequest(mockDeleteStack);
        doReturn(stackResource).when(heatClient).getStacks();
        doReturn(mockDeleteStack).when(stackResource).deleteByName("stackName/id");

        heatUtils.handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
        Mockito.verify(heatUtils, times(1)).executeAndRecordOpenstackRequest(mockDeleteStack);
        Mockito.verify(heatUtils, times(0)).pollStackForStatus(120, stack, "DELETE_IN_PROGRESS", cloudSiteId, tenantId,
                true);
        Mockito.verify(heatUtils, times(0)).postProcessStackDelete(deletedStack);
    }


    @Test
    public final void handleUnknownCreateStackFailure_Null_Stack_Test() throws MsoException, IOException {
        Stack stack = null;
        exceptionRule.expect(StackCreationException.class);
        exceptionRule.expectMessage("Cannot Find Stack Name or Id");
        heatUtils.handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
    }

    @Test
    public final void postProcessStackDelete_Stack_Test() throws MsoException, IOException {
        Stack deletedStack = new Stack();
        deletedStack.setId("id");
        deletedStack.setStackName("stackName");
        deletedStack.setStackStatus("DELETE_FAILED");
        deletedStack.setStackStatusReason("Stack DID NOT DELETE");
        exceptionRule.expect(StackRollbackException.class);
        exceptionRule.expectMessage(
                "Stack Deletion completed with status: DELETE_FAILED Status Reason: Stack DID NOT DELETE");
        heatUtils.postProcessStackDelete(deletedStack);
    }

    @Test
    public final void postProcessStackDelete__Null_Stack_Test() throws MsoException, IOException {
        Stack stack = null;
        exceptionRule.expect(StackRollbackException.class);
        exceptionRule.expectMessage("Cannot Find Stack Name or Id");
        heatUtils.postProcessStackDelete(stack);
    }

    @Test
    public final void isKeyPairFailure_Test() throws MsoException, IOException {
        boolean actual = heatUtils.isKeyPairFailure(
                "Exception during create VF 0 : Stack error (CREATE_FAILED): Resource CREATE failed: Conflict: resources.bfnm_my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID:req-941b0af6-63ae-4d6a-afbc-90b728bacf82) - stack successfully deleted'rolledBack='true'");
        assertEquals(true, actual);
    }

    @Test
    public final void handleKeyPairConflict_Test() throws MsoException, IOException, NovaClientException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_FAILED");
        stack.setStackStatusReason(
                "Resource CREATE failed: Conflict: resources.my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID: req-941b0af6-63ae-4d6a-afbc-90b728bacf82");

        Stack createdStack = new Stack();
        createdStack.setId("id");
        createdStack.setStackName("stackName");
        createdStack.setStackStatus("CREATE_COMPLETE");
        createdStack.setStackStatusReason("Stack Created");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        doReturn(null).when(heatUtils).handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
        doReturn(createdStack).when(heatUtils).createStack(createStackParam, cloudSiteId, tenantId);
        doReturn(createdStack).when(heatUtils).processCreateStack(cloudSiteId, tenantId, 120, true, createdStack,
                createStackParam, false);

        heatUtils.handleKeyPairConflict(cloudSiteId, tenantId, createStackParam, 120, true, stack);
        Mockito.verify(novaClient, times(1)).deleteKeyPair(cloudSiteId, tenantId, "hst3bbfnm0011vm001");
        Mockito.verify(heatUtils, times(1)).handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
        Mockito.verify(heatUtils, times(1)).createStack(createStackParam, cloudSiteId, tenantId);
        Mockito.verify(heatUtils, times(1)).processCreateStack(cloudSiteId, tenantId, 120, true, createdStack,
                createStackParam, false);
    }

    @Test
    public final void processCreateStack_Test() throws MsoException, IOException, NovaClientException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_FAILED");
        stack.setStackStatusReason(
                "Resource CREATE failed: Conflict: resources.my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID: req-941b0af6-63ae-4d6a-afbc-90b728bacf82");

        Stack createdStack = new Stack();
        createdStack.setId("id");
        createdStack.setStackName("stackName");
        createdStack.setStackStatus("CREATE_COMPLETE");
        createdStack.setStackStatusReason("Stack Created");


        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        doReturn(createdStack).when(heatUtils).pollStackForStatus(120, stack, "CREATE_IN_PROGRESS", cloudSiteId,
                tenantId, false);
        doReturn(createdStack).when(heatUtils).postProcessStackCreate(createdStack, true, 120, true, cloudSiteId,
                tenantId, createStackParam);

        heatUtils.processCreateStack(cloudSiteId, tenantId, 120, true, stack, createStackParam, true);
        Mockito.verify(heatUtils, times(1)).pollStackForStatus(120, stack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId,
                false);
        Mockito.verify(heatUtils, times(1)).postProcessStackCreate(createdStack, true, 120, true, cloudSiteId, tenantId,
                createStackParam);
    }

    @Test
    public final void processCreateStack_Exception_Backout_Test()
            throws MsoException, IOException, NovaClientException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_FAILED");
        stack.setStackStatusReason(
                "Resource CREATE failed: Conflict: resources.my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID: req-941b0af6-63ae-4d6a-afbc-90b728bacf82");

        Stack deletedStack = new Stack();
        deletedStack.setId("id");
        deletedStack.setStackName("stackName");
        deletedStack.setStackStatus("DELETE_COMPLETE");
        deletedStack.setStackStatusReason("Stack Deleted");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        doThrow(new StackCreationException("Error")).when(heatUtils).pollStackForStatus(120, stack,
                "CREATE_IN_PROGRESS", cloudSiteId, tenantId, false);
        exceptionRule.expect(MsoException.class);
        exceptionRule.expectMessage("Error");
        heatUtils.processCreateStack(cloudSiteId, tenantId, 120, true, stack, createStackParam, true);
        Mockito.verify(heatUtils, times(1)).pollStackForStatus(120, stack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId,
                false);
        Mockito.verify(heatUtils, times(1)).postProcessStackCreate(stack, true, 120, true, cloudSiteId, tenantId,
                createStackParam);
    }


    @Test
    public final void createStack_Test() throws MsoException, IOException, NovaClientException {
        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        String requestId = getRequestId();
        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        doReturn(stackResource).when(heatClient).getStacks();
        doReturn(mockCreateStack).when(stackResource).create(createStackParam);

        doReturn(null).when(heatUtils).executeAndRecordOpenstackRequest(mockCreateStack);

        heatUtils.createStack(createStackParam, cloudSiteId, tenantId);
        Mockito.verify(stackResource, times(1)).create(createStackParam);
        Mockito.verify(heatUtils, times(1)).saveStackRequest(createStackParam, requestId, "stackName");
        Mockito.verify(heatClient, times(1)).getStacks();
        Mockito.verify(stackResource, times(1)).create(createStackParam);
    }

    @Test
    public final void saveStack_Test() throws MsoException, IOException, NovaClientException {

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", "value");
        String environment =
                "parameters:\n  mmn_volume_name_1: \"data-mn-v-vdb\"\n  mmn_volume_name_2: \"arch-mn-v-vdc\"\n";
        createStackParam.setParameters(parameters);
        createStackParam.setEnvironment(environment);

        CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.setEnvironment(environment);
        createStackRequest.setParameters(parameters);
        ObjectMapper mapper = new ObjectMapper();
        String stackRequest = mapper.writeValueAsString(createStackRequest);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("requestId");
        doReturn(request).when(requestDbClient).getInfraActiveRequestbyRequestId("requestId");
        doNothing().when(requestDbClient).updateInfraActiveRequests(request);
        heatUtils.saveStackRequest(createStackParam, "requestId", "stackName");
        Mockito.verify(requestDbClient, times(1)).updateInfraActiveRequests(request);
        assertNotNull(request.getCloudApiRequests().get(0));
        assertEquals("requestId", request.getCloudApiRequests().get(0).getRequestId());
        assertEquals(stackRequest, request.getCloudApiRequests().get(0).getRequestBody());
    }

    @Test
    public final void saveStack__Exists_Test() throws MsoException, IOException, NovaClientException {
        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", "value");
        createStackParam.setParameters(parameters);
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("requestId");
        CloudApiRequests cloudRequest = new CloudApiRequests();
        cloudRequest.setCloudIdentifier("stackName");
        cloudRequest.setRequestBody("testMe");
        cloudRequest.setRequestId("requestId");
        request.getCloudApiRequests().add(cloudRequest);
        doReturn(request).when(requestDbClient).getInfraActiveRequestbyRequestId("requestId");
        doNothing().when(requestDbClient).updateInfraActiveRequests(request);
        heatUtils.saveStackRequest(createStackParam, "requestId", "stackName");
        Mockito.verify(requestDbClient, times(1)).updateInfraActiveRequests(request);
        assertNotNull(request.getCloudApiRequests().get(0));
        assertEquals("requestId", request.getCloudApiRequests().get(0).getRequestId());
        assertNotEquals("testMe", request.getCloudApiRequests().get(0).getRequestBody());
    }

    @Test
    public final void createStack_Error_Test() throws MsoException, IOException, NovaClientException {
        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        doReturn(stackResource).when(heatClient).getStacks();
        doReturn(mockCreateStack).when(stackResource).create(createStackParam);

        doThrow(new OpenStackResponseException("Unknown Error", 500)).when(heatUtils)
                .executeAndRecordOpenstackRequest(mockCreateStack);
        exceptionRule.expect(MsoOpenstackException.class);
        exceptionRule.expectMessage("Unknown Error");
        heatUtils.createStack(createStackParam, cloudSiteId, tenantId);
        Mockito.verify(stackResource, times(1)).create(createStackParam);
        Mockito.verify(heatUtils, times(1)).saveStackRequest(eq(createStackParam), isNull(), eq("stackName"));
        Mockito.verify(heatClient, times(1)).getStacks();
        Mockito.verify(stackResource, times(1)).create(createStackParam);
    }

    @Test
    public final void createStack_Error_404_Test() throws MsoException, IOException, NovaClientException {
        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        doReturn(heatClient).when(heatUtils).getHeatClient(cloudSiteId, tenantId);
        doReturn(stackResource).when(heatClient).getStacks();
        doReturn(mockCreateStack).when(stackResource).create(createStackParam);

        doThrow(new OpenStackResponseException("Not Found", 409)).when(heatUtils)
                .executeAndRecordOpenstackRequest(mockCreateStack);
        exceptionRule.expect(MsoStackAlreadyExists.class);
        exceptionRule.expectMessage("Stack stackName already exists in Tenant tenantId in Cloud cloudSiteId");
        heatUtils.createStack(createStackParam, cloudSiteId, tenantId);
        Mockito.verify(stackResource, times(1)).create(createStackParam);
        Mockito.verify(heatUtils, times(1)).saveStackRequest(eq(createStackParam), isNull(), eq("stackName"));
        Mockito.verify(heatClient, times(1)).getStacks();
        Mockito.verify(stackResource, times(1)).create(createStackParam);
    }

    @Test
    public final void processCreateStack_Exception_No_Backout_Test()
            throws MsoException, IOException, NovaClientException {
        Stack stack = new Stack();
        stack.setId("id");
        stack.setStackName("stackName");
        stack.setStackStatus("CREATE_FAILED");
        stack.setStackStatusReason(
                "Resource CREATE failed: Conflict: resources.my_keypair: Key pair 'hst3bbfnm0011vm001' already exists. (HTTP 409) (Request-ID: req-941b0af6-63ae-4d6a-afbc-90b728bacf82");

        Stack deletedStack = new Stack();
        deletedStack.setId("id");
        deletedStack.setStackName("stackName");
        deletedStack.setStackStatus("DELETE_COMPLETE");
        deletedStack.setStackStatusReason("Stack Deleted");

        CreateStackParam createStackParam = new CreateStackParam();
        createStackParam.setStackName("stackName");

        doThrow(new StackCreationException("Error")).when(heatUtils).pollStackForStatus(120, stack,
                "CREATE_IN_PROGRESS", cloudSiteId, tenantId, false);

        exceptionRule.expect(MsoException.class);
        exceptionRule.expectMessage("Error");
        heatUtils.processCreateStack(cloudSiteId, tenantId, 120, false, stack, createStackParam, true);
        Mockito.verify(heatUtils, times(1)).pollStackForStatus(120, stack, "CREATE_IN_PROGRESS", cloudSiteId, tenantId,
                false);
        Mockito.verify(heatUtils, times(0)).handleUnknownCreateStackFailure(stack, 120, cloudSiteId, tenantId);
    }

    @Test
    public void testGetVfHeatTimeoutValue() {

        VfModuleCustomization vfmc = new VfModuleCustomization();
        VfModule vf = new VfModule();
        HeatTemplate heat = new HeatTemplate();
        heat.setTimeoutMinutes(110);
        vf.setModuleHeatTemplate(heat);
        vfmc.setVfModule(vf);

        Mockito.when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("uuid")).thenReturn(vfmc);

        int timeout = heatUtils.getVfHeatTimeoutValue("uuid", false);
        assertEquals(110, timeout);
        Mockito.verify(catalogDbClient, times(1)).getVfModuleCustomizationByModelCuztomizationUUID("uuid");
    }

    @Test
    public void testGetNetworkHeatTimeoutValue() {

        NetworkResourceCustomization mc = new NetworkResourceCustomization();
        NetworkResource nr = new NetworkResource();
        HeatTemplate heat = new HeatTemplate();
        heat.setTimeoutMinutes(110);
        nr.setHeatTemplate(heat);
        mc.setNetworkResource(nr);

        Mockito.when(catalogDbClient.getNetworkResourceCustomizationByModelCustomizationUUID("uuid")).thenReturn(mc);

        int timeout = heatUtils.getNetworkHeatTimeoutValue("uuid", "type");
        assertEquals(110, timeout);
        Mockito.verify(catalogDbClient, times(1)).getNetworkResourceCustomizationByModelCustomizationUUID("uuid");
    }

}
