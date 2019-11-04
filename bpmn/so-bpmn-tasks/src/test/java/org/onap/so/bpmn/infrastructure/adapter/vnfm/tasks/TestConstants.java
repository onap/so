/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.ADDITIONAL_PARAMS;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.EXT_VIRTUAL_LINKS;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author waqas.ikram@est.tech
 *
 */
public class TestConstants {

    public static final String EXT_VIRTUAL_LINK_ID = "ac1ed33d-8dc1-4800-8ce8-309b99c38eec";
    public static final String DUMMY_GENERIC_VND_ID = "5956a99d-9736-11e8-8caf-022ac9304eeb";
    public static final String DUMMY_BASIC_AUTH = "Basic 123abc";
    public static final String DUMMY_URL = "http://localhost:30406/so/vnfm-adapter/v1/";
    public static final String EXPECTED_URL = DUMMY_URL + "vnfs/" + DUMMY_GENERIC_VND_ID;

    public static final String DUMMY_JOB_ID = UUID.randomUUID().toString();
    public static final String JOB_STATUS_EXPECTED_URL = DUMMY_URL + "jobs/" + DUMMY_JOB_ID;

    public static final String EXT_VIRTUAL_LINK_VALUE = "{\"id\":\"" + EXT_VIRTUAL_LINK_ID + "\","
            + "\"tenant\":{\"cloudOwner\":\"CloudOwner\",\"regionName\":\"RegionOne\","
            + "\"tenantId\":\"80c26954-2536-4bca-9e20-10f8a2c9c2ad\"},\"resourceId\":\"8ef8cd54-75fd-4372-a6dd-2e05ea8fbd9b\","
            + "\"extCps\":[{\"cpdId\":\"f449292f-2f0f-4656-baa3-a18d86bac80f\","
            + "\"cpConfig\":[{\"cpInstanceId\":\"07876709-b66f-465c-99a7-0f4d026197f2\","
            + "\"linkPortId\":null,\"cpProtocolData\":null}]}],\"extLinkPorts\":null}";

    public static final String ADDITIONAL_PARAMS_VALUE = "{\"image_id\": \"DUMMYVNF\",\"instance_type\": \"m1.small\","
            + "\"ftp_address\": \"ftp://0.0.0.0:2100/\"}";

    public static final String EXT_VIRTUAL_LINKS_VALUE = "[" + EXT_VIRTUAL_LINK_VALUE + "]";


    public static VnfmBasicHttpConfigProvider getVnfmBasicHttpConfigProvider() {
        return getVnfmBasicHttpConfigProvider(DUMMY_URL, DUMMY_BASIC_AUTH);
    }

    public static VnfmBasicHttpConfigProvider getVnfmBasicHttpConfigProvider(final String url, final String auth) {
        final VnfmBasicHttpConfigProvider vnfmBasicHttpConfigProvider = new VnfmBasicHttpConfigProvider();
        vnfmBasicHttpConfigProvider.setUrl(url);
        vnfmBasicHttpConfigProvider.setAuth(auth);
        return vnfmBasicHttpConfigProvider;
    }

    public static Map<String, Object> getUserParamsMap(final String additionalParams,
            final String extVirtualLinksValue) {
        final Map<String, Object> userParams = new HashMap<>();
        userParams.put(ADDITIONAL_PARAMS, additionalParams);
        userParams.put(EXT_VIRTUAL_LINKS, extVirtualLinksValue);
        return userParams;
    }

    private TestConstants() {}

}
