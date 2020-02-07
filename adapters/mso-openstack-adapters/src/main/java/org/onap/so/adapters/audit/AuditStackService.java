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

package org.onap.so.adapters.audit;

import javax.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.onap.so.utils.ExternalTaskServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AuditStackService {

    private static final String MSO_AUDIT_LOCK_TIME = "mso.audit.lock-time";

    private static final Logger logger = LoggerFactory.getLogger(AuditStackService.class);

    private static final String DEFAULT_AUDIT_LOCK_TIME = "60000";

    private static final String DEFAULT_MAX_CLIENTS_FOR_TOPIC = "10";


    @Autowired
    public Environment env;

    @Autowired
    private AuditCreateStackService auditCreateStack;

    @Autowired
    private AuditDeleteStackService auditDeleteStack;

    @Autowired
    private AuditQueryStackService auditQueryStack;

    @Autowired
    private ExternalTaskServiceUtils externalTaskServiceUtils;

    @PostConstruct
    public void auditAddAAIInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryAddAudit")
                    .lockDuration(Long.parseLong(env.getProperty(MSO_AUDIT_LOCK_TIME, DEFAULT_AUDIT_LOCK_TIME)))
                    .handler(auditCreateStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditDeleteAAIInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryDeleteAudit")
                    .lockDuration(Long.parseLong(env.getProperty(MSO_AUDIT_LOCK_TIME, DEFAULT_AUDIT_LOCK_TIME)))
                    .handler(auditDeleteStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditQueryInventory() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("InventoryQueryAudit")
                    .lockDuration(Long.parseLong(env.getProperty(MSO_AUDIT_LOCK_TIME, DEFAULT_AUDIT_LOCK_TIME)))
                    .handler(auditQueryStack::executeExternalTask).open();
        }
    }

}
