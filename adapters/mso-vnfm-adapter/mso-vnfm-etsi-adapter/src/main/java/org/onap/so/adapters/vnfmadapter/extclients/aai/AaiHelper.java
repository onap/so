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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.lcn.model.LcnVnfLcmOperationOccurrenceNotificationAffectedVnfcs;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.TenantNotFoundException;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfmNotFoundException;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIVersion;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.vnfmadapter.v1.model.Tenant;
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
     * Add a relationship to the given generic VNF to the given VNFM.
     *
     * @param vnf the generic VNF
     * @param vnfmId the ID of the VNFM
     */
    public void addRelationshipFromGenericVnfToVnfm(final GenericVnf vnf, final String vnfmId) {
        if (vnf.getRelationshipList() == null) {
            vnf.setRelationshipList(new RelationshipList());
        }
        final RelationshipList vnfmRelationshiplist = vnf.getRelationshipList();
        vnfmRelationshiplist.getRelationship().add(createRelationshipToVnfm(vnfmId));

        aaiServiceProvider.invokePutGenericVnf(vnf);
    }

    private Relationship createRelationshipToVnfm(final String vnfmId) {
        final Relationship relationship = new Relationship();
        relationship.setRelatedTo("esr-vnfm");
        relationship.setRelationshipLabel("tosca.relationships.DependsOn");
        relationship.setRelatedLink("/aai/" + AAIVersion.LATEST
                + AAIUriFactory.createResourceUri(AAIObjectType.VNFM, vnfmId).build().toString());
        relationship.getRelationshipData().add(createRelationshipData("esr-vnfm.vnfm-id", vnfmId));
        return relationship;
    }

    private RelationshipData createRelationshipData(final String key, final String value) {
        final RelationshipData data = new RelationshipData();
        data.setRelationshipKey(key);
        data.setRelationshipValue(value);
        return data;
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
        return getRelationshipKey(relationship, "esr-vnfm.vnfm-id");
    }

    /**
     * Get the tenant assigned for use for the given generic VNF.
     *
     * @param vnf the generic VNF
     * @return the tenant to use, or <code>null</code> if no tenant has been assigned yet
     */
    public Tenant getAssignedTenant(final GenericVnf vnf) {
        final Relationship relationship = getRelationship(vnf, "tenant");
        final String cloudOwner = getRelationshipKey(relationship, "cloud-region.cloud-owner");
        final String cloudRegion = getRelationshipKey(relationship, "cloud-region.cloud-region-id");
        final String tenantId = getRelationshipKey(relationship, "tenant.tenant-id");
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

    private String getRelationshipKey(final Relationship relationship, final String relationshipKey) {
        if (relationship != null) {
            for (final RelationshipData relationshipData : relationship.getRelationshipData()) {
                if (relationshipData.getRelationshipKey().equals(relationshipKey)) {
                    return relationshipData.getRelationshipValue();
                }
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
            if (vnfmHasMatchingEsrSystemInfoType(vnfm, vnf.getNfType())) {
                return vnfm;
            }
        }
        throw new VnfmNotFoundException("No matching VNFM found in AAI ESR");
    }

    private boolean vnfmHasMatchingEsrSystemInfoType(final EsrVnfm vnfm, final String type) {
        logger.debug("Checking VNFM ID: " + vnfm + ": " + vnfm.getVnfmId());

        final EsrSystemInfoList systemInfolist = aaiServiceProvider.invokeGetVnfmEsrSystemInfoList(vnfm.getVnfmId());
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

    /**
     * Add a relationship to the given vserver to the given VNF.
     *
     * @param vnf the vserver
     * @param vnfmId the ID of the VNF
     */
    public void addRelationshipFromVserverVnfToGenericVnf(final Vserver vserver, final String vnfId) {
        if (vserver.getRelationshipList() == null) {
            vserver.setRelationshipList(new RelationshipList());
        }
        final RelationshipList vserverRelationshiplist = vserver.getRelationshipList();
        vserverRelationshiplist.getRelationship().add(createRelationshipToGenericVnf(vnfId));
    }

    private Relationship createRelationshipToGenericVnf(final String vnfId) {
        final Relationship relationship = new Relationship();
        relationship.setRelatedTo("generic-vnf");
        relationship.setRelationshipLabel("tosca.relationships.HostedOn");
        relationship.setRelatedLink("/aai/" + AAIVersion.LATEST
                + AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).build().toString());
        relationship.getRelationshipData().add(createRelationshipData("generic-vnf.vnf-id", vnfId));
        return relationship;
    }

    public void setOamIpAddressSource(final String vnfId, final OamIpAddressSource oamIpAddressSource) {
        mapOfVnfIdToOamIpAddressHolder.put(vnfId, oamIpAddressSource);
    }

    public OamIpAddressSource getOamIpAddressSource(final String vnfId) {
        return mapOfVnfIdToOamIpAddressHolder.get(vnfId);
    }
}
