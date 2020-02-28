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

    public static final String SDNC_HOST = "sdnc.host";
    public static final String SDNC_AUTH = "sdnc.auth";
    public static final String LCM_PATH = "sdnc.lcm.path";

    public static final String DMAAP_HOST = "sdnc.dmaap.host";
    public static final String DMAAP_AUTH = "sdnc.dmaap.auth";
    public static final String DMAAP_PARTITION = "sdnc.dmaap.partition";
    public static final String DMAAP_TIMEOUT = "sdnc.dmaap.timeout";
    public static final String DMAAP_ENVIRONMENT = "sdnc.dmaap.environment";
    public static final String LCM_DMAAP_READ_TOPIC = "sdnc.lcm.dmapp.readTopic";
    public static final String LCM_DMAAP_WRITE_TOPIC = "sdnc.lcm.dmaap.writeTopic";

    public static final String MSO_KEY = "mso.msoKey";

    public SDNCLcmPropertiesImpl() {}

    @Override
    public String getHost() {
        return UrnPropertiesReader.getVariable(SDNC_HOST);
    }

    @Override
    public String getPath() {
        String path = UrnPropertiesReader.getVariable(LCM_PATH);
        return (path != null) ? path : SDNCConstants.LCM_API_BASE_PATH;
    }

    @Override
    public String getBasicAuth() {
        return UrnPropertiesReader.getVariable(SDNC_AUTH);
    }

    @Override
    public String getDmaapHost() {
        return UrnPropertiesReader.getVariable(DMAAP_HOST);
    }

    @Override
    public String getDmaapAuth() {
        return UrnPropertiesReader.getVariable(DMAAP_AUTH);
    }

    @Override
    public String getDmaapPartition() {
        String partition = UrnPropertiesReader.getVariable(DMAAP_PARTITION);
        return (partition != null) ? partition : SDNCConstants.LCM_DMAAP_PARTITION;
    }

    @Override
    public String getDmaapTimeout() {
        String timeout = UrnPropertiesReader.getVariable(DMAAP_TIMEOUT);
        return (timeout != null) ? timeout : SDNCConstants.LCM_DMAAP_TIMEOUT;
    }

    @Override
    public String getDmaapEnvironment() {
        String environment = UrnPropertiesReader.getVariable(DMAAP_ENVIRONMENT);
        return (environment != null) ? environment : SDNCConstants.LCM_DMAAP_ENVIRONMENT;
    }

    @Override
    public String getDmaaPLcmReadTopic() {
        String readTopic = UrnPropertiesReader.getVariable(LCM_DMAAP_READ_TOPIC);
        return (readTopic != null) ? readTopic : SDNCConstants.LCM_DMAAP_READ_TOPIC;
    }

    @Override
    public String getDmaaPLcmWriteTopic() {
        String writeTopic = UrnPropertiesReader.getVariable(LCM_DMAAP_WRITE_TOPIC);
        return (writeTopic != null) ? writeTopic : SDNCConstants.LCM_DMAAP_WRITE_TOPIC;
    }

    @Override
    public String getKey() {
        return UrnPropertiesReader.getVariable(MSO_KEY);
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
