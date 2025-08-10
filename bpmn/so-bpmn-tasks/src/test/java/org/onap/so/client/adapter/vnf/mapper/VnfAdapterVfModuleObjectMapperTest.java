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

package org.onap.so.client.adapter.vnf.mapper;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.entity.MsoRequest;

public class VnfAdapterVfModuleObjectMapperTest {

    @Spy
    private VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createVnfcSubInterfaceKeyTest() {

        assertEquals("type_0_subint_role_port_0", mapper.createVnfcSubInterfaceKey("type", 0, "role", 0));
    }

    @Test
    public void createGlobalVnfcSubInterfaceKeyTest() {

        assertEquals("type_subint_role_port_0", mapper.createGlobalVnfcSubInterfaceKey("type", "role", 0));
    }

    @Test
    public void addPairToMapTest() {
        Map<String, Object> map = new HashMap<>();

        mapper.addPairToMap(map, "test", "_key", Arrays.asList("a", "b"));

        assertEquals("a,b", map.get("test_key"));

        mapper.addPairToMap(map, "test", "_key2", Arrays.asList());

        assertThat(map.containsKey("test_key2"), equalTo(false));

        mapper.addPairToMap(map, "test", "_key3", "myVal");

        assertEquals("myVal", map.get("test_key3"));

    }

    @Test
    public void test_deleteVfModuleNoHeatIdRequestMapper() throws Exception {
        DeleteVfModuleRequest expectedDeleteVfModuleRequest = new DeleteVfModuleRequest();

        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setLcpCloudRegionId("lcpCloudRegionId");
        expectedDeleteVfModuleRequest.setCloudSiteId(cloudRegion.getLcpCloudRegionId());

        cloudRegion.setTenantId("tenantId");
        expectedDeleteVfModuleRequest.setTenantId(cloudRegion.getTenantId());

        GenericVnf genericVnf = new GenericVnf();
        VfModule vfModule = new VfModule();
        vfModule.setHeatStackId("heatStackId");
        expectedDeleteVfModuleRequest.setVfModuleStackId("heatStackId");
        expectedDeleteVfModuleRequest.setSkipAAI(true);

        MsoRequest msoRequest = new MsoRequest();
        RequestContext requestContext = new RequestContext();
        requestContext.setMsoRequestId("msoRequestId");
        msoRequest.setRequestId(requestContext.getMsoRequestId());
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("serviceInstanceId");
        msoRequest.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        expectedDeleteVfModuleRequest.setMsoRequest(msoRequest);

        String messageId = "messageId";
        String endpoint = "endpoint";
        doNothing().when(mapper).setIdAndUrl(any());
        expectedDeleteVfModuleRequest.setMessageId(messageId);
        expectedDeleteVfModuleRequest.setNotificationUrl(endpoint + "/VNFAResponse/" + messageId);

        DeleteVfModuleRequest actualDeleteVfModuleRequest =
                mapper.deleteVfModuleRequestMapper(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);

        assertThat(actualDeleteVfModuleRequest,
                sameBeanAs(expectedDeleteVfModuleRequest).ignoring("messageId").ignoring("notificationUrl"));
    }

}
