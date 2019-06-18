/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest.handler;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.Status;
import org.onap.so.apihandlerinfra.infra.rest.exception.NoRecipeException;
import org.onap.so.apihandlerinfra.infra.rest.handler.VFModuleRestHandler;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class VfModuleRestHandlerTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @InjectMocks
    VFModuleRestHandler restHandler;

    @Mock
    ContainerRequestContext mockRequestContext;

    @Mock
    private CatalogDbClient catalogDbClient;

    @Mock
    private RequestsDbClient infraActiveRequestsClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test_find_vf_module_recipe() throws MalformedURLException, NoRecipeException {
        VnfComponentsRecipe expected = new VnfComponentsRecipe();
        expected.setAction("createInstance");
        doReturn(expected).when(catalogDbClient)
                .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction("testModelId",
                        ModelType.vfModule.toString(), Action.createInstance.toString());
        Recipe actual = restHandler.findVfModuleRecipe("testModelId", ModelType.vfModule.toString(),
                Action.createInstance.toString());
        assertThat(actual, sameBeanAs(expected));
        Mockito.verify(catalogDbClient, Mockito.times(1))
                .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction("testModelId",
                        ModelType.vfModule.toString(), Action.createInstance.toString());
    }

    @Test
    public void test_find_vf_module_recipe_default_recipe() throws MalformedURLException, NoRecipeException {
        VnfComponentsRecipe expected = new VnfComponentsRecipe();
        expected.setAction("createInstance");
        doReturn(null).when(catalogDbClient).getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
                "testModelId", ModelType.vfModule.toString(), Action.createInstance.toString());
        doReturn(expected).when(catalogDbClient)
                .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction("GR-API-DEFAULT",
                        ModelType.vfModule.toString(), Action.createInstance.toString());
        Recipe actual = restHandler.findVfModuleRecipe("testModelId", ModelType.vfModule.toString(),
                Action.createInstance.toString());
        assertThat(actual, sameBeanAs(expected));
        Mockito.verify(catalogDbClient, Mockito.times(1))
                .getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction("testModelId",
                        ModelType.vfModule.toString(), Action.createInstance.toString());
    }

    @Test
    public void test_find_vf_module_recipe_not_found() throws MalformedURLException, NoRecipeException {
        VnfComponentsRecipe expected = new VnfComponentsRecipe();
        expected.setAction("createInstance");
        exceptionRule.expect(NoRecipeException.class);
        exceptionRule.expectMessage(
                "Unable to locate custom or default recipe for ModelType: vfModule , Action: createInstance, CustomizationId: testModelId");
        restHandler.findVfModuleRecipe("testModelId", ModelType.vfModule.toString(), Action.createInstance.toString());
    }

    @Test
    public void test_checkDuplicateRequest() throws MalformedURLException, NoRecipeException {
        ArgumentCaptor<HashMap> instanceIdCaptor = ArgumentCaptor.forClass(HashMap.class);
        restHandler.checkDuplicateRequest("serviceInstanceId", "vnfId", "vfModuleId", "instanceName", "requestId");
        Mockito.verify(infraActiveRequestsClient, Mockito.times(1)).checkInstanceNameDuplicate(
                instanceIdCaptor.capture(), eq("instanceName"), eq(ModelType.vfModule.toString()));
        Map actualMap = instanceIdCaptor.getValue();
        assertEquals("ServiceInstanceID should exist in map", "serviceInstanceId", actualMap.get("serviceInstanceId"));
        assertEquals("VnfId should exit in map", "vnfId", actualMap.get("vnfInstanceId"));
        assertEquals("VFModuleId should exit in map", "vfModuleId", actualMap.get("vfModuleInstanceId"));
    }

    @Test
    public void test_saveInstanceName() throws MalformedURLException, NoRecipeException {
        ServiceInstancesRequest request = createTestRequest();
        InfraActiveRequests dbRequest = createDatabaseRecord();
        restHandler.saveInstanceName(request, dbRequest);
        Mockito.verify(infraActiveRequestsClient, Mockito.times(1)).updateInfraActiveRequests(dbRequest);
        assertEquals("InstanceName Should Be Equal", "instanceName", dbRequest.getVfModuleName());
    }

    @Test
    public void test_buildRequestParams() throws Exception {
        RequestClientParameter expected = new RequestClientParameter.Builder().setRequestId("requestId")
                .setServiceInstanceId("serviceInstanceId").setVnfId("vnfId").setVfModuleId("vfModuleId")
                .setALaCarte(true).setRequestDetails(mapper.writeValueAsString(createTestRequest()))
                .setRequestAction(Action.deleteInstance.toString())
                .setRequestUri("http://localhost:8080/serviceInstances").setApiVersion("v8").build();
        RequestClientParameter actual = restHandler.buildRequestParams(createTestRequest(),
                "http://localhost:8080/serviceInstances", "requestId", "serviceInstanceId", "vnfId", "vfModuleId");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void test_createInfraActiveRequestForDelete() throws Exception {
        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setRequestAction(Action.deleteInstance.toString());
        expected.setAction(Action.deleteInstance.toString());
        expected.setServiceInstanceId("serviceInstanceId");
        expected.setVnfId("vnfId");
        expected.setVfModuleId("vfModuleId");
        expected.setRequestId("requestId");
        expected.setRequestorId("userId");
        expected.setSource("VID");
        expected.setRequestStatus(Status.IN_PROGRESS.toString());
        expected.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        expected.setRequestUrl("http://localhost:9090");
        expected.setRequestScope(ModelType.vfModule.toString());
        InfraActiveRequests actual = restHandler.createInfraActiveRequestForDelete("requestId", "vfModuleId",
                "serviceInstanceId", "vnfId", "userId", "VID", "http://localhost:9090");
        assertThat(actual, sameBeanAs(expected).ignoring("startTime"));
        Mockito.verify(infraActiveRequestsClient, Mockito.times(1)).save(actual);
    }

    private ServiceInstancesRequest createTestRequest() {
        ServiceInstancesRequest request = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setInstanceName("instanceName");
        requestDetails.setRequestInfo(requestInfo);
        request.setRequestDetails(requestDetails);
        return request;
    }

    private InfraActiveRequests createDatabaseRecord() {
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("requestId");
        return request;
    }

}
