/*
 * Copyright (C) 2019 Bell Canada.
 *
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
 */
package org.onap.so.client.cds;

import java.net.MalformedURLException;
import java.net.URL;

public class TestCDSPropertiesImpl implements CDSProperties {

    public TestCDSPropertiesImpl() {
        // Needed for service loader
    }

    @Override
    public String getHost() {
        return "endpoint";
    }

    @Override
    public int getPort() {
        return 9999;
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
}
