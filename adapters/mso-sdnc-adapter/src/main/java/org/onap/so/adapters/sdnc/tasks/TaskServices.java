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

package org.onap.so.adapters.sdnc.tasks;

import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.onap.so.utils.ExternalTaskServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class TaskServices {

    @Autowired
    private ExternalTaskServiceUtils externalTaskServiceUtils;

    @Autowired
    private SDNCService service;

    @PostConstruct
    public void post() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("sdncPost").lockDuration(externalTaskServiceUtils.getLockDurationLong())
                    .handler(service::executePostTask).open();
        }
    }

    @PostConstruct
    public void get() throws Exception {
        for (int i = 0; i < externalTaskServiceUtils.getMaxClients(); i++) {
            ExternalTaskClient client = externalTaskServiceUtils.createExternalTaskClient();
            client.subscribe("sdncGet").lockDuration(externalTaskServiceUtils.getLockDurationLong())
                    .handler(service::executeGetTask).open();
        }
    }
}
