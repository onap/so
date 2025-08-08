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

package org.onap.so.client.orchestration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIServiceInstanceResourcesTest extends TestDataSetup {

    @InjectMocks
    private AAIServiceInstanceResources aaiServiceInstanceResources = new AAIServiceInstanceResources();

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    private ServiceInstance serviceInstance;
    private ServiceSubscription serviceSubscription;
    private Customer customer;
    private Project project;
    private OwningEntity owningEntity;

    @Before
    public void before() {
        serviceInstance = buildServiceInstance();
        serviceSubscription = buildServiceSubscription();
        customer = buildCustomer();
        project = buildProject();
        owningEntity = buildOwningEntity();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }

    @Test
    public void deleteServiceInstanceSuccessTest() {
        aaiServiceInstanceResources.deleteServiceInstance(serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void deleteServiceInstanceExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));
        aaiServiceInstanceResources.deleteServiceInstance(serviceInstance);
    }

    @Test
    public void existsServiceInstanceTest() {
        aaiServiceInstanceResources.existsServiceInstance(serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).exists(any(AAIResourceUri.class));
    }

    @Test
    public void createServiceSubscriptionTest() {
        serviceSubscription.setServiceType("IP-FLEX");
        customer.setServiceSubscription(serviceSubscription);
        doReturn(new org.onap.aai.domain.yang.ServiceSubscription()).when(MOCK_aaiObjectMapper)
                .mapServiceSubscription(customer.getServiceSubscription());
        aaiServiceInstanceResources.createServiceSubscription(customer);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    @Test
    public void createServiceInstanceTest() {
        serviceSubscription.setServiceType("testSubscriberType");
        customer.setServiceSubscription(serviceSubscription);
        doReturn(new org.onap.aai.domain.yang.ServiceInstance()).when(MOCK_aaiObjectMapper)
                .mapServiceInstance(serviceInstance);
        serviceInstance.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        aaiServiceInstanceResources.createServiceInstance(serviceInstance, customer);

        assertEquals(OrchestrationStatus.INVENTORIED, serviceInstance.getOrchestrationStatus());
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    @Test
    public void createProjectTest() {
        doReturn(new org.onap.aai.domain.yang.Project()).when(MOCK_aaiObjectMapper).mapProject(project);
        aaiServiceInstanceResources.createProject(project);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    @Test
    public void createProjectandConnectServiceInstanceTest() {
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(any(AAIResourceUri.class),
                any(Optional.class));
        doNothing().when(MOCK_aaiResourcesClient).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
        doReturn(new org.onap.aai.domain.yang.Project()).when(MOCK_aaiObjectMapper).mapProject(project);
        aaiServiceInstanceResources.createProjectandConnectServiceInstance(project, serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void createOwningEntityTest() {
        doReturn(new org.onap.aai.domain.yang.OwningEntity()).when(MOCK_aaiObjectMapper).mapOwningEntity(owningEntity);
        aaiServiceInstanceResources.createOwningEntity(owningEntity);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
    }

    @Test
    public void existsOwningEntityTest() {
        aaiServiceInstanceResources.existsOwningEntity(owningEntity);
        verify(MOCK_aaiResourcesClient, times(1)).exists(any(AAIResourceUri.class));
    }

    @Test
    public void connectOwningEntityandServiceInstanceTest() {
        aaiServiceInstanceResources.connectOwningEntityandServiceInstance(owningEntity, serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void createOwningEntityandConnectServiceInstanceTest() {
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(any(AAIResourceUri.class),
                any(Optional.class));
        doNothing().when(MOCK_aaiResourcesClient).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
        doReturn(new org.onap.aai.domain.yang.OwningEntity()).when(MOCK_aaiObjectMapper).mapOwningEntity(owningEntity);
        aaiServiceInstanceResources.createOwningEntityandConnectServiceInstance(owningEntity, serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    @Ignore
    public void updateOrchestrationStatusServiceInstanceTest() {
        aaiServiceInstanceResources.updateOrchestrationStatusServiceInstance(serviceInstance,
                OrchestrationStatus.ACTIVE);
        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class),
                any(org.onap.aai.domain.yang.ServiceInstance.class));
    }

    @Test
    @Ignore
    public void test_updateServiceInstance() {
        aaiServiceInstanceResources.updateServiceInstance(serviceInstance);
        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class),
                any(org.onap.aai.domain.yang.ServiceInstance.class));
    }

    @Test
    public void checkInstanceServiceNameInUseTrueTest() {
        AAIPluralResourceUri uri = AAIUriFactory.createNodesUri(Types.SERVICE_INSTANCES.getFragment())
                .queryParam("service-instance-name", serviceInstance.getServiceInstanceName());
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(uri));
        boolean nameInUse = aaiServiceInstanceResources.checkInstanceServiceNameInUse(serviceInstance);
        assertTrue(nameInUse);
    }

    @Test
    public void checkInstanceServiceNameInUseFalseTest() {
        AAIPluralResourceUri uri = AAIUriFactory.createNodesUri(Types.SERVICE_INSTANCES.getFragment())
                .queryParam("service-instance-name", serviceInstance.getServiceInstanceName());
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(uri));
        boolean nameInUse = aaiServiceInstanceResources.checkInstanceServiceNameInUse(serviceInstance);
        assertFalse(nameInUse);
    }

}
