/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import javax.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.BaseTest;
import org.onap.so.TestApplication;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.OperationStatusId;
import org.onap.so.db.request.data.repository.OperationStatusRepository;
import org.onap.so.db.request.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

public class OperationStatusTest extends BaseTest {

    @Autowired
    private OperationStatusRepository repository;

    @Value("${spring.datasource.url}")
    String url;

    @Test
    @Transactional
    public void timeStampCreated() throws InterruptedException, NoEntityFoundException {

        final String testServiceId = "test-service-id";
        final String testOperationId = "test-operation-id";
        final OperationStatusId id = new OperationStatusId(testServiceId, testOperationId);
        OperationStatus status = new OperationStatus();

        status.setServiceId(testServiceId);
        status.setOperationId(testOperationId);

        status = repository.saveAndFlush(status);

        OperationStatus found =
                repository.findById(id).orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));

        Date operateAt = found.getOperateAt();
        assertNotNull(operateAt);
        assertEquals(testServiceId, found.getServiceId());
        Date finishedAt = found.getFinishedAt();
        status.setProgress("test-progress");
        // timestamps only set to save on 1 second changes
        Thread.sleep(1000);
        repository.saveAndFlush(status);

        OperationStatus foundUpdate =
                repository.findById(id).orElseThrow(() -> new NoEntityFoundException("Cannot Find Operation"));

        assertEquals(operateAt.toString(), foundUpdate.getOperateAt().toString());
        assertNotNull(foundUpdate.getFinishedAt());
        assertNotEquals(finishedAt.toString(), foundUpdate.getFinishedAt().toString());
        assertEquals("test-progress", foundUpdate.getProgress());
    }
}
