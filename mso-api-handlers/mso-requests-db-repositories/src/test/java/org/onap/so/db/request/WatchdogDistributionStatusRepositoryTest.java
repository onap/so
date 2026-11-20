/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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

package org.onap.so.db.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

public class WatchdogDistributionStatusRepositoryTest extends BaseTest {

    private static final String DISTRIBUTION_ID = "watchdog-race-distribution";

    @Autowired
    private WatchdogDistributionStatusRepository repository;

    @Test
    public void findOrCreateCreatesMissingRow() {
        WatchdogDistributionStatus created = repository.findOrCreate("watchdog-new-distribution");

        assertEquals("watchdog-new-distribution", created.getDistributionId());
        assertTrue(repository.findById("watchdog-new-distribution").isPresent());
    }

    @Test
    public void findOrCreateKeepsExistingRow() {
        WatchdogDistributionStatus existing = new WatchdogDistributionStatus("watchdog-existing-distribution");
        existing.setDistributionIdStatus("DISTRIBUTION_COMPLETE_OK");
        repository.saveAndFlush(existing);

        WatchdogDistributionStatus found = repository.findOrCreate("watchdog-existing-distribution");

        assertEquals("DISTRIBUTION_COMPLETE_OK", found.getDistributionIdStatus());
    }

    @Test
    public void findOrCreateReadsRowInsertedBySnapshotIsolationLoser() {
        assertReadsConcurrentlyInsertedRow(new CannotAcquireLockException("Record has changed since last read"));
    }

    @Test
    public void findOrCreateReadsRowInsertedByDuplicateKeyLoser() {
        assertReadsConcurrentlyInsertedRow(new DuplicateKeyException("Duplicate entry"));
    }

    @Test
    public void findOrCreateRethrowsWhenRowIsStillMissing() {
        WatchdogDistributionStatusRepository racingRepository =
                mock(WatchdogDistributionStatusRepository.class, CALLS_REAL_METHODS);
        DataIntegrityViolationException failure = new DataIntegrityViolationException("column cannot be null");
        doReturn(Optional.empty()).when(racingRepository).findById(DISTRIBUTION_ID);
        doThrow(failure).when(racingRepository).saveAndFlush(any(WatchdogDistributionStatus.class));

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> racingRepository.findOrCreate(DISTRIBUTION_ID)));
    }

    private void assertReadsConcurrentlyInsertedRow(RuntimeException insertFailure) {
        WatchdogDistributionStatusRepository racingRepository =
                mock(WatchdogDistributionStatusRepository.class, CALLS_REAL_METHODS);
        WatchdogDistributionStatus insertedByOtherThread = new WatchdogDistributionStatus(DISTRIBUTION_ID);
        doReturn(Optional.empty(), Optional.of(insertedByOtherThread)).when(racingRepository).findById(DISTRIBUTION_ID);
        doThrow(insertFailure).when(racingRepository).saveAndFlush(any(WatchdogDistributionStatus.class));

        assertSame(insertedByOtherThread, racingRepository.findOrCreate(DISTRIBUTION_ID));
        verify(racingRepository, never()).save(any(WatchdogDistributionStatus.class));
    }
}
