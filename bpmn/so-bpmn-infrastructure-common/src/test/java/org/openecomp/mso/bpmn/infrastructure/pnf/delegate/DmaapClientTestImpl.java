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

package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import org.openecomp.mso.bpmn.infrastructure.pnf.dmaap.DmaapClient;
import java.util.Objects;

public class DmaapClientTestImpl implements DmaapClient {

    private String correlationId;
    private Runnable informConsumer;

    @Override
    public void registerForUpdate(String correlationId, Runnable informConsumer) {
        this.correlationId = correlationId;
        this.informConsumer = informConsumer;
    }

    @Override
    public Runnable unregister(String correlationId) {
        if (Objects.equals(this.correlationId, correlationId)) {
            this.correlationId = null;
            Runnable informConsumer = this.informConsumer;
            this.informConsumer = null;
            return informConsumer;
        }
        return null;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Runnable getInformConsumer() {
        return informConsumer;
    }

    public boolean haveRegisteredConsumer() {
        return correlationId != null;
    }
}
