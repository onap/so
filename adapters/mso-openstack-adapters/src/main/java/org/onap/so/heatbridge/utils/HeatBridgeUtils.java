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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.heatbridge.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.collections.CollectionUtils;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.SriovVf;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;

public final class HeatBridgeUtils {

    /**
     * IaaS naming convention for compute/p-interface to openstack/physical-network name mapping
     */
    private static final String OS_SIDE_SHARED_SRIOV_PREFIX = "shared-";
    private static final String OS_SIDE_DEDICATED_SRIOV_PREFIX = "dedicated-";
    private static final String COMPUTE_SIDE_SHARED_SRIOV_PREFIX = "sriov-s-";
    private static final String COMPUTE_SIDE_DEDICATED_SRIOV_PREFIX = "sriov-d-";
    private static final String PSERVER = "pserver";
    private static final String VSERVER = "vserver";
    private static final String CLOUD_OWNER = "cloud-region.cloud-owner";
    private static final String CLOUD_REGION_ID = "cloud-region.cloud-region-id";
    private static final String TENANT_ID = "tenant.tenant-id";
    private static final String VSERVER_ID = "vserver.vserver-id";
    private static final String PSERVER_HOSTNAME = "pserver.hostname";

    private HeatBridgeUtils() {
        throw new IllegalStateException("Trying to instantiate a utility class.");
    }

    public static Optional<String> getMatchingPserverPifName(@Nonnull final String physicalNetworkName) {
        Preconditions.checkState(!Strings.isNullOrEmpty(physicalNetworkName),
                "Physical network name is null or " + "empty!");
        if (physicalNetworkName.contains(OS_SIDE_DEDICATED_SRIOV_PREFIX)) {
            return Optional.of(
                    physicalNetworkName.replace(OS_SIDE_DEDICATED_SRIOV_PREFIX, COMPUTE_SIDE_DEDICATED_SRIOV_PREFIX));
        } else if (physicalNetworkName.contains(OS_SIDE_SHARED_SRIOV_PREFIX)) {
            return Optional
                    .of(physicalNetworkName.replace(OS_SIDE_SHARED_SRIOV_PREFIX, COMPUTE_SIDE_SHARED_SRIOV_PREFIX));
        }
        return Optional.empty();
    }

    public static List<String> extractPciIdsFromVServer(Vserver vserver) {
        return vserver.getLInterfaces().getLInterface().stream()
                .filter(lInterface -> lInterface.getSriovVfs() != null
                        && CollectionUtils.isNotEmpty(lInterface.getSriovVfs().getSriovVf()))
                .flatMap(lInterface -> lInterface.getSriovVfs().getSriovVf().stream()).map(SriovVf::getPciId)
                .collect(Collectors.toList());
    }

    public static List<String> extractRelationshipDataValue(final RelationshipList relationshipList) {
        if (relationshipList != null && relationshipList.getRelationship() != null) {
            return relationshipList.getRelationship().stream()
                    .filter(relationship -> relationship.getRelatedTo().equals(PSERVER))
                    .map(Relationship::getRelationshipData).flatMap(Collection::stream)
                    .filter(data -> data.getRelationshipKey() != null
                            && PSERVER_HOSTNAME.equals(data.getRelationshipKey()))
                    .map(RelationshipData::getRelationshipValue).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static List<AAIResourceUri> getVserverUris(VfModule vfModule) {
        List<AAIResourceUri> vserverUris = new ArrayList<>();

        if (vfModule.getRelationshipList() == null || vfModule.getRelationshipList().getRelationship() == null) {
            return vserverUris;
        }

        List<Relationship> relationshipList = vfModule.getRelationshipList().getRelationship().stream()
                .filter(relationship -> relationship.getRelatedTo().equals(VSERVER)).collect(Collectors.toList());

        for (Relationship relationship : relationshipList) {
            Optional<String> cloudOwner = getRelationshipDataByKey(relationship, CLOUD_OWNER);
            Optional<String> cloudRegionId = getRelationshipDataByKey(relationship, CLOUD_REGION_ID);
            Optional<String> tenantId = getRelationshipDataByKey(relationship, TENANT_ID);
            Optional<String> vserverId = getRelationshipDataByKey(relationship, VSERVER_ID);
            if (cloudOwner.isPresent() && cloudRegionId.isPresent() && tenantId.isPresent() && vserverId.isPresent()) {
                vserverUris.add(AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner.get(),
                        cloudRegionId.get(), tenantId.get(), vserverId.get()));
            }
        }
        return vserverUris;
    }

    private static Optional<String> getRelationshipDataByKey(Relationship relationship, String key) {
        if (relationship == null || relationship.getRelationshipData() == null
                || relationship.getRelationshipData().isEmpty()) {
            return Optional.empty();
        }
        return relationship.getRelationshipData().stream()
                .filter(relationshipData -> relationshipData.getRelationshipKey().equals(key))
                .map(RelationshipData::getRelationshipValue).findFirst();
    }
}
