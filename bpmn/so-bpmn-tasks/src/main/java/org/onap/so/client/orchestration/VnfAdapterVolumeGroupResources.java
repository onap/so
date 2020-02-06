/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.adapter.vnf.VnfVolumeAdapterClientImpl;
import org.onap.so.client.adapter.vnf.mapper.VnfAdapterObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class VnfAdapterVolumeGroupResources {

    @Autowired
    private VnfAdapterObjectMapper vnfAdapterObjectMapper;

    @Autowired
    private VnfVolumeAdapterClientImpl vnfVolumeAdapterClient;

    public CreateVolumeGroupRequest createVolumeGroupRequest(RequestContext requestContext, CloudRegion cloudRegion,
            OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, GenericVnf genericVnf,
            VolumeGroup volumeGroup, String sdncVfModuleQueryResponse) throws IOException {
        return vnfAdapterObjectMapper.createVolumeGroupRequestMapper(requestContext, cloudRegion, orchestrationContext,
                serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
    }

    public DeleteVolumeGroupRequest deleteVolumeGroupRequest(RequestContext requestContext, CloudRegion cloudRegion,
            ServiceInstance serviceInstance, VolumeGroup volumeGroup) throws IOException {
        return vnfAdapterObjectMapper.deleteVolumeGroupRequestMapper(requestContext, cloudRegion, serviceInstance,
                volumeGroup);
    }
}
