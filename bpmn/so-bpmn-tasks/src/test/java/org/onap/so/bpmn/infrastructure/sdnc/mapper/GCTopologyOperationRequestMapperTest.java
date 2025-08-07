/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.sdnc.mapper;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdnc.northbound.client.model.GenericResourceApiGcTopologyOperationInformation;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GCTopologyOperationRequestMapperTest extends TestDataSetup {


    @Spy
    private GeneralTopologyObjectMapper generalTopologyObjectMapper;

    @InjectMocks
    private GCTopologyOperationRequestMapper genObjMapper = new GCTopologyOperationRequestMapper();

    @Test
    public void deactivateOrUnassignVnrReqMapperTest() throws URISyntaxException {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("MsoRequestId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("ServiceInstanceId");
        Configuration Configuration = new Configuration();
        Configuration.setConfigurationId("ConfigurationId");
        Configuration.setConfigurationType("VLAN-NETWORK-RECEPTOR");
        GenericResourceApiGcTopologyOperationInformation genericInfo =
                genObjMapper.deactivateOrUnassignVnrReqMapper(SDNCSvcAction.UNASSIGN, serviceInstance, requestContext,
                        Configuration, "uuid", new URI("http://localhost"));

        Assert.assertNotNull(genericInfo);
        Assert.assertNotNull(genericInfo.getRequestInformation());
        Assert.assertNotNull(genericInfo.getSdncRequestHeader());
        Assert.assertNotNull(genericInfo.getClass());
        Assert.assertNotNull(genericInfo.getServiceInformation());
        Assert.assertEquals("ConfigurationId", genericInfo.getConfigurationInformation().getConfigurationId());
        Assert.assertEquals("VLAN-NETWORK-RECEPTOR", genericInfo.getConfigurationInformation().getConfigurationType());
        Assert.assertEquals("uuid", genericInfo.getSdncRequestHeader().getSvcRequestId());
        Assert.assertEquals("http://localhost", genericInfo.getSdncRequestHeader().getSvcNotificationUrl());
        Assert.assertEquals("MsoRequestId", genericInfo.getRequestInformation().getRequestId());
    }

}
