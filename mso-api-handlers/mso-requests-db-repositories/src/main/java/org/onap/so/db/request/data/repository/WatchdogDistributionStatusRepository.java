/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

package org.onap.so.db.request.data.repository;

import java.util.Optional;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "watchdogDistributionStatus", path = "watchdogDistributionStatus")
public interface WatchdogDistributionStatusRepository extends JpaRepository<WatchdogDistributionStatus, String> {

    public WatchdogDistributionStatus findOneByDistributionId(String distributionId);

    /**
     * Returns the status row of the distribution, creating it if it does not exist yet.
     * <p>
     * The SDC notification thread and the SDC status thread both create this row, and another component's
     * COMPONENT_DONE_OK can arrive before SO has stored the notification. The thread that loses the insert gets a
     * duplicate key, which MariaDB with innodb_snapshot_isolation reports as "Record has changed since last read"
     * (1020), so it reads the row the other thread created instead. Must not be called inside a transaction, because
     * the failed insert marks it rollback-only.
     */
    default WatchdogDistributionStatus findOrCreate(String distributionId) {
        Optional<WatchdogDistributionStatus> existing = findById(distributionId);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            return saveAndFlush(new WatchdogDistributionStatus(distributionId));
        } catch (DataIntegrityViolationException | PessimisticLockingFailureException e) {
            return findById(distributionId).orElseThrow(() -> e);
        }
    }

}
