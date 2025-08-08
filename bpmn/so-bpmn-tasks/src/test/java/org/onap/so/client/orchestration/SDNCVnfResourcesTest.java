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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.VnfTopologyOperationRequestMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCVnfResourcesTest extends TestDataSetup {
    @InjectMocks
    private SDNCVnfResources sdncVnfResources;

    @Mock
    protected VnfTopologyOperationRequestMapper MOCK_vnfTopologyOperationRequestMapper;

    @Mock
    protected SDNCClient MOCK_sdncClient;

    private GenericVnf genericVnf;
    private ServiceInstance serviceInstance;
    private Customer customer;
    private CloudRegion cloudRegion;
    private RequestContext requestContext;
    private GenericResourceApiVnfOperationInformation sdncReq;
    private URI testURI;

    @Before
    public void before() {
        serviceInstance = buildServiceInstance();
        genericVnf = buildGenericVnf();
        customer = buildCustomer();
        cloudRegion = buildCloudRegion();
        requestContext = buildRequestContext();
        sdncReq = new GenericResourceApiVnfOperationInformation();
        try {
            testURI = new URI("http://localhost:9800");
        } catch (URISyntaxException e) {

        }
    }

    @Test
    public void assignVnfTest() {
        doReturn(sdncReq).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.assignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, false, testURI);
        verify(MOCK_vnfTopologyOperationRequestMapper, times(1)).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
    }

    @Test
    public void activateVnfTest() {
        doReturn(sdncReq).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.activateVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
        verify(MOCK_vnfTopologyOperationRequestMapper, times(1)).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
    }

    @Test
    public void deleteVnfTest() {
        doReturn(sdncReq).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.deleteVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
        verify(MOCK_vnfTopologyOperationRequestMapper, times(1)).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
    }

    @Test
    public void queryVnfTest() throws MapperException, BadResponseException {
        doReturn("test").when(MOCK_sdncClient).get(isA(String.class));
        genericVnf.setSelflink("testSelflink");
        sdncVnfResources.queryVnf(genericVnf);
        verify(MOCK_sdncClient, times(1)).get(isA(String.class));
    }

    @Test
    public void queryVnfWithResourcePrefixTest() throws MapperException, BadResponseException {
        doReturn("test").when(MOCK_sdncClient).get(isA(String.class));
        genericVnf.setSelflink("restconf/test:testSelflink");
        sdncVnfResources.queryVnf(genericVnf);
        verify(MOCK_sdncClient, times(1)).get(isA(String.class));
    }

    @Test
    public void changeModelVnfTest() {
        doReturn(sdncReq).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(isA(SDNCSvcOperation.class),
                isA(SDNCSvcAction.class), isA(GenericResourceApiRequestActionEnumeration.class), isA(GenericVnf.class),
                isA(ServiceInstance.class), isA(Customer.class), isA(CloudRegion.class), isA(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.changeModelVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
        verify(MOCK_vnfTopologyOperationRequestMapper, times(1)).reqMapper(eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.CHANGE_ASSIGN), eq(GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE),
                eq(genericVnf), eq(serviceInstance), eq(customer), eq(cloudRegion), eq(requestContext), eq(false),
                any(URI.class));
    }

    @Test
    public void deactivateVnfSuccessTest() {
        doReturn(sdncReq).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(
                eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DEACTIVATE),
                isA(GenericResourceApiRequestActionEnumeration.class), any(GenericVnf.class),
                any(ServiceInstance.class), any(Customer.class), any(CloudRegion.class), any(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.deactivateVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
        verify(MOCK_vnfTopologyOperationRequestMapper, times(1)).reqMapper(eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.DEACTIVATE), isA(GenericResourceApiRequestActionEnumeration.class),
                any(GenericVnf.class), any(ServiceInstance.class), any(Customer.class), any(CloudRegion.class),
                any(RequestContext.class), anyBoolean(), any(URI.class));

    }

    @Test
    public void deactivateVnfExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(
                eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION), eq(SDNCSvcAction.DEACTIVATE),
                isA(GenericResourceApiRequestActionEnumeration.class), any(GenericVnf.class),
                any(ServiceInstance.class), any(Customer.class), any(CloudRegion.class), any(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.deactivateVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
    }

    @Test
    public void unassignVnfSuccessTest() {
        doReturn(sdncReq).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(
                eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION), eq(SDNCSvcAction.UNASSIGN),
                isA(GenericResourceApiRequestActionEnumeration.class), any(GenericVnf.class),
                any(ServiceInstance.class), any(Customer.class), any(CloudRegion.class), any(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
        verify(MOCK_vnfTopologyOperationRequestMapper, times(1)).reqMapper(eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION),
                eq(SDNCSvcAction.UNASSIGN), isA(GenericResourceApiRequestActionEnumeration.class),
                any(GenericVnf.class), any(ServiceInstance.class), any(Customer.class), any(CloudRegion.class),
                any(RequestContext.class), anyBoolean(), any(URI.class));
    }

    @Test
    public void unassignVnfExceptionTest() {
        expectedException.expect(Exception.class);
        doThrow(Exception.class).when(MOCK_vnfTopologyOperationRequestMapper).reqMapper(
                eq(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION), eq(SDNCSvcAction.UNASSIGN),
                isA(GenericResourceApiRequestActionEnumeration.class), any(GenericVnf.class),
                any(ServiceInstance.class), any(Customer.class), any(CloudRegion.class), any(RequestContext.class),
                anyBoolean(), any(URI.class));
        sdncVnfResources.unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, testURI);
    }
}
