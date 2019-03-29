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

import com.google.common.base.Optional;
import java.util.Collections;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrSystemInfo;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfm;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfmList;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrvnfmEsrsysteminfolist;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.GenericVnf;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.GenericvnfRelationshiplist;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.Relationship;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.RelationshipData;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfmNotFoundException;
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
    private final AaiUrlProvider urlProvider;

    @Autowired
    public AaiHelper(final AaiServiceProvider aaiServiceProvider, final AaiUrlProvider urlProvider) {
        this.aaiServiceProvider = aaiServiceProvider;
        this.urlProvider = urlProvider;
    }

    /**
     * Add a relationship to the given generic VNF to the given VNFM.
     *
     * @param vnf the generic VNF
     * @param vnfmId the ID of the VNFM
     */
    public void addRelationshipFromGenericVnfToVnfm(final GenericVnf vnf, final String vnfmId) {
        if (vnf.getRelationshipList() == null) {
            vnf.setRelationshipList(new GenericvnfRelationshiplist());
        }
        final GenericvnfRelationshiplist vnfmRelationshiplist = vnf.getRelationshipList();
        vnfmRelationshiplist.addRelationshipItem(createRelationshipToVnfm(vnfmId));

        aaiServiceProvider.invokePutGenericVnf(vnf);
    }

    private Relationship createRelationshipToVnfm(final String vnfmId) {
        final Relationship relationship = new Relationship();
        relationship.setRelatedTo("esr-vnfm");
        relationship.setRelationshipLabel("tosca.relationships.DependsOn");
        relationship.setRelatedLink(urlProvider.convertToRelativeUrl(urlProvider.getVnfmUrl(vnfmId)));
        relationship.addRelationshipDataItem(createRelationshipData("esr-vnfm.vnfm-id", vnfmId));
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
        String vnfmUrl = null;
        for (final Relationship relationship : vnf.getRelationshipList() == null ? Collections.<Relationship>emptyList()
                : vnf.getRelationshipList().getRelationship()) {
            if ("esr-vnfm".equals(relationship.getRelatedTo())) {
                vnfmUrl = relationship.getRelatedLink();
                logger.debug("VNFM URL from GenericVnf relataionship: " + vnfmUrl);
                final Optional<EsrVnfm> vnfmOptional = aaiServiceProvider.invokeGet(vnfmUrl, EsrVnfm.class);
                if (vnfmOptional.isPresent()) {
                    return vnfmOptional.get();
                }
            }
        }
        return null;
    }

    /**
     * Select a VNFM to use for the given generic VNF. Should only be used when no VNFM has already been
     * assigned to the VNF.
     *
     * @param vnf the generic VNF
     * @return the VNFM to use
     */
    public EsrVnfm selectVnfm(final GenericVnf vnf) {
        final Optional<EsrVnfmList> vnfmsInEsr = aaiServiceProvider.invokeGetVnfms();

        if (!vnfmsInEsr.isPresent() || vnfmsInEsr.get().getEsrVnfm() == null) {
            throw new VnfmNotFoundException("No VNFMs found in AAI ESR");
        }
        logger.debug("VNFMs in ESR: " + vnfmsInEsr.get().getEsrVnfm());

        for (final EsrVnfm vnfm : vnfmsInEsr.get().getEsrVnfm()) {
            if (vnfmHasMatchingEsrSystemInfoType(vnfm, vnf.getNfType())) {
                return vnfm;
            }
        }
        throw new VnfmNotFoundException("No matching VNFM found in AAI ESR");
    }

    private boolean vnfmHasMatchingEsrSystemInfoType(final EsrVnfm vnfm, final String type) {
        logger.debug("Checking VNFM ID: " + vnfm + ": " + vnfm.getVnfmId());

        final Optional<EsrvnfmEsrsysteminfolist> systemInfolist =
                aaiServiceProvider.invokeGetVnfmEsrSystemInfoList(vnfm.getVnfmId());
        if (systemInfolist.isPresent()) {
            for (final EsrSystemInfo esrSystemInfo : systemInfolist.get().getEsrSystemInfo()) {
                if (esrSystemInfo.getType().equals(type)) {
                    logger.debug("Matched VNFM ID: " + vnfm + ", based on type");
                    return true;
                }
            }
        }
        return false;
    }

}
