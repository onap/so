package org.openecomp.mso.client.sdnc.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.BuildingBlockTestDataSetup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.*;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.ServiceProxy;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;


public class GCTopologyOperationRequestMapperTest extends BuildingBlockTestDataSetup{

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
