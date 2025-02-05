/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.TestApplication;
import org.onap.so.db.request.beans.OrchestrationTask;
import org.onap.so.db.request.data.repository.OrchestrationTaskRepository;
import org.onap.so.db.request.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import jakarta.transaction.Transactional;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrchestrationTaskTest {

    @Autowired
    private OrchestrationTaskRepository repository;

    @Test
    @Transactional
    public void timeStampCreated() throws NoEntityFoundException {

        final String testTaskId = "test-task-id";
        final String testRequestId = "test-request-id";
        final String testTaskName = "test-task-name";
        final String testTaskStatus = "test-task-status";
        final String testIsManual = "test-is-manual";
        OrchestrationTask task = new OrchestrationTask();

        task.setTaskId(testTaskId);
        task.setRequestId(testRequestId);
        task.setName(testTaskName);
        task.setStatus(testTaskStatus);
        task.setIsManual(testIsManual);
        repository.saveAndFlush(task);

        OrchestrationTask found =
                repository.findById(testTaskId).orElseThrow(() -> new NoEntityFoundException("Cannot Find Task"));

        Date createdTime = found.getCreatedTime();
        assertNotNull(createdTime);
        assertEquals(testTaskId, found.getTaskId());
        assertEquals(testRequestId, found.getRequestId());
        assertEquals(testTaskName, found.getName());
        assertEquals(testTaskStatus, found.getStatus());
        assertEquals(testIsManual, found.getIsManual());
    }
}
