/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.asdc.client;

import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.utils.DistributionStatusEnum;

public class FinalDistributionStatusMessage implements IFinalDistrStatusMessage {

    private String componentName;

    private String consumerID;

    private String distributionID;

    private DistributionStatusEnum status;

    private long timestamp;

    public FinalDistributionStatusMessage(String distributionId, final DistributionStatusEnum distributionStatusEnum,
            final long timestampL, String consumerId) {
        consumerID = consumerId;
        distributionID = distributionId;
        status = distributionStatusEnum;
        timestamp = timestampL;
    }

    public DistributionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DistributionStatusEnum status) {
        this.status = status;
    }

    public String getDistributionID() {
        return distributionID;
    }

    public void setDistributionID(String distributionID) {
        this.distributionID = distributionID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public String getConsumerID() {
        return consumerID;
    }

    public void setConsumerID(String consumerID) {
        this.consumerID = consumerID;
    }

}
