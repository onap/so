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

package org.onap.so.adapters.inventory.create;

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
public class CreateInventoryService {

    @Autowired
    public Environment env;

    @Autowired
    private CreateInventoryTask createInventory;

    @Autowired
    private ExternalTaskServiceUtils externalTaskServiceUtils;

    @PostConstruct
    public void auditAAIInventory() throws Exception {

        ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
        client.subscribe("InventoryCreate")
                .lockDuration(Long.parseLong(env.getProperty("mso.audit.lock-time", "60000")))
                .handler(createInventory::executeExternalTask).open();
    }

}
