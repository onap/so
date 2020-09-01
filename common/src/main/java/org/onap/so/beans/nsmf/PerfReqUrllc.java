/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerfReqUrllc {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int e2eLatency;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int jitter;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int survivalTime;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private float csAvailability;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private float reliability;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int expDataRate;

    private String payloadSize;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int trafficDensity;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int connDensity;

    private String serviceAreaDimension;

    public int getE2eLatency() {
        return e2eLatency;
    }

    public void setE2eLatency(int e2eLatency) {
        this.e2eLatency = e2eLatency;
    }

    public int getJitter() {
        return jitter;
    }

    public void setJitter(int jitter) {
        this.jitter = jitter;
    }

    public int getSurvivalTime() {
        return survivalTime;
    }

    public void setSurvivalTime(int survivalTime) {
        this.survivalTime = survivalTime;
    }

    public float getReliability() {
        return reliability;
    }

    public void setReliability(float reliability) {
        this.reliability = reliability;
    }

    public int getExpDataRate() {
        return expDataRate;
    }

    public void setExpDataRate(int expDataRate) {
        this.expDataRate = expDataRate;
    }

    public String getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(String payloadSize) {
        this.payloadSize = payloadSize;
    }

    public int getTrafficDensity() {
        return trafficDensity;
    }

    public void setTrafficDensity(int trafficDensity) {
        this.trafficDensity = trafficDensity;
    }

    public int getConnDensity() {
        return connDensity;
    }

    public void setConnDensity(int connDensity) {
        this.connDensity = connDensity;
    }

    public String getServiceAreaDimension() {
        return serviceAreaDimension;
    }

    public void setServiceAreaDimension(String serviceAreaDimension) {
        this.serviceAreaDimension = serviceAreaDimension;
    }

    public float getCsAvailability() {
        return csAvailability;
    }

    public void setCsAvailability(float csAvailability) {
        this.csAvailability = csAvailability;
    }
}
