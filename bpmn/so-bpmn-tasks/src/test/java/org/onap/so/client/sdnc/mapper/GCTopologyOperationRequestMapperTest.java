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

package org.onap.so.client.sdnc.mapper;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBondingLink;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;

import org.onap.sdnc.apps.client.model.GenericResourceApiGcTopologyOperationInformation;


public class GCTopologyOperationRequestMapperTest extends TestDataSetup{

    private GCTopologyOperationRequestMapper genObjMapper = new GCTopologyOperationRequestMapper();

    @Test
    public void deactivateOrUnassignVnrReqMapperTest() {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("MsoRequestId");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("ServiceInstanceId");
        Configuration Configuration = new Configuration();
        Configuration.setConfigurationId("ConfigurationId");
        GenericResourceApiGcTopologyOperationInformation genericInfo = genObjMapper.deactivateOrUnassignVnrReqMapper
                (SDNCSvcAction.UNASSIGN, serviceInstance, requestContext, Configuration);

        Assert.assertNotNull(genericInfo);
        Assert.assertNotNull(genericInfo.getSdncRequestHeader().getSvcRequestId());
    }



    private VpnBondingLink getVpnBondingLink() {
        VpnBondingLink vpnBondingLink = new VpnBondingLink();
        Configuration vrfConfiguration = getVRFConfiguration();
        vpnBondingLink.setVrfConfiguration(vrfConfiguration);
        Configuration vnrConfiguration = getVNRConfiguration();
        vpnBondingLink.setVnrConfiguration(vnrConfiguration);
        vpnBondingLink.setTransportServiceProxy(buildServiceProxy(buildServiceInstance(buildGenericVnf())));
        return vpnBondingLink;
    }

    private RequestContext getRequestContext() {
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("MsoRequestId");
        HashMap<String, String> userParams = getUserParams();
        requestContext.setUserParams(userParams);
        return requestContext;
    }

    private HashMap<String, String> getUserParams() {
        HashMap<String,String> userParams = new HashMap<>();
        userParams.put("lppCustomerId","lppCustomerId");
        return userParams;
    }

    private ServiceProxy buildServiceProxy(ServiceInstance serviceInstance) {
        ServiceProxy serviceProxy = new ServiceProxy();
        serviceProxy.setServiceInstance(serviceInstance);
        return serviceProxy;
    }

    private Configuration getVRFConfiguration() {
        Configuration vrfConfiguration = new Configuration();
        vrfConfiguration.setConfigurationId("ConfigurationId");
        vrfConfiguration.setConfigurationName("ConfigurationName");
        vrfConfiguration.setConfigurationSubType("ConfigurationSubType");
        vrfConfiguration.setConfigurationType("VRF-ENTRY");
        return vrfConfiguration;
    }

    public Configuration getVNRConfiguration() {
        Configuration vnrConfiguration = new Configuration();
        vnrConfiguration.setConfigurationId("ConfigurationId");
        vnrConfiguration.setConfigurationName("ConfigurationName");
        vnrConfiguration.setConfigurationSubType("ConfigurationSubType");
        vnrConfiguration.setConfigurationType("VNRConfiguration");
        L3Network l3Network = getL3Network();
        vnrConfiguration.setNetwork(l3Network);
        return vnrConfiguration;
    }

    public L3Network getL3Network() {
        L3Network l3Network = new L3Network();
        l3Network.setNetworkId("l3NetworkId");
        Subnet ipv4subnet = getSubnet("ipv4CidrMask", "ipv4NetworkStartAddress", "IPV4");
        Subnet ipv6subnet = getSubnet("ipv6CidrMask", "ipv6NetworkStartAddress", "IPV6");
        l3Network.getSubnets().add(ipv4subnet);
        l3Network.getSubnets().add(ipv6subnet);
        return l3Network;
    }

    private Subnet getSubnet(String ipv4CidrMask, String ipv4NetworkStartAddress, String ipv4) {
        Subnet ipv4subnet = new Subnet();
        ipv4subnet.setCidrMask(ipv4CidrMask);
        ipv4subnet.setNetworkStartAddress(ipv4NetworkStartAddress);
        ipv4subnet.setIpVersion(ipv4);
        return ipv4subnet;
    }

    private ServiceInstance  buildServiceInstance(GenericVnf vnf) {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("ServiceInstanceId");
        List<GenericVnf> vnfs = serviceInstance.getVnfs();
        vnfs.add(vnf);
        return serviceInstance;
    }
}
