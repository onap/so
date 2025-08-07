/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 Bell Canada.
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

package org.onap.so.client.cds;

import java.util.concurrent.TimeUnit;
import org.onap.so.client.RestProperties;

public interface CDSProperties extends RestProperties {

    String getHost();

    int getPort();

    String getBasicAuth();

    int getTimeout();

    boolean getUseSSL();

    boolean getUseBasicAuth();

    /**
     * Gets grpc keep alive ping interval, which is useful for detecting connection issues when the server dies
     * abruptly. If the value is set lower than what is allowed by the server (default 5 min), the connection will be
     * closed after a few pings.
     *
     * If no value is set this method will default to 6 min (server default minimum + 1)
     *
     * @see io.grpc.netty.NettyChannelBuilder#keepAliveTime(long, TimeUnit)
     * @return
     */
    long getKeepAlivePingMinutes();
}
