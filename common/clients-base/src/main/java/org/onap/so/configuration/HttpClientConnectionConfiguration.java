/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.configuration;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This class is used configure the parameters needed for {@link org.apache.http.impl.client.CloseableHttpClient}
 *
 * @author waqas.ikram@est.tech
 */
@Service
public class HttpClientConnectionConfiguration {

    @Value(value = "${rest.http.client.configuration.connTimeOutInSec:10}")
    private int connectionTimeOutInSeconds;

    @Value(value = "${rest.http.client.configuration.socketTimeOutInSec:180}")
    private int socketTimeOutInSeconds;

    @Value(value = "${rest.http.client.configuration.timeToLiveInSeconds:600}")
    private int timeToLiveInSeconds;

    @Value(value = "${rest.http.client.configuration.maxConnections:100}")
    private int maxConnections;

    @Value(value = "${rest.http.client.configuration.maxConnectionsPerRoute:20}")
    private int maxConnectionsPerRoute;

    @Value(value = "${rest.http.client.configuration.evictIdleConnectionsTimeInSec:5}")
    private int evictIdleConnectionsTimeInSec;

    /**
     * @return the socket connection time out in milliseconds
     */
    public int getSocketTimeOutInMiliSeconds() {
        return (int) TimeUnit.SECONDS.toMillis(socketTimeOutInSeconds);
    }

    /**
     * @return the maximum total connection value.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @return the maximum connection per route value.
     */
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * @return the connect time out value in milliseconds.
     */
    public int getConnectionTimeOutInMilliSeconds() {
        return (int) TimeUnit.SECONDS.toMillis(connectionTimeOutInSeconds);
    }

    /**
     * @return the connection time to live value in mintues.
     */
    public int getTimeToLiveInMins() {
        return (int) TimeUnit.SECONDS.toMinutes(timeToLiveInSeconds);
    }

    public long getEvictIdleConnectionsTimeInSec() {
        return evictIdleConnectionsTimeInSec;
    }

}
