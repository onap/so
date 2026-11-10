/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

/*-
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */
package org.onap.so.heatbridge.openstack.factory;

import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.api.OpenstackClientImpl;
import org.onap.so.openstack.utils.CinderClientImpl;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.NeutronClientImpl;
import org.onap.so.openstack.utils.NovaClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenstackClientFactory {

    @Autowired
    private NovaClientImpl novaClient;

    @Autowired
    private NeutronClientImpl neutronClient;

    @Autowired
    private CinderClientImpl cinderClient;

    @Autowired
    private MsoHeatUtils heatUtils;

    public OpenstackClient create(String cloudSiteId, String tenantId) {
        return new OpenstackClientImpl(novaClient, neutronClient, cinderClient, heatUtils, cloudSiteId, tenantId);
    }
}
