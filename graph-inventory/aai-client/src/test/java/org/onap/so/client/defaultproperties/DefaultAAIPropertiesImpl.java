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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.onap.so.client.aai.AAIProperties;
import org.onap.so.client.aai.AAIVersion;

public class DefaultAAIPropertiesImpl implements AAIProperties {


    final Map<Object, Object> props;

    public DefaultAAIPropertiesImpl() {
        File initialFile = new File("src/test/resources/aai.properties");
        Map<Object, Object> temp;
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            Properties properties = new Properties();
            properties.load(targetStream);
            temp = properties;
        } catch (IOException e) {
            temp = new HashMap<>();
        }
        this.props = temp;

    }

    public DefaultAAIPropertiesImpl(int port) {
        File initialFile = new File("src/test/resources/aai.properties");
        Map<Object, Object> temp;
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            Properties properties = new Properties();
            properties.load(targetStream);
            temp = properties;
        } catch (IOException e) {
            temp = new HashMap<>();
        }
        this.props = temp;
        this.props.put("aai.endpoint", this.props.get("aai.endpoint").toString().replaceFirst(":\\d+", ":" + port));

    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(props.get("aai.endpoint").toString());
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return AAIVersion.LATEST;
    }

    @Override
    public String getAuth() {
        Object value = props.get("aai.auth");
        return value == null ? null : value.toString();
    }

    @Override
    public String getKey() {
        Object value = props.get("mso.msoKey");
        return value == null ? null : value.toString();
    }
}
