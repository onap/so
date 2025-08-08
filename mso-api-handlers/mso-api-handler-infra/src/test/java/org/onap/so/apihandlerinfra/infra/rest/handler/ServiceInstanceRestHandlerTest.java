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
import org.onap.so.apihandlerinfra.infra.rest.exception.NoRecipeException;
import org.onap.so.apihandlerinfra.infra.rest.exception.RequestConflictedException;
import org.onap.so.constants.Status;
import org.onap.so.db.catalog.beans.Recipe;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInstanceRestHandlerTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @InjectMocks
    ServiceInstanceRestHandler restHandler;

    @Mock
    ContainerRequestContext mockRequestContext;

    @Mock
    private CatalogDbClient catalogDbClient;

    @Mock
    private RequestsDbClient infraActiveRequestsClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test_find_service_recipe() throws NoRecipeException {
        ServiceRecipe expected = new ServiceRecipe();
        expected.setAction("createInstance");
        doReturn(expected).when(catalogDbClient)
                .findServiceRecipeByActionAndServiceModelUUID(Action.createInstance.toString(), "testModelId");
        Recipe actual = restHandler.findServiceRecipe("testModelId", Action.createInstance.toString());
        assertThat(actual, sameBeanAs(expected));
        Mockito.verify(catalogDbClient, Mockito.times(1))
                .findServiceRecipeByActionAndServiceModelUUID(Action.createInstance.toString(), "testModelId");
    }

    @Test
    public void test_find_service_recipe_default_recipe() throws NoRecipeException {
        ServiceRecipe expected = new ServiceRecipe();
        expected.setAction("createInstance");
        doReturn(null).when(catalogDbClient)
                .findServiceRecipeByActionAndServiceModelUUID(Action.createInstance.toString(), "testModelId");
        doReturn(expected).when(catalogDbClient).findServiceRecipeByActionAndServiceModelUUID(
                Action.createInstance.toString(), "d88da85c-d9e8-4f73-b837-3a72a431622b");
        Recipe actual = restHandler.findServiceRecipe("testModelId", Action.createInstance.toString());
        assertThat(actual, sameBeanAs(expected));
        Mockito.verify(catalogDbClient, Mockito.times(1))
                .findServiceRecipeByActionAndServiceModelUUID(Action.createInstance.toString(), "testModelId");
        Mockito.verify(catalogDbClient, Mockito.times(1)).findServiceRecipeByActionAndServiceModelUUID(
                Action.createInstance.toString(), "d88da85c-d9e8-4f73-b837-3a72a431622b");
    }

    @Test
    public void test_find_service_recipe_not_found() throws NoRecipeException {
        ServiceRecipe expected = new ServiceRecipe();
        expected.setAction("createInstance");
        doReturn(null).when(catalogDbClient)
                .findServiceRecipeByActionAndServiceModelUUID(Action.createInstance.toString(), "testModelId");
        doReturn(null).when(catalogDbClient).findServiceRecipeByActionAndServiceModelUUID(
                Action.createInstance.toString(), "d88da85c-d9e8-4f73-b837-3a72a431622b");
        exceptionRule.expect(NoRecipeException.class);
        exceptionRule.expectMessage(
                "Unable to locate custom or default recipe for, Action: createInstance, Model UUID: testModelId");
        restHandler.findServiceRecipe("testModelId", Action.createInstance.toString());
    }

    @Test
    public void test_checkDuplicateRequest()
            throws RequestConflictedException {
        ArgumentCaptor<HashMap> instanceIdCaptor = ArgumentCaptor.forClass(HashMap.class);
        restHandler.checkDuplicateRequest("serviceInstanceId", "instanceName", "requestId");
        Mockito.verify(infraActiveRequestsClient, Mockito.times(1)).checkInstanceNameDuplicate(
                instanceIdCaptor.capture(), eq("instanceName"), eq(ModelType.service.toString()));
        Map actualMap = instanceIdCaptor.getValue();
        assertEquals("serviceInstanceId", actualMap.get("serviceInstanceId"));
    }

    @Test
    public void test_saveInstanceName() {
        ServiceInstancesRequest request = createTestRequest();
        InfraActiveRequests dbRequest = createDatabaseRecord();
        restHandler.saveInstanceName(request, dbRequest);
        Mockito.verify(infraActiveRequestsClient, Mockito.times(1)).updateInfraActiveRequests(dbRequest);
        assertEquals("InstanceName Should Be Equal", "instanceName", dbRequest.getServiceInstanceName());
    }

    @Test
    public void test_buildRequestParams() throws Exception {
        RequestClientParameter expected =
                new RequestClientParameter.Builder().setRequestId("requestId").setServiceInstanceId("serviceInstanceId")
                        .setALaCarte(true).setRequestDetails(mapper.writeValueAsString(createTestRequest()))
                        .setRequestAction(Action.deleteInstance.toString())
                        .setRequestUri("http://localhost:8080/serviceInstances").setApiVersion("v8").build();
        RequestClientParameter actual = restHandler.buildRequestParams(createTestRequest(),
                "http://localhost:8080/serviceInstances", "requestId", "serviceInstanceId");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void test_createInfraActiveRequestForDelete() {
        InfraActiveRequests expected = new InfraActiveRequests();
        expected.setRequestAction(Action.deleteInstance.toString());
        expected.setServiceInstanceId("serviceInstanceId");
        expected.setRequestId("requestId");
        expected.setRequestorId("userId");
        expected.setSource("VID");
        expected.setRequestStatus(Status.IN_PROGRESS.toString());
        expected.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        expected.setRequestUrl("http://localhost:9090");
        expected.setRequestScope(ModelType.service.toString());
        InfraActiveRequests actual = restHandler.createInfraActiveRequestForDelete("requestId", "serviceInstanceId",
                "userId", "VID", "http://localhost:9090");
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
