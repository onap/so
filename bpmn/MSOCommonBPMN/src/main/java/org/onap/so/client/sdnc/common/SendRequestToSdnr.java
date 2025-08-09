/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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

package org.onap.so.client.sdnc.common;

import static org.onap.so.bpmn.common.scripts.GenericUtils.isBlank;
import javax.inject.Provider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.dmaapproperties.SdnrDmaapPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("SendRequestToSdnr")
public class SendRequestToSdnr implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(SendRequestToSdnr.class);
    ExceptionUtil exceptionUtil = new ExceptionUtil();

    @Autowired
    private Provider<SdnrDmaapPublisher> sdnrDmaapPublisher;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        logger.debug("SendRequestToSdnr_start");

        String requestId = (String) execution.getVariable("correlator");
        if (isBlank(requestId)) {
            String msg = "Cannot process SDNR Request : correlator is null";
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg);
        }

        String messageType = (String) execution.getVariable("messageType");
        if (isBlank(messageType)) {
            String msg = "Cannot process SDNR Request : messageType is null";
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg);
        }

        String timeout = (String) execution.getVariable("timeout");
        if (isBlank(timeout)) {
            timeout = UrnPropertiesReader.getVariable("sdnc.dmaap.callback.timeout", execution);
            if (isBlank(timeout)) {
                logger.debug("Setting SDNR timeout to default : PT30M");
                timeout = "PT30M";
            }
        }
        logger.debug("Async Callback Timeout will be: {}", timeout);
        String msg = (String) execution.getVariable("sdnrRequest");
        logger.debug("msg to be sent on dmaap {}", msg);
        sdnrDmaapPublisher.get().send(msg);

    }
}


