/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.onap.so.adapters.etsi.sol003.adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import java.time.Instant;
import java.util.UUID;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;

public class DmaapEvent {

    public static final String MSERVICE = "microservice.stringmatcher";
    public static final String ONSET = "ONSET";
    public static final String VNF = "VNF";
    public static final String VNFID = "generic-vnf.vnf-id";
    public static final String ETSI = "ETSI";

    private final String closedLoopControlName;
    private final long closedLoopAlarmStart;
    private final String closedLoopEventClient;
    private final String closedLoopEventStatus;
    private final String requestId;
    private final String targetType;
    private final String target;
    private final AaiEvent aaiEvent;
    private final String from;
    private final String version;
    private final VnfLcmOperationOccurrenceNotification etsiLcmEvent;

    public DmaapEvent(final String closedLoopControlName, final String version,
            final VnfLcmOperationOccurrenceNotification etsiLcmEvent, final String genericId) {
        this.closedLoopControlName = closedLoopControlName;
        this.closedLoopAlarmStart = Instant.now().getLong(INSTANT_SECONDS);
        this.closedLoopEventClient = MSERVICE;
        this.closedLoopEventStatus = ONSET;
        this.requestId = UUID.randomUUID().toString();
        this.targetType = VNF;
        this.target = VNFID;
        this.aaiEvent = new AaiEvent(false, genericId);
        this.from = ETSI;
        this.version = version;
        this.etsiLcmEvent = etsiLcmEvent;
    }

    public String getClosedLoopControlName() {
        return closedLoopControlName;
    }

    public long getClosedLoopAlarmStart() {
        return closedLoopAlarmStart;
    }

    public String getClosedLoopEventClient() {
        return closedLoopEventClient;
    }

    public String getClosedLoopEventStatus() {
        return closedLoopEventStatus;
    }

    @JsonProperty("requestID")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("target_type")
    public String getTargetType() {
        return targetType;
    }

    public String getTarget() {
        return target;
    }

    @JsonProperty("AAI")
    public AaiEvent getAaiEvent() {
        return aaiEvent;
    }

    public String getFrom() {
        return from;
    }

    public String getVersion() {
        return version;
    }

    public VnfLcmOperationOccurrenceNotification getEtsiLcmEvent() {
        return etsiLcmEvent;
    }
}
