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

package org.onap.so.adapters.requestsdb;

import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.OrchestrationTask;
import org.onap.so.db.request.data.repository.OrchestrationTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class OrchestrationTaskRepositoryCustomController {

    @Autowired
    private OrchestrationTaskRepository orchestrationTaskRepository;

    @GetMapping(value = "/orchestrationTask")
    public List<OrchestrationTask> getAllOrchestrationTask() {
        return orchestrationTaskRepository.findAll();
    }

    @GetMapping(value = "/orchestrationTask/{taskId}")
    public OrchestrationTask getOrchestrationTask(@PathVariable("taskId") String taskId) throws MsoRequestsDbException {
        return orchestrationTaskRepository.findById(taskId)
                .orElseThrow(() -> new MsoRequestsDbException("orchestration task not found: " + taskId));
    }

    @PostMapping(value = "/orchestrationTask/")
    public OrchestrationTask createOrchestrationTask(@RequestBody OrchestrationTask orchestrationTask) {
        return orchestrationTaskRepository.save(orchestrationTask);
    }

    @PutMapping(value = "/orchestrationTask/{taskId}")
    public OrchestrationTask updateOrchestrationTask(@PathVariable("taskId") String taskId,
            @RequestBody OrchestrationTask orchestrationTask) {
        return orchestrationTaskRepository.save(orchestrationTask);
    }

    @DeleteMapping(value = "/orchestrationTask/{taskId}")
    public void deleteOrchestrationTask(@PathVariable("taskId") String taskId) {
        orchestrationTaskRepository.deleteById(taskId);
    }
}
