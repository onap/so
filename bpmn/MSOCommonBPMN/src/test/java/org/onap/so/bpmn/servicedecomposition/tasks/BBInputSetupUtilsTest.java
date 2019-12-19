/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Nokia
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.Configurations;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.L3Networks;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.MultipleObjectsFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.NoServiceInstanceFoundException;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class BBInputSetupUtilsTest {
    protected ObjectMapper mapper = new ObjectMapper();
    private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";
    private static final String FLOWS_TO_EXECUTE_PATH = RESOURCE_PATH + "flowsToExecute.json";
    private static final String CONFIGURATION_INPUT_PATH = RESOURCE_PATH + "ConfigurationInput.json";
    private static final String AAI_GENERIC_VNF_INPUT_PATH = RESOURCE_PATH + "aaiGenericVnfInput.json";
    private static final String INSTANCE_GROUP_PATH = RESOURCE_PATH + "InstanceGroup.json";
    private static final String CATALOG_SERVICE_EXPECTED_PATH = RESOURCE_PATH + "CatalogServiceExpected.json";
    private static final String VNFC_INSTANCE_GROUP_CUSTOMIZATION_PATH = RESOURCE_PATH + "VnfcInstanceGroupCustomization.json";
    private static final String INFRA_ACTIVE_REQUEST_EXPECTED_PATH = RESOURCE_PATH + "InfraActiveRequestExpected.json";
    private static final String REQUEST_DETAILS_EXPECTED_PATH = RESOURCE_PATH + "RequestDetailsExpected.json";

    @InjectMocks
    BBInputSetupUtils bbInputSetupUtils = new BBInputSetupUtils();

    @Mock
    protected CatalogDbClient MOCK_catalogDbClient;

    @Mock
    protected RequestsDbClient MOCK_requestsDbClient;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }

    @Test
    public void testGetCatalogServiceByModelUUID() throws IOException {
        Service expected = mapper.readValue(new File(CATALOG_SERVICE_EXPECTED_PATH), Service.class);
        final String modelVersionId = "38849269-c043-41e0-b95f-daedd3117d76";

        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId(modelVersionId);
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setModelInfo(modelInfo);
        doReturn(expected).when(MOCK_catalogDbClient).getServiceByID(modelVersionId);
        Service actual = bbInputSetupUtils.getCatalogServiceByModelUUID(modelInfo.getModelVersionId());

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void testGetCatalogServiceByModelVersionAndModelInvariantUUID() throws IOException {
        String modelVersion = "modelVersion";
        String modelInvariantUUID = "6f926538-f868-4f7a-b2e1-51d5c0ac480a";
        Service expectedService =
                mapper.readValue(new File(CATALOG_SERVICE_EXPECTED_PATH), Service.class);

        doReturn(expectedService).when(MOCK_catalogDbClient)
                .getServiceByModelVersionAndModelInvariantUUID(modelVersion, modelInvariantUUID);

        Service actualService = bbInputSetupUtils.getCatalogServiceByModelVersionAndModelInvariantUUID(modelVersion, modelInvariantUUID);

        assertThat(actualService, sameBeanAs(expectedService));
    }

    @Test
    public void testGetVnfcInstanceGroups() throws IOException {
        final String modelCustomizationUUID = "178bce76-03c7-45bd-9f9f-0ff7a9bc94ff";
        VnfcInstanceGroupCustomization vnfc = mapper.readValue(
                new File(VNFC_INSTANCE_GROUP_CUSTOMIZATION_PATH), VnfcInstanceGroupCustomization.class);

        doReturn(Arrays.asList(vnfc)).when(MOCK_catalogDbClient)
                .getVnfcInstanceGroupsByVnfResourceCust(isA(String.class));

        assertThat(bbInputSetupUtils.getVnfcInstanceGroups(modelCustomizationUUID), sameBeanAs(Arrays.asList(vnfc)));
    }

    @Test
    public void testGetRequestDetails() throws IOException {
        InfraActiveRequests infraActiveRequest = mapper
                .readValue(new File(INFRA_ACTIVE_REQUEST_EXPECTED_PATH), InfraActiveRequests.class);

        RequestDetails expected =
                mapper.readValue(new File(REQUEST_DETAILS_EXPECTED_PATH), RequestDetails.class);
        String requestId = "requestId";
        doReturn(infraActiveRequest).when(MOCK_requestsDbClient).getInfraActiveRequestbyRequestId(requestId);

        assertThat(bbInputSetupUtils.getRequestDetails(requestId), sameBeanAs(expected));
    }

    @Test
    public void getRequestDetailsNullTest() throws IOException {
        assertNull(bbInputSetupUtils.getRequestDetails(""));
    }

    @Test
    public void testGetCloudRegion() {
        CloudConfiguration cloudConfig = new CloudConfiguration();
        cloudConfig.setLcpCloudRegionId("lcpCloudRegionId");
        Optional<org.onap.aai.domain.yang.CloudRegion> expected =
                Optional.of(new org.onap.aai.domain.yang.CloudRegion());
        doReturn(expected).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.CloudRegion.class,
                AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, cloudConfig.getCloudOwner(), cloudConfig.getLcpCloudRegionId()).depth(Depth.TWO));

        assertThat(bbInputSetupUtils.getCloudRegion(cloudConfig), sameBeanAs(expected.get()));
    }

    @Test
    public void testGetCloudRegionExceptionTest() {
        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(isA(Class.class), isA(AAIResourceUri.class));
        assertNull(bbInputSetupUtils.getCloudRegion(new CloudConfiguration()));
    }

    @Test
    public void testGetCloudRegionEmptyId() {
        CloudConfiguration cloudConfig = new CloudConfiguration();
        cloudConfig.setLcpCloudRegionId("");

        assertNull(bbInputSetupUtils.getCloudRegion(cloudConfig));
    }

    @Test
    public void testGetCloudRegionEmptyConfiguration() {
        RequestDetails requestDetails = new RequestDetails();
        assertNull(bbInputSetupUtils.getCloudRegion(requestDetails.getCloudConfiguration()));
    }

    @Test
    public void testGetAAIInstanceGroup() {
        Optional<org.onap.aai.domain.yang.InstanceGroup> expected =
                Optional.of(new org.onap.aai.domain.yang.InstanceGroup());
        String instanceGroupId = "instanceGroupId";
        expected.get().setId(instanceGroupId);
        doReturn(expected).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.InstanceGroup.class,
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroupId));

        assertThat(bbInputSetupUtils.getAAIInstanceGroup(instanceGroupId), sameBeanAs(expected.get()));
    }

    @Test
    public void testGetAAIInstanceGroupThrowNotFound() {
        String instanceGroupId = "instanceGroupId";
        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.InstanceGroup.class,
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroupId));

        assertNull(bbInputSetupUtils.getAAIInstanceGroup(instanceGroupId));
    }

    @Test
    public void testGetAAICustomer() {
        Optional<org.onap.aai.domain.yang.Customer> expected = Optional.of(new org.onap.aai.domain.yang.Customer());
        String globalSubscriberId = "globalSubscriberId";
        expected.get().setGlobalCustomerId(globalSubscriberId);
        doReturn(expected).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.Customer.class,
                AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, globalSubscriberId));

        assertThat(bbInputSetupUtils.getAAICustomer(globalSubscriberId), sameBeanAs(expected.get()));
    }

    @Test
    public void testGetAAICustomerThrowNotFound() {
        String globalSubscriberId = "globalSubscriberId";
        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.Customer.class,
                AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, globalSubscriberId));

        assertNull(bbInputSetupUtils.getAAICustomer(globalSubscriberId));
    }

    @Test
    public void testGetAAIServiceSubscription() {
        Optional<org.onap.aai.domain.yang.ServiceSubscription> expected =
                Optional.of(new org.onap.aai.domain.yang.ServiceSubscription());
        String globalSubscriberId = "globalSubscriberId";
        String subscriptionServiceType = "subscriptionServiceType";
        expected.get().setServiceType(subscriptionServiceType);
        doReturn(expected).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.ServiceSubscription.class,
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_SUBSCRIPTION, globalSubscriberId, subscriptionServiceType));

        assertThat(bbInputSetupUtils.getAAIServiceSubscription(globalSubscriberId, subscriptionServiceType), sameBeanAs(expected.get()));
    }

    private static Stream<Arguments> testGetAAIServiceSubscriptionErrors() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of("", ""),
            Arguments.of("", null),
            Arguments.of(null, "")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testGetAAIServiceSubscriptionErrors(String globalSubId, String subServiceType) {
        org.onap.aai.domain.yang.ServiceSubscription actual =
            bbInputSetupUtils.getAAIServiceSubscription(globalSubId, subServiceType);
        assertNull(actual);
    }

    @Test
    public void testGetAAIServiceSubscriptionThrowNotFound() {
        String globalSubscriberId = "globalSubscriberId";
        String subscriptionServiceType = "subscriptionServiceType";
        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.ServiceSubscription.class,
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_SUBSCRIPTION, globalSubscriberId, subscriptionServiceType));
        assertNull(bbInputSetupUtils.getAAIServiceSubscription(globalSubscriberId, subscriptionServiceType));
    }

    @Test
    public void testGetAAIServiceInstanceById() {
        String serviceInstanceId = "serviceInstanceId";

        ServiceInstance expectedServiceInstance = new ServiceInstance();

        doReturn(Optional.of(expectedServiceInstance)).when(MOCK_aaiResourcesClient).get(isA(Class.class),
                isA(AAIResourceUri.class));

        assertThat(bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId), sameBeanAs(expectedServiceInstance));
    }

    @Test
    public void testGetAAIServiceInstanceByIdThrowNotFound() {
        String serviceInstanceId = "serviceInstanceId";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(isA(Class.class), isA(AAIResourceUri.class));

        assertNull(bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId));
    }

    @Test
    public void testGetAAIServiceInstanceByIdAndCustomer() {
        String globalCustomerId = "globalCustomerId";
        String serviceType = "serviceType";
        String serviceInstanceId = "serviceInstanceId";
        ServiceInstance expected = new ServiceInstance();
        expected.setServiceInstanceId(serviceInstanceId);

        doReturn(Optional.of(expected)).when(MOCK_aaiResourcesClient).get(ServiceInstance.class,
            AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustomerId, serviceType, serviceInstanceId).depth(Depth.TWO));

        assertThat(bbInputSetupUtils.getAAIServiceInstanceByIdAndCustomer(globalCustomerId, serviceType, serviceInstanceId), sameBeanAs(expected));
    }

    @Test
    public void testGetAAIServiceInstanceByIdAndCustomerThrowNotFound() {
        String globalCustomerId = "globalCustomerId";
        String serviceType = "serviceType";
        String serviceInstanceId = "serviceInstanceId";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(isA(Class.class), isA(AAIResourceUri.class));

        assertNull(bbInputSetupUtils.getAAIServiceInstanceByIdAndCustomer(globalCustomerId,serviceType, serviceInstanceId));
    }

    @Test
    public void testGetAAIServiceInstanceByName() throws Exception {
        String serviceInstanceName = "serviceInstanceName";

        ServiceInstance expectedServiceInstance = new ServiceInstance();
        expectedServiceInstance.setServiceInstanceId("serviceInstanceId");

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("serviceType");

        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");
        customer.setServiceSubscription(serviceSubscription);

        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(expectedServiceInstance);

        doReturn(Optional.of(serviceInstances)).when(MOCK_aaiResourcesClient).get(ServiceInstances.class,
            AAIUriFactory.createResourceUri(AAIObjectPlurals.SERVICE_INSTANCE, customer.getGlobalCustomerId(),
                    customer.getServiceSubscription().getServiceType())
                .queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO));

        assertThat(bbInputSetupUtils.getAAIServiceInstanceByName(serviceInstanceName, customer), sameBeanAs(expectedServiceInstance));
    }

    @Test
    public void testGetAAIServiceInstanceByNameException() {
        String serviceInstanceName = "serviceInstanceName";

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("serviceType");

        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");
        customer.setServiceSubscription(serviceSubscription);

        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(new ServiceInstance());
        serviceInstances.getServiceInstance().add(new ServiceInstance());

        doReturn(Optional.of(serviceInstances)).when(MOCK_aaiResourcesClient).get(isA(Class.class),
                isA(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class, () ->
            bbInputSetupUtils.getAAIServiceInstanceByName(serviceInstanceName, customer)
        );
        assertEquals(exception.getMessage(), "Multiple Service Instances Returned");
    }

    @Test
    public void testGetAAIServiceInstanceByNameNull() throws Exception {
        String serviceInstanceName = "serviceInstanceName";

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("serviceType");

        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");
        customer.setServiceSubscription(serviceSubscription);

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(isA(Class.class), isA(AAIResourceUri.class));

        assertNull(bbInputSetupUtils.getAAIServiceInstanceByName(serviceInstanceName, customer));
    }

    @Test
    public void testGetOptionalAAIServiceInstanceByNameException() {
        final String globalCustomerId = "globalCustomerId";
        final String serviceType = "serviceType";
        final String serviceInstanceId = "serviceInstanceId";

        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(new ServiceInstance());
        serviceInstances.getServiceInstance().add(new ServiceInstance());

        doReturn(Optional.of(serviceInstances)).when(MOCK_aaiResourcesClient).get(isA(Class.class),
                isA(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class,
            () -> bbInputSetupUtils.getAAIServiceInstanceByName(globalCustomerId, serviceType, serviceInstanceId));

        assertEquals(exception.getMessage(), "Multiple service instances found for customer-id: globalCustomerId, service-type: serviceType and service-instance-name: serviceInstanceId.");
    }

    @Test
    public void testGetOptionalAAIServiceInstanceByNameNull() throws Exception {
        String globalCustomerId = "globalCustomerId";
        String serviceType = "serviceType";
        String serviceInstanceId = "serviceInstanceId";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(isA(Class.class), isA(AAIResourceUri.class));
        Optional<ServiceInstance> actual = bbInputSetupUtils.getAAIServiceInstanceByName(globalCustomerId, serviceType, serviceInstanceId);

        assertFalse(actual.isPresent());
    }

    @Test
    public void testGetCatalogInstanceGroup() throws IOException {
        String modelUUID = "3ed64a41-9f7e-4d39-b31d-4924c4f568ad";

        org.onap.so.db.catalog.beans.InstanceGroup expectedInstanceGroup = mapper.readValue(
                new File(INSTANCE_GROUP_PATH), org.onap.so.db.catalog.beans.InstanceGroup.class);

        doReturn(expectedInstanceGroup).when(MOCK_catalogDbClient).getInstanceGroupByModelUUID(isA(String.class));

        assertThat(bbInputSetupUtils.getCatalogInstanceGroup(modelUUID), sameBeanAs(expectedInstanceGroup));
    }

    @Test
    public void testGetCollectionResourceInstanceGroupCustomization() {
        String modelCustomizationUUID = "d45bf2dd-be1c-4821-a2e2-c45c13b6f403";

        CollectionResourceInstanceGroupCustomization expectedCollection =
                new CollectionResourceInstanceGroupCustomization();

        doReturn(Arrays.asList(expectedCollection)).when(MOCK_catalogDbClient)
                .getCollectionResourceInstanceGroupCustomizationByModelCustUUID(modelCustomizationUUID);

        List<CollectionResourceInstanceGroupCustomization> actualCollection =
                bbInputSetupUtils.getCollectionResourceInstanceGroupCustomization(modelCustomizationUUID);

        assertThat(actualCollection, sameBeanAs(Arrays.asList(expectedCollection)));
    }

    @Test
    public void testGetAAIGenericVnf() throws IOException {
        String vnfId = "vnfId";

        GenericVnf expectedAaiVnf = mapper.readValue(new File(AAI_GENERIC_VNF_INPUT_PATH), GenericVnf.class);

        AAIResourceUri expectedUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE);
        doReturn(Optional.of(expectedAaiVnf)).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.GenericVnf.class,
            expectedUri);

        assertThat(bbInputSetupUtils.getAAIGenericVnf(vnfId), sameBeanAs(expectedAaiVnf));
    }

    @Test
    public void testGetAAIConfiguration() throws IOException {
        String configurationId = "configurationId";

        Configuration expectedAaiConfiguration =
                mapper.readValue(new File(CONFIGURATION_INPUT_PATH), Configuration.class);

        AAIResourceUri expectedUri = AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, configurationId).depth(Depth.ONE);
        doReturn(Optional.of(expectedAaiConfiguration)).when(MOCK_aaiResourcesClient).get(org.onap.aai.domain.yang.Configuration.class,
                expectedUri);

        assertThat(bbInputSetupUtils.getAAIConfiguration(configurationId), sameBeanAs(expectedAaiConfiguration));
    }

    @Test
    public void testGetAAIGenericVnfThrowNotFound() {
        String vnfId = "vnfId";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(isA(Class.class), isA(AAIResourceUri.class));

        assertNull(bbInputSetupUtils.getAAIGenericVnf(vnfId));
    }

    @Test
    public void testGetAAIResourceDepthOne() {
        String vnfId = "vnfId";
        AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
        AAIResourceUri expectedUri = aaiResourceUri.clone().depth(Depth.ONE);
        AAIResourceUri aaiResourceUriClone = aaiResourceUri.clone();
        bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri);

        verify(MOCK_aaiResourcesClient, times(1)).get(expectedUri);
        assertEquals("Uri should not have changed", aaiResourceUriClone.build(), aaiResourceUri.build());
    }

    @Test
    public void testGetAAIResourceDepthTwo() {
        String vnfId = "vnfId";
        AAIResourceUri aaiResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
        AAIResourceUri expectedUri = aaiResourceUri.clone().depth(Depth.TWO);
        AAIResourceUri aaiResourceUriClone = aaiResourceUri.clone();
        bbInputSetupUtils.getAAIResourceDepthTwo(aaiResourceUri);

        verify(MOCK_aaiResourcesClient, times(1)).get(expectedUri);
        assertEquals("Uri should not have changed", aaiResourceUriClone.build(), aaiResourceUri.build());
    }

    @Test
    public void getRelatedNetworkByNameFromServiceInstanceTest() throws Exception {
        String networkId = "id123";
        String networkName = "name123";

        Optional<L3Networks> expected = Optional.of(new L3Networks());
        L3Network network = new L3Network();
        network.setNetworkId(networkId);
        network.setNetworkName(networkName);
        expected.get().getL3Network().add(network);
        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(L3Networks.class), any(AAIResourceUri.class));
        Optional<L3Network> actual =
                bbInputSetupUtils.getRelatedNetworkByNameFromServiceInstance(networkId, networkName);
        assertEquals(actual.get().getNetworkId(), expected.get().getL3Network().get(0).getNetworkId());
    }

    @Test
    public void getRelatedNetworkByNameFromServiceInstanceMultipleNetworksExceptionTest() {
        final String serviceInstanceId = "serviceInstanceId";
        final String networkName = "networkName";

        L3Networks expected = new L3Networks();
        expected.getL3Network().add(new L3Network());
        expected.getL3Network().add(new L3Network());

        doReturn(Optional.of(expected)).when(MOCK_aaiResourcesClient).get(eq(L3Networks.class), any(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class,
            () -> bbInputSetupUtils.getRelatedNetworkByNameFromServiceInstance(serviceInstanceId, networkName));

        assertEquals(exception.getMessage(), "Multiple networks found for service-instance-id: serviceInstanceId and network-name: networkName.");
    }

    @Test
    public void getRelatedNetworkByNameFromServiceInstanceNotFoundTest() throws Exception {
        final String serviceInstanceId = "serviceInstanceId";
        final String networkName = "networkName";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(eq(L3Networks.class), any(AAIResourceUri.class));

        Optional<L3Network> actualNetwork =
                bbInputSetupUtils.getRelatedNetworkByNameFromServiceInstance(serviceInstanceId, networkName);

        assertEquals(Optional.empty(), actualNetwork);
    }

    @Test
    public void getRelatedServiceInstanceFromInstanceGroupTest() throws Exception {
        Optional<ServiceInstances> expected = Optional.of(new ServiceInstances());
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        serviceInstance.setServiceInstanceName("serviceInstanceName");
        expected.get().getServiceInstance().add(serviceInstance);

        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(ServiceInstances.class), any(AAIResourceUri.class));
        Optional<ServiceInstance> actual = bbInputSetupUtils.getRelatedServiceInstanceFromInstanceGroup("ig-001");
        assertEquals(actual.get().getServiceInstanceId(),
                expected.get().getServiceInstance().get(0).getServiceInstanceId());
    }

    @Test
    public void getRelatedServiceInstanceFromInstanceGroupMultipleTest() {
        Optional<ServiceInstances> serviceInstances = Optional.of(new ServiceInstances());
        serviceInstances.get().getServiceInstance().add(new ServiceInstance());
        serviceInstances.get().getServiceInstance().add(new ServiceInstance());

        doReturn(serviceInstances).when(MOCK_aaiResourcesClient).get(eq(ServiceInstances.class),
                any(AAIResourceUri.class));

        MultipleObjectsFoundException exception = assertThrows(MultipleObjectsFoundException.class,
            () -> bbInputSetupUtils.getRelatedServiceInstanceFromInstanceGroup("ig-001"));

        assertEquals(exception.getMessage(), "Mulitple service instances were found for instance-group-id: ig-001.");
    }

    @Test
    public void getRelatedServiceInstanceFromInstanceGroupNotFoundTest() {
        doReturn(Optional.of(new ServiceInstances())).when(MOCK_aaiResourcesClient).get(eq(ServiceInstances.class),
                any(AAIResourceUri.class));

        NoServiceInstanceFoundException exception = assertThrows(NoServiceInstanceFoundException.class,
            () -> this.bbInputSetupUtils.getRelatedServiceInstanceFromInstanceGroup("ig-001"));

        assertEquals(exception.getMessage(), "No ServiceInstances Returned");
    }

    @Test
    public void getRelatedVnfByNameFromServiceInstanceTest() throws Exception {
        String vnfId = "id123";
        String vnfName = "name123";

        Optional<GenericVnfs> expected = Optional.of(new GenericVnfs());
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId(vnfId);
        vnf.setVnfName(vnfName);
        expected.get().getGenericVnf().add(vnf);

        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(GenericVnfs.class), any(AAIResourceUri.class));
        Optional<GenericVnf> actual = bbInputSetupUtils.getRelatedVnfByNameFromServiceInstance(vnfId, vnfName);
        assertEquals(actual.get().getVnfId(), expected.get().getGenericVnf().get(0).getVnfId());
    }

    @Test
    public void getRelatedVnfByNameFromServiceInstanceMultipleVnfsExceptionTest() {
        final String serviceInstanceId = "serviceInstanceId";
        final String vnfName = "vnfName";

        GenericVnfs expectedVnf = new GenericVnfs();
        expectedVnf.getGenericVnf().add(new GenericVnf());
        expectedVnf.getGenericVnf().add(new GenericVnf());

        doReturn(Optional.of(expectedVnf)).when(MOCK_aaiResourcesClient).get(eq(GenericVnfs.class), any(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class,
            () -> bbInputSetupUtils.getRelatedVnfByNameFromServiceInstance(serviceInstanceId, vnfName));

        assertEquals(exception.getMessage(), "Multiple vnfs found for service-instance-id: serviceInstanceId and vnf-name: vnfName.");
    }

    @Test
    public void getRelatedVnfByNameFromServiceInstanceNotFoundTest() throws Exception {
        final String serviceInstanceId = "serviceInstanceId";
        final String vnfName = "vnfName";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(eq(GenericVnfs.class), any(AAIResourceUri.class));

        Optional<GenericVnf> actualVnf =
                this.bbInputSetupUtils.getRelatedVnfByNameFromServiceInstance(serviceInstanceId, vnfName);

        assertEquals(actualVnf, Optional.empty());
    }

    @Test
    public void getRelatedVolumeGroupByNameFromVnfTest() throws Exception {
        final String volumeGroupId = "id123";
        final String volumeGroupName = "name123";

        Optional<VolumeGroups> expected = Optional.of(new VolumeGroups());
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId);
        volumeGroup.setVolumeGroupName(volumeGroupName);
        expected.get().getVolumeGroup().add(volumeGroup);

        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class), any(AAIResourceUri.class));
        Optional<VolumeGroup> actual = bbInputSetupUtils.getRelatedVolumeGroupByNameFromVnf(volumeGroupId, volumeGroupName);

        assertEquals(actual.get().getVolumeGroupId(), expected.get().getVolumeGroup().get(0).getVolumeGroupId());
    }

    @Test
    public void getRelatedVolumeGroupByNameFromVnfMultipleVolumeGroupsExceptionTest() {
        final String vnfId = "vnfId";
        final String volumeGroupName = "volumeGroupName";

        VolumeGroups expectedVolumeGroup = new VolumeGroups();
        expectedVolumeGroup.getVolumeGroup().add(new VolumeGroup());
        expectedVolumeGroup.getVolumeGroup().add(new VolumeGroup());

        doReturn(Optional.of(expectedVolumeGroup)).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class),
                any(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class,
            () -> bbInputSetupUtils.getRelatedVolumeGroupByNameFromVnf(vnfId, volumeGroupName));

        assertEquals(exception.getMessage(), "Multiple volume-groups found for vnf-id: vnfId and volume-group-name: volumeGroupName.");
    }

    @Test
    public void getRelatedVolumeGroupByNameFromVnfNotFoundTest() throws Exception {
        final String vnfId = "vnfId";
        final String volumeGroupName = "volumeGroupName";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class), any(AAIResourceUri.class));

        assertEquals(bbInputSetupUtils.getRelatedVolumeGroupByNameFromVnf(vnfId, volumeGroupName), Optional.empty());
    }

    @Test
    public void getRelatedVolumeGroupByNameFromVfModuleTest() throws Exception {
        final String volumeGroupName = "name123";
        Optional<VolumeGroups> expected = Optional.of(new VolumeGroups());
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupName(volumeGroupName);
        expected.get().getVolumeGroup().add(volumeGroup);

        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class), any(AAIResourceUri.class));

        Optional<VolumeGroup> actual = bbInputSetupUtils.getRelatedVolumeGroupByNameFromVfModule("vnfId123", "id123", volumeGroupName);
        assertEquals(actual.get().getVolumeGroupId(), expected.get().getVolumeGroup().get(0).getVolumeGroupId());
    }

    @Test
    public void getRelatedVolumeGroupByNameFromVfModuleMultipleVolumeGroupsExceptionTest() {
        final String vnfId = "vnfId";
        final String volumeGroupId = "volumeGroupId";
        final String volumeGroupName = "volumeGroupName";

        VolumeGroups expectedVolumeGroup = new VolumeGroups();
        expectedVolumeGroup.getVolumeGroup().add(new VolumeGroup());
        expectedVolumeGroup.getVolumeGroup().add(new VolumeGroup());

        doReturn(Optional.of(expectedVolumeGroup)).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class),
                any(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class,
            () -> bbInputSetupUtils.getRelatedVolumeGroupByNameFromVfModule(vnfId, volumeGroupId, volumeGroupName));

        assertEquals(exception.getMessage(), "Multiple voulme-groups found for vnf-id: vnfId, vf-module-id: volumeGroupId and volume-group-name: volumeGroupName.");
    }

    @Test
    public void getRelatedVolumeGroupFromVfModuleNotFoundTest() throws Exception {
        String vnfId = "vnfId";
        String volumeGroupId = "volumeGroupId";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class), any(AAIResourceUri.class));

        Optional<VolumeGroup> actualVolumeGroup =
                bbInputSetupUtils.getRelatedVolumeGroupFromVfModule(vnfId, volumeGroupId);

        assertEquals(actualVolumeGroup, Optional.empty());
    }

    @Test
    public void getRelatedVolumeGroupFromVfModuleTest() throws Exception {
        Optional<VolumeGroups> expected = Optional.of(new VolumeGroups());
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("id123");
        expected.get().getVolumeGroup().add(volumeGroup);
        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class), any(AAIResourceUri.class));
        Optional<VolumeGroup> actual = this.bbInputSetupUtils.getRelatedVolumeGroupFromVfModule("id123", "id123");
        assertEquals(actual.get().getVolumeGroupId(), expected.get().getVolumeGroup().get(0).getVolumeGroupId());
    }

    @Test
    public void getRelatedVolumeGroupByNameFromVfModuleNotFoundTest() throws Exception {
        final String vnfId = "vnfId";
        final String volumeGroupId = "volumeGroupId";
        final String volumeGroupName = "volumeGroupName";

        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(eq(VolumeGroups.class), any(AAIResourceUri.class));

        Optional<VolumeGroup> actualVolumeGroup =
                bbInputSetupUtils.getRelatedVolumeGroupByNameFromVfModule(vnfId, volumeGroupId, volumeGroupName);

        assertEquals(actualVolumeGroup, Optional.empty());
    }

    @Test
    public void loadOriginalFlowExecutionPathTest() throws IOException{
        final String requestId = "123";
        final String originalRequestId = "originalRequestId";
        ObjectMapper objectMapper = new ObjectMapper();

        final String flowsToExecuteString = new String(
            Files.readAllBytes(
                Paths.get(FLOWS_TO_EXECUTE_PATH)), StandardCharsets.UTF_8);
        ExecuteBuildingBlock[] buildingBlocks = objectMapper.readValue(flowsToExecuteString, ExecuteBuildingBlock[].class);

        InfraActiveRequests request = new InfraActiveRequests();
        request.setOriginalRequestId(originalRequestId);

        RequestProcessingData rpd = new RequestProcessingData();
        rpd.setValue(flowsToExecuteString);

        doReturn(request).when(MOCK_requestsDbClient).getInfraActiveRequestbyRequestId(requestId);
        doReturn(rpd).when(MOCK_requestsDbClient).getRequestProcessingDataBySoRequestIdAndName(originalRequestId, "flowExecutionPath");

        List<ExecuteBuildingBlock> flowsToExecute = bbInputSetupUtils.loadOriginalFlowExecutionPath(requestId);

        assertEquals(objectMapper.writeValueAsString(flowsToExecute), objectMapper.writeValueAsString(buildingBlocks));
    }

    @Test
    public void getRelatedConfigurationByNameFromServiceInstanceExceptionTest() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setConfigurationId("id123");

        Configurations configurations = new Configurations();
        configurations.getConfiguration().add(configuration);
        configurations.getConfiguration().add(configuration);

        Optional<Configurations> optConfigurations = Optional.of(configurations);

        doReturn(optConfigurations).when(MOCK_aaiResourcesClient).get(eq(Configurations.class),
                any(AAIResourceUri.class));

        expectedException.expect(MultipleObjectsFoundException.class);
        this.bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance("id123", "name123");
    }

    @Test
    public void getRelatedConfigurationByNameFromServiceInstanceNotFoundTest() throws Exception {
        doReturn(Optional.empty()).when(MOCK_aaiResourcesClient).get(eq(Configurations.class),
                any(AAIResourceUri.class));
        Optional<Configuration> actualConfiguration =
                bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance("id123", "name123");
        assertEquals(actualConfiguration, Optional.empty());
    }

    @Test
    public void getRelatedConfigurationByNameFromServiceInstanceTest() throws Exception {
        Optional<Configurations> expected = Optional.of(new Configurations());
        Configuration expectedConfiguration = new Configuration();
        expectedConfiguration.setConfigurationId("id123");
        expected.get().getConfiguration().add(expectedConfiguration);

        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(Configurations.class), any(AAIResourceUri.class));
        Optional<Configuration> actualConfiguration =
                bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance("id1", "name1");

        assertEquals(actualConfiguration.get(), expected.get().getConfiguration().get(0));
    }

    @Test
    public void throwExceptionWhenMultipleConfigurationsExistTest() {
        Optional<Configurations> expected = Optional.of(new Configurations());
        expected.get().getConfiguration().add(new Configuration());
        expected.get().getConfiguration().add(new Configuration());

        doReturn(expected).when(MOCK_aaiResourcesClient).get(eq(Configurations.class), any(AAIResourceUri.class));

        Exception exception = assertThrows(Exception.class, () ->
            bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance("id1", "name1")
        );

        assertEquals(exception.getMessage(), "Multiple configurations found for service-instance-d: id1 and configuration-name: name1.");
    }

    @Test
    public void returnEmptyOptionalWhenConfigurationsIsEmpty() throws Exception {
        Optional<Configuration> actualConfiguration = prepareConfiguration(Optional.empty());
        assertFalse(actualConfiguration.isPresent());
    }

    @Test
    public void returnEmptyOptionalWhenConfigurationsSizeIsZero() throws Exception {
        Optional<Configuration> actualConfiguration = prepareConfiguration(Optional.of(new Configurations()));
        assertFalse(actualConfiguration.isPresent());
    }

    private Optional<Configuration> prepareConfiguration(Optional<Configurations> configurations) throws Exception {
        doReturn(configurations).when(MOCK_aaiResourcesClient).get(eq(Configurations.class), any(AAIResourceUri.class));
        return bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance("id1", "name1");
    }
    @Test
    public void existsAAIVfModuleGloballyByNameTest() throws Exception {
        AAIResourceUri expectedUri =
                AAIUriFactory.createNodesUri(AAIObjectPlurals.VF_MODULE).queryParam("vf-module-name", "testVfModule");
        bbInputSetupUtils.existsAAIVfModuleGloballyByName("testVfModule");
        verify(MOCK_aaiResourcesClient, times(1)).exists(expectedUri);
    }

    @Test
    public void existsAAIConfigurationGloballyByNameTest() throws Exception {
        AAIResourceUri expectedUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.CONFIGURATION)
                .queryParam("configuration-name", "testConfig");
        bbInputSetupUtils.existsAAIConfigurationGloballyByName("testConfig");
        verify(MOCK_aaiResourcesClient, times(1)).exists(expectedUri);
    }

    @Test
    public void existsAAINetworksGloballyByNameTest() throws Exception {
        AAIResourceUri expectedUri =
                AAIUriFactory.createResourceUri(AAIObjectPlurals.L3_NETWORK).queryParam("network-name", "testNetwork");
        bbInputSetupUtils.existsAAINetworksGloballyByName("testNetwork");
        verify(MOCK_aaiResourcesClient, times(1)).exists(expectedUri);
    }

    @Test
    public void existsAAIVolumeGroupGloballyByNameTest() throws Exception {
        AAIResourceUri expectedUri = AAIUriFactory.createNodesUri(AAIObjectPlurals.VOLUME_GROUP)
                .queryParam("volume-group-name", "testVoumeGroup");
        bbInputSetupUtils.existsAAIVolumeGroupGloballyByName("testVoumeGroup");
        verify(MOCK_aaiResourcesClient, times(1)).exists(expectedUri);
    }
}
