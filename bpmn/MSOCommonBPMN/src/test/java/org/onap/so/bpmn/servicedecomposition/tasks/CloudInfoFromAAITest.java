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

package org.onap.so.bpmn.servicedecomposition.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class CloudInfoFromAAITest {

    private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";

    @Spy
    private CloudInfoFromAAI SPY_CloudInfoFromAAI = new CloudInfoFromAAI();

    protected ObjectMapper mapper = new ObjectMapper();

    @Mock
    private BBInputSetupUtils SPY_bbInputSetupUtils;

    @Before
    public void setup() {
        SPY_CloudInfoFromAAI.setBbInputSetupUtils(SPY_bbInputSetupUtils);
    }

    @Test
    public void testGetCloudInfoFromAAI() throws IOException {
        // Test vnfs
        ServiceInstance serviceInstance =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        CloudRegion expected = new CloudRegion();
        GenericVnf vnf = new GenericVnf();
        String vnfId = "vnfId";
        vnf.setVnfId(vnfId);
        serviceInstance.getVnfs().add(vnf);
        org.onap.aai.domain.yang.GenericVnf aaiVnf = new org.onap.aai.domain.yang.GenericVnf();
        aaiVnf.setVnfId(vnfId);
        Relationships relationships = Mockito.mock(Relationships.class);
        Optional<Relationships> relationshipsOp = Optional.of(relationships);
        doReturn(aaiVnf).when(SPY_bbInputSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doReturn(relationshipsOp).when(SPY_CloudInfoFromAAI).getRelationshipsFromWrapper(isA(AAIResultWrapper.class));
        doReturn(Optional.of(expected)).when(SPY_CloudInfoFromAAI).getRelatedCloudRegionAndTenant(relationships);
        Optional<CloudRegion> actual = SPY_CloudInfoFromAAI.getCloudInfoFromAAI(serviceInstance);
        assertThat(actual.get(), sameBeanAs(expected));

        // Test networks
        serviceInstance =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        L3Network l3Network = new L3Network();
        String networkId = "networkId";
        l3Network.setNetworkId(networkId);
        serviceInstance.getNetworks().add(l3Network);
        org.onap.aai.domain.yang.L3Network aaiL3Network = new org.onap.aai.domain.yang.L3Network();
        aaiL3Network.setNetworkId(networkId);
        doReturn(aaiL3Network).when(SPY_bbInputSetupUtils).getAAIL3Network(l3Network.getNetworkId());
        actual = SPY_CloudInfoFromAAI.getCloudInfoFromAAI(serviceInstance);
        assertThat(actual.get(), sameBeanAs(expected));

        // Test no relationships

        doReturn(Optional.empty()).when(SPY_CloudInfoFromAAI).getRelationshipsFromWrapper(isA(AAIResultWrapper.class));
        actual = SPY_CloudInfoFromAAI.getCloudInfoFromAAI(serviceInstance);
        assertEquals(actual, Optional.empty());

        // Test null
        serviceInstance =
                mapper.readValue(new File(RESOURCE_PATH + "ServiceInstance_getServiceInstanceNOAAIExpected.json"),
                        ServiceInstance.class);
        actual = SPY_CloudInfoFromAAI.getCloudInfoFromAAI(serviceInstance);
        assertEquals(actual, Optional.empty());
    }

    @Test
    public void testGetRelatedCloudRegionAndTenant() throws JsonProcessingException {
        String cloudOwner = "cloudOwner";
        String cloudRegionId = "cloudRegionId";
        String cloudRegionVersion = "cloudRegionVersion";
        String cloudRegionComplexName = "cloudRegionComplexName";
        String tenantId = "tenantId";
        CloudRegion expected = new CloudRegion();
        expected.setCloudOwner(cloudOwner);
        expected.setCloudRegionVersion(cloudRegionVersion);
        expected.setComplex(cloudRegionComplexName);
        expected.setLcpCloudRegionId(cloudRegionId);
        expected.setTenantId(tenantId);

        Relationships relationships = Mockito.mock(Relationships.class);
        List<AAIResultWrapper> cloudRegions = new ArrayList<>();
        org.onap.aai.domain.yang.CloudRegion cloudRegion = new org.onap.aai.domain.yang.CloudRegion();
        cloudRegion.setCloudOwner(cloudOwner);
        cloudRegion.setCloudRegionId(cloudRegionId);
        cloudRegion.setCloudRegionVersion(cloudRegionVersion);
        cloudRegion.setComplexName(cloudRegionComplexName);
        AAIResultWrapper cloudRegionWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(cloudRegion));
        cloudRegions.add(cloudRegionWrapper);

        doReturn(cloudRegions).when(relationships).getByType(Types.CLOUD_REGION);
        List<AAIResultWrapper> tenants = new ArrayList<>();
        org.onap.aai.domain.yang.Tenant tenant = new org.onap.aai.domain.yang.Tenant();
        tenant.setTenantId(tenantId);
        AAIResultWrapper tenantWrapper =
                new AAIResultWrapper(new AAICommonObjectMapperProvider().getMapper().writeValueAsString(tenant));
        tenants.add(tenantWrapper);
        doReturn(tenants).when(relationships).getByType(Types.TENANT);

        Optional<CloudRegion> actual = SPY_CloudInfoFromAAI.getRelatedCloudRegionAndTenant(relationships);

        assertThat(actual.get(), sameBeanAs(expected));
    }
}
