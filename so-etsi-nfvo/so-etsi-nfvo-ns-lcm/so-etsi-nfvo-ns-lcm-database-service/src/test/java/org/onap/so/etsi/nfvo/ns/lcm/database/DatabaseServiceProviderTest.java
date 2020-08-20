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
package org.onap.so.etsi.nfvo.ns.lcm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobAction;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJobStatus;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DatabaseServiceProviderTest {

    private static final String RANDOM_ID = UUID.randomUUID().toString();
    private static final String DUMMY_NAME = "NAME";
    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();
    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Test
    public void testAddJob_StoredInDatabase() {
        final NfvoJob expected = new NfvoJob().jobType("TYPE").jobAction(JobAction.CREATE).resourceId(RANDOM_ID)
                .resourceName(DUMMY_NAME).startTime(CURRENT_DATE_TIME).status(JobStatusEnum.STARTED);
        databaseServiceProvider.addJob(expected);

        final Optional<NfvoJob> actual = databaseServiceProvider.getJob(expected.getJobId());
        assertEquals(expected, actual.get());
    }

    @Test
    public void testAddJobWithJobStatus_StoredInDatabase() {
        final NfvoJob job = new NfvoJob().jobType("TYPE").jobAction(JobAction.CREATE).resourceId(RANDOM_ID)
                .resourceName(DUMMY_NAME).startTime(CURRENT_DATE_TIME).status(JobStatusEnum.STARTED);
        databaseServiceProvider.addJob(job);

        final NfvoJobStatus jobStatus = new NfvoJobStatus().status(JobStatusEnum.STARTED)
                .description("Create NS workflow process started").updatedTime(CURRENT_DATE_TIME);
        databaseServiceProvider.addJob(job.nfvoJobStatus(jobStatus));

        final Optional<NfvoJob> actual = databaseServiceProvider.getJob(job.getJobId());
        final NfvoJob actualNfvoJob = actual.get();

        assertEquals(job.getJobId(), actualNfvoJob.getJobId());
        assertFalse(actualNfvoJob.getNfvoJobStatuses().isEmpty());
        assertEquals(job.getJobId(), actualNfvoJob.getNfvoJobStatuses().get(0).getNfvoJob().getJobId());

    }

    @Test
    public void testAddNsInst_StoredInDatabase_ableTofindByQuery() {

        final NfvoNsInst nsInst = new NfvoNsInst().name(DUMMY_NAME).nsdId(RANDOM_ID).status(State.NOT_INSTANTIATED)
                .nsdInvariantId(RANDOM_ID).statusUpdatedTime(CURRENT_DATE_TIME);

        databaseServiceProvider.saveNfvoNsInst(nsInst);

        Optional<NfvoNsInst> actual = databaseServiceProvider.getNfvoNsInst(nsInst.getNsInstId());
        NfvoNsInst actualNfvoNsInst = actual.get();
        assertEquals(nsInst.getNsInstId(), actualNfvoNsInst.getNsInstId());
        assertEquals(RANDOM_ID, actualNfvoNsInst.getNsdId());
        assertEquals(State.NOT_INSTANTIATED, actualNfvoNsInst.getStatus());
        assertEquals(RANDOM_ID, actualNfvoNsInst.getNsdInvariantId());
        assertEquals(CURRENT_DATE_TIME, actualNfvoNsInst.getStatusUpdatedTime());

        actual = databaseServiceProvider.getNfvoNsInstByName(DUMMY_NAME);
        actualNfvoNsInst = actual.get();

        assertEquals(nsInst.getNsInstId(), actualNfvoNsInst.getNsInstId());
        assertEquals(RANDOM_ID, actualNfvoNsInst.getNsdId());
        assertEquals(State.NOT_INSTANTIATED, actualNfvoNsInst.getStatus());
        assertEquals(RANDOM_ID, actualNfvoNsInst.getNsdInvariantId());
        assertEquals(CURRENT_DATE_TIME, actualNfvoNsInst.getStatusUpdatedTime());


        assertTrue(databaseServiceProvider.isNsInstExists(DUMMY_NAME));
    }

}
