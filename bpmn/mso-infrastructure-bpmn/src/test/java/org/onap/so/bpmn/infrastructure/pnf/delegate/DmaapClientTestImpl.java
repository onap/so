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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import java.util.Map;
import java.util.Objects;
import org.onap.so.bpmn.infrastructure.pnf.dmaap.DmaapClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DmaapClientTestImpl implements DmaapClient {

    private String pnfCorrelationId;
    private Runnable informConsumer;

    @Override
    public void registerForUpdate(String pnfCorrelationId, Runnable informConsumer, Map<String, String> updateInfo) {
        this.pnfCorrelationId = pnfCorrelationId;
        this.informConsumer = informConsumer;
    }

    @Override
    public Runnable unregister(String pnfCorrelationId) {
        if (Objects.equals(this.pnfCorrelationId, pnfCorrelationId)) {
            this.pnfCorrelationId = null;
            Runnable informConsumer = this.informConsumer;
            this.informConsumer = null;
            return informConsumer;
        }
        return null;
    }

    public String getPnfCorrelationId() {
        return pnfCorrelationId;
    }

    public Runnable getInformConsumer() {
        return informConsumer;
    }

    public void sendMessage() {
        informConsumer.run();
    }

    public boolean haveRegisteredConsumer() {
        return pnfCorrelationId != null;
    }
}
