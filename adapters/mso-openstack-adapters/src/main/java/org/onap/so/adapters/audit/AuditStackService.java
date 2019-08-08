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

import java.security.GeneralSecurityException;
import javax.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AuditStackService {

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

    @PostConstruct
    public void auditAddAAIInventory() {
        for (int i = 0; i < getMaxClients(); i++) {
            ExternalTaskClient client = createExternalTaskClient();
            client.subscribe("InventoryAddAudit")
                    .lockDuration(Long.parseLong(env.getProperty("mso.audit.lock-time", DEFAULT_AUDIT_LOCK_TIME)))
                    .handler(auditCreateStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditDeleteAAIInventory() {
        for (int i = 0; i < getMaxClients(); i++) {
            ExternalTaskClient client = createExternalTaskClient();
            client.subscribe("InventoryDeleteAudit")
                    .lockDuration(Long.parseLong(env.getProperty("mso.audit.lock-time", DEFAULT_AUDIT_LOCK_TIME)))
                    .handler(auditDeleteStack::executeExternalTask).open();
        }
    }

    @PostConstruct
    public void auditQueryInventory() {
        for (int i = 0; i < getMaxClients(); i++) {
            ExternalTaskClient client = createExternalTaskClient();
            client.subscribe("InventoryQueryAudit")
                    .lockDuration(Long.parseLong(env.getProperty("mso.audit.lock-time", DEFAULT_AUDIT_LOCK_TIME)))
                    .handler(auditQueryStack::executeExternalTask).open();
        }
    }

    protected ExternalTaskClient createExternalTaskClient() {
        ClientRequestInterceptor interceptor = createClientRequestInterceptor();
        return ExternalTaskClient.create().baseUrl(env.getRequiredProperty("mso.workflow.endpoint")).maxTasks(1)
                .addInterceptor(interceptor).asyncResponseTimeout(120000)
                .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0)).build();
    }

    protected ClientRequestInterceptor createClientRequestInterceptor() {
        String auth = "";
        try {
            auth = CryptoUtils.decrypt(env.getRequiredProperty("mso.auth"), env.getRequiredProperty("mso.msoKey"));
        } catch (IllegalStateException | GeneralSecurityException e) {
            logger.error("Error Decrypting Password", e);
        }
        return new BasicAuthProvider(env.getRequiredProperty("mso.config.cadi.aafId"), auth);
    }

    protected int getMaxClients() {
        return Integer.parseInt(env.getProperty("workflow.topics.maxClients", DEFAULT_MAX_CLIENTS_FOR_TOPIC));
    }


}
