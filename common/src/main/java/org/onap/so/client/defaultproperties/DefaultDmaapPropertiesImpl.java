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

package org.onap.so.client.defaultproperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.onap.so.client.dmaap.DmaapProperties;

public class DefaultDmaapPropertiesImpl implements DmaapProperties {

    private final Map<String, String> properties;

    public DefaultDmaapPropertiesImpl() throws IOException {
        File initialFile = new File("src/test/resources/dmaap.properties");
        Properties properties = new Properties();
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            properties.load(targetStream);
            this.properties = new HashMap<>();
            properties.forEach((key, value) -> this.properties.put((String) key, (String) value));
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

}
