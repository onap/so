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

package org.onap.so.client.adapter.network;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.adapter.rest.AdapterRestProperties;
import org.onap.so.utils.BasicAuth;


public class NetworkAdapterRestProperties implements AdapterRestProperties {

    public NetworkAdapterRestProperties() {}

    @Override
    public String getUsername() {
        return UrnPropertiesReader.getVariable(BasicAuth.SO_USERNAME_PROPERTY);
    }

    @Override
    public String getPassword() {
        return UrnPropertiesReader.getVariable(BasicAuth.SO_PASSWORD_PROPERTY);
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(UrnPropertiesReader.getVariable("mso.adapters.network.rest.endpoint"));
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

}
