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

import java.util.Optional;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIInstanceGroupResources {
    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public void createInstanceGroup(InstanceGroup instanceGroup) {
        AAIResourceUri instanceGroupUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()));
        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = aaiObjectMapper.mapInstanceGroup(instanceGroup);
        injectionHelper.getAaiClient().createIfNotExists(instanceGroupUri, Optional.of(aaiInstanceGroup));
    }

    public void deleteInstanceGroup(InstanceGroup instanceGroup) {
        AAIResourceUri instanceGroupUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()));
        injectionHelper.getAaiClient().delete(instanceGroupUri);
    }

    public void connectInstanceGroupToVnf(InstanceGroup instanceGroup, GenericVnf vnf) {
        AAIResourceUri instanceGroupUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()));
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().connect(instanceGroupUri, vnfURI);
    }

    public void connectInstanceGroupToVnf(InstanceGroup instanceGroup, GenericVnf vnf, AAIEdgeLabel aaiLabel) {
        AAIResourceUri instanceGroupUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()));
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().connect(instanceGroupUri, vnfURI, aaiLabel);
    }

    public boolean exists(InstanceGroup instanceGroup) {
        AAIResourceUri instanceGroupUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()));
        return injectionHelper.getAaiClient().exists(instanceGroupUri);
    }

    public void createInstanceGroupandConnectServiceInstance(InstanceGroup instanceGroup,
            ServiceInstance serviceInstance) {
        AAIResourceUri instanceGroupUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroup.getId()));
        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = aaiObjectMapper.mapInstanceGroup(instanceGroup);
        AAIResourceUri serviceInstanceURI = AAIClientUriFactory
                .createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance.getServiceInstanceId()));
        injectionHelper.getAaiClient().createIfNotExists(instanceGroupUri, Optional.of(aaiInstanceGroup))
                .connect(instanceGroupUri, serviceInstanceURI);
    }

    public boolean checkInstanceGroupNameInUse(String instanceGroupName) {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroups())
                .queryParam("instance-group-name", instanceGroupName);
        return injectionHelper.getAaiClient().exists(uri);
    }

}
