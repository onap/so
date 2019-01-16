/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.model;

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
public class ActivityInstanceDetail {
    private final String activityId;
    private final String activityName;
    private final String activityType;
    private final String processInstanceId;
    private final String calledProcessInstanceId;
    private final String startTime;
    private final String endTime;
    private final String durationInMilliseconds;

    public ActivityInstanceDetail(final ActivityInstanceDetailBuilder builder) {
        this.activityId = builder.activityId;
        this.activityName = builder.activityName;
        this.activityType = builder.activityType;
        this.processInstanceId = builder.processInstanceId;
        this.calledProcessInstanceId = builder.calledProcessInstanceId;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.durationInMilliseconds = builder.durationInMilliseconds;
    }

    /**
     * @return the activityId
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * @return the activityName
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * @return the activityType
     */
    public String getActivityType() {
        return activityType;
    }

    /**
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @return the calledProcessInstanceId
     */
    public String getCalledProcessInstanceId() {
        return calledProcessInstanceId;
    }

    /**
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @return the endTime
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @return the durationInMillis
     */
    public String getDurationInMillis() {
        return durationInMilliseconds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
        result = prime * result + ((activityName == null) ? 0 : activityName.hashCode());
        result = prime * result + ((activityType == null) ? 0 : activityType.hashCode());
        result = prime * result + ((calledProcessInstanceId == null) ? 0 : calledProcessInstanceId.hashCode());
        result = prime * result + ((durationInMilliseconds == null) ? 0 : durationInMilliseconds.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof ActivityInstanceDetail) {
            final ActivityInstanceDetail other = (ActivityInstanceDetail) obj;
            return isEqual(activityId, other.activityId) && isEqual(activityName, other.activityName)
                    && isEqual(activityType, other.activityType) && isEqual(processInstanceId, other.processInstanceId)
                    && isEqual(calledProcessInstanceId, other.calledProcessInstanceId)
                    && isEqual(startTime, other.startTime) && isEqual(endTime, other.endTime)
                    && isEqual(durationInMilliseconds, other.durationInMilliseconds);
        }

        return false;
    }


    public static final class ActivityInstanceDetailBuilder {

        private String activityId;
        private String activityName;
        private String activityType;
        private String processInstanceId;
        private String calledProcessInstanceId;
        private String startTime;
        private String endTime;
        private String durationInMilliseconds;

        public ActivityInstanceDetailBuilder activityId(final String activityId) {
            this.activityId = activityId;
            return this;
        }

        public ActivityInstanceDetailBuilder activityName(final String activityName) {
            this.activityName = activityName;
            return this;
        }

        public ActivityInstanceDetailBuilder activityType(final String activityType) {
            this.activityType = activityType;
            return this;
        }

        public ActivityInstanceDetailBuilder processInstanceId(final String processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public ActivityInstanceDetailBuilder calledProcessInstanceId(final String calledProcessInstanceId) {
            this.calledProcessInstanceId = calledProcessInstanceId;
            return this;
        }

        public ActivityInstanceDetailBuilder startTime(final String startTime) {
            this.startTime = startTime;
            return this;
        }

        public ActivityInstanceDetailBuilder endTime(final String endTime) {
            this.endTime = endTime;
            return this;
        }

        public ActivityInstanceDetailBuilder durationInMilliseconds(final String durationInMilliseconds) {
            this.durationInMilliseconds = durationInMilliseconds;
            return this;
        }

        public ActivityInstanceDetail build() {
            return new ActivityInstanceDetail(this);
        }
    }

}
