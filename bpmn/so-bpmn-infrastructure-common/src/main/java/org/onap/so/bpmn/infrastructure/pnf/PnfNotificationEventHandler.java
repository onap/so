/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.pnf;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PnfNotificationEventHandler implements ApplicationListener<PnfNotificationEvent> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, DelegateExecution> pnfCorrelationIdMap = new HashMap<String, DelegateExecution>();

    private boolean useNotification;

    public void registerPnf(String pnfCorrelationId, DelegateExecution delegateExecution) {
        pnfCorrelationIdMap.put(pnfCorrelationId, delegateExecution);
    }

    public void unregisterPnf(String pnfCorrelationId) {
        pnfCorrelationIdMap.remove(pnfCorrelationId);
    }

    @Override
    public void onApplicationEvent(PnfNotificationEvent event) {
        logger.info("Received application event for pnfCorrelationId: {}", event.getPnfCorrelationId());
        if (useNotification) {
            if (pnfCorrelationIdMap.containsKey(event.getPnfCorrelationId())) {
                DelegateExecution execution = pnfCorrelationIdMap.get(event.getPnfCorrelationId());
                RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
                runtimeService.createMessageCorrelation("WorkflowMessage")
                        .processInstanceBusinessKey(execution.getProcessBusinessKey()).correlateWithResult();
            }
            unregisterPnf(event.getPnfCorrelationId());
        }
    }

    @Value("${pnf.dmaap.notificationEnable:true}")
    public void setUseNotification(final boolean useNotification) {
        this.useNotification = useNotification;
    }

    public Map<String, DelegateExecution> getPnfCorrelationIdMap() {
        return pnfCorrelationIdMap;
    }
}
