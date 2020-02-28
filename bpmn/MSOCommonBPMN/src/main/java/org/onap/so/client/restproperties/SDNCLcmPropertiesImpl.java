/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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
import org.onap.so.client.sdnc.common.SDNCConstants;
import org.onap.so.client.sdnc.lcm.SDNCLcmProperties;

public class SDNCLcmPropertiesImpl implements SDNCLcmProperties {

    private static final String ENDPOINT = "sdnc.host";
    private static final String AUTH = "sdnc.auth";
    private static final String PATH = "sdnc.lcm.path";

    @Override
    public String getHost() {
        return UrnPropertiesReader.getVariable(ENDPOINT);
    }

    @Override
    public String getPath() {
        String path = UrnPropertiesReader.getVariable(PATH);
        if (path == null) {
            path = SDNCConstants.LCM_API_BASE_PATH;
        }
        return path;
    }

    @Override
    public String getBasicAuth() {
        return UrnPropertiesReader.getVariable(AUTH);
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(getHost());
    }

    @Override
    public String getSystemName() {
        return SDNCConstants.SYSTEM_NAME;
    }
}
