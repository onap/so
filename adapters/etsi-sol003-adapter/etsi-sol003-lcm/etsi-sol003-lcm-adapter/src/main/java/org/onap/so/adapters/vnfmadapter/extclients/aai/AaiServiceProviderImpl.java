/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.extclients.aai;

import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.vnfmadapter.v1.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AaiServiceProviderImpl implements AaiServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(AaiServiceProviderImpl.class);
    private final AaiClientProvider aaiClientProvider;

    @Autowired
    public AaiServiceProviderImpl(final AaiClientProvider aaiClientProvider) {
        this.aaiClientProvider = aaiClientProvider;
    }

    @Override
    public GenericVnf invokeGetGenericVnf(final String vnfId) {
        return aaiClientProvider.getAaiClient()
                .get(GenericVnf.class, AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId))
                .orElseGet(() -> {
                    logger.debug("No vnf found in AAI with ID: {}", vnfId);
                    return null;
                });
    }

    @Override
    public GenericVnfs invokeQueryGenericVnf(final String selfLink) {
        return aaiClientProvider.getAaiClient()
                .get(GenericVnfs.class,
                        AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNFS).queryParam("selflink", selfLink))
                .orElseGet(() -> {
                    logger.debug("No vnf found in AAI with selflink: {}", selfLink);
                    return null;
                });
    }

    @Override
    public EsrVnfmList invokeGetVnfms() {
        return aaiClientProvider.getAaiClient()
                .get(EsrVnfmList.class, AAIUriFactory.createResourceUri(AAIObjectType.VNFM_LIST)).orElseGet(() -> {
                    logger.debug("No VNFMs in AAI");
                    return null;
                });
    }

    @Override
    public EsrVnfm invokeGetVnfm(final String vnfmId) {
        return aaiClientProvider.getAaiClient()
                .get(EsrVnfm.class, AAIUriFactory.createResourceUri(AAIObjectType.VNFM, vnfmId).depth(Depth.ONE))
                .orElseGet(() -> {
                    logger.debug("VNFM not found in AAI");
                    return null;
                });
    }

    @Override
    public EsrSystemInfoList invokeGetVnfmEsrSystemInfoList(final String vnfmId) {
        return aaiClientProvider.getAaiClient()
                .get(EsrSystemInfoList.class,
                        AAIUriFactory.createResourceUri(AAIObjectType.VNFM_ESR_SYSTEM_INFO_LIST, vnfmId))
                .orElseGet(() -> {
                    logger.debug("VNFM ESR system info list not found in AAI");
                    return null;
                });
    }

    @Override
    public void invokePatchGenericVnf(final GenericVnf vnf) {
        aaiClientProvider.getAaiClient()
                .update(AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId()), vnf);
    }

    @Override
    public void invokePutGenericVnfToVnfmRelationship(final GenericVnf vnf, final String vnfmId) {
        aaiClientProvider.getAaiClient().connect(AAIUriFactory.createResourceUri(AAIObjectType.VNFM, vnfmId),
                AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId()));
    }

    @Override
    public void invokePutVserver(final String cloudOwner, final String cloudRegion, final String tenant,
            final Vserver vserver) {
        aaiClientProvider.getAaiClient().create(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner,
                cloudRegion, tenant, vserver.getVserverId()), vserver);
    }

    @Override
    public void invokePutVserverToVnfRelationship(final String cloudOwner, final String cloudRegion,
            final String tenant, final Vserver vserver, final String vnfId) {
        aaiClientProvider.getAaiClient()
                .connect(
                        AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion, tenant,
                                vserver.getVserverId()),
                        AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId));
    }

    @Override
    public void invokeDeleteVserver(final String cloudOwner, final String cloudRegion, final String tenant,
            final String vserverId) {
        aaiClientProvider.getAaiClient().delete(
                AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion, tenant, vserverId));
    }

    @Override
    public Tenant invokeGetTenant(final String cloudOwner, final String cloudRegion, final String tenantId) {
        return aaiClientProvider.getAaiClient()
                .get(Tenant.class,
                        AAIUriFactory.createResourceUri(AAIObjectType.TENANT, cloudOwner, cloudRegion, tenantId))
                .orElseGet(() -> {
                    logger.debug("Tenant not found in AAI");
                    return null;
                });
    }

    @Override
    public EsrSystemInfoList invokeGetCloudRegionEsrSystemInfoList(final String cloudOwner, final String cloudRegion) {
        return aaiClientProvider
                .getAaiClient().get(EsrSystemInfoList.class, AAIUriFactory
                        .createResourceUri(AAIObjectType.CLOUD_ESR_SYSTEM_INFO_LIST, cloudOwner, cloudRegion))
                .orElseGet(() -> {
                    logger.debug("Cloud esr system info list not found in AAI");
                    return null;
                });
    }

}
