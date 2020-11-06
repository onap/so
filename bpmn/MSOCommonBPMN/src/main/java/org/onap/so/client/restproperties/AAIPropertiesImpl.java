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
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.springframework.stereotype.Component;

@Component
public class AAIPropertiesImpl implements AAIProperties {

    public static final String MSO_MSO_KEY = "mso.msoKey";
    public static final String AAI_AUTH = "aai.auth";
    public static final String AAI_ENDPOINT = "aai.endpoint";
    public static final String AAI_READ_TIMEOUT = "aai.readTimeout";
    private UrnPropertiesReader reader;

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(UrnPropertiesReader.getVariable(AAI_ENDPOINT));
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
        return UrnPropertiesReader.getVariable(AAI_AUTH);
    }

    @Override
    public String getKey() {
        return UrnPropertiesReader.getVariable(MSO_MSO_KEY);
    }

    @Override
    public Long getReadTimeout() {
        return Long.valueOf(reader.getVariable(AAI_READ_TIMEOUT, "60000"));
    }

}
