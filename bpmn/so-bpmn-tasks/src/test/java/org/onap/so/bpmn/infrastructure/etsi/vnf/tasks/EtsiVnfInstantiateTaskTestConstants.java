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

package org.onap.so.bpmn.infrastructure.etsi.vnf.tasks;

/**
 * @author waqas.ikram@est.tech
 *
 */
public class EtsiVnfInstantiateTaskTestConstants {

    public static final String DUMMY_GENERIC_VND_ID = "5956a99d-9736-11e8-8caf-022ac9304eeb";
    public static final String DUMMY_BASIC_AUTH = "Basic 123abc";
    public static final String DUMMY_URL = "http://localhost:30406/so/vnfm-adapter/v1/";
    public static final String EXPECTED_URL = DUMMY_URL + "vnfs/" + DUMMY_GENERIC_VND_ID;

    public static VnfmBasicHttpConfigProvider getVnfmBasicHttpConfigProvider() {
        return getVnfmBasicHttpConfigProvider(DUMMY_URL, DUMMY_BASIC_AUTH);
    }

    public static VnfmBasicHttpConfigProvider getVnfmBasicHttpConfigProvider(final String url, final String auth) {
        final VnfmBasicHttpConfigProvider vnfmBasicHttpConfigProvider = new VnfmBasicHttpConfigProvider();
        vnfmBasicHttpConfigProvider.setUrl(url);
        vnfmBasicHttpConfigProvider.setAuth(auth);
        return vnfmBasicHttpConfigProvider;
    }

    private EtsiVnfInstantiateTaskTestConstants() {}

}
