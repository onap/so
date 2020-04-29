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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowActionExtractResourcesAAI {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowActionExtractResourcesAAI.class);

    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;

    public Optional<Configuration> extractRelationshipsConfiguration(Relationships relationships) {
        List<AAIResultWrapper> configurations = relationships.getByType(AAIObjectType.CONFIGURATION);
        for (AAIResultWrapper configWrapper : configurations) {
            Optional<Configuration> config = configWrapper.asBean(Configuration.class);
            if (config.isPresent()) {
                return config;
            }
        }
        return Optional.empty();
    }

    public Optional<VpnBinding> extractRelationshipsVpnBinding(Relationships relationships) {
        List<AAIResourceUri> configurations = relationships.getRelatedUris(AAIObjectType.VPN_BINDING);
        for (AAIResourceUri vpnBindingUri : configurations) {
            AAIResultWrapper vpnBindingWrapper = bbInputSetupUtils.getAAIResourceDepthOne(vpnBindingUri);
            Optional<VpnBinding> vpnBinding = vpnBindingWrapper.asBean(VpnBinding.class);
            if (vpnBinding.isPresent()) {
                return vpnBinding;
            }
        }
        return Optional.empty();
    }

    public Optional<Relationships> extractRelationshipsVnfc(Relationships relationships) {
        List<AAIResultWrapper> vnfcs = relationships.getByType(AAIObjectType.VNFC);
        for (AAIResultWrapper vnfcWrapper : vnfcs) {
            if (vnfcWrapper.getRelationships().isPresent()) {
                return vnfcWrapper.getRelationships();
            }
        }
        return Optional.empty();
    }
}
