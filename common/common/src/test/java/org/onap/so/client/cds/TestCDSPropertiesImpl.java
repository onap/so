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

import java.net.URL;

public class TestCDSPropertiesImpl implements CDSProperties {

    public TestCDSPropertiesImpl() {
        // Needed for service loader
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public int getPort() {
        return 9111;
    }

    @Override
    public String getBasicAuth() {
        return "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==";
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
        return 60;
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
        return 6L;
    }
}
