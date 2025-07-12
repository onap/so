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

package org.onap.so.simulator;

import java.net.MalformedURLException;
import java.net.URL;
import org.onap.aaiclient.client.aai.AAIVersion;


public class AAIPropertiesImpl implements org.onap.aaiclient.client.aai.AAIProperties {

    public static final String AAI_ENDPOINT = "aai.endpoint";


    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL("http://so-aai-resources-svc:9900");
    }

    @Override
    public String getSystemName() {
        return "SIMULATOR";
    }

    @Override
    public AAIVersion getDefaultVersion() {
        return AAIVersion.LATEST;
    }

    @Override
    public String getAuth() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }


}
