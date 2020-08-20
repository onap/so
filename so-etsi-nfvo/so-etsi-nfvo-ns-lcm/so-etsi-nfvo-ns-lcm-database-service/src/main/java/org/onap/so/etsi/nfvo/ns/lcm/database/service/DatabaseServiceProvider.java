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
package org.onap.so.etsi.nfvo.ns.lcm.database.service;

import java.util.Optional;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.repository.NfvoJobRepository;
import org.onap.so.etsi.nfvo.ns.lcm.database.repository.NfvoNsInstRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */

@Service
public class DatabaseServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceProvider.class);

    private final NfvoJobRepository nfvoJobRepository;

    private final NfvoNsInstRepository nfvoNsInstRepository;

    @Autowired
    public DatabaseServiceProvider(final NfvoJobRepository nfvoJobRepository,
            final NfvoNsInstRepository nfvoNsInstRepository) {
        this.nfvoJobRepository = nfvoJobRepository;
        this.nfvoNsInstRepository = nfvoNsInstRepository;
    }

    public boolean addJob(final NfvoJob job) {
        logger.info("Adding NfvoJob: {} to database", job);
        return nfvoJobRepository.save(job) != null;
    }

    public Optional<NfvoJob> getJob(final String jobId) {
        logger.info("Querying database for NfvoJob using jobId: {}", jobId);
        return nfvoJobRepository.findById(jobId);
    }

    public Optional<NfvoJob> getJobByResourceId(final String resourceId) {
        logger.info("Querying database for NfvoJob using resourceId: {}", resourceId);
        return nfvoJobRepository.findByResourceId(resourceId);
    }

    public boolean isNsInstExists(final String name) {
        logger.info("Checking if NfvoNsInst entry exists in database using name: {}", name);
        return nfvoNsInstRepository.existsNfvoNsInstByName(name);
    }

    public boolean saveNfvoNsInst(final NfvoNsInst nfvoNsInst) {
        logger.info("Saving NfvoNsInst: {} to database", nfvoNsInst);
        return nfvoNsInstRepository.save(nfvoNsInst) != null;
    }

    public Optional<NfvoNsInst> getNfvoNsInst(final String nsInstId) {
        logger.info("Querying database for NfvoNsInst using nsInstId: {}", nsInstId);
        return nfvoNsInstRepository.findById(nsInstId);
    }

    public Optional<NfvoNsInst> getNfvoNsInstByName(final String name) {
        logger.info("Querying database for NfvoNsInst using name: {}", name);
        return nfvoNsInstRepository.findByName(name);
    }

    public Optional<NfvoNsInst> getNfvoNsInstByNsInstId(final String nsInstId) {
        logger.info("Querying database for NfvoNsInst using nsInstId: {}", nsInstId);
        return nfvoNsInstRepository.findByNsInstId(nsInstId);
    }

}
