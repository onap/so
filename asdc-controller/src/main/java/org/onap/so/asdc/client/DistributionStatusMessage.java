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

package org.onap.so.asdc.client;


import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.utils.DistributionStatusEnum;

public class DistributionStatusMessage implements IDistributionStatusMessage {

    private String artifactURL;

    private String consumerID;

    private String distributionID;

    private DistributionStatusEnum distributionStatus;

    private long timestamp;

    public DistributionStatusMessage(final String artifactUrl, final String consumerId, final String distributionId,
            final DistributionStatusEnum distributionStatusEnum, final long timestampL) {
        artifactURL = artifactUrl;
        consumerID = consumerId;
        distributionID = distributionId;
        distributionStatus = distributionStatusEnum;
        timestamp = timestampL;
    }

    @Override
    public String getArtifactURL() {

        return artifactURL;
    }

    @Override
    public String getConsumerID() {

        return consumerID;
    }

    @Override
    public String getDistributionID() {

        return distributionID;
    }

    @Override
    public DistributionStatusEnum getStatus() {

        return distributionStatus;
    }

    @Override
    public long getTimestamp() {

        return timestamp;
    }

}
