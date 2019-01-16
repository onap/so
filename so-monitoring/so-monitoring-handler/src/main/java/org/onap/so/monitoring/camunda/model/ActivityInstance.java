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
package org.onap.so.monitoring.camunda.model;

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityInstance {

    private String activityId;
    private String activityName;
    private String activityType;
    private String processInstanceId;
    private String calledProcessInstanceId;
    private String startTime;
    private String endTime;
    private String durationInMillis;

    public ActivityInstance() {}


    /**
     * @return the activityId
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * @param activityId the activityId to set
     */
    public void setActivityId(final String activityId) {
        this.activityId = activityId;
    }

    /**
     * @return the activityName
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * @param activityName the activityName to set
     */
    public void setActivityName(final String activityName) {
        this.activityName = activityName;
    }

    /**
     * @return the activityType
     */
    public String getActivityType() {
        return activityType;
    }

    /**
     * @param activityType the activityType to set
     */
    public void setActivityType(final String activityType) {
        this.activityType = activityType;
    }

    /**
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @param processInstanceId the processInstanceId to set
     */
    public void setProcessInstanceId(final String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * @return the calledProcessInstanceId
     */
    public String getCalledProcessInstanceId() {
        return calledProcessInstanceId;
    }

    /**
     * @param calledProcessInstanceId the calledProcessInstanceId to set
     */
    public void setCalledProcessInstanceId(final String calledProcessInstanceId) {
        this.calledProcessInstanceId = calledProcessInstanceId;
    }

    /**
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the durationInMillis
     */
    public String getDurationInMillis() {
        return durationInMillis;
    }

    /**
     * @param durationInMillis the durationInMillis to set
     */
    public void setDurationInMillis(final String durationInMillis) {
        this.durationInMillis = durationInMillis;
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
        result = prime * result + ((activityName == null) ? 0 : activityName.hashCode());
        result = prime * result + ((activityType == null) ? 0 : activityType.hashCode());
        result = prime * result + ((calledProcessInstanceId == null) ? 0 : calledProcessInstanceId.hashCode());
        result = prime * result + ((durationInMillis == null) ? 0 : durationInMillis.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @JsonIgnore
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ActivityInstance) {
            final ActivityInstance other = (ActivityInstance) obj;
            return isEqual(activityId, other.activityId) && isEqual(activityName, other.activityName)
                    && isEqual(activityType, other.activityType) && isEqual(processInstanceId, other.processInstanceId)
                    && isEqual(calledProcessInstanceId, other.calledProcessInstanceId)
                    && isEqual(startTime, other.startTime) && isEqual(endTime, other.endTime)
                    && isEqual(durationInMillis, other.durationInMillis);
        }
        return false;
    }
    
}
