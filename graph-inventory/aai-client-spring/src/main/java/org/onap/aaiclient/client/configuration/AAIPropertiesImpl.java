/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AAIPropertiesImpl implements AAIProperties {


    final Properties properties = new Properties();

    public AAIPropertiesImpl() {
        this("src/test/resources/aai.properties");
    }

    public AAIPropertiesImpl(String propertiesPath) {
        File initialFile = new File(propertiesPath);
        try {
            properties.load(new FileInputStream(initialFile));
        } catch (IOException e) {
            log.warn("Could not load AAIProperties from file {}", propertiesPath, e);
        }
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(properties.get("aai.endpoint").toString());
    }

    @Override
    public String getSystemName() {
        return "SO";
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return AAIVersion.LATEST;
    }

    @Override
    public String getAuth() {
        Object value = properties.get("aai.auth");
        return value == null ? null : value.toString();
    }

    @Override
    public String getKey() {
        Object value = properties.get("mso.msoKey");
        return value == null ? null : value.toString();
    }
}
