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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.VfModuleTopologyOperationRequestMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCVfModuleResourcesTest extends TestDataSetup {

    @InjectMocks
    private SDNCVfModuleResources sdncVfModuleResources;

    @Mock
    protected VfModuleTopologyOperationRequestMapper vfModuleTopologyMapper;

    private VfModule vfModule;
    private GenericVnf vnf;
    private ServiceInstance serviceInstance;
    private VolumeGroup volumeGroup;
    private Customer customer;
    private CloudRegion cloudRegion;
    private RequestContext requestContext;
    private GenericResourceApiVfModuleOperationInformation sdncReq;

    private URI testURI;

    @Before
    public void before() {
        vfModule = buildVfModule();
        vnf = buildGenericVnf();
        serviceInstance = buildServiceInstance();
        volumeGroup = buildVolumeGroup();
        customer = buildCustomer();
        cloudRegion = buildCloudRegion();
        requestContext = buildRequestContext();
        sdncReq = new GenericResourceApiVfModuleOperationInformation();
        try {
            testURI = new URI("http://localhost:9800");
        } catch (URISyntaxException e) {

        }
    }

    @Test
    public void assignVfModuleTest() throws MapperException {
        doReturn(sdncReq).when(vfModuleTopologyMapper).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.ASSIGN, vfModule, volumeGroup, vnf, serviceInstance, customer, cloudRegion,
                requestContext, null, testURI);
        sdncVfModuleResources.assignVfModule(vfModule, volumeGroup, vnf, serviceInstance, customer, cloudRegion,
                requestContext, testURI);
        verify(vfModuleTopologyMapper, times(1)).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.ASSIGN, vfModule, volumeGroup, vnf, serviceInstance, customer, cloudRegion,
                requestContext, null, testURI);
    }

    @Test
    public void unassignVfModuleTest() throws MapperException {
        doReturn(sdncReq).when(vfModuleTopologyMapper).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.UNASSIGN, vfModule, null, vnf, serviceInstance, null, null, requestContext, null,
                testURI);
        sdncVfModuleResources.unassignVfModule(vfModule, vnf, serviceInstance, requestContext, testURI);
        verify(vfModuleTopologyMapper, times(1)).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.UNASSIGN, vfModule, null, vnf, serviceInstance, null, null, requestContext, null,
                testURI);
    }

    @Test
    public void activateVfModuleTest() throws MapperException {
        doReturn(sdncReq).when(vfModuleTopologyMapper).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.ACTIVATE, vfModule, null, vnf, serviceInstance, customer, cloudRegion, requestContext,
                null, testURI);
        sdncVfModuleResources.activateVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext,
                testURI);
        verify(vfModuleTopologyMapper, times(1)).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.ACTIVATE, vfModule, null, vnf, serviceInstance, customer, cloudRegion, requestContext,
                null, testURI);
    }

    @Test
    public void deactivateVfModuleTest() throws MapperException {
        doReturn(sdncReq).when(vfModuleTopologyMapper).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.DEACTIVATE, vfModule, null, vnf, serviceInstance, customer, cloudRegion, requestContext,
                null, testURI);
        sdncVfModuleResources.deactivateVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext,
                testURI);
        verify(vfModuleTopologyMapper, times(1)).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.DEACTIVATE, vfModule, null, vnf, serviceInstance, customer, cloudRegion, requestContext,
                null, testURI);
    }

    @Test
    public void changeAssignVfModuleTest() throws MapperException, BadResponseException {
        doReturn(sdncReq).when(vfModuleTopologyMapper).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.CHANGE_ASSIGN, vfModule, null, vnf, serviceInstance, customer, cloudRegion,
                requestContext, null, testURI);
        sdncVfModuleResources.changeAssignVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion,
                requestContext, testURI);
        verify(vfModuleTopologyMapper, times(1)).reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
                SDNCSvcAction.CHANGE_ASSIGN, vfModule, null, vnf, serviceInstance, customer, cloudRegion,
                requestContext, null, testURI);
    }
}
