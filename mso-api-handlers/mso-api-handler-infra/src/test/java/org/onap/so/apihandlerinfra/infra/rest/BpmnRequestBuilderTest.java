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

package org.onap.so.apihandlerinfra.infra.rest;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import java.io.File;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class BpmnRequestBuilderTest {

    private static final String RESOURCE_PATH = "src/test/resources/__files/infra/";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();


    @Mock
    private AAIResourcesClient aaiResourcesClient;

    @InjectMocks
    private AAIDataRetrieval aaiData = spy(AAIDataRetrieval.class);

    @Mock
    private RequestsDbClient requestDBClient;

    @InjectMocks
    private BpmnRequestBuilder reqBuilder = spy(BpmnRequestBuilder.class);


    private ObjectMapper mapper = new ObjectMapper();

    private GraphInventoryCommonObjectMapperProvider provider = new GraphInventoryCommonObjectMapperProvider();

    @Before
    public void setup() {
        // aaiData.setAaiResourcesClient(aaiResourcesClient);
    }

    @Test
    public void test_buildServiceInstanceDeleteRequest() throws Exception {
        ServiceInstance service =
                provider.getMapper().readValue(new File(RESOURCE_PATH + "ServiceInstance.json"), ServiceInstance.class);

        doReturn(service).when(aaiData).getServiceInstance("serviceId");
        ServiceInstancesRequest expectedRequest = mapper
                .readValue(new File(RESOURCE_PATH + "ExpectedServiceRequest.json"), ServiceInstancesRequest.class);
        expectedRequest.getRequestDetails().getModelInfo().setModelId(null); // bad getter/setter setting multiple
                                                                             // fields
        ServiceInstancesRequest actualRequest = reqBuilder.buildServiceDeleteRequest("serviceId");
        assertThat(actualRequest, sameBeanAs(expectedRequest));
    }

    @Test
    public void test_buildVnfDeleteRequest() throws Exception {
        GenericVnf vnf = provider.getMapper().readValue(new File(RESOURCE_PATH + "Vnf.json"), GenericVnf.class);

        doReturn(Optional.of(vnf)).when(aaiResourcesClient)
                .get(GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "vnfId"));

        ServiceInstancesRequest expectedRequest =
                mapper.readValue(new File(RESOURCE_PATH + "ExpectedVnfRequest.json"), ServiceInstancesRequest.class);
        ServiceInstancesRequest actualRequest = reqBuilder.buildVnfDeleteRequest("vnfId");
        assertThat(actualRequest, sameBeanAs(expectedRequest));
    }

    @Test
    public void test_buildVFModuleDeleteRequest() throws Exception {
        GenericVnf vnf = provider.getMapper().readValue(new File(RESOURCE_PATH + "Vnf.json"), GenericVnf.class);

        doReturn(Optional.of(vnf)).when(aaiResourcesClient)
                .get(GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "vnfId"));
        VfModule vfModule = provider.getMapper().readValue(new File(RESOURCE_PATH + "VfModule.json"), VfModule.class);

        doReturn(Optional.of(vfModule)).when(aaiResourcesClient)
                .get(VfModule.class, AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, "vnfId", "vfModuleId"));

        ServiceInstancesRequest expectedRequest = mapper
                .readValue(new File(RESOURCE_PATH + "ExpectedVfModuleRequest.json"), ServiceInstancesRequest.class);
        ServiceInstancesRequest actualRequest =
                reqBuilder.buildVFModuleDeleteRequest("vnfId", "vfModuleId", ModelType.vfModule);
        assertThat(actualRequest, sameBeanAs(expectedRequest));
    }

    @Test
    public void test_buildVolumeGroupDeleteRequest() throws Exception {
        GenericVnf vnf = provider.getMapper().readValue(new File(RESOURCE_PATH + "Vnf.json"), GenericVnf.class);

        doReturn(Optional.of(vnf)).when(aaiResourcesClient)
                .get(GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "vnfId"));
        VolumeGroup volumeGroup =
                provider.getMapper().readValue(new File(RESOURCE_PATH + "VolumeGroup.json"), VolumeGroup.class);
        AAIResultWrapper wrapper = new AAIResultWrapper(volumeGroup);
        doReturn(wrapper).when(aaiResourcesClient)
                .get(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "vnfId")
                        .relatedTo(AAIObjectType.VOLUME_GROUP, "volumeGroupId"));

        ServiceInstancesRequest expectedRequest = mapper
                .readValue(new File(RESOURCE_PATH + "ExpectedVolumeGroupRequest.json"), ServiceInstancesRequest.class);
        ServiceInstancesRequest actualRequest = reqBuilder.buildVolumeGroupDeleteRequest("vnfId", "volumeGroupId");
        assertThat(actualRequest, sameBeanAs(expectedRequest));
    }

}

