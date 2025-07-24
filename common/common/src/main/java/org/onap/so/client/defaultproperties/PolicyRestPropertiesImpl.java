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
import org.onap.so.client.policy.PolicyRestProperties;

public class PolicyRestPropertiesImpl implements PolicyRestProperties {


    final Map<Object, Object> props;

    public PolicyRestPropertiesImpl() {
        File initialFile = new File("src/test/resources/policy.properties");
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

    @Override
    public URL getEndpoint() {
        try {
            return new URL((String) props.getOrDefault("policy.endpoint", ""));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

    @Override
    public String getClientAuth() {
        return (String) props.get("policy.client.auth");
    }

    @Override
    public String getAuth() {
        return (String) props.get("policy.auth");
    }

    @Override
    public String getEnvironment() {
        return (String) props.get("policy.environment");
    }

}
