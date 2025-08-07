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

package org.onap.so.bpmn.infrastructure.sdnc.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.generalobjects.License;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;

@RunWith(MockitoJUnitRunner.class)
public class VnfTopologyOperationRequestMapperTest {

    @Spy
    private GeneralTopologyObjectMapper generalTopologyObjectMapper;

    @InjectMocks
    private VnfTopologyOperationRequestMapper mapper = new VnfTopologyOperationRequestMapper();

    @Test
    public void reqMapperTest() throws Exception {
        // prepare and set service instance
        ServiceInstance serviceInstance = new ServiceInstance();
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setModelInvariantUuid("modelInvariantUuid");
        modelInfoServiceInstance.setModelName("modelName");
        modelInfoServiceInstance.setModelUuid("modelUuid");
        modelInfoServiceInstance.setModelVersion("modelVersion");
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);

        // prepare VNF
        ModelInfoGenericVnf genericVnf = new ModelInfoGenericVnf();
        genericVnf.setModelInvariantUuid("vnfModelInvariantUUID");
        genericVnf.setModelVersion("vnfModelVersion");
        genericVnf.setModelName("vnfModelName");
        genericVnf.setModelUuid("vnfModelUUID");
        genericVnf.setModelCustomizationUuid("vnfModelCustomizationUUID");

        ModelInfoInstanceGroup modelL3Network = new ModelInfoInstanceGroup();
        modelL3Network.setType("L3-NETWORK");

        InstanceGroup instanceGroup1 = new InstanceGroup();
        instanceGroup1.setId("l3-network-ig-111");
        instanceGroup1.setModelInfoInstanceGroup(modelL3Network);

        InstanceGroup instanceGroup2 = new InstanceGroup();
        instanceGroup2.setId("l3-network-ig-222");
        instanceGroup2.setModelInfoInstanceGroup(modelL3Network);

        GenericVnf vnf = new GenericVnf();
        vnf.setModelInfoGenericVnf(genericVnf);
        vnf.setVnfId("vnfId");
        vnf.setVnfType("vnfType");
        vnf.getInstanceGroups().add(instanceGroup1);
        vnf.getInstanceGroups().add(instanceGroup2);
        License license = new License();
        List<String> entitlementPoolUuids = new ArrayList<>();
        entitlementPoolUuids.add("entitlementPoolUuid");
        List<String> licenseKeyGroupUuids = new ArrayList<>();
        licenseKeyGroupUuids.add("licenseKeyGroupUuid");
        license.setEntitlementPoolUuids(entitlementPoolUuids);
        license.setLicenseKeyGroupUuids(licenseKeyGroupUuids);
        vnf.setLicense(license);

        // prepare Customer object
        Customer customer = new Customer();
        customer.setGlobalCustomerId("globalCustomerId");

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType("productFamilyId");
        customer.setServiceSubscription(serviceSubscription);

        // set Customer on service instance
        customer.getServiceSubscription().getServiceInstances().add(serviceInstance);


        // prepare RequestContext
        RequestContext requestContext = new RequestContext();
        Map<String, Object> userParams = new HashMap<>();
        userParams.put("key1", "value1");
        requestContext.setUserParams(userParams);
        requestContext.setProductFamilyId("productFamilyId");
        requestContext.setMsoRequestId("MsoRequestId");

        CloudRegion cloudRegion = new CloudRegion();

        GenericResourceApiVnfOperationInformation vnfOpInformation =
                mapper.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                        GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer,
                        cloudRegion, requestContext, true, new URI("http://localhost:8080"));
        GenericResourceApiVnfOperationInformation vnfOpInformationNullReqContext =
                mapper.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                        GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer,
                        cloudRegion, null, true, new URI("http://localhost:8080"));

        assertNull(vnfOpInformation.getServiceInformation().getOnapModelInformation().getModelCustomizationUuid());
        assertEquals("vnfModelCustomizationUUID",
                vnfOpInformation.getVnfInformation().getOnapModelInformation().getModelCustomizationUuid());
        assertEquals(2, vnfOpInformation.getVnfRequestInput().getVnfNetworkInstanceGroupIds().size());
        assertEquals("l3-network-ig-111", vnfOpInformation.getVnfRequestInput().getVnfNetworkInstanceGroupIds().get(0)
                .getVnfNetworkInstanceGroupId());
        assertEquals("l3-network-ig-222", vnfOpInformation.getVnfRequestInput().getVnfNetworkInstanceGroupIds().get(1)
                .getVnfNetworkInstanceGroupId());
        assertEquals("entitlementPoolUuid",
                vnfOpInformation.getVnfRequestInput().getVnfInputParameters().getParam().get(1).getValue());
        assertEquals("licenseKeyGroupUuid",
                vnfOpInformation.getVnfRequestInput().getVnfInputParameters().getParam().get(2).getValue());
        assertEquals("MsoRequestId", vnfOpInformation.getRequestInformation().getRequestId());
        assertNotNull(vnfOpInformationNullReqContext.getRequestInformation().getRequestId());
    }
}
