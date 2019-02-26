/*
 * Copyright (C) 2019 Bell Canada.
 *
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
 */
package org.onap.so.client.cds;

import io.grpc.Status;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDSProcessingListenerImpl implements CDSProcessingListener {

    private static final Logger log = LoggerFactory.getLogger(CDSProcessingListenerImpl.class);

    @Override
    public void onMessage(ExecutionServiceOutput message) {
        log.info("Received notification from CDS: {}", message);

        // TODO handle message correctly, e.g. update SO request status
    }

    @Override
    public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        log.error("Failed processing blueprint {}", status, t);
    }
}
