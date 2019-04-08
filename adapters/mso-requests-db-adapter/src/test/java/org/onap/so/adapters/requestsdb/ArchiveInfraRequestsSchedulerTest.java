/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.requestsdb;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.ArchivedInfraRequestsRepository;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

public class ArchiveInfraRequestsSchedulerTest extends RequestsAdapterBase {

    @Autowired
    private ArchiveInfraRequestsScheduler scheduler;

    @Autowired
    private InfraActiveRequestsRepository iarRepo;

    @Autowired
    private ArchivedInfraRequestsRepository archivedRepo;

    @Value("${mso.infra-requests.archived.period}")
    private int archivedPeriod;

    @Test
    @Transactional
    public void testArchiveInfraRequests() throws Exception {
        String requestId1 = "requestId1";
        String requestId2 = "requestId2";

        InfraActiveRequests iar1 = new InfraActiveRequests();
        iar1.setRequestId(requestId1);
        iar1.setAction("action1");

        InfraActiveRequests iar2 = new InfraActiveRequests();
        iar2.setRequestId(requestId2);
        iar2.setAction("action2");

        List<InfraActiveRequests> requests = new ArrayList<>();
        requests.add(iar1);
        requests.add(iar2);
        iarRepo.saveAll(requests);

        scheduler.archiveInfraRequests(requests);

        assertEquals(2, archivedRepo.count());
        assertEquals(requestId1,
                archivedRepo.findById(requestId1).orElseThrow(() -> new Exception("Request Not Found")).getRequestId());
        assertEquals(requestId2,
                archivedRepo.findById(requestId2).orElseThrow(() -> new Exception("Request Not Found")).getRequestId());
    }

}
