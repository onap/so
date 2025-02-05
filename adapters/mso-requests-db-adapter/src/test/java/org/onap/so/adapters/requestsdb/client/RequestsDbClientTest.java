/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * Copyright (c) 2022, Samsung Electronics. All rights reserved.
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

package org.onap.so.adapters.requestsdb.client;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.requestsdb.RequestsAdapterBase;
import org.onap.so.db.request.beans.CloudApiRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.serviceinstancebeans.ModelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public class RequestsDbClientTest extends RequestsAdapterBase {

    @Autowired
    private RequestDbClientPortChanger requestsDbClient;

    private InfraActiveRequests infraActiveRequests;

    @LocalServerPort
    private int port;

    @Before
    public void setup() {
        requestsDbClient.setPort(port);

        infraActiveRequests = new InfraActiveRequests();
        infraActiveRequests.setRequestId(UUID.randomUUID().toString());
        infraActiveRequests.setOperationalEnvId(UUID.randomUUID().toString());
        infraActiveRequests.setServiceInstanceId(UUID.randomUUID().toString());
        infraActiveRequests.setServiceInstanceName("serviceInstanceNameTest");
        infraActiveRequests.setVnfId(UUID.randomUUID().toString());
        infraActiveRequests.setVnfName("vnfInstanceNameTest");
        infraActiveRequests.setVfModuleId(UUID.randomUUID().toString());
        infraActiveRequests.setVfModuleName("vfModuleInstanceNameTest");
        infraActiveRequests.setVolumeGroupId(UUID.randomUUID().toString());
        infraActiveRequests.setVolumeGroupName("volumeGroupInstanceNameTest");
        infraActiveRequests.setNetworkId(UUID.randomUUID().toString());
        infraActiveRequests.setNetworkName("networkInstanceNameTest");
        infraActiveRequests.setConfigurationId(UUID.randomUUID().toString());
        infraActiveRequests.setConfigurationName("configurationInstanceNameTest");
        infraActiveRequests.setCloudRegion("1");
        infraActiveRequests.setTenantId(UUID.randomUUID().toString());
        infraActiveRequests.setRequestScope("operationalEnvironment");
        infraActiveRequests.setRequestorId(UUID.randomUUID().toString());
        infraActiveRequests.setSource("sourceTest");
        infraActiveRequests.setOperationalEnvName(UUID.randomUUID().toString());
        infraActiveRequests.setRequestStatus("IN_PROGRESS");
        infraActiveRequests.setRequestAction("someaction");
        infraActiveRequests.setStartTime(new Timestamp(System.currentTimeMillis()));
        infraActiveRequests
                .setRequestUrl("http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances");
        List<CloudApiRequests> cloudApiRequests = new ArrayList<>();
        CloudApiRequests cloudRequest = new CloudApiRequests();
        cloudRequest.setCloudIdentifier("heatstackanme/id");
        cloudRequest.setId(1);
        cloudRequest.setRequestBody("requestBody");
        cloudRequest.setRequestId(infraActiveRequests.getRequestId());
        cloudApiRequests.add(cloudRequest);
        infraActiveRequests.setCloudApiRequests(cloudApiRequests);
        requestsDbClient.save(infraActiveRequests);

        InfraActiveRequests infraActiveRequests2 = new InfraActiveRequests();
        infraActiveRequests2.setRequestId(UUID.randomUUID().toString());
        infraActiveRequests2.setOperationalEnvId(UUID.randomUUID().toString());
        infraActiveRequests2.setServiceInstanceId(UUID.randomUUID().toString());
        infraActiveRequests2.setServiceInstanceName("serviceInstanceNameTest");
        infraActiveRequests2.setVnfId(UUID.randomUUID().toString());
        infraActiveRequests2.setVnfName("vnfInstanceNameTest");
        infraActiveRequests2.setVfModuleId(UUID.randomUUID().toString());
        infraActiveRequests2.setVfModuleName("vfModuleInstanceNameTest");
        infraActiveRequests2.setVolumeGroupId(UUID.randomUUID().toString());
        infraActiveRequests2.setVolumeGroupName("volumeGroupInstanceNameTest");
        infraActiveRequests2.setNetworkId(UUID.randomUUID().toString());
        infraActiveRequests2.setNetworkName("networkInstanceNameTest");
        infraActiveRequests2.setConfigurationId(UUID.randomUUID().toString());
        infraActiveRequests2.setConfigurationName("configurationInstanceNameTest");
        infraActiveRequests2.setCloudRegion("1");
        infraActiveRequests2.setTenantId(UUID.randomUUID().toString());
        infraActiveRequests2.setRequestScope("operationalEnvironment");
        infraActiveRequests2.setRequestorId(UUID.randomUUID().toString());
        infraActiveRequests2.setSource("sourceTest");
        infraActiveRequests2.setOperationalEnvName(UUID.randomUUID().toString());
        infraActiveRequests2.setRequestStatus("IN_PROGRESS");
        infraActiveRequests2.setRequestAction("someaction");
        infraActiveRequests2.setStartTime(new Timestamp(System.currentTimeMillis()));
        infraActiveRequests
                .setRequestUrl("http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances");
    }

    private void verifyOperationStatus(OperationStatus request, OperationStatus response) {
        assertThat(request, sameBeanAs(response).ignoring("operateAt").ignoring("finishedAt"));
    }


    private void verifyInfraActiveRequests(InfraActiveRequests infraActiveRequestsResponse) {
        assertThat(infraActiveRequestsResponse, sameBeanAs(infraActiveRequests).ignoring("modifyTime").ignoring("log")
                .ignoring("cloudApiRequests.created").ignoring("cloudApiRequests.id"));
    }

    @Test
    public void getCloudOrchestrationFiltersFromInfraActiveTest() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("operationalEnvironmentId", infraActiveRequests.getOperationalEnvId());
        requestMap.put("operationalEnvironmentName", infraActiveRequests.getOperationalEnvName());
        requestMap.put("resourceType", "operationalEnvironment");

        List<InfraActiveRequests> iarr = requestsDbClient.getCloudOrchestrationFiltersFromInfraActive(requestMap);

        assertEquals(1, iarr.size());
        InfraActiveRequests infraActiveRequestsResponse = iarr.get(0);
        verifyInfraActiveRequests(infraActiveRequestsResponse);
    }

    @Test
    public void checkVnfIdStatusTest() {
        InfraActiveRequests infraActiveRequestsResponse =
                requestsDbClient.checkVnfIdStatus(infraActiveRequests.getOperationalEnvId());
        verifyInfraActiveRequests(infraActiveRequestsResponse);
        assertNull(requestsDbClient.checkVnfIdStatus(UUID.randomUUID().toString()));
    }

    @Test
    public void checkInstanceNameDuplicateTest() {
        InfraActiveRequests infraActiveRequestsResponse = requestsDbClient.checkInstanceNameDuplicate(null,
                infraActiveRequests.getOperationalEnvName(), infraActiveRequests.getRequestScope());

        verifyInfraActiveRequests(infraActiveRequestsResponse);
    }

    @Test
    public void checkInstanceNameDuplicateViaTest() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("operationalEnvironmentId", infraActiveRequests.getOperationalEnvId());

        InfraActiveRequests infraActiveRequestsResponse = requestsDbClient.checkInstanceNameDuplicate(
                (HashMap<String, String>) requestMap, null, infraActiveRequests.getRequestScope());

        verifyInfraActiveRequests(infraActiveRequestsResponse);
    }

    @Test
    public void getOrchestrationFiltersFromInfraActiveTest() {
        Map<String, List<String>> requestMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("EQUALS");
        values.add(infraActiveRequests.getServiceInstanceId());
        requestMap.put("serviceInstanceId", values);

        values = new ArrayList<>();
        values.add("EQUALS");
        values.add(infraActiveRequests.getServiceInstanceName());
        requestMap.put("serviceInstanceName", values);

        List<InfraActiveRequests> iaar = requestsDbClient.getOrchestrationFiltersFromInfraActive(requestMap);

        assertEquals(1, iaar.size());
        InfraActiveRequests infraActiveRequestsResponse = iaar.get(0);

        verifyInfraActiveRequests(infraActiveRequestsResponse);
        values = new ArrayList<>();
        values.add("EQUALS");
        values.add(UUID.randomUUID().toString());
        requestMap.put("serviceInstanceName", values);
        requestsDbClient.getOrchestrationFiltersFromInfraActive(requestMap);
    }

    @Test
    public void getInfraActiveRequestbyRequestIdTest() {
        InfraActiveRequests infraActiveRequestsResponse =
                requestsDbClient.getInfraActiveRequestbyRequestId(infraActiveRequests.getRequestId());
        verifyInfraActiveRequests(infraActiveRequestsResponse);
        infraActiveRequestsResponse =
                requestsDbClient.getInfraActiveRequestbyRequestId(infraActiveRequests.getRequestId());

        assertNull(requestsDbClient.getInfraActiveRequestbyRequestId(UUID.randomUUID().toString()));
    }

    @Test
    public void getInfraActiveRequestbyRequestIdWhereRequestUrlNullTest() {
        // requestUrl setup to null and save
        infraActiveRequests.setRequestUrl(null);
        requestsDbClient.updateInfraActiveRequests(infraActiveRequests);
        InfraActiveRequests infraActiveRequestsResponse =
                requestsDbClient.getInfraActiveRequestbyRequestId(infraActiveRequests.getRequestId());
        verifyInfraActiveRequests(infraActiveRequestsResponse);

        assertNull(infraActiveRequestsResponse.getRequestUrl());
    }

    @Test
    public void getOneByServiceIdAndOperationIdTest() {
        OperationStatus operationStatus = new OperationStatus();
        operationStatus.setProgress("IN_PROGRESS");
        operationStatus.setResult("FAILED");
        operationStatus.setServiceId(UUID.randomUUID().toString());
        operationStatus.setOperationContent("operation-content");
        operationStatus.setOperation("operation");
        operationStatus.setOperationId(UUID.randomUUID().toString());
        operationStatus.setReason("reason-test");
        operationStatus.setUserId(UUID.randomUUID().toString());
        operationStatus.setServiceName("test-service");
        requestsDbClient.save(operationStatus);

        OperationStatus operationStatusResponse = requestsDbClient
                .getOneByServiceIdAndOperationId(operationStatus.getServiceId(), operationStatus.getOperationId());

        verifyOperationStatus(operationStatus, operationStatusResponse);

        assertNull(requestsDbClient.getOneByServiceIdAndOperationId(UUID.randomUUID().toString(),
                operationStatus.getOperationId()));
    }


    @Test
    public void getRequestProcessingDataBySoRequestIdTest() {
        List<RequestProcessingData> requestProcessingDataList =
                requestsDbClient.getRequestProcessingDataBySoRequestId("00032ab7-na18-42e5-965d-8ea592502018");
        assertNotNull(requestProcessingDataList);
        assertFalse(requestProcessingDataList.isEmpty());
        assertEquals(2, requestProcessingDataList.size());
    }


    @Test
    public void findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestIdTest() {
        OperationalEnvServiceModelStatus operationalEnvServiceModelStatus =
                requestsDbClient.findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId("1234", "TEST1234",
                        "00032ab7-3fb3-42e5-965d-8ea592502017");
        assertNotNull(operationalEnvServiceModelStatus);
        assertEquals("1234", operationalEnvServiceModelStatus.getOperationalEnvId());
        assertEquals("TEST1234", operationalEnvServiceModelStatus.getServiceModelVersionId());

        OperationalEnvServiceModelStatus operationalEnvServiceModelStatus1 =
                requestsDbClient.findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId("1234", "TEST1235",
                        "00032ab7-3fb3-42e5-965d-8ea592502018");
        assertNotNull(operationalEnvServiceModelStatus1);
        assertEquals("00032ab7-3fb3-42e5-965d-8ea592502018", operationalEnvServiceModelStatus1.getRequestId());
        assertEquals("1234", operationalEnvServiceModelStatus1.getOperationalEnvId());
        assertEquals("TEST1235", operationalEnvServiceModelStatus1.getServiceModelVersionId());
    }

    @Test
    public void getAllByOperationalEnvIdAndRequestId() {
        List<OperationalEnvServiceModelStatus> operationalEnvServiceModelStatuses =
                requestsDbClient.getAllByOperationalEnvIdAndRequestId("1234", "00032ab7-3fb3-42e5-965d-8ea592502017");
        assertNotNull(operationalEnvServiceModelStatuses);
        assertFalse(operationalEnvServiceModelStatuses.isEmpty());
        assertEquals(2, operationalEnvServiceModelStatuses.size());
    }

    @Test
    public void getDistributionStatusByIdTest() {
        OperationalEnvDistributionStatus operationalEnvDistributionStatus =
                requestsDbClient.getDistributionStatusById("111");
        assertNotNull(operationalEnvDistributionStatus);
        assertEquals("111", operationalEnvDistributionStatus.getDistributionId());
        assertEquals("ERROR", operationalEnvDistributionStatus.getDistributionIdErrorReason());
        assertEquals("00032ab7-3fb3-42e5-965d-8ea592502017", operationalEnvDistributionStatus.getRequestId());
    }

    @Test
    public void getRequestProcessingDataBySoRequestIdAndName() {
        RequestProcessingData requestProcessingData = requestsDbClient
                .getRequestProcessingDataBySoRequestIdAndNameAndGrouping("00032ab7-na18-42e5-965d-8ea592502018",
                        "requestAction", "7d2e8c07-4d10-456d-bddc-37abf38ca714");
        assertNotNull(requestProcessingData);

    }

    @Test
    public void getInfraActiveRequestbyRequestId_Filters_Test() {
        Map<String, String[]> filters = new HashMap<>();
        filters.put("requestStatus", new String[] {"EQ", "IN_PROGRESS"});
        filters.put("action", new String[] {"EQ", "create"});
        filters.put("serviceInstanceId", new String[] {"EQ", infraActiveRequests.getServiceInstanceId()});
        List<InfraActiveRequests> infraActiveRequestsResponse = requestsDbClient.getRequest(filters);

        verifyInfraActiveRequests(infraActiveRequestsResponse.get(0));
    }

    @Test
    public void getInProgressVolumeGroupsAndVfModulesTest() {
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId(UUID.randomUUID().toString());
        request.setVfModuleId(UUID.randomUUID().toString());
        request.setRequestStatus("IN_PROGRESS");
        request.setRequestScope(ModelType.vfModule.toString());
        Instant startInstant = Instant.now().minus(3, ChronoUnit.MINUTES);
        request.setStartTime(Timestamp.from(startInstant));
        request.setRequestAction("create");
        requestsDbClient.save(request);

        List<InfraActiveRequests> infraActiveRequests = requestsDbClient.getInProgressVolumeGroupsAndVfModules();
        assertThat(request, sameBeanAs(infraActiveRequests.get(0)).ignoring("modifyTime"));
    }

    @Test
    public void getRequestProcessingDataBySoRequestIdAndNameAndTag() {
        List<RequestProcessingData> requestProcessingData =
                requestsDbClient.getRequestProcessingDataBySoRequestIdAndNameAndTagOrderByCreateTimeDesc(
                        "00032ab7-na18-42e5-965d-8ea592502018", "requestAction", "pincFabricConfigRequest");
        assertNotNull(requestProcessingData);
        assertEquals(1, requestProcessingData.size());
        assertEquals("assign", requestProcessingData.get(0).getValue());
    }
}

