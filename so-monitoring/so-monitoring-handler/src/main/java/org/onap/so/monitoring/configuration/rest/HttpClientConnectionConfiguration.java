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
package org.onap.so.monitoring.configuration.rest;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HttpClientConnectionConfiguration {

    @Value(value = "${rest.http.client.configuration.connTimeOutInSec:10}")
    private int connectionTimeOutInSeconds;

    @Value(value = "${rest.http.client.configuration.socketTimeOutInSec:180}")
    private int socketTimeOutInSeconds;

    @Value(value = "${rest.http.client.configuration.socketTimeOutInSec:600}")
    private int timeToLiveInSeconds;

    @Value(value = "${rest.http.client.configuration.maxConnections:10}")
    private int maxConnections;

    @Value(value = "${rest.http.client.configuration.maxConnectionsPerRoute:2}")
    private int maxConnectionsPerRoute;

    /**
     * @return the socketTimeOut
     */
    public int getSocketTimeOutInMiliSeconds() {
        return (int) TimeUnit.SECONDS.toMillis(socketTimeOutInSeconds);
    }

    /**
     * @return the maxConnections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @return the maxConnectionsPerRoute
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * @return the connectionTimeOut
     */
    public int getConnectionTimeOutInMilliSeconds() {
        return (int) TimeUnit.SECONDS.toMillis(connectionTimeOutInSeconds);
    }

    /**
     * @return the timeToLive
     */
    public int getTimeToLiveInMins() {
        return (int) TimeUnit.SECONDS.toMinutes(timeToLiveInSeconds);
    }

    @Override
    public String toString() {
        return "HttpClientConnectionConfiguration [connectionTimeOutInSeconds=" + connectionTimeOutInSeconds
                + ", socketTimeOutInSeconds=" + socketTimeOutInSeconds + ", timeToLiveInSeconds=" + timeToLiveInSeconds
                + ", maxConnections=" + maxConnections + ", maxConnectionsPerRoute=" + maxConnectionsPerRoute + "]";
    }

}
