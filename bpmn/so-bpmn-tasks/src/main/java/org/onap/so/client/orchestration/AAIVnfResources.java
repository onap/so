/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import java.io.IOException;
import java.util.Optional;
import org.onap.aai.domain.yang.Vserver;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.AAIValidatorImpl;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIVnfResources {

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    private AAIValidatorImpl aaiValidatorImpl = new AAIValidatorImpl();

    public void createVnfandConnectServiceInstance(GenericVnf vnf, ServiceInstance serviceInstance) {
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        vnf.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        AAIResourceUri serviceInstanceURI = AAIClientUriFactory
                .createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstance.getServiceInstanceId()));
        injectionHelper.getAaiClient().createIfNotExists(vnfURI, Optional.of(aaiObjectMapper.mapVnf(vnf)))
                .connect(vnfURI, serviceInstanceURI);
    }

    public void createPlatformandConnectVnf(Platform platform, GenericVnf vnf) {
        AAIResourceUri platformURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().platform(platform.getPlatformName()));
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().createIfNotExists(platformURI, Optional.of(platform)).connect(vnfURI,
                platformURI);
    }

    public void createLineOfBusinessandConnectVnf(LineOfBusiness lineOfBusiness, GenericVnf vnf) {
        AAIResourceUri lineOfBusinessURI = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.business().lineOfBusiness(lineOfBusiness.getLineOfBusinessName()));
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().createIfNotExists(lineOfBusinessURI, Optional.of(lineOfBusiness)).connect(vnfURI,
                lineOfBusinessURI);
    }

    public void deleteVnf(GenericVnf vnf) {
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().delete(vnfURI);
    }

    public void updateOrchestrationStatusVnf(GenericVnf vnf, OrchestrationStatus orchestrationStatus) {
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));

        GenericVnf copiedVnf = vnf.shallowCopyId();

        vnf.setOrchestrationStatus(orchestrationStatus);
        copiedVnf.setOrchestrationStatus(orchestrationStatus);
        injectionHelper.getAaiClient().update(vnfURI, aaiObjectMapper.mapVnf(copiedVnf));
    }

    public void updateObjectVnf(GenericVnf vnf) {
        AAIResourceUri vnfUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().update(vnfUri, aaiObjectMapper.mapVnf(vnf));
    }

    /**
     * Retrieve Generic VNF from AAI using vnf Id
     *
     * @param vnfId - vnf-id required vnf
     * @return AAI Generic Vnf
     */
    public Optional<org.onap.aai.domain.yang.GenericVnf> getGenericVnf(String vnfId) {
        return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.GenericVnf.class,
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)));
    }

    /**
     * Check inMaint flag value of Generic VNF from AAI using vnf Id
     *
     * @param vnfId - vnf-id required vnf
     * @return inMaint flag value
     */
    public boolean checkInMaintFlag(String vnfId) {
        org.onap.aai.domain.yang.GenericVnf vnf = injectionHelper.getAaiClient()
                .get(org.onap.aai.domain.yang.GenericVnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)))
                .orElse(new org.onap.aai.domain.yang.GenericVnf());
        return vnf.isInMaint();
    }

    public void connectVnfToCloudRegion(GenericVnf vnf, CloudRegion cloudRegion) {
        AAIResourceUri cloudRegionURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId()));
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().connect(vnfURI, cloudRegionURI);
    }

    public void connectVnfToTenant(GenericVnf vnf, CloudRegion cloudRegion) {
        AAIResourceUri tenantURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId())
                .tenant(cloudRegion.getTenantId()));
        AAIResourceUri vnfURI =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()));
        injectionHelper.getAaiClient().connect(tenantURI, vnfURI);
    }

    public boolean checkVnfClosedLoopDisabledFlag(String vnfId) {
        org.onap.aai.domain.yang.GenericVnf vnf = injectionHelper.getAaiClient()
                .get(org.onap.aai.domain.yang.GenericVnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)))
                .orElse(new org.onap.aai.domain.yang.GenericVnf());
        return vnf.isIsClosedLoopDisabled();
    }

    public boolean checkVnfPserversLockedFlag(String vnfId) throws IOException {
        org.onap.aai.domain.yang.GenericVnf vnf = injectionHelper.getAaiClient()
                .get(org.onap.aai.domain.yang.GenericVnf.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)))
                .orElse(new org.onap.aai.domain.yang.GenericVnf());
        AAIRestClientImpl client = new AAIRestClientImpl();
        aaiValidatorImpl.setClient(client);
        return aaiValidatorImpl.isPhysicalServerLocked(vnf.getVnfId());

    }

    public boolean checkNameInUse(String vnfName) {
        AAIPluralResourceUri vnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs())
                .queryParam("vnf-name", vnfName);
        return injectionHelper.getAaiClient().exists(vnfUri);
    }

    public AAIResultWrapper queryVnfWrapperById(GenericVnf vnf) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnf.getVnfId()))
                .depth(Depth.ALL);
        return injectionHelper.getAaiClient().get(uri);
    }

    public Optional<Vserver> getVserver(AAIResourceUri uri) {
        return injectionHelper.getAaiClient().get(uri).asBean(Vserver.class);
    }
}
