/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.vfcmodel;



/**
 * <br>
 * <p>
 * </p>
 *
 * @author
 * @version ONAP Amsterdam Release 2017-9-26
 */
public class ScaleNsByStepsData {

    /**
     * scaling Direction
     */
    private String scalingDirection;

    /**
     * aspect ID
     */
    private String aspectId;

    /**
     * number of Steps
     */
    private int numberOfSteps;

    /**
     * @return Returns the scalingDirection.
     */
    public String getScalingDirection() {
        return scalingDirection;
    }

    /**
     * @param scalingDirection The scalingDirection to set.
     */
    public void setScalingDirection(String scalingDirection) {
        this.scalingDirection = scalingDirection;
    }

    /**
     * @return Returns the aspectId.
     */
    public String getAspectId() {
        return aspectId;
    }

    /**
     * @param aspectId The aspectId to set.
     */
    public void setAspectId(String aspectId) {
        this.aspectId = aspectId;
    }

    /**
     * @return Returns the numberofSteps.
     */
    public int getNumberOfSteps() {
        return numberOfSteps;
    }

    /**
     * @param numberOfSteps The numberofSteps to set.
     */
    public void setNumberOfSteps(int numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }
}
