/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai;

import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@Service
public class AaiServiceProviderImpl implements AaiServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(AaiServiceProviderImpl.class);
    private final AaiClientProvider aaiClientProvider;

    @Autowired
    public AaiServiceProviderImpl(final AaiClientProvider aaiClientProvider) {
        this.aaiClientProvider = aaiClientProvider;
    }

    @Override
    public void createServiceInstance(final String globalCustomerId, final String serviceType,
            final ServiceInstance aaiServiceInstance) {
        logger.info("Creating service instance in AAI: {}", aaiServiceInstance);
        final AAIResourceUri serviceInstanceURI = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                globalCustomerId, serviceType, aaiServiceInstance.getServiceInstanceId());
        aaiClientProvider.getAaiClient().createIfNotExists(serviceInstanceURI, Optional.of(aaiServiceInstance));

    }

    @Override
    public void createGenericVnfAndConnectServiceInstance(final String serviceInstanceId, final String vnfId,
            final GenericVnf genericVnf) {
        logger.info("Creating GenericVnf in AAI: {}", genericVnf);
        final AAIResourceUri genericVnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
        final AAIResourceUri serviceInstanceURI =
                AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
        aaiClientProvider.getAaiClient().createIfNotExists(genericVnfURI, Optional.of(genericVnf))
                .connect(genericVnfURI, serviceInstanceURI);

    }

    @Override
    public void connectGenericVnfToTenant(final String vnfId, final String cloudOwner, final String cloudRegion,
            final String tenantId) {
        logger.info("Connecting GenericVnf {} to {}/{}/{} in AAI", vnfId, cloudOwner, cloudRegion, tenantId);
        final AAIResourceUri tenantURI =
                AAIUriFactory.createResourceUri(AAIObjectType.TENANT, cloudOwner, cloudRegion, tenantId);
        final AAIResourceUri genericVnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
        aaiClientProvider.getAaiClient().connect(tenantURI, genericVnfURI);
    }

    @Override
    public Optional<GenericVnf> getGenericVnf(final String vnfId) {
        return aaiClientProvider.getAaiClient().get(GenericVnf.class,
                AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId));
    }
}
