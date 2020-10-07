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
package org.onap.so.etsi.nfvo.ns.lcm.lifecycle;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.etsi.nfvo.ns.lcm.EtsiSoNsLcmManagerUrlProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpOcc;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstanceLinksSelf;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsLcmOpOccsNsLcmOpOcc;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsLcmOpOccsNsLcmOpOccLinks;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@Service
public class NsLcmOperationOccurrenceManager {

    private static final Logger logger = getLogger(NsLcmOperationOccurrenceManager.class);

    private final DatabaseServiceProvider databaseServiceProvider;
    private final EtsiSoNsLcmManagerUrlProvider etsiSoNsLcmManagerUrlProvider;

    @Autowired
    public NsLcmOperationOccurrenceManager(final DatabaseServiceProvider databaseServiceProvider,
            final EtsiSoNsLcmManagerUrlProvider etsiSoNsLcmManagerUrlProvider) {
        this.databaseServiceProvider = databaseServiceProvider;
        this.etsiSoNsLcmManagerUrlProvider = etsiSoNsLcmManagerUrlProvider;
    }

    public Optional<NsLcmOpOccsNsLcmOpOcc> getNsLcmOperationOccurrence(final String nsLcmOpOccId) {
        logger.info("Getting NS LCM Operation Occurrence Operation for id: {}", nsLcmOpOccId);
        final Optional<NsLcmOpOcc> optionalNsLcmOpOcc = databaseServiceProvider.getNsLcmOpOcc(nsLcmOpOccId);

        if (optionalNsLcmOpOcc.isEmpty()) {
            logger.info("No NS LCM Operation Occurrence found for id: {}", nsLcmOpOccId);
            return Optional.empty();
        }

        logger.info("Found NS LCM Operation Occurrence for id: {}", nsLcmOpOccId);
        final NsLcmOpOcc nsLcmOpOcc = optionalNsLcmOpOcc.get();
        final NsLcmOpOccsNsLcmOpOcc nsLcmOpOccsNsLcmOpOcc = convertToNsLcmOpOccsNsLcmOpOcc(nsLcmOpOcc);
        return Optional.of(nsLcmOpOccsNsLcmOpOcc);
    }

    private NsLcmOpOccsNsLcmOpOcc convertToNsLcmOpOccsNsLcmOpOcc(final NsLcmOpOcc nsLcmOpOcc) {
        logger.info("Converting Database NsLcmOpOcc to API NsLcmOpOcc... ");
        final NsLcmOpOccsNsLcmOpOcc nsLcmOpOccsNsLcmOpOcc =
                new NsLcmOpOccsNsLcmOpOcc().id(nsLcmOpOcc.getId()).statusEnteredTime(nsLcmOpOcc.getStateEnteredTime())
                        .startTime(nsLcmOpOcc.getStartTime()).isAutomaticInvocation(nsLcmOpOcc.getIsAutoInvocation())
                        .isCancelPending(nsLcmOpOcc.getIsCancelPending());

        if (nsLcmOpOcc.getNfvoNsInst() != null) {
            nsLcmOpOccsNsLcmOpOcc.setNsInstanceId(nsLcmOpOcc.getNfvoNsInst().getNsInstId());
        }

        if (nsLcmOpOcc.getOperationState() != null) {
            nsLcmOpOccsNsLcmOpOcc.setOperationState(
                    NsLcmOpOccsNsLcmOpOcc.OperationStateEnum.fromValue(nsLcmOpOcc.getOperationState().toString()));
        }

        if (nsLcmOpOcc.getOperation() != null) {
            nsLcmOpOccsNsLcmOpOcc.setLcmOperationType(
                    NsLcmOpOccsNsLcmOpOcc.LcmOperationTypeEnum.fromValue(nsLcmOpOcc.getOperation().toString()));
        }

        if (nsLcmOpOcc.getOperationParams() != null) {
            nsLcmOpOccsNsLcmOpOcc.setOperationParams(nsLcmOpOcc.getOperationParams());
        }

        if (nsLcmOpOcc.getCancelMode() != null) {
            nsLcmOpOccsNsLcmOpOcc.setCancelMode(
                    NsLcmOpOccsNsLcmOpOcc.CancelModeEnum.fromValue(nsLcmOpOcc.getCancelMode().toString()));
        }

        nsLcmOpOccsNsLcmOpOcc.setLinks(generateLinks(nsLcmOpOcc));

        logger.info("Database NsLcmOpOcc converted to API NsLcmOpOcc successfully... {}", nsLcmOpOccsNsLcmOpOcc);
        return nsLcmOpOccsNsLcmOpOcc;
    }

    private NsLcmOpOccsNsLcmOpOccLinks generateLinks(final NsLcmOpOcc nsLcmOpOcc) {
        logger.info("Generating links...");
        final String nsLcmOpOccId = nsLcmOpOcc.getId();
        final NsInstancesNsInstanceLinksSelf linksSelfNsLcmOpOcc = new NsInstancesNsInstanceLinksSelf()
                .href(etsiSoNsLcmManagerUrlProvider.getNsLcmOpOccUri(nsLcmOpOccId).toString());
        final NsLcmOpOccsNsLcmOpOccLinks links = new NsLcmOpOccsNsLcmOpOccLinks().self(linksSelfNsLcmOpOcc);

        if (nsLcmOpOcc.getNfvoNsInst() != null) {
            final String nsInstId = nsLcmOpOcc.getNfvoNsInst().getNsInstId();
            final NsInstancesNsInstanceLinksSelf linksSelfNsInst = new NsInstancesNsInstanceLinksSelf()
                    .href(etsiSoNsLcmManagerUrlProvider.getCreatedNsResourceUri(nsInstId).toString());
            links.setNsInstance(linksSelfNsInst);
        }

        return links;
    }

}
