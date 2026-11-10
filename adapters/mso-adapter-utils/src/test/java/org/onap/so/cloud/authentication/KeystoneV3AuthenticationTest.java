/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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

package org.onap.so.cloud.authentication;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.junit.Test;
import com.woorea.openstack.keystone.v3.model.Token.Service;

public class KeystoneV3AuthenticationTest {

    private final KeystoneV3Authentication keystoneV3Authentication = new KeystoneV3Authentication();

    @Test
    public void findEndpointURL_returnsFirstListedTypeThatExists() {
        Service v3 = service("volumev3", "RegionOne", "public", "http://cinder/v3/t");
        Service v2 = service("volumev2", "RegionOne", "public", "http://cinder/v2/t");

        String url = keystoneV3Authentication.findEndpointURL(Arrays.asList(v2, v3), "RegionOne", "public",
                "block-storage", "volumev3", "volumev2");

        assertEquals("http://cinder/v3/t", url);
    }

    @Test(expected = ServiceEndpointNotFoundException.class)
    public void findEndpointURL_throwsWhenNoTypeMatches() {
        keystoneV3Authentication.findEndpointURL(
                Arrays.asList(service("volumev2", "RegionOne", "public", "http://cinder/v2/t")), "RegionOne", "public",
                "block-storage", "volumev3");
    }

    private static Service service(String type, String region, String iface, String url) {
        Service.Endpoint endpoint = new Service.Endpoint();
        endpoint.setRegion(region);
        endpoint.setInterface(iface);
        endpoint.setUrl(url);
        Service service = new Service();
        service.setType(type);
        service.setEndpoints(Arrays.asList(endpoint));
        return service;
    }
}
