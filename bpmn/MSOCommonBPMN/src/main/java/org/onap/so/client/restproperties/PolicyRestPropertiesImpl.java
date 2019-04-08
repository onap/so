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

package org.onap.so.client.restproperties;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.policy.PolicyRestProperties;

public class PolicyRestPropertiesImpl implements PolicyRestProperties {

    public static final String POLICY_ENDPOINT = "policy.endpoint";
    public static final String POLICY_ENVIRONMENT = "policy.environment";
    public static final String POLICY_AUTH = "policy.auth";
    public static final String POLICY_CLIENT_AUTH = "policy.client.auth";

    public PolicyRestPropertiesImpl() {}

    @Override
    public URL getEndpoint() {
        try {
            return new URL(UrnPropertiesReader.getVariable(POLICY_ENDPOINT));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

    public String getClientAuth() {
        return UrnPropertiesReader.getVariable(POLICY_CLIENT_AUTH);
    }

    public String getAuth() {
        return UrnPropertiesReader.getVariable(POLICY_AUTH);
    }

    public String getEnvironment() {
        return UrnPropertiesReader.getVariable(POLICY_ENVIRONMENT);
    }

}
