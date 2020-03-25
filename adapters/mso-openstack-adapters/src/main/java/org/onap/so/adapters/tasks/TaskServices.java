/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.tasks;

import javax.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.onap.so.utils.ExternalTaskServiceUtils;
import org.onap.so.adapters.tasks.audit.AuditCreateStackService;
import org.onap.so.adapters.tasks.audit.AuditDeleteStackService;
import org.onap.so.adapters.tasks.audit.AuditQueryStackService;
import org.onap.so.adapters.tasks.inventory.CreateInventoryTask;
import org.onap.so.adapters.tasks.orchestration.PollService;
import org.onap.so.adapters.tasks.orchestration.RollbackService;
import org.onap.so.adapters.tasks.orchestration.StackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class TaskServices {

    private static final Logger logger = LoggerFactory.getLogger(TaskServices.class);

    @Autowired
    private ExternalTaskServiceUtils externalTaskServiceUtils;

    @Autowired
    private AuditCreateStackService auditCreateStack;

    @Autowired
    private AuditDeleteStackService auditDeleteStack;

    @Autowired
    private AuditQueryStackService auditQueryStack;

    @Autowired
    private CreateInventoryTask createInventory;

    @Autowired
    private StackService stackService;

    @Autowired
    private PollService pollService;

    @Autowired
    private RollbackService rollbackService;

    @PostConstruct
    public void auditAddAAIInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryAddAudit").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(auditCreateStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditDeleteAAIInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryDeleteAudit").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(auditDeleteStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditQueryInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryQueryAudit").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(auditQueryStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditAAIInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryCreate").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(createInventory::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void openstackInvoker() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("OpenstackAdapterInvoke").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(stackService::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void openstackPoller() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("OpenstackAdapterPolling").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(pollService::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void openstackRollback() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("OpenstackAdapterRollback").lockDuration(externalTaskServiceUtils.getLockDuration())
                    .handler(rollbackService::executeExternalTask).open();
        }
    }

}
