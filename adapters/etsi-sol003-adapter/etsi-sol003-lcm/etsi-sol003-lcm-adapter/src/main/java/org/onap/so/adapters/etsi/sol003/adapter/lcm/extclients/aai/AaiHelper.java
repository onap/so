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

package org.onap.so.adapters.etsi.sol003.adapter.lcm.extclients.aai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.Vserver;
import org.onap.etsi.sol003.adapter.lcm.v1.model.Tenant;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.rest.exceptions.TenantNotFoundException;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.rest.exceptions.VnfmNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides helper methods for interactions with AAI.
 */
@Service
public class AaiHelper {

    private static final Logger logger = LoggerFactory.getLogger(AaiHelper.class);
    private final AaiServiceProvider aaiServiceProvider;
    private final Map<String, OamIpAddressSource> mapOfVnfIdToOamIpAddressHolder = new HashMap<>();

    @Autowired
    public AaiHelper(final AaiServiceProvider aaiServiceProvider) {
        this.aaiServiceProvider = aaiServiceProvider;
    }

    /**
     * Get the VNFM assigned for use for the given generic VNF.
     *
     * @param vnf the generic VNF
     * @return the VNFM to use, or <code>null</code> if no VNFM has been assigned yet
     */
    public EsrVnfm getAssignedVnfm(final GenericVnf vnf) {
        final String vnfmId = getIdOfAssignedVnfm(vnf);
        return vnfmId == null ? null : aaiServiceProvider.invokeGetVnfm(vnfmId);
    }

    /**
     * Get the ID of the VNFM assigned for use for the given generic VNF.
     *
     * @param vnf the generic VNF
     * @return the ID of the VNFM to use, or <code>null</code> if no VNFM has been assigned yet
     */
    public String getIdOfAssignedVnfm(final GenericVnf vnf) {
        final Relationship relationship = getRelationship(vnf, "esr-vnfm");
        return getRelationshipData(relationship, "esr-vnfm.vnfm-id");
    }

    /**
     * Get the tenant assigned for use for the given generic VNF.
     *
     * @param vnf the generic VNF
     * @return the tenant to use, or <code>null</code> if no tenant has been assigned yet
     */
    public Tenant getAssignedTenant(final GenericVnf vnf) {
        final Relationship relationship = getRelationship(vnf, "tenant");
        final String cloudOwner = getRelationshipData(relationship, "cloud-region.cloud-owner");
        final String cloudRegion = getRelationshipData(relationship, "cloud-region.cloud-region-id");
        final String tenantId = getRelationshipData(relationship, "tenant.tenant-id");
        if (cloudOwner == null || cloudRegion == null || tenantId == null) {
            throw new TenantNotFoundException("No matching Tenant found in AAI. VNFID: " + vnf.getVnfId());
        } else {
            return new Tenant().cloudOwner(cloudOwner).regionName(cloudRegion).tenantId(tenantId);
        }
    }

    private Relationship getRelationship(final GenericVnf vnf, final String relationshipRelatedToValue) {
        for (final Relationship relationship : vnf.getRelationshipList() == null ? Collections.<Relationship>emptyList()
                : vnf.getRelationshipList().getRelationship()) {
            if (relationship.getRelatedTo().equals(relationshipRelatedToValue)) {
                return relationship;
            }
        }
        return null;
    }

    /**
     * Get the value of the relationship data with the given key in the given relationship.
     *
     * @param relationship the relationship
     * @param relationshipDataKey the key for the relationship data
     * @return the value of the relationship data for the given key
     */
    public String getRelationshipData(final Relationship relationship, final String relationshipDataKey) {
        if (relationship != null) {
            for (final RelationshipData relationshipData : relationship.getRelationshipData()) {
                if (relationshipData.getRelationshipKey().equals(relationshipDataKey)) {
                    return relationshipData.getRelationshipValue();
                }
            }
        }
        return null;
    }

    /**
     * Delete from the given VNF the relationship matching the given criteria.
     *
     * @param vnf the VNF
     * @param relationshipRelatedToValue the related-to value for the relationship
     * @param dataKey the relationship data key to match on
     * @param dataValue the value the relationship data with the given key must match
     * @return the deleted relationship or <code>null</code> if none found matching the given criteria
     */
    public Relationship deleteRelationshipWithDataValue(final GenericVnf vnf, final String relationshipRelatedToValue,
            final String dataKey, final String dataValue) {
        final Iterator<Relationship> relationships =
                vnf.getRelationshipList() == null ? Collections.<Relationship>emptyList().iterator()
                        : vnf.getRelationshipList().getRelationship().iterator();

        while (relationships.hasNext()) {
            final Relationship relationship = relationships.next();
            if (relationship.getRelatedTo().equals(relationshipRelatedToValue)
                    && dataValue.equals(getRelationshipData(relationship, dataKey))) {
                relationships.remove();
                return relationship;
            }
        }
        return null;
    }

    /**
     * Select a VNFM to use for the given generic VNF. Should only be used when no VNFM has already been assigned to the
     * VNF.
     *
     * @param vnf the generic VNF
     * @return the VNFM to use
     */
    public EsrVnfm selectVnfm(final GenericVnf vnf) {
        final EsrVnfmList vnfmsInEsr = aaiServiceProvider.invokeGetVnfms();

        if (vnfmsInEsr == null) {
            throw new VnfmNotFoundException("No VNFMs found in AAI ESR");
        }
        logger.debug("VNFMs in ESR: " + vnfmsInEsr);

        for (final EsrVnfm vnfm : vnfmsInEsr.getEsrVnfm()) {
            final EsrSystemInfoList systemInfolist =
                    aaiServiceProvider.invokeGetVnfmEsrSystemInfoList(vnfm.getVnfmId());
            vnfm.setEsrSystemInfoList(systemInfolist);
            if (vnfmHasMatchingEsrSystemInfoType(vnfm, vnf.getNfType())) {
                return vnfm;
            }
        }
        throw new VnfmNotFoundException("No matching VNFM found in AAI ESR");
    }

    private boolean vnfmHasMatchingEsrSystemInfoType(final EsrVnfm vnfm, final String type) {
        logger.debug("Checking VNFM ID: " + vnfm + ": " + vnfm.getVnfmId());

        final EsrSystemInfoList systemInfolist = vnfm.getEsrSystemInfoList();
        if (systemInfolist != null) {
            for (final EsrSystemInfo esrSystemInfo : systemInfolist.getEsrSystemInfo()) {
                if (esrSystemInfo.getType().equals(type)) {
                    logger.debug("Matched VNFM ID: " + vnfm + ", based on type");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a vserver.
     *
     * @param vnfc the VNFC to base the vserver on
     * @return the vserver
     */
    public Vserver createVserver(final LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs vnfc) {
        final Vserver vserver = new Vserver();
        vserver.setVserverId(vnfc.getComputeResource().getResourceId());
        vserver.setVserverName(vnfc.getId());
        vserver.setProvStatus("active");
        vserver.setVserverSelflink("Not available");
        return vserver;
    }

    public void setOamIpAddressSource(final String vnfId, final OamIpAddressSource oamIpAddressSource) {
        mapOfVnfIdToOamIpAddressHolder.put(vnfId, oamIpAddressSource);
    }

    public OamIpAddressSource getOamIpAddressSource(final String vnfId) {
        return mapOfVnfIdToOamIpAddressHolder.get(vnfId);
    }

}
