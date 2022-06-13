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

package org.onap.so.client.restproperties;

import java.net.URL;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.cds.CDSProperties;

public class CDSPropertiesImpl implements CDSProperties {

    private static final String ENDPOINT = "cds.endpoint";
    private static final String PORT = "cds.port";
    private static final String AUTH = "cds.auth";
    private static final String TIMEOUT = "cds.timeout";
    private static final String KEEP_ALIVE_PING_MINUTES = "keep-alive-ping-minutes";
    private static final long GRPC_SERVER_DEFAULT_MIN_ALLOWED_PING_INTERVAL = 5;

    public CDSPropertiesImpl() {
        // Needed for service loader
    }

    @Override
    public String getHost() {
        return Objects.requireNonNull(UrnPropertiesReader.getVariable(ENDPOINT));
    }

    @Override
    public int getPort() {
        return Integer.parseInt(Objects.requireNonNull(UrnPropertiesReader.getVariable(PORT)));
    }

    @Override
    public String getBasicAuth() {
        return Objects.requireNonNull(UrnPropertiesReader.getVariable(AUTH));
    }

    @Override
    public URL getEndpoint() {
        return null;
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

    @Override
    public Integer getRetries() {
        return null;
    }

    @Override
    public Long getDelayBetweenRetries() {
        return null;
    }

    @Override
    public boolean mapNotFoundToEmpty() {
        return false;
    }

    @Override
    public int getTimeout() {
        return Integer.parseInt(Objects.requireNonNull(UrnPropertiesReader.getVariable(TIMEOUT)));
    }

    @Override
    public boolean getUseSSL() {
        return false;
    }

    @Override
    public boolean getUseBasicAuth() {
        return true;
    }

    @Override
    public long getKeepAlivePingMinutes() {
        String value = UrnPropertiesReader.getVariable(KEEP_ALIVE_PING_MINUTES);
        if (StringUtils.isBlank(value)) {
            return GRPC_SERVER_DEFAULT_MIN_ALLOWED_PING_INTERVAL + 1L;
        }
        return Long.parseLong(Objects.requireNonNull(value));
    }
}
