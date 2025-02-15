/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.aai;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.RelatedToProperty;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.Vserver;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AaiConnection {

    private static final Logger logger = LoggerFactory.getLogger(AaiConnection.class);
    private static final String VSERVER = "vserver";
    private static final String CLOUD_OWNER = "cloud-region.cloud-owner";
    private static final String CLOUD_REGION_ID = "cloud-region.cloud-region-id";
    private static final String TENANT_ID = "tenant.tenant-id";
    private static final String VSERVER_ID = "vserver.vserver-id";
    private static final String VSERVER_NAME = "vserver.vserver-name";
    private static final String SELFLINK = "selflink";
    private static final int FIRST_INDEX = 0;

    private AAIResourcesClient resourcesClient = null;

    private static void isValid(final List<EsrSystemInfo> infos) throws VeVnfmException {
        if (infos == null || infos.isEmpty() || Strings.isBlank(infos.get(FIRST_INDEX).getServiceUrl())) {
            throw new VeVnfmException("No 'url' field in VNFM info");
        }
    }

    static String getRelationshipData(final Relationship relationship, final String relationshipDataKey) {
        if (relationship != null && relationship.getRelationshipData() != null) {
            for (final RelationshipData relationshipData : relationship.getRelationshipData()) {
                if (relationshipDataKey.equals(relationshipData.getRelationshipKey())) {
                    return relationshipData.getRelationshipValue();
                }
            }
        }

        return null;
    }

    static String getRelatedToProperty(final Relationship relationship, final String propertyKey) {
        if (relationship != null && relationship.getRelatedToProperty() != null) {
            for (final RelatedToProperty relatedToProperty : relationship.getRelatedToProperty()) {
                if (propertyKey.equals(relatedToProperty.getPropertyKey())) {
                    return relatedToProperty.getPropertyValue();
                }
            }
        }

        return null;
    }

    private AAIResourcesClient getResourcesClient() {
        if (resourcesClient == null) {
            resourcesClient = new AAIResourcesClient();
        }

        return resourcesClient;
    }

    public List<EsrSystemInfo> receiveVnfm() throws VeVnfmException {
        List<EsrSystemInfo> infos;

        try {
            infos = receiveVnfmInternal();
        } catch (Exception e) {
            throw new VeVnfmException(e);
        }

        isValid(infos);

        return infos;
    }

    private List<EsrSystemInfo> receiveVnfmInternal() {
        final AAIPluralResourceUri resourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.externalSystem().esrVnfmList());
        final Optional<EsrVnfmList> response = getResourcesClient().get(EsrVnfmList.class, resourceUri);

        if (response.isPresent()) {
            final EsrVnfmList esrVnfmList = response.get();
            logger.info("The AAI ESR replied with: {}", esrVnfmList);
            final List<EsrVnfm> esrVnfm = esrVnfmList.getEsrVnfm();

            final List<EsrSystemInfo> infos = new LinkedList<>();

            for (final EsrVnfm vnfm : esrVnfm) {
                final String vnfmId = vnfm.getVnfmId();
                infos.addAll(receiveVnfmServiceUrl(vnfmId));
            }

            return infos;
        }

        return null;
    }

    private List<EsrSystemInfo> receiveVnfmServiceUrl(final String vnfmId) {
        final Optional<EsrVnfm> response = getResourcesClient().get(EsrVnfm.class, AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.externalSystem().esrVnfm(vnfmId)).depth(Depth.ONE));

        if (response.isPresent()) {
            final EsrVnfm esrVnfm = response.get();
            logger.info("The AAI ESR replied with: {}", esrVnfm);
            final EsrSystemInfoList esrSystemInfoList = esrVnfm.getEsrSystemInfoList();

            if (esrSystemInfoList != null) {
                return esrSystemInfoList.getEsrSystemInfo();
            }
        }

        return Collections.emptyList();
    }

    public String receiveGenericVnfId(final String href) {
        final AAIPluralResourceUri resourceUri = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.network().genericVnfs()).queryParam(SELFLINK, href);
        final Optional<GenericVnfs> response = getResourcesClient().get(GenericVnfs.class, resourceUri);

        if (response.isPresent()) {
            final GenericVnfs vnfs = response.get();
            logger.info("The AAI replied with: {}", vnfs);
            final List<GenericVnf> genericVnfList = vnfs.getGenericVnf();
            final int size = genericVnfList.size();

            if (size == 1) {
                final GenericVnf genericVnf = genericVnfList.get(FIRST_INDEX);
                return genericVnf.getVnfId();
            } else if (size > 1) {
                logger.warn("more generic vnfs available");
            }
        }

        return null;
    }

    public String receiveVserverName(final String genericId) {
        final AAIResourceUri resourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(genericId));
        final Optional<GenericVnf> response = getResourcesClient().get(GenericVnf.class, resourceUri);

        if (response.isPresent()) {
            final GenericVnf genericVnf = response.get();
            final RelationshipList relationshipList = genericVnf.getRelationshipList();

            if (relationshipList == null || relationshipList.getRelationship() == null) {
                return null;
            }

            for (final Relationship relationship : relationshipList.getRelationship()) {
                if (VSERVER.equals(relationship.getRelatedTo())) {
                    final String vserverName = getRelatedToProperty(relationship, VSERVER_NAME);

                    if (vserverName == null) {
                        final String cloudOwner = getRelationshipData(relationship, CLOUD_OWNER);
                        final String cloudId = getRelationshipData(relationship, CLOUD_REGION_ID);
                        final String tenantId = getRelationshipData(relationship, TENANT_ID);
                        final String vserverId = getRelationshipData(relationship, VSERVER_ID);
                        return receiveVserverNameFromParams(cloudOwner, cloudId, tenantId, vserverId);
                    }

                    return vserverName;
                }
            }
        }

        return null;
    }

    private String receiveVserverNameFromParams(final String cloudOwner, final String cloudId, final String tenantId,
            final String vserverId) {
        final AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudOwner, cloudId).tenant(tenantId).vserver(vserverId));
        final Optional<Vserver> response = getResourcesClient().get(Vserver.class, resourceUri);

        if (response.isPresent()) {
            final Vserver vserver = response.get();
            return vserver.getVserverName();
        }

        return null;
    }
}
