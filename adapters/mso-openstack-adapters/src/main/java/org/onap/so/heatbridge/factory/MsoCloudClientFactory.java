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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.heatbridge.factory;

import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;

/**
 * Defines contract to load the cloud configuration from SO, authenticate with keystone for a given cloud-region and
 * tenant.
 */
public interface MsoCloudClientFactory {

    /**
     * Get the Openstack Client for keystone version specified in cloud configuration.
     *
     * @param url openstack url
     * @param msoId openstack user for mso
     * @param msoPass openstack password for mso user
     * @param regionId region identifier
     * @param tenantId tenant identifier
     * @return Openstack Client for the keystone version requested
     * @throws HeatBridgeException if any errors when reading cloud configuration or getting openstack client
     */


    OpenstackClient getOpenstackClient(String url, String msoId, String msoPass, String regionId, String tenantId,
            String keystoneVersion) throws HeatBridgeException;
}
